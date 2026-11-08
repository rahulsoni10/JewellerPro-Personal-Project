package com.jeweldesk.app.pdf

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.jeweldesk.app.data.AppData
import com.jeweldesk.app.data.Book
import com.jeweldesk.app.data.Girvi
import com.jeweldesk.app.data.Invoice
import com.jeweldesk.app.data.Kind
import com.jeweldesk.app.data.Party
import com.jeweldesk.app.data.PartyType
import com.jeweldesk.app.data.Product
import com.jeweldesk.app.data.amountInWords
import com.jeweldesk.app.data.calc
import com.jeweldesk.app.data.dateStr
import com.jeweldesk.app.data.interestAsOf
import com.jeweldesk.app.data.money
import com.jeweldesk.app.data.now
import com.jeweldesk.app.data.partyDue
import com.jeweldesk.app.data.plain
import com.jeweldesk.app.data.totals
import java.io.File

// ─────────────────────────────────────────────────────────────────
// PDF helper — generates PDFs using the built-in PdfDocument API.
// All sizes are in PDF points (1pt ≈ 1/72 inch).
// ─────────────────────────────────────────────────────────────────

// ── Colour constants (file-level so PdfPage can access them) ─────

private val C_GOLD   = Color.parseColor("#D4A21C")
private val C_GOLD_BG = Color.parseColor("#FFF4D6")
private val C_SUB    = Color.parseColor("#7A6F62")
private val C_BDR    = Color.parseColor("#EBE1CF")
private val C_RED    = Color.parseColor("#E5484D")
private val C_GRN    = Color.parseColor("#1E9E5A")

// ── Paint factory functions (file-level) ─────────────────────────

private fun tp(sz: Float, bold: Boolean = false, color: Int = Color.BLACK) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = sz
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        this.color = color
    }

private fun lp(color: Int = C_BDR, w: Float = 0.5f) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color; strokeWidth = w; style = Paint.Style.STROKE
    }

private fun fp(color: Int) =
    Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color; style = Paint.Style.FILL }

// ── Multi-page drawing context ────────────────────────────────────

private const val PW = 595
private const val PH = 842
private const val MARGIN = 36f
private val CW = PW - 2 * MARGIN

private class PdfCtx(val doc: PdfDocument) {
    var page: PdfDocument.Page? = null
    var c: android.graphics.Canvas? = null
    var y = MARGIN
    var pageNum = 1

    fun nextPage(startY: Float = MARGIN + 10f) {
        page?.let { doc.finishPage(it) }
        val pi = PdfDocument.PageInfo.Builder(PW, PH, pageNum++).create()
        page = doc.startPage(pi)
        c = page!!.canvas
        y = startY
    }

    fun need(h: Float) { if (y + h > PH - MARGIN) nextPage() }

    fun advance(h: Float) { y += h }

    fun text(s: String, x: Float, p: Paint) {
        c!!.drawText(s, x, y, p)
    }

    fun textR(s: String, x: Float, p: Paint) {
        val old = p.textAlign; p.textAlign = Paint.Align.RIGHT
        c!!.drawText(s, x, y, p)
        p.textAlign = old
    }

    fun textC(s: String, cx: Float, p: Paint) {
        val old = p.textAlign; p.textAlign = Paint.Align.CENTER
        c!!.drawText(s, cx, y, p)
        p.textAlign = old
    }

    fun row2(left: String, right: String, lp: Paint, rp: Paint) {
        text(left, MARGIN, lp)
        textR(right, PW - MARGIN, rp)
    }

    fun hLine(x1: Float = MARGIN, x2: Float = PW - MARGIN, color: Int = C_BDR) =
        c!!.drawLine(x1, y, x2, y, lp(color))

    fun rect(x: Float, top: Float, w: Float, h: Float, color: Int) =
        c!!.drawRect(x, top, x + w, top + h, fp(color))

    fun finish(fileName: String, ctx: Context): File {
        page?.let { doc.finishPage(it) }
        val f = pdfFile(ctx, fileName)
        f.outputStream().use { doc.writeTo(it) }
        doc.close()
        return f
    }
}

// ── File helpers ──────────────────────────────────────────────────

private fun pdfFile(ctx: Context, name: String): File {
    val dir = File(ctx.cacheDir, "shared").apply { mkdirs() }
    return File(dir, name)
}

// ─────────────────────────────────────────────────────────────────
// Public API object
// ─────────────────────────────────────────────────────────────────

object Pdf {

    // ── Share / Print ─────────────────────────────────────────────

    fun share(ctx: Context, file: File, whatsapp: Boolean = false, phone: String = "") {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            if (whatsapp) {
                `package` = "com.whatsapp"
                if (phone.length == 10) putExtra("jid", "91$phone@s.whatsapp.net")
            }
        }
        try {
            ctx.startActivity(if (whatsapp) sendIntent else Intent.createChooser(sendIntent, "Share PDF").also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            ctx.startActivity(Intent.createChooser(sendIntent.apply { `package` = null }, "Share PDF").also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    fun print(ctx: Context, file: File, title: String = "Invoice") {
        val uri = FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            ctx.startActivity(intent)
        } catch (_: Exception) {
            ctx.startActivity(Intent.createChooser(intent, "Open $title").also {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }

    // ── Invoice PDF ───────────────────────────────────────────────

    fun invoice(ctx: Context, d: AppData, inv: Invoice): File {
        val dc = PdfCtx(PdfDocument())
        dc.nextPage(MARGIN)
        val t = inv.totals()
        val s = d.invoiceSettings(inv.type.book)
        val p = d.profile
        val c = dc.c!!

        // ── Header: logo circle + business info ──
        val logoR = 22f
        val logoX = MARGIN + logoR
        val logoY = dc.y + logoR + 2f
        c.drawCircle(logoX, logoY, logoR, fp(C_GOLD_BG))
        c.drawCircle(logoX, logoY, logoR, lp(C_GOLD, 1.5f))
        val initLtr = s.companyInitial.ifBlank { p?.businessName?.take(1) ?: "J" }.uppercase()
        val ltPaint = tp(20f, true, C_GOLD).apply { textAlign = Paint.Align.CENTER }
        c.drawText(initLtr, logoX, logoY + 7f, ltPaint)

        val bx = logoX * 2 + 8f
        c.drawText(p?.businessName ?: "Jewellers", bx, dc.y + 15f, tp(16f, true))
        val addr = listOfNotNull(
            p?.address?.takeIf { it.isNotBlank() },
            p?.city?.takeIf { it.isNotBlank() },
            p?.state?.takeIf { it.isNotBlank() },
            p?.pincode?.takeIf { it.isNotBlank() }
        ).joinToString(", ")
        if (addr.isNotBlank()) c.drawText(addr, bx, dc.y + 28f, tp(9f, color = C_SUB))
        if (!p?.mobile.isNullOrBlank()) c.drawText("Ph: ${p!!.mobile}", bx, dc.y + 40f, tp(9f, color = C_SUB))
        var bizY = dc.y + 40f
        if (!p?.gstin.isNullOrBlank()) { bizY += 12f; c.drawText("GSTIN: ${p!!.gstin}", bx, bizY, tp(9f, color = C_SUB)) }
        if (s.bisNo.isNotBlank()) { bizY += 12f; c.drawText("BIS: ${s.bisNo}", bx, bizY, tp(9f, color = C_SUB)) }

        // ── Header: invoice title (right aligned) ──
        val titlePaint = tp(18f, true, if (inv.type.isGst) Color.BLACK else C_GOLD).apply { textAlign = Paint.Align.RIGHT }
        c.drawText(if (inv.type.isGst) "TAX INVOICE" else "ESTIMATE", PW - MARGIN, dc.y + 15f, titlePaint)
        val typePaint = tp(10f, color = C_SUB).apply { textAlign = Paint.Align.RIGHT }
        c.drawText(inv.type.title, PW - MARGIN, dc.y + 28f, typePaint)
        if (s.showHallmarkLogo) {
            val hlPaint = tp(8f, true, C_GOLD).apply { textAlign = Paint.Align.RIGHT }
            c.drawText("BIS Hallmark \u2605", PW - MARGIN, dc.y + 40f, hlPaint)
        }
        dc.advance(68f)
        dc.hLine(color = C_GOLD)
        dc.advance(6f)

        // ── Invoice number & date ──
        c.drawText("#${inv.displayNo}", MARGIN, dc.y + 14f, tp(12f, true))
        val dateP = tp(11f).apply { textAlign = Paint.Align.RIGHT }
        c.drawText("Date: ${inv.date.dateStr()}", PW - MARGIN, dc.y + 14f, dateP)
        dc.advance(20f)

        if (inv.type.isGst && inv.placeOfSupply.isNotBlank()) {
            val gstType = if (inv.interState) "IGST" else "SGST+CGST"
            c.drawText("Place of Supply: ${inv.placeOfSupply}  ($gstType)", MARGIN, dc.y + 12f, tp(9f, color = C_SUB))
            dc.advance(16f)
        }
        dc.advance(4f)

        // ── Bill-to block ──
        dc.hLine()
        dc.advance(6f)
        c.drawText(if (inv.type.isSaleSide) "BILL TO" else "FROM SUPPLIER", MARGIN, dc.y + 12f, tp(10f, true, C_GOLD))
        dc.advance(18f)
        c.drawText(inv.partyName.ifBlank { "Walk-in Customer" }, MARGIN, dc.y + 13f, tp(13f, true))
        dc.advance(17f)
        listOfNotNull(
            inv.partyMobile.takeIf { it.isNotBlank() }?.let { "Mobile: $it" },
            inv.partyAddress.takeIf { it.isNotBlank() },
            inv.partyGstin.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" },
        ).forEach { line ->
            dc.need(14f)
            c.drawText(line, MARGIN, dc.y + 10f, tp(9f, color = C_SUB)); dc.advance(13f)
        }
        dc.advance(6f)

        // ── Items table ──
        if (inv.items.isNotEmpty()) {
            dc.need(30f)
            dc.hLine(color = C_GOLD)
            dc.advance(1f)
            dc.rect(MARGIN, dc.y, CW, 20f, C_GOLD_BG)

            val cx = floatArrayOf(MARGIN + 4f, MARGIN + 115f, MARGIN + 170f, MARGIN + 215f, MARGIN + 265f, MARGIN + 310f, MARGIN + 355f, PW - MARGIN)
            val hdrs = listOf("Item", "Purity", "GW / LW(g)", "Net(g)", "Rate", "Making", "HSN")
            hdrs.forEachIndexed { i, h -> c.drawText(h, cx[i], dc.y + 14f, tp(8f, true)) }
            tp(8f, true).apply { textAlign = Paint.Align.RIGHT }.let { p2 -> c.drawText("Total \u20b9", cx[7], dc.y + 14f, p2) }
            dc.advance(22f)
            dc.hLine(color = C_GOLD)
            dc.advance(2f)

            inv.items.forEachIndexed { i, item ->
                dc.need(20f)
                val ic = item.calc()
                val vp = tp(9f)
                val sv = tp(9f, color = C_SUB)
                c.drawText("${i + 1}. ${item.name.take(18)}", cx[0], dc.y + 12f, vp)
                c.drawText(item.purity.ifBlank { "\u2014" }, cx[1], dc.y + 12f, sv)
                c.drawText("${item.grossWt.plain(3)}/${item.lessWt.plain(3)}", cx[2], dc.y + 12f, sv)
                c.drawText(ic.net.plain(3), cx[3], dc.y + 12f, sv)
                c.drawText(item.rate.plain(2), cx[4], dc.y + 12f, sv)
                c.drawText(ic.making.plain(0), cx[5], dc.y + 12f, sv)
                c.drawText(item.hsn.ifBlank { "7113" }, cx[6], dc.y + 12f, sv)
                val totP = tp(9f, true).apply { textAlign = Paint.Align.RIGHT }
                c.drawText(ic.total.plain(0), cx[7], dc.y + 12f, totP)
                dc.advance(14f)
                if (item.huid.isNotBlank()) {
                    c.drawText("HUID: ${item.huid}", cx[0], dc.y + 11f, tp(8f, color = C_SUB))
                    dc.advance(12f)
                }
                dc.hLine(color = C_BDR)
                dc.advance(2f)
            }
        }

        // ── Collected metal / purchased metal ──
        if (inv.metals.isNotEmpty()) {
            dc.need(24f)
            dc.advance(4f)
            c.drawText(
                if (inv.type.kind == Kind.PURCHASE_METAL) "METAL PURCHASED" else "OLD JEWELLERY / COLLECTED METAL",
                MARGIN, dc.y + 12f, tp(10f, true, C_GOLD)
            )
            dc.advance(18f)
            inv.metals.forEach { m ->
                dc.need(14f)
                val ml = "${m.stockName}: ${m.givenWt.plain(3)}g @ ${m.tunch}% fine = ${m.fineWt.plain(3)}g"
                c.drawText(ml, MARGIN, dc.y + 11f, tp(9f))
                val mAmtP = tp(9f, true).apply { textAlign = Paint.Align.RIGHT }
                c.drawText(m.amount.plain(0), PW - MARGIN, dc.y + 11f, mAmtP)
                dc.advance(14f)
            }
        }

        // ── Totals block ──
        val totX = MARGIN + CW * 0.55f
        fun totRow(label: String, value: String, bold: Boolean = false, valColor: Int = Color.BLACK) {
            dc.need(16f)
            c.drawText(label, totX, dc.y + 12f, tp(10f, bold))
            val vp = tp(10f, bold, valColor).apply { textAlign = Paint.Align.RIGHT }
            c.drawText(value, PW - MARGIN, dc.y + 12f, vp)
            dc.advance(14f)
        }

        dc.advance(6f)
        dc.hLine(x1 = totX)
        dc.advance(2f)
        totRow("Subtotal", t.subtotal.money())
        if (t.discount > 0) totRow("Discount", "\u2013 ${t.discount.money()}")
        if (t.oldMetal > 0) totRow("Old Metal", "\u2013 ${t.oldMetal.money()}")
        if (inv.type.isGst) {
            totRow("Taxable Amount", t.taxable.money())
            val gstLabel = if (inv.interState) "IGST (${inv.gstRate.toInt()}%)" else "GST (${inv.gstRate.toInt()}%)"
            totRow(gstLabel, t.gst.money())
        }
        if (inv.roundOff != 0.0) {
            val roStr = if (inv.roundOff > 0) "+${inv.roundOff.plain(2)}" else inv.roundOff.plain(2)
            totRow("Round Off", roStr)
        }
        dc.hLine(x1 = totX)
        dc.advance(2f)
        totRow("Total Amount", t.total.money(), bold = true)
        if (inv.advanceApplied > 0) totRow("Advance Adjusted", "\u2013 ${inv.advanceApplied.money()}")
        if (inv.duesIncluded > 0) totRow("Previous Dues", "+${inv.duesIncluded.money()}")
        dc.hLine(x1 = totX)
        dc.advance(2f)
        totRow("Amount to Collect", t.toCollect.money(), bold = true)

        if (inv.payments.isNotEmpty()) {
            dc.advance(2f)
            inv.payments.forEach { pay ->
                totRow("${pay.mode} paid", "\u2013 ${pay.amount.money()}", valColor = C_GRN)
            }
            dc.hLine(x1 = totX)
            dc.advance(2f)
            val remColor = if (t.remaining > 0.5) C_RED else C_GRN
            totRow("Balance Due", t.remaining.money(), bold = true, valColor = remColor)
        }

        // ── Amount in words ──
        dc.advance(6f)
        dc.need(14f)
        c.drawText("In words: ${amountInWords(t.total)}", MARGIN, dc.y + 11f, tp(9f, color = C_SUB))
        dc.advance(14f)

        if (inv.description.isNotBlank() && s.showInvoiceDescription) {
            dc.need(14f)
            c.drawText("Note: ${inv.description}", MARGIN, dc.y + 11f, tp(9f, color = C_SUB))
            dc.advance(14f)
        }

        // ── Bank details ──
        if (s.showBank && (s.accountNumber.isNotBlank() || s.upiId.isNotBlank())) {
            dc.need(50f)
            dc.advance(4f)
            dc.hLine()
            dc.advance(4f)
            c.drawText("Bank / Payment Details", MARGIN, dc.y + 12f, tp(10f, true, C_GOLD))
            dc.advance(18f)
            if (s.bankName.isNotBlank()) { c.drawText("Bank: ${s.bankName}", MARGIN, dc.y + 10f, tp(9f)); dc.advance(13f) }
            if (s.accountNumber.isNotBlank()) { c.drawText("A/c: ${s.accountNumber}", MARGIN, dc.y + 10f, tp(9f)); dc.advance(13f) }
            if (s.ifsc.isNotBlank()) { c.drawText("IFSC: ${s.ifsc}", MARGIN, dc.y + 10f, tp(9f)); dc.advance(13f) }
            if (s.upiId.isNotBlank()) { c.drawText("UPI: ${s.upiId}", MARGIN, dc.y + 10f, tp(9f)); dc.advance(13f) }
        }

        // ── Terms & Declaration ──
        dc.need(60f)
        dc.advance(6f)
        dc.hLine()
        dc.advance(4f)
        if (s.declaration.isNotBlank()) {
            c.drawText(s.declaration.take(100), MARGIN, dc.y + 10f, tp(8f, color = C_SUB))
            dc.advance(13f)
        }
        listOf(s.terms1, s.terms2, s.terms3).filter { it.isNotBlank() }.forEachIndexed { i, term ->
            dc.need(12f)
            c.drawText("${i + 1}. $term", MARGIN, dc.y + 10f, tp(8f, color = C_SUB))
            dc.advance(12f)
        }

        // ── Signatures ──
        dc.need(50f)
        dc.advance(24f)
        dc.hLine()
        dc.advance(4f)
        c.drawText("Customer Signature", MARGIN, dc.y + 11f, tp(9f, color = C_SUB))
        val authP = tp(9f, color = C_SUB).apply { textAlign = Paint.Align.RIGHT }
        c.drawText("For ${p?.businessName ?: ""}", PW - MARGIN, dc.y + 11f, authP)
        dc.advance(28f)
        val signP = tp(9f, color = C_SUB).apply { textAlign = Paint.Align.RIGHT }
        c.drawText("Authorised Signatory", PW - MARGIN, dc.y + 11f, signP)

        return dc.finish("invoice_${inv.displayNo.replace("/", "_").replace("\\", "_")}.pdf", ctx)
    }

    // ── Girvi Slip ────────────────────────────────────────────────

    fun girviSlip(ctx: Context, d: AppData, g: Girvi): File {
        val slipH = 560
        val doc = PdfDocument()
        val pi = PdfDocument.PageInfo.Builder(PW, slipH, 1).create()
        val page = doc.startPage(pi)
        val c = page.canvas
        val r = g.interestAsOf()
        val p = d.profile
        val gs = d.girviSettings

        var y = MARGIN

        fun hLine() = c.drawLine(MARGIN, y, PW - MARGIN, y, lp())
        fun kv(label: String, value: String) {
            c.drawText(label, MARGIN, y + 11f, tp(9f, color = C_SUB))
            val vp = tp(9f, true).apply { textAlign = Paint.Align.RIGHT }
            c.drawText(value, PW - MARGIN, y + 11f, vp)
            y += 14f
        }
        fun sectionTitle(title: String) {
            y += 4f; hLine(); y += 6f
            c.drawText(title, MARGIN, y + 12f, tp(11f, true, C_GOLD)); y += 18f
        }

        // header
        val centerB = tp(16f, true).apply { textAlign = Paint.Align.CENTER }
        c.drawText(p?.businessName ?: "Business", PW / 2f, y + 16f, centerB); y += 20f
        val centerS = tp(9f, color = C_SUB).apply { textAlign = Paint.Align.CENTER }
        val hdrDetails = listOfNotNull(
            p?.address?.takeIf { it.isNotBlank() },
            p?.city?.takeIf { it.isNotBlank() },
            p?.mobile?.takeIf { it.isNotBlank() }
        ).joinToString(" | ")
        c.drawText(hdrDetails, PW / 2f, y + 11f, centerS); y += 16f
        val centerTitle = tp(13f, true, C_GOLD).apply { textAlign = Paint.Align.CENTER }
        c.drawText("GIRVI SLIP", PW / 2f, y + 14f, centerTitle); y += 20f

        hLine(); y += 6f
        kv("Girvi No.", "#${g.number}")
        kv("Date", g.date.dateStr())
        kv("Customer", g.customerName)
        if (g.mobile.isNotBlank()) kv("Mobile", g.mobile)
        val addr = listOf(g.address, g.city, g.state).filter { it.isNotBlank() }.joinToString(", ")
        if (addr.isNotBlank()) kv("Address", addr.take(55))

        sectionTitle("JEWELLERY PLEDGED")
        g.items.forEach { item ->
            c.drawText("\u2022 ${item.name} (${item.metal.label}) ${item.pcs} pcs", MARGIN, y + 11f, tp(10f))
            val wt = "GW:${item.grossWt.plain(3)}g  NW:${item.netWt.plain(3)}g"
            val wp = tp(9f, color = C_SUB).apply { textAlign = Paint.Align.RIGHT }
            c.drawText(wt, PW - MARGIN, y + 11f, wp)
            y += 14f
        }
        if (g.approxValue > 0 && gs.approxValue) kv("Approx Value", g.approxValue.money())

        sectionTitle("LOAN DETAILS")
        kv("Principal", g.principal.money())
        kv("Interest Rate", "${g.rate}% / month (${g.interestType.label})")
        kv("Calculation", g.method.label)
        kv("Duration", "${r.days} days")
        if (gs.slipRate) kv("Interest accrued", r.interest.money())
        kv("Amount Paid", r.paid.money())
        val outAmt = r.outstanding(g.principal)
        y += 2f; hLine(); y += 4f
        c.drawText("Outstanding Amount", MARGIN, y + 13f, tp(11f, true))
        val outP = tp(12f, true, if (outAmt > 0.5) C_RED else C_GRN).apply { textAlign = Paint.Align.RIGHT }
        c.drawText(outAmt.money(), PW - MARGIN, y + 13f, outP); y += 20f

        if (gs.declaration.isNotBlank()) {
            hLine(); y += 6f
            c.drawText(gs.declaration.take(100), MARGIN, y + 10f, tp(8f, color = C_SUB)); y += 12f
        }
        listOf(gs.terms1, gs.terms2, gs.terms3).filter { it.isNotBlank() }.forEachIndexed { i, term ->
            c.drawText("${i + 1}. $term", MARGIN, y + 10f, tp(8f, color = C_SUB)); y += 12f
        }
        y += 20f; hLine(); y += 6f
        c.drawText("Customer Signature", MARGIN, y + 11f, tp(9f, color = C_SUB))
        val authP = tp(9f, color = C_SUB).apply { textAlign = Paint.Align.RIGHT }
        c.drawText("Authorised Signatory", PW - MARGIN, y + 11f, authP)

        doc.finishPage(page)
        val f = pdfFile(ctx, "girvi_${g.number}.pdf")
        f.outputStream().use { doc.writeTo(it) }
        doc.close()
        return f
    }

    // ── Party Statement ───────────────────────────────────────────

    fun statement(ctx: Context, d: AppData, party: Party): File {
        data class LedgerLine(val date: Long, val title: String, val amount: Double)

        val ledger = buildList<LedgerLine> {
            if (party.openingDue != 0.0) add(LedgerLine(party.createdAt, "Opening Balance", party.openingDue))
            d.invoices.filter { it.partyId == party.id }.sortedBy { it.date }.forEach { inv ->
                val t = inv.totals()
                val ret = inv.type.kind == Kind.SALE_RETURN || inv.type.kind == Kind.PURCHASE_RETURN
                val sign = if (ret) -1 else 1
                add(LedgerLine(inv.date, "${inv.type.short} #${inv.displayNo}", sign * t.total))
                if (t.received > 0) add(LedgerLine(inv.date, "Payment #${inv.displayNo}", -sign * t.received))
                if (inv.advanceApplied > 0) add(LedgerLine(inv.date, "Advance adj. #${inv.displayNo}", -inv.advanceApplied))
            }
            d.khata.filter { it.partyId == party.id && it.type.dueSign != 0 }.forEach { k ->
                val lbl = if (party.type == PartyType.CUSTOMER) k.type.customerLabel else k.type.supplierLabel
                add(LedgerLine(k.date, "$lbl${if (k.note.isNotBlank()) " \u2013 ${k.note}" else ""}", k.type.dueSign * k.amount))
            }
        }.sortedBy { it.date }

        val dc = PdfCtx(PdfDocument())
        dc.nextPage(MARGIN)
        val c = dc.c!!
        val p = d.profile

        // header
        dc.textC(p?.businessName ?: "Business", PW / 2f, tp(16f, true)); dc.advance(20f)
        dc.textC("Account Statement", PW / 2f, tp(10f, color = C_SUB)); dc.advance(16f)
        dc.hLine(); dc.advance(6f)

        c.drawText("Party: ${party.name}", MARGIN, dc.y + 13f, tp(13f, true)); dc.advance(18f)
        listOfNotNull(
            party.mobile.takeIf { it.isNotBlank() }?.let { "Mobile: $it" },
            party.gstin.takeIf { it.isNotBlank() }?.let { "GSTIN: $it" },
            "Generated: ${now().dateStr()}"
        ).forEach { l -> c.drawText(l, MARGIN, dc.y + 10f, tp(9f, color = C_SUB)); dc.advance(14f) }
        dc.advance(4f); dc.hLine(); dc.advance(4f)

        // table header
        dc.rect(MARGIN, dc.y, CW, 20f, C_GOLD_BG)
        val cx2 = floatArrayOf(MARGIN + 4f, MARGIN + 65f, MARGIN + CW - 90f, MARGIN + CW - 20f, PW - MARGIN)
        listOf("Date", "Description").forEachIndexed { i, h -> c.drawText(h, cx2[i], dc.y + 14f, tp(8f, true)) }
        c.drawText("Debit \u20b9",  cx2[2], dc.y + 14f, tp(8f, true))
        c.drawText("Credit \u20b9", cx2[3], dc.y + 14f, tp(8f, true))
        val balH = tp(8f, true).apply { textAlign = Paint.Align.RIGHT }
        c.drawText("Balance \u20b9", cx2[4], dc.y + 14f, balH)
        dc.advance(22f); dc.hLine(color = C_GOLD); dc.advance(2f)

        var running = 0.0
        ledger.forEach { l ->
            dc.need(14f)
            running += l.amount
            c.drawText(l.date.dateStr(), cx2[0], dc.y + 11f, tp(9f))
            c.drawText(l.title.take(32), cx2[1], dc.y + 11f, tp(9f))
            if (l.amount > 0) {
                val drP = tp(9f, color = C_RED).apply { textAlign = Paint.Align.RIGHT }
                c.drawText(l.amount.plain(0), cx2[3], dc.y + 11f, drP)
            } else {
                val crP = tp(9f, color = C_GRN).apply { textAlign = Paint.Align.RIGHT }
                c.drawText((-l.amount).plain(0), cx2[2], dc.y + 11f, crP)
            }
            val balP = tp(9f, true, if (running > 0) C_RED else C_GRN).apply { textAlign = Paint.Align.RIGHT }
            c.drawText(kotlin.math.abs(running).plain(0), cx2[4], dc.y + 11f, balP)
            dc.advance(13f); dc.hLine(color = C_BDR); dc.advance(2f)
        }

        dc.advance(8f); dc.need(20f); dc.hLine(); dc.advance(4f)
        val totalDue = d.partyDue(party.id)
        val totMsg = when {
            totalDue > 0.5  -> "Balance Due: ${totalDue.money()}"
            totalDue < -0.5 -> "Overpaid: ${(-totalDue).money()}"
            else             -> "Account Settled \u2713"
        }
        val totP2 = tp(12f, true, if (totalDue > 0.5) C_RED else C_GRN).apply { textAlign = Paint.Align.RIGHT }
        c.drawText(totMsg, PW - MARGIN, dc.y + 14f, totP2)

        return dc.finish("statement_${party.name.replace(Regex("[^A-Za-z0-9]"), "_").take(20)}.pdf", ctx)
    }

    // ── Product Tags / Labels ─────────────────────────────────────

    fun tags(ctx: Context, d: AppData, products: List<Product>, isLabel: Boolean): File {
        val layout = if (isLabel) d.label else d.tag
        val mmToPt = 2.835f
        val tagW = (layout.lengthMm * mmToPt).toInt().coerceIn(80, 595)
        val tagH = (layout.breadthMm * mmToPt).toInt().coerceIn(30, 200)

        val doc = PdfDocument()
        products.forEachIndexed { idx, prod ->
            val pi = PdfDocument.PageInfo.Builder(tagW, tagH, idx + 1).create()
            val page = doc.startPage(pi)
            val c = page.canvas
            val cat = d.category(prod.categoryId)
            val pad = 3f

            c.drawRect(0f, 0f, tagW.toFloat(), tagH.toFloat(), fp(Color.WHITE))
            c.drawRect(0f, 0f, tagW.toFloat(), tagH.toFloat(), lp(C_BDR))

            val textW = tagW * 0.65f
            var ty = pad + 9f
            c.drawText(prod.name.take(22), pad, ty, tp(7f, true)); ty += 9f
            if (prod.code.isNotBlank()) { c.drawText(prod.code, pad, ty, tp(6f, color = C_SUB)); ty += 8f }
            if (prod.purity.isNotBlank()) { c.drawText(prod.purity, pad, ty, tp(6f, color = C_SUB)); ty += 8f }
            c.drawText("GW:${prod.grossWt.plain(3)}g  NW:${prod.netWt.plain(3)}g", pad, ty, tp(6f, color = C_SUB)); ty += 8f
            cat?.let { c.drawText("Cat: ${it.name}", pad, ty, tp(6f, color = C_SUB)); ty += 8f }
            if (prod.huids.isNotEmpty()) c.drawText("HUID: ${prod.huids.first().take(6)}", pad, ty, tp(6f, color = C_SUB))

            // right panel
            val rx = textW + 4f
            val rw = tagW - rx - pad
            c.drawLine(textW, pad, textW, tagH - pad, lp(C_BDR))
            val fnP = tp(6f, true, C_GOLD).apply { textAlign = Paint.Align.CENTER }
            c.drawText((d.profile?.businessName ?: "").take(12), rx + rw / 2, pad + 8f, fnP)
            if (prod.size.isNotBlank()) {
                val sP = tp(6f).apply { textAlign = Paint.Align.CENTER }
                c.drawText("Size:${prod.size}", rx + rw / 2, pad + 18f, sP)
            }
            doc.finishPage(page)
        }

        val f = pdfFile(ctx, "tags_${System.currentTimeMillis()}.pdf")
        f.outputStream().use { doc.writeTo(it) }
        doc.close()
        return f
    }
}
