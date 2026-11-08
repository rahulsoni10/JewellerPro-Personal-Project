package com.jeweldesk.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.Invoice
import com.jeweldesk.app.data.InvoiceType
import com.jeweldesk.app.data.Kind
import com.jeweldesk.app.data.Ops
import com.jeweldesk.app.data.PAYMENT_MODES
import com.jeweldesk.app.data.Payment
import com.jeweldesk.app.data.calc
import com.jeweldesk.app.data.dateStr
import com.jeweldesk.app.data.money
import com.jeweldesk.app.data.now
import com.jeweldesk.app.data.num
import com.jeweldesk.app.data.startOfDay
import com.jeweldesk.app.data.totals
import com.jeweldesk.app.data.wt
import com.jeweldesk.app.pdf.Pdf
import com.jeweldesk.app.ui.ConfirmDialog
import com.jeweldesk.app.ui.EmptyState
import com.jeweldesk.app.ui.GoldButton
import com.jeweldesk.app.ui.GoldFab
import com.jeweldesk.app.ui.HeaderCard
import com.jeweldesk.app.ui.InlineDrop
import com.jeweldesk.app.ui.JCard
import com.jeweldesk.app.ui.JDialog
import com.jeweldesk.app.ui.LocalNav
import com.jeweldesk.app.ui.NumField
import com.jeweldesk.app.ui.OutlineGoldButton
import com.jeweldesk.app.ui.Page
import com.jeweldesk.app.ui.ScreenColumn
import com.jeweldesk.app.ui.ScrollTabs
import com.jeweldesk.app.ui.SearchBar
import com.jeweldesk.app.ui.SectionCard
import com.jeweldesk.app.ui.SolidPill
import com.jeweldesk.app.ui.SquareIconButton
import com.jeweldesk.app.ui.ValueRow
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.toast
import java.util.Calendar

private data class InvTab(val label: String, val types: Set<InvoiceType>, val totalLabel: String?, val actions: List<InvoiceType>)

private val INV_TABS = listOf(
    InvTab("GST Sale", setOf(InvoiceType.GST_SALE), "Total Sale", listOf(InvoiceType.GST_SALE, InvoiceType.GST_SALE_RETURN)),
    InvTab("Estimate Sale", setOf(InvoiceType.EST_SALE), "Total Sale", listOf(InvoiceType.EST_SALE)),
    InvTab("GST Purchase", setOf(InvoiceType.GST_PURCHASE, InvoiceType.GST_PURCHASE_METAL), "Total Purchase", listOf(InvoiceType.GST_PURCHASE, InvoiceType.GST_PURCHASE_METAL)),
    InvTab("Estimate Purchase", setOf(InvoiceType.EST_PURCHASE, InvoiceType.EST_PURCHASE_METAL), "Total Purchase", listOf(InvoiceType.EST_PURCHASE, InvoiceType.EST_PURCHASE_METAL)),
    InvTab("GST Sale Return", setOf(InvoiceType.GST_SALE_RETURN), null, listOf(InvoiceType.GST_SALE_RETURN)),
    InvTab("GST Purchase Return", setOf(InvoiceType.GST_PURCHASE_RETURN), null, listOf(InvoiceType.GST_PURCHASE_RETURN)),
)

val PERIODS = listOf("All", "Today", "This Week", "This Month", "Last Month", "This Year")

fun periodRange(p: String): LongRange? {
    val c = Calendar.getInstance()
    val today = startOfDay(now())
    return when (p) {
        "Today" -> today..Long.MAX_VALUE
        "This Week" -> { c.timeInMillis = today; c.set(Calendar.DAY_OF_WEEK, c.firstDayOfWeek); c.timeInMillis..Long.MAX_VALUE }
        "This Month" -> { c.timeInMillis = today; c.set(Calendar.DAY_OF_MONTH, 1); c.timeInMillis..Long.MAX_VALUE }
        "Last Month" -> {
            c.timeInMillis = today; c.set(Calendar.DAY_OF_MONTH, 1); val end = c.timeInMillis - 1
            c.add(Calendar.MONTH, -1); c.timeInMillis..end
        }
        "This Year" -> { c.timeInMillis = today; c.set(Calendar.DAY_OF_YEAR, 1); c.timeInMillis..Long.MAX_VALUE }
        else -> null
    }
}

@Composable
fun InvoicesTab() {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var period by rememberSaveable { mutableStateOf("All") }
    var searching by rememberSaveable { mutableStateOf(false) }
    var q by rememberSaveable { mutableStateOf("") }
    var dial by remember { mutableStateOf(false) }
    val t = INV_TABS[tab]
    val range = periodRange(period)
    val list = d.invoices.filter { it.type in t.types && (range == null || it.date in range) }
        .filter { q.isBlank() || it.displayNo.contains(q, true) || it.partyName.contains(q, true) || it.partyMobile.contains(q) }
        .sortedWith(compareByDescending<Invoice> { it.date }.thenByDescending { it.number })

    Page(
        header = {
            HeaderCard("Invoices", "GST & estimate invoices", Icons.Default.Description, actions = {
                SquareIconButton(Icons.Default.Refresh) { ctx.toast("Up to date") }
            })
        },
        navPadding = false,
        fab = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (dial) {
                    t.actions.forEachIndexed { i, type ->
                        SolidPill(type.title, if (i == 0) Icons.Default.Description else Icons.AutoMirrored.Filled.Undo, if (i == 0) Gold.Main else Color(0xFF2E8F63)) {
                            dial = false; nav.navigate("inv/new/${type.name}")
                        }
                    }
                    GoldFab("Close", Icons.Default.Close) { dial = false }
                } else GoldFab(if (t.actions.size == 1) t.actions.first().short else t.actions.first().title, Icons.Default.Add) {
                    if (t.actions.size == 1) nav.navigate("inv/new/${t.actions.first().name}") else dial = true
                }
            }
        },
    ) {
        Column(Modifier.fillMaxSize()) {
            ScrollTabs(INV_TABS.map { it.label }, tab) { tab = it; dial = false }
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Row(
                    Modifier.weight(1f).height(52.dp).clip(RoundedCornerShape(10.dp)).border(1.dp, jc.border, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (t.totalLabel != null) {
                        Text("${t.totalLabel}: ${list.sumOf { it.totals().total }.money()}", fontWeight = FontWeight.Bold, color = jc.text, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    PeriodDrop(period) { period = it }
                    if (t.totalLabel == null) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.width(10.dp))
                SquareIconButton(if (searching) Icons.Default.Close else Icons.Default.Search, tint = Gold.Text) { searching = !searching; q = "" }
            }
            if (searching) SearchBar(q, { q = it }, "Invoice no., party or mobile", Modifier.padding(horizontal = 16.dp))
            if (list.isEmpty()) EmptyState(Icons.Default.Description, "No invoices found")
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(list, key = { it.id }) { inv -> InvoiceRow(inv) { nav.navigate("inv/view/${inv.id}") } }
            }
        }
    }
}

@Composable
fun PeriodDrop(value: String, onPick: (String) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        Row(Modifier.clickable { open = true }.padding(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(value, color = Gold.Text, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.KeyboardArrowDown, null, tint = Gold.Text)
        }
        DropdownMenu(open, { open = false }) {
            PERIODS.forEach { p -> DropdownMenuItem(text = { Text(p) }, onClick = { open = false; onPick(p) }) }
        }
    }
}

@Composable
fun InvoiceRow(inv: Invoice, onClick: () -> Unit) {
    val t = inv.totals()
    JCard(onClick = onClick) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("#${inv.displayNo}  ${inv.partyName.ifBlank { "Walk-in" }}", fontWeight = FontWeight.SemiBold, color = jc.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${inv.type.short} • ${inv.date.dateStr()}", color = jc.sub, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(t.total.money(), fontWeight = FontWeight.Bold, color = jc.text)
                if (t.remaining > 0.5) Text("Due ${t.remaining.money()}", color = Gold.Red, fontSize = 13.sp)
                else Text("Settled", color = Gold.Green, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun InvoiceViewScreen(id: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    val gate = rememberGstGate()
    val inv = d.invoices.firstOrNull { it.id == id }
    var confirmDelete by remember { mutableStateOf(false) }
    var addPay by remember { mutableStateOf(false) }
    if (inv == null) {
        Page({ HeaderCard("Invoice", back = true) }) { EmptyState(Icons.Default.Description, "Invoice not found") }
        return
    }
    val t = inv.totals()
    Page({
        HeaderCard("${inv.type.short} #${inv.displayNo}", inv.type.title, back = true, actions = {
            SquareIconButton(Icons.Default.Share) { Pdf.share(ctx, Pdf.invoice(ctx, d, inv)) }
        })
    }) {
        ScreenColumn {
            SectionCard(if (inv.type.isSaleSide) "Customer" else "Supplier", Icons.Default.Person) {
                Text(inv.partyName.ifBlank { "Walk-in customer" }, fontWeight = FontWeight.SemiBold, color = jc.text, fontSize = 17.sp)
                if (inv.partyMobile.isNotBlank()) Text(inv.partyMobile, color = jc.sub)
                if (inv.partyAddress.isNotBlank()) Text(inv.partyAddress, color = jc.sub)
                if (inv.partyGstin.isNotBlank()) Text("GSTIN: ${inv.partyGstin}", color = jc.sub)
                ValueRow("Date", inv.date.dateStr(), jc.text)
                if (inv.placeOfSupply.isNotBlank()) ValueRow("Place of Supply", inv.placeOfSupply, jc.text)
            }
            if (inv.items.isNotEmpty()) SectionCard("Products", Icons.Outlined.Diamond) {
                inv.items.forEachIndexed { i, it ->
                    val c = it.calc()
                    Column {
                        Text("${i + 1}. ${it.name} ${if (it.code.isNotBlank()) "(${it.code})" else ""}", fontWeight = FontWeight.Medium, color = jc.text)
                        Text(
                            listOfNotNull(
                                it.purity.takeIf { s -> s.isNotBlank() },
                                "Gross ${it.grossWt.wt()}", if (it.lessWt > 0) "Less ${it.lessWt.wt()}" else null,
                                "Net ${c.net.wt()}", "Rate ${it.rate.money()}/g", if (it.huid.isNotBlank()) "HUID ${it.huid}" else null,
                            ).joinToString(" • "),
                            color = jc.sub, fontSize = 13.sp,
                        )
                        Row {
                            Text(
                                "Metal ${c.metal.money()}" + (if (c.making > 0) " + Making ${c.making.money()}" else "") +
                                    (if (c.wastage > 0) " + Wastage ${c.wastage.money()}" else "") + (if (c.hallmark > 0) " + HM ${c.hallmark.money()}" else "") +
                                    (if (c.stone > 0) " + Stone ${c.stone.money()}" else "") + (if (c.labour > 0) " + Labour ${c.labour.money()}" else ""),
                                color = jc.sub, fontSize = 13.sp, modifier = Modifier.weight(1f),
                            )
                            Text(c.total.money(), color = Gold.Text, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            if (inv.metals.isNotEmpty()) SectionCard(if (inv.type.kind == Kind.PURCHASE_METAL) "Metal Purchased" else "Collected Metal", Icons.Outlined.Diamond) {
                inv.metals.forEach { m ->
                    ValueRow("${m.stockName}: ${m.givenWt.wt()} @ ${m.tunch}% = ${m.fineWt.wt()}", m.amount.money())
                }
            }
            SectionCard("Totals", Icons.Default.Calculate) {
                ValueRow("Subtotal", t.subtotal.money())
                if (t.discount > 0) ValueRow("Discount", "- " + t.discount.money())
                if (t.oldMetal > 0) ValueRow("Old Metal", "- " + t.oldMetal.money())
                if (inv.type.isGst) {
                    ValueRow("Taxable Amount", t.taxable.money())
                    ValueRow("GST(${inv.gstRate.toInt()}%)", t.gst.money(), labelSuffix = if (inv.interState) "(IGST)" else "(SGST, CGST)")
                }
                if (inv.roundOff != 0.0) ValueRow("Round Off", inv.roundOff.money())
                ValueRow("Total Amount", t.total.money(), jc.text, bold = true)
                if (inv.duesIncluded > 0) ValueRow("Previous Dues", inv.duesIncluded.money())
                if (inv.advanceApplied > 0) ValueRow("Advance Adjusted", "- " + inv.advanceApplied.money())
                ValueRow("Total to Collect", t.toCollect.money())
                ValueRow(if (inv.type.cashSign > 0) "Total Received" else "Total Paid", t.received.money())
                ValueRow("Remaining Amount", t.remaining.money(), if (t.remaining > 0.5) Gold.Red else Gold.Green, bold = true)
            }
            if (inv.payments.isNotEmpty()) SectionCard("Payments", Icons.Default.Payments) {
                inv.payments.forEach { p -> ValueRow("${p.mode} • ${(if (p.date > 0) p.date else inv.date).dateStr()}", p.amount.money()) }
            }
            if (inv.description.isNotBlank()) SectionCard("Invoice Description", Icons.Default.Description) { Text(inv.description, color = jc.text) }

            if (t.remaining > 0.5) GoldButton("Add Payment", icon = Icons.Default.Add) { addPay = true }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineGoldButton("Share PDF", Modifier.weight(1f), Icons.Default.Share) { Pdf.share(ctx, Pdf.invoice(ctx, d, inv)) }
                OutlineGoldButton("Print", Modifier.weight(1f), Icons.Default.Print) { Pdf.print(ctx, Pdf.invoice(ctx, d, inv), "Invoice ${inv.displayNo}") }
            }
            OutlineGoldButton("WhatsApp", icon = Icons.Default.Share) { Pdf.share(ctx, Pdf.invoice(ctx, d, inv), whatsapp = true, phone = inv.partyMobile) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineGoldButton("Edit", Modifier.weight(1f), Icons.Default.Edit) {
                    if (inv.type.isGst) gate { nav.navigate("inv/edit/${inv.id}") } else nav.navigate("inv/edit/${inv.id}")
                }
                OutlineGoldButton("Delete", Modifier.weight(1f), Icons.Default.Delete, Gold.Red) {
                    if (inv.type.isGst) gate { confirmDelete = true } else confirmDelete = true
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
    if (confirmDelete) ConfirmDialog("Delete invoice?", "This will reverse its stock and balance effects.", onDismiss = { confirmDelete = false }) {
        Ops.deleteInvoice(inv.id); nav.popBackStack()
    }
    if (addPay) {
        var amt by remember { mutableStateOf(t.remaining.toLong().toString()) }
        var mode by remember { mutableStateOf("Cash") }
        JDialog("Add Payment", Icons.Default.Payments, { addPay = false }) {
            NumField(amt, { amt = it }, "Amount", icon = Icons.Default.CurrencyRupee, trailing = { InlineDrop(mode, PAYMENT_MODES, { it }) { mode = it } })
            GoldButton("Save Payment") {
                val a = amt.num()
                if (a <= 0) { ctx.toast("Enter amount"); return@GoldButton }
                Ops.saveInvoice(inv.copy(payments = inv.payments + Payment(a, mode, now())))
                addPay = false
            }
        }
    }
}
