package com.jeweldesk.app.data

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.round

// ---------- formatting ----------

private val inLocale = Locale("en", "IN")

fun Double.money(decimals: Int = 2): String {
    val nf = NumberFormat.getNumberInstance(inLocale)
    nf.maximumFractionDigits = decimals
    nf.minimumFractionDigits = 0
    return "₹ " + nf.format(this.round2())
}

fun Double.plain(decimals: Int = 2): String {
    val nf = NumberFormat.getNumberInstance(inLocale)
    nf.maximumFractionDigits = decimals
    nf.minimumFractionDigits = 0
    nf.isGroupingUsed = false
    return nf.format(this)
}

fun Double.wt(): String = String.format(Locale.US, "%.3f g", this)
fun Double.wtShort(): String = if (this == 0.0) "0g" else String.format(Locale.US, "%.3fg", this)
fun Double.round2(): Double = round(this * 100.0) / 100.0

fun String.num(): Double = this.trim().replace(",", "").toDoubleOrNull() ?: 0.0

fun Long.dateStr(): String = SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date(this))
fun Long.monthStr(): String = SimpleDateFormat("MMM yyyy", Locale.US).format(Date(this))
fun Long.dateTimeStr(): String = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.US).format(Date(this))

fun monthKey(ms: Long): Int {
    val c = Calendar.getInstance().apply { timeInMillis = ms }
    return c.get(Calendar.YEAR) * 12 + c.get(Calendar.MONTH)
}

fun monthStart(key: Int): Long = Calendar.getInstance().apply {
    clear(); set(key / 12, key % 12, 1)
}.timeInMillis

fun startOfDay(ms: Long): Long = Calendar.getInstance().apply {
    timeInMillis = ms; set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
}.timeInMillis

// ---------- rates ----------

fun Rates.gold22Eff() = if (gold22 > 0) gold22 else gold24 * 0.916
fun Rates.gold18Eff() = if (gold18 > 0) gold18 else gold24 * 0.75
fun Rates.silver925Eff() = if (silver925 > 0) silver925 else silver999 * 0.925

/** Default per-gram rate for an item given its metal, purity label and tunch. */
fun Rates.rateFor(metal: Metal, purity: String, tunch: String): Double {
    val t = tunch.num()
    if (metal == Metal.GOLD) {
        when (purity.uppercase().trim()) {
            "24K", "999", "995" -> return gold24
            "22K", "916" -> return gold22Eff()
            "18K", "750" -> return gold18Eff()
        }
        val k = purity.uppercase().removeSuffix("K").toDoubleOrNull()
        if (k != null && k in 1.0..24.0) return gold24 * k / 24.0
        if (t > 0) return gold24 * t / 100.0
        return gold22Eff()
    } else {
        when (purity.trim()) {
            "999" -> return silver999
            "925" -> return silver925Eff()
        }
        val p = purity.trim().toDoubleOrNull()
        if (p != null && p in 1.0..1000.0) return silver999 * p / 1000.0
        if (t > 0) return silver999 * t / 100.0
        return silver999
    }
}

fun Rates.fineRate(metal: Metal) = if (metal == Metal.GOLD) gold24 else silver999

// ---------- invoice math ----------

data class ItemCalc(
    val net: Double, val metal: Double, val wastage: Double, val making: Double,
    val hallmark: Double, val stone: Double, val labour: Double = 0.0,
) {
    val total get() = metal + wastage + making + hallmark + stone + labour
}

fun chargeAmount(value: Double, unit: ChargeUnit, net: Double, pcs: Int, metalValue: Double): Double = when (unit) {
    ChargeUnit.PER_GRAM -> value * net
    ChargeUnit.PERCENT -> metalValue * value / 100.0
    ChargeUnit.FIXED -> value
    ChargeUnit.PER_PIECE -> value * max(pcs, 1)
}

fun InvoiceItem.calc(): ItemCalc {
    val net = netWt
    val metalValue = net * rate
    return ItemCalc(
        net = net,
        metal = metalValue,
        wastage = net * wastagePct / 100.0 * rate,
        making = chargeAmount(making, makingUnit, net, pcs, metalValue),
        hallmark = chargeAmount(hallmark, hallmarkUnit, net, pcs, metalValue),
        stone = stone,
        labour = labour * net,
    )
}

data class Totals(
    val subtotal: Double,
    val oldMetal: Double,
    val discount: Double,
    val taxable: Double,
    val gst: Double,
    val amountWithTax: Double,
    val total: Double,
    val toCollect: Double,
    val received: Double,
    val remaining: Double,
) {
    val cgst get() = gst / 2
    val sgst get() = gst / 2
}

fun Invoice.totals(): Totals {
    val metalSum = metals.sumOf { it.amount }
    val itemsSum = if (type.kind == Kind.PURCHASE_METAL) metalSum else items.sumOf { it.calc().total }
    val oldMetal = if (type.kind == Kind.SALE) metalSum else 0.0
    val disc = if (discountPercent) itemsSum * discount / 100.0 else discount
    val taxable: Double
    val gst: Double
    val withTax: Double
    if (type.isGst) {
        if (gstBeforeOldMetal) {
            taxable = itemsSum - disc
            gst = max(taxable, 0.0) * gstRate / 100.0
            withTax = taxable + gst - oldMetal
        } else {
            taxable = itemsSum - disc - oldMetal
            gst = max(taxable, 0.0) * gstRate / 100.0
            withTax = taxable + gst
        }
    } else {
        taxable = itemsSum - disc - oldMetal
        gst = 0.0
        withTax = taxable
    }
    val total = withTax + roundOff
    val toCollect = total + duesIncluded - advanceApplied
    val received = payments.sumOf { it.amount }
    return Totals(itemsSum, oldMetal, disc, taxable, gst, withTax, total, toCollect, received, toCollect - received)
}

// ---------- party balances ----------

/** Positive = customer owes the shop / shop owes the supplier. */
fun AppData.partyDue(partyId: String): Double {
    val p = party(partyId) ?: return 0.0
    var due = p.openingDue
    invoices.filter { it.partyId == partyId }.forEach { inv ->
        val t = inv.totals()
        val sign = when (inv.type.kind) {
            Kind.SALE, Kind.PURCHASE, Kind.PURCHASE_METAL -> 1
            Kind.SALE_RETURN, Kind.PURCHASE_RETURN -> -1
        }
        due += sign * t.remaining - inv.duesIncluded
    }
    khata.filter { it.partyId == partyId }.forEach { due += it.type.dueSign * it.amount }
    return due
}

fun AppData.partyAdvance(partyId: String): Double {
    var adv = 0.0
    khata.filter { it.partyId == partyId }.forEach { adv += it.type.advSign * it.amount }
    adv -= invoices.filter { it.partyId == partyId }.sumOf { it.advanceApplied }
    return adv
}

// ---------- cash / bank balances ----------

data class BalanceRow(val date: Long, val title: String, val book: Book, val cash: Boolean, val amount: Double, val adjustmentId: String = "")

fun AppData.balanceRows(): List<BalanceRow> {
    val rows = mutableListOf<BalanceRow>()
    invoices.forEach { inv ->
        inv.payments.forEach { pay ->
            rows += BalanceRow(
                if (pay.date > 0) pay.date else inv.date,
                "${inv.type.short} #${inv.displayNo} ${inv.partyName}".trim(),
                inv.type.book, isCashMode(pay.mode), inv.type.cashSign * pay.amount,
            )
        }
    }
    khata.forEach { k ->
        val p = party(k.partyId)
        val supplier = p?.type == PartyType.SUPPLIER
        if (k.type.cashSign != 0) {
            val sign = if (supplier) -k.type.cashSign else k.type.cashSign
            val label = if (supplier) k.type.supplierLabel else k.type.customerLabel
            rows += BalanceRow(k.date, "$label - ${p?.name ?: ""}", k.book, isCashMode(k.mode), sign * k.amount)
        }
    }
    expenses.forEach { e -> rows += BalanceRow(e.date, "Expense - ${e.category}", e.book, isCashMode(e.mode), -e.amount) }
    cashEntries.forEach { c -> rows += BalanceRow(c.date, c.note.ifBlank { "Adjustment" }, c.book, c.cash, c.amount, c.id) }
    girvis.forEach { g ->
        rows += BalanceRow(g.date, "Girvi #${g.number} loan - ${g.customerName}", g.book, isCashMode(g.mode), -g.principal)
        g.payments.forEach { p -> rows += BalanceRow(p.date, "Girvi #${g.number} receipt", g.book, isCashMode(p.mode), p.amount) }
        g.transfer?.let { t -> rows += BalanceRow(t.date, "Girvi #${g.number} transferred to ${t.toName}", g.book, true, t.amount) }
    }
    return rows.sortedByDescending { it.date }
}

fun AppData.balance(book: Book, cash: Boolean): Double =
    balanceRows().filter { it.book == book && it.cash == cash }.sumOf { it.amount }

fun AppData.monthTotal(types: Set<InvoiceType>, month: Int): Double =
    invoices.filter { it.type in types && monthKey(it.date) == month }.sumOf { it.totals().total }

// ---------- stock ----------

fun AppData.stockProducts(metal: Metal) = products.filter { it.metal == metal && it.inStock && (it.type == StockType.INDIVIDUAL || it.grossWt > 0) }
fun AppData.totalStockWeight(metal: Metal) = stockProducts(metal).sumOf { it.netWt }
fun AppData.gstStockWeight(metal: Metal) = gstWeight[metal] ?: 0.0

// ---------- girvi interest ----------

data class InterestResult(val days: Long, val months: Double, val interest: Double, val paid: Double) {
    fun outstanding(principal: Double) = principal + interest - paid
}

fun computeInterest(
    principal: Double,
    ratePerMonth: Double,
    from: Long,
    to: Long,
    type: InterestType,
    method: CalcMethod,
): Pair<Long, Double> {
    val days = max(0L, (startOfDay(to) - startOfDay(from)) / 86_400_000L)
    val raw = days / 30.0
    val months = when (method) {
        CalcMethod.MONTHLY -> max(1.0, ceil(raw))
        CalcMethod.DAILY -> raw
        CalcMethod.ONE_MONTH_PLUS_DAILY -> max(1.0, raw)
        CalcMethod.FIFTEEN_DAYS_PLUS_DAILY -> max(0.5, raw)
    }
    val r = ratePerMonth / 100.0
    val interest = when (type) {
        InterestType.SIMPLE -> principal * r * months
        InterestType.COMPOUND -> {
            val years = floor(months / 12.0)
            val rem = months - years * 12
            val amount = principal * (1 + r * 12).pow(years) * (1 + r * rem)
            amount - principal
        }
    }
    return days to interest
}

fun Girvi.interestAsOf(asOf: Long = now()): InterestResult {
    val end = if (released && releasedAt > 0) releasedAt else asOf
    val (days, interest) = computeInterest(principal, rate, date, end, interestType, method)
    return InterestResult(days, days / 30.0, interest, payments.sumOf { it.amount })
}

// ---------- amount in words (Indian system) ----------

private val ones = arrayOf(
    "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve",
    "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen",
)
private val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

private fun twoDigits(n: Int): String = if (n < 20) ones[n] else (tens[n / 10] + if (n % 10 != 0) " " + ones[n % 10] else "")

private fun threeDigits(n: Int): String {
    val h = n / 100
    val r = n % 100
    return listOfNotNull(if (h > 0) ones[h] + " Hundred" else null, if (r > 0) twoDigits(r) else null).joinToString(" ")
}

fun amountInWords(amount: Double): String {
    val rupees = floor(kotlin.math.abs(amount)).toLong()
    val paise = round((kotlin.math.abs(amount) - rupees) * 100).toInt()
    if (rupees == 0L && paise == 0) return "Zero Rupees Only"
    val parts = mutableListOf<String>()
    var n = rupees
    val crore = n / 10_000_000; n %= 10_000_000
    val lakh = n / 100_000; n %= 100_000
    val thousand = n / 1000; n %= 1000
    if (crore > 0) parts += (if (crore >= 100) amountInWords(crore.toDouble()).removeSuffix(" Rupees Only") else twoDigits(crore.toInt())) + " Crore"
    if (lakh > 0) parts += twoDigits(lakh.toInt()) + " Lakh"
    if (thousand > 0) parts += twoDigits(thousand.toInt()) + " Thousand"
    if (n > 0) parts += threeDigits(n.toInt())
    var s = parts.joinToString(" ") + " Rupees"
    if (paise > 0) s += " and " + twoDigits(paise) + " Paise"
    return "$s Only"
}

fun sha256(text: String): String {
    val md = java.security.MessageDigest.getInstance("SHA-256")
    return md.digest(text.toByteArray()).joinToString("") { "%02x".format(it) }
}
