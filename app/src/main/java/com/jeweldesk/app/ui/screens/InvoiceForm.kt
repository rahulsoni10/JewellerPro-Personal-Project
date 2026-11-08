package com.jeweldesk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.AppData
import com.jeweldesk.app.data.ChargeUnit
import com.jeweldesk.app.data.DEFAULT_METALS
import com.jeweldesk.app.data.INDIAN_STATES
import com.jeweldesk.app.data.Invoice
import com.jeweldesk.app.data.InvoiceItem
import com.jeweldesk.app.data.InvoiceSettings
import com.jeweldesk.app.data.InvoiceType
import com.jeweldesk.app.data.Kind
import com.jeweldesk.app.data.Metal
import com.jeweldesk.app.data.MetalItem
import com.jeweldesk.app.data.Ops
import com.jeweldesk.app.data.PAYMENT_MODES
import com.jeweldesk.app.data.Party
import com.jeweldesk.app.data.Payment
import com.jeweldesk.app.data.Product
import com.jeweldesk.app.data.StockType
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.data.calc
import com.jeweldesk.app.data.dateStr
import com.jeweldesk.app.data.fineRate
import com.jeweldesk.app.data.money
import com.jeweldesk.app.data.newId
import com.jeweldesk.app.data.now
import com.jeweldesk.app.data.num
import com.jeweldesk.app.data.partyAdvance
import com.jeweldesk.app.data.partyDue
import com.jeweldesk.app.data.plain
import com.jeweldesk.app.data.rateFor
import com.jeweldesk.app.data.round2
import com.jeweldesk.app.data.totals
import com.jeweldesk.app.data.wt
import com.jeweldesk.app.ui.DNum
import com.jeweldesk.app.ui.DateField
import com.jeweldesk.app.ui.DropField
import com.jeweldesk.app.ui.GoldButton
import com.jeweldesk.app.ui.GoldSquareButton
import com.jeweldesk.app.ui.HeaderCard
import com.jeweldesk.app.ui.InlineDrop
import com.jeweldesk.app.ui.JCard
import com.jeweldesk.app.ui.JDialog
import com.jeweldesk.app.ui.JField
import com.jeweldesk.app.ui.LocalNav
import com.jeweldesk.app.ui.NumField
import com.jeweldesk.app.ui.Page
import com.jeweldesk.app.ui.Pill
import com.jeweldesk.app.ui.SegTabs
import com.jeweldesk.app.ui.ScreenColumn
import com.jeweldesk.app.ui.SectionCard
import com.jeweldesk.app.ui.SmallToggle
import com.jeweldesk.app.ui.ValueRow
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.toast
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.min

/** Hand-off from the fixed amount calculator to a new invoice. */
object Prefill {
    var item: InvoiceItem? = null
}

fun lineFrom(p: Product, d: AppData): InvoiceItem {
    val bulk = p.type == StockType.BULK
    return InvoiceItem(
        productId = p.id, name = p.name, code = p.code, metal = p.metal, stockType = p.type, purity = p.purity,
        tunch = p.tunch, hsn = p.hsn, huid = p.huids.joinToString(", "), size = p.size,
        pcs = if (bulk) 1 else p.pcs, grossWt = if (bulk) 0.0 else p.grossWt, lessWt = if (bulk) 0.0 else p.lessWt,
        diamondWt = p.diamondWt, rate = d.rates.rateFor(p.metal, p.purity, p.tunch),
        making = p.charges.making, makingUnit = p.charges.makingUnit, wastagePct = p.charges.wastagePct,
        hallmark = p.charges.hallmark, hallmarkUnit = p.charges.hallmarkUnit, stone = p.charges.stone,
    )
}

fun isWholesale(d: AppData) = d.profile?.businessType?.contains("Wholesale", true) == true

@Composable
fun InvoiceFormScreen(newType: InvoiceType?, editId: String?, partyId: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    val existing = remember { editId?.let { id -> Store.value.invoices.firstOrNull { it.id == id } } }
    val type = existing?.type ?: newType!!
    val s = d.invoiceSettings(type.book)
    val kind = type.kind
    val sale = type.isSaleSide
    var inv by remember {
        mutableStateOf(
            existing ?: Invoice(
                id = newId(), type = type, number = Ops.nextNumber(Store.value, type.name), prefix = s.prefix, date = now(),
                placeOfSupply = Store.value.profile?.state ?: "", gstBeforeOldMetal = s.gstBeforeOldMetal, createdAt = now(),
            ).let { base ->
                val p = Store.value.party(partyId)
                if (p != null) base.copy(partyId = p.id, partyName = p.name, partyMobile = p.mobile, partyAddress = listOf(p.address, p.city).filter { it.isNotBlank() }.joinToString(", "), partyGstin = p.gstin) else base
            },
        )
    }
    val uids = remember { mutableStateListOf<String>().apply { repeat(maxOf(1, inv.items.size)) { add(newId()) } } }
    val lines = remember {
        mutableStateListOf<InvoiceItem?>().apply {
            if (existing != null) addAll(existing.items)
            else if (Prefill.item != null) { add(Prefill.item); Prefill.item = null }
            if (isEmpty() && kind != Kind.PURCHASE_METAL) add(null)
        }
    }
    if (uids.size < lines.size) repeat(lines.size - uids.size) { uids.add(newId()) }
    val metals = remember { mutableStateListOf<MetalItem>().apply { addAll(inv.metals); if (kind == Kind.PURCHASE_METAL && isEmpty()) add(defaultMetal(Store.value)) } }
    var pickerFor by remember { mutableStateOf(-1) }
    var editMeta by remember { mutableStateOf(false) }
    var payAmt by remember { mutableStateOf("") }
    var payMode by remember { mutableStateOf("Cash") }

    val current = inv.copy(items = lines.filterNotNull(), metals = metals.toList())
    val t = current.totals()
    val otherData = d.copy(invoices = d.invoices.filterNot { it.id == inv.id })
    val party = d.party(inv.partyId)
    val prevDue = if (party != null) otherData.partyDue(party.id) else 0.0
    val advBal = if (party != null) otherData.partyAdvance(party.id) else 0.0
    val returns = kind == Kind.SALE_RETURN || kind == Kind.PURCHASE_RETURN
    val wholesale = isWholesale(d)
    val unit = s.metalRateUnit
    val myState = d.profile?.state ?: ""
    val inter = type.isGst && inv.placeOfSupply.isNotBlank() && myState.isNotBlank() && inv.placeOfSupply != myState

    Page({ HeaderCard(if (existing != null) "Edit ${type.title}" else "Create ${type.title}", type.title, back = true) }) {
        ScreenColumn {
            // ---- meta ----
            JCard {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        MetaLine(Icons.Default.Tag, "Invoice No.", inv.displayNo)
                        MetaLine(Icons.Default.CalendarMonth, "Date", inv.date.dateStr())
                    }
                    Column(Modifier.clip(RoundedCornerShape(10.dp)).clickable { editMeta = true }.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Edit, null, tint = Gold.Text)
                        Text("Edit", color = Gold.Text)
                    }
                }
            }

            // ---- party ----
            SectionCard(if (sale) "Customer Details" else "Supplier Details", Icons.Default.PersonPin) {
                PartyPicker(d, type.partyType, inv.partyId) { p ->
                    inv = if (p == null) inv.copy(partyId = "", partyName = "", partyMobile = "", partyAddress = "", partyGstin = "", duesIncluded = 0.0, advanceApplied = 0.0)
                    else inv.copy(
                        partyId = p.id, partyName = p.name, partyMobile = p.mobile, partyGstin = p.gstin,
                        partyAddress = listOf(p.address, p.city, p.state).filter { it.isNotBlank() }.joinToString(", "),
                        placeOfSupply = if (p.state.isNotBlank()) p.state else inv.placeOfSupply, duesIncluded = 0.0, advanceApplied = 0.0,
                    )
                }
                if (type.isGst) {
                    DropField("Place of Supply", inv.placeOfSupply, INDIAN_STATES, { it }, { inv = inv.copy(placeOfSupply = it) }, icon = Icons.Default.Map)
                }
            }

            // ---- product lines ----
            if (kind != Kind.PURCHASE_METAL) {
                lines.forEachIndexed { i, line ->
                    key(uids[i]) {
                        ItemCard(i, line, s, unit.grams, wholesale, d, type, onPick = { pickerFor = i }, onChange = { lines[i] = it }, onDelete = {
                            lines.removeAt(i); uids.removeAt(i)
                            if (lines.isEmpty()) { lines.add(null); uids.add(newId()) }
                        })
                    }
                }
                GoldButton("Add Product", icon = Icons.Default.Add) { lines.add(null); uids.add(newId()) }
            }
            if ((kind == Kind.SALE && s.collectOldMetal) || kind == Kind.PURCHASE_METAL) {
                metals.forEachIndexed { i, m ->
                    MetalCard(i, m, d, unit.grams, onChange = { metals[i] = it }, onDelete = { metals.removeAt(i) })
                }
                GoldButton(if (kind == Kind.PURCHASE_METAL) "Add Metal" else "Collect Metal", icon = Icons.Default.Add) { metals.add(defaultMetal(d)) }
            }

            SectionCard("Invoice Description", Icons.Default.Description) {
                JField(inv.description, { inv = inv.copy(description = it) }, "Invoice Description", icon = Icons.AutoMirrored.Filled.Notes, singleLine = false)
            }

            // ---- totals ----
            SectionCard("Totals", Icons.Default.Calculate) {
                ValueRow("Subtotal", t.subtotal.money())
                DNum(
                    inv.discount, { inv = inv.copy(discount = it) }, if (inv.discountPercent) "Discount (%)" else "Discount (₹)",
                    icon = Icons.Default.LocalOffer, decimals = 2,
                    trailing = {
                        Box(Modifier.padding(end = 6.dp)) {
                            SmallToggle(listOf("%", "₹"), if (inv.discountPercent) 0 else 1) { inv = inv.copy(discountPercent = it == 0) }
                        }
                    },
                )
                HorizontalDivider(color = jc.border)
                if (t.oldMetal > 0) ValueRow("Old Metal", "- " + t.oldMetal.money())
                if (type.isGst) {
                    ValueRow("Taxable Amount", t.taxable.money())
                    ValueRow("GST(${inv.gstRate.toInt()}%)", t.gst.money(), labelSuffix = if (inter) "(IGST)" else "(SGST, CGST)")
                    ValueRow("Amount with Tax", t.amountWithTax.money())
                }
                RoundOffRow(inv.roundOff, t.amountWithTax) { inv = inv.copy(roundOff = it) }
                HorizontalDivider(color = jc.border)
                ValueRow("Total Amount", t.total.money(), jc.text, bold = true)
            }

            // ---- dues / advance ----
            if (!returns) SectionCard(if (sale) "Customer Advance / Udhar" else "Supplier Advance / Dues", Icons.Default.VolunteerActivism) {
                if (party == null) Text("Select a ${if (sale) "customer" else "supplier"} to view unsettled records.", color = jc.text)
                else {
                    if (prevDue <= 0.5 && advBal <= 0.5) Text("No unsettled records.", color = jc.sub)
                    if (prevDue > 0.5) Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            inv.duesIncluded > 0, { on -> inv = inv.copy(duesIncluded = if (on) prevDue.round2() else 0.0) },
                            colors = CheckboxDefaults.colors(checkedColor = Gold.Main, checkmarkColor = Color.Black),
                        )
                        Text("Include previous ${if (sale) "udhar" else "dues"} ${prevDue.money()}", color = jc.text)
                    }
                    if (advBal > 0.5) {
                        Text("Advance available: ${advBal.money()}", color = Gold.Green)
                        DNum(inv.advanceApplied, { v -> inv = inv.copy(advanceApplied = min(v, min(advBal, t.total + inv.duesIncluded).coerceAtLeast(0.0))) }, "Adjust advance", icon = Icons.Default.CurrencyRupee, decimals = 2)
                    }
                }
            }

            // ---- payments ----
            SectionCard(if (type.cashSign > 0) "Collect Payment" else "Make Payment", Icons.Default.Payments) {
                NumField(payAmt, { payAmt = it }, "Amount", icon = Icons.Default.CurrencyRupee, trailing = {
                    InlineDrop(payMode, PAYMENT_MODES, { it }) { payMode = it }
                })
                GoldButton("Add Payment", icon = Icons.Default.Add, height = 50.dp) {
                    val a = payAmt.num()
                    if (a <= 0) { ctx.toast("Enter amount"); return@GoldButton }
                    inv = inv.copy(payments = inv.payments + Payment(a, payMode, inv.date))
                    payAmt = ""
                }
                inv.payments.forEachIndexed { i, p ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${p.mode}: ${p.amount.money()}", color = jc.text, modifier = Modifier.weight(1f))
                        Icon(Icons.Default.Close, null, tint = Gold.Red, modifier = Modifier.clickable {
                            inv = inv.copy(payments = inv.payments.filterIndexed { j, _ -> j != i })
                        })
                    }
                }
                if (t.remaining > 0.5 && inv.payments.isEmpty()) {
                    Text("Collect full amount", color = Gold.Text, fontWeight = FontWeight.Medium, modifier = Modifier.clickable {
                        inv = inv.copy(payments = listOf(Payment(t.toCollect.round2(), payMode, inv.date)))
                    })
                }
                HorizontalDivider(color = jc.border)
                ValueRow(if (type.cashSign > 0) "Total to Collect" else "Total to Pay", t.toCollect.money())
                ValueRow(if (type.cashSign > 0) "Total Received" else "Total Paid", t.received.money())
                ValueRow("Remaining Amount", t.remaining.money(), if (t.remaining > 0.5) Gold.Red else Gold.Green)
            }

            GoldButton(if (existing != null) "Update Invoice" else "Create Invoice", height = 64.dp) {
                val items = lines.filterNotNull()
                if (kind != Kind.PURCHASE_METAL && items.isEmpty()) { ctx.toast("Add at least one product"); return@GoldButton }
                if (kind == Kind.PURCHASE_METAL && metals.none { it.givenWt > 0 }) { ctx.toast("Add metal weight"); return@GoldButton }
                if (items.any { it.netWt <= 0 && it.stone <= 0 }) { ctx.toast("Enter weight for every product"); return@GoldButton }
                if (kind == Kind.SALE || kind == Kind.PURCHASE_RETURN) {
                    val old = existing?.items.orEmpty()
                    items.filter { it.stockType == StockType.BULK && it.productId.isNotBlank() }.groupBy { it.productId }.forEach { (pid, its) ->
                        val p = d.product(pid) ?: return@forEach
                        val available = p.grossWt + old.filter { it.productId == pid }.sumOf { it.grossWt }
                        if (its.sumOf { it.grossWt } > available + 0.0005) { ctx.toast("Only ${available.wt()} of ${p.name} in stock"); return@GoldButton }
                    }
                }
                if (inv.partyId.isBlank() && t.remaining > 0.5) {
                    ctx.toast("Select a ${if (sale) "customer" else "supplier"} to keep a balance, or collect full payment"); return@GoldButton
                }
                val final = current.copy(items = items, interState = inter)
                Ops.saveInvoice(final)
                nav.popBackStack()
                nav.navigate("inv/view/${final.id}")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (pickerFor >= 0) {
        ProductPicker(type, onDismiss = { pickerFor = -1 }) { picked ->
            val idx = pickerFor
            pickerFor = -1
            picked.forEachIndexed { k, p ->
                val l = lineFrom(p, d)
                if (k == 0 && idx < lines.size) lines[idx] = l else { lines.add(l); uids.add(newId()) }
            }
        }
    }
    if (editMeta) {
        var no by remember { mutableStateOf(inv.number.toString()) }
        var prefix by remember { mutableStateOf(inv.prefix) }
        var date by remember { mutableStateOf(inv.date) }
        JDialog("Invoice No. & Date", Icons.Default.Receipt, { editMeta = false }) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                JField(prefix, { prefix = it.take(10) }, "Prefix", Modifier.weight(1f))
                NumField(no, { no = it }, "Invoice No.", Modifier.weight(1f), integer = true)
            }
            DateField("Date", date, { date = it })
            GoldButton("Save") {
                val n = no.toIntOrNull() ?: 0
                if (n <= 0) { ctx.toast("Invalid number"); return@GoldButton }
                if (d.invoices.any { it.id != inv.id && it.type == type && it.number == n && it.prefix == prefix }) { ctx.toast("Invoice number already used"); return@GoldButton }
                inv = inv.copy(number = n, prefix = prefix, date = date, payments = inv.payments.map { it.copy(date = date) })
                editMeta = false
            }
        }
    }
}

fun defaultMetal(d: AppData): MetalItem {
    val name = (d.metalStock.firstOrNull { it.name == "Old Gold" } ?: d.metalStock.firstOrNull() ?: DEFAULT_METALS[3])
    return MetalItem(stockName = name.name, metal = name.metal, tunch = if (name.metal == Metal.GOLD) 91.6 else 92.5, rate = d.rates.fineRate(name.metal))
}

@Composable
private fun MetaLine(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = jc.text, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Column {
            Text(label, color = jc.sub, fontSize = 12.sp, letterSpacing = 1.sp)
            Text(value, color = jc.text, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PartyPicker(d: AppData, type: com.jeweldesk.app.data.PartyType, selectedId: String, onSelect: (Party?) -> Unit) {
    var q by remember { mutableStateOf("") }
    var adding by remember { mutableStateOf(false) }
    val selected = d.party(selectedId)
    val label = if (type == com.jeweldesk.app.data.PartyType.CUSTOMER) "Customer" else "Supplier"
    if (selected != null) {
        Row(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(jc.cream).border(1.dp, Gold.Main, RoundedCornerShape(14.dp)).padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Default.Person, null, tint = Gold.Text)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(selected.name, fontWeight = FontWeight.SemiBold, color = jc.text)
                if (selected.mobile.isNotBlank()) Text(selected.mobile, color = jc.sub, fontSize = 13.sp)
            }
            Icon(Icons.Default.Close, null, tint = Gold.Red, modifier = Modifier.clickable { onSelect(null) })
        }
        return
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.weight(1f)) {
            JField(q, { q = it }, "Search $label", icon = Icons.Default.Search)
            val matches = d.parties.filter { it.type == type && q.isNotBlank() && (it.name.contains(q, true) || it.mobile.contains(q)) }.take(8)
            DropdownMenu(matches.isNotEmpty(), { q = "" }, properties = androidx.compose.ui.window.PopupProperties(focusable = false)) {
                matches.forEach { p ->
                    DropdownMenuItem(text = { Text("${p.name}  ${p.mobile}") }, onClick = { q = ""; onSelect(p) })
                }
            }
        }
        Spacer(Modifier.width(10.dp))
        GoldSquareButton(Icons.Default.Add) { adding = true }
    }
    if (q.isBlank()) {
        val recent = d.parties.filter { it.type == type }.sortedByDescending { it.createdAt }.take(4)
        if (recent.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            recent.forEach { p ->
                Text(p.name, color = jc.text, fontSize = 13.sp, maxLines = 1, modifier = Modifier.weight(1f, fill = false).clip(Pill).border(1.dp, jc.border, Pill).clickable { onSelect(p) }.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
    if (adding) QuickPartyDialog(type, q, { adding = false }) { adding = false; q = ""; onSelect(it) }
}

@Composable
fun QuickPartyDialog(type: com.jeweldesk.app.data.PartyType, initialName: String, onDismiss: () -> Unit, onSaved: (Party) -> Unit) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var mobile by remember { mutableStateOf("") }
    var gstin by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var state by remember { mutableStateOf(Store.value.profile?.state ?: "") }
    JDialog("Add ${type.label}", Icons.Default.Person, onDismiss) {
        JField(name, { name = it }, "Name *", icon = Icons.Default.Person)
        JField(mobile, { s -> mobile = s.filter { it.isDigit() }.take(10) }, "Mobile", icon = Icons.Default.Phone, keyboard = KeyboardType.Phone)
        JField(gstin, { gstin = it.uppercase().take(15) }, "GSTIN", icon = Icons.Default.Receipt)
        JField(address, { address = it }, "Address", icon = Icons.Default.Map)
        DropField("State", state, INDIAN_STATES, { it }, { state = it })
        GoldButton("Save ${type.label}") {
            if (name.isBlank()) { ctx.toast("Enter name"); return@GoldButton }
            if (gstin.isNotBlank() && !isValidGstin(gstin)) { ctx.toast("Invalid GSTIN"); return@GoldButton }
            val p = Party(newId(), type, name.trim(), mobile, gstin = gstin, address = address.trim(), state = state, createdAt = now())
            Ops.saveParty(p)
            onSaved(p)
        }
    }
}

@Composable
private fun ItemCard(
    index: Int,
    item: InvoiceItem?,
    s: InvoiceSettings,
    unitGrams: Double,
    wholesale: Boolean,
    d: AppData,
    type: InvoiceType,
    onPick: () -> Unit,
    onChange: (InvoiceItem) -> Unit,
    onDelete: () -> Unit,
) {
    JCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Circle, null, tint = Gold.Main, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Product ${index + 1}", fontWeight = FontWeight.Medium, color = jc.text, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFDECEC)).clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.Delete, null, tint = Color(0xFFD9363E)) }
            }
            if (item == null) {
                Box(
                    Modifier.fillMaxWidth().height(56.dp).clip(Pill).background(jc.field).border(1.dp, jc.border, Pill).clickable(onClick = onPick).padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart,
                ) { Text("Select Product", color = jc.sub, fontSize = 18.sp) }
                Text("or enter a custom item", color = Gold.Text, fontSize = 13.sp, modifier = Modifier.clickable {
                    onChange(InvoiceItem(name = "Item", rate = d.rates.rateFor(Metal.GOLD, "22K", ""), purity = "22K", makingUnit = s.makingUnit, hallmarkUnit = s.hallmarkUnit))
                })
                return@Column
            }
            val c = item.calc()
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                JField(item.name, { onChange(item.copy(name = it)) }, "Product Name", Modifier.weight(1f))
                if (item.productId.isBlank()) Box(Modifier.width(120.dp)) {
                    DropField("Metal", item.metal.label, Metal.entries, { it.label }, { onChange(item.copy(metal = it, rate = d.rates.rateFor(it, item.purity, item.tunch))) })
                }
            }
            if (item.code.isNotBlank()) Text("${item.code} • ${item.metal.label} • ${item.stockType.label}", color = jc.sub, fontSize = 13.sp)
            if (s.showStockWeights && item.stockType == StockType.BULK && item.productId.isNotBlank()) {
                d.product(item.productId)?.let { Text("In stock: ${it.grossWt.wt()}", color = Gold.Green, fontSize = 13.sp) }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (s.colPurity) JField(item.purity, { onChange(item.copy(purity = it, rate = d.rates.rateFor(item.metal, it, item.tunch))) }, "Purity", Modifier.weight(1f))
                if (s.colPcs) DNum(item.pcs.toDouble(), { onChange(item.copy(pcs = it.toInt())) }, "PCS", Modifier.weight(1f), integer = true)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DNum(item.grossWt, { onChange(item.copy(grossWt = it)) }, "Gross Wt", Modifier.weight(1f), suffix = "g")
                if (s.colGrossLess) DNum(item.lessWt, { onChange(item.copy(lessWt = it)) }, "Less Wt", Modifier.weight(1f), suffix = "g")
            }
            if (wholesale && d.wholesale.packagingWt) DNum(item.packagingWt, { onChange(item.copy(packagingWt = it)) }, "Packaging Wt", suffix = "g")
            NetBar(c.net)
            DNum(item.rate * unitGrams, { onChange(item.copy(rate = it / unitGrams)) }, "Rate (${s.metalRateUnit.label})", icon = Icons.Default.CurrencyRupee, decimals = 2)
            if (s.makingCharge) DNum(item.making, { onChange(item.copy(making = it)) }, "Making Charges", decimals = 2, trailing = {
                InlineDrop(item.makingUnit.label, ChargeUnit.entries, { it.label }) { onChange(item.copy(makingUnit = it)) }
            })
            if (s.wastageCharge) DNum(item.wastagePct, { onChange(item.copy(wastagePct = it)) }, "Wastage", suffix = "%", decimals = 2)
            if (s.hallmarkCharge) DNum(item.hallmark, { onChange(item.copy(hallmark = it)) }, "Hallmark Charges", decimals = 2, trailing = {
                InlineDrop(item.hallmarkUnit.label, ChargeUnit.entries, { it.label }) { onChange(item.copy(hallmarkUnit = it)) }
            })
            if (s.stoneCharge) DNum(item.stone, { onChange(item.copy(stone = it)) }, "Diamond / Stone Charge (₹)", decimals = 2)
            if (wholesale && d.wholesale.labour) DNum(item.labour, { onChange(item.copy(labour = it)) }, "Labour (per gram)", decimals = 2)
            if (s.colHuid) JField(item.huid, { onChange(item.copy(huid = it.uppercase())) }, "HUID")
            if (s.colSize) JField(item.size, { onChange(item.copy(size = it)) }, "Size")
            if (s.colDescription) JField(item.description, { onChange(item.copy(description = it)) }, "Product Description")
            if (type.isGst) JField(item.hsn, { onChange(item.copy(hsn = it)) }, "HSN Code", keyboard = KeyboardType.Number)
            Row {
                Text(
                    "Metal ${c.metal.money()}" + (if (c.making > 0) " + MC ${c.making.money()}" else "") + (if (c.wastage > 0) " + W ${c.wastage.money()}" else "") +
                        (if (c.hallmark > 0) " + HM ${c.hallmark.money()}" else "") + (if (c.stone > 0) " + St ${c.stone.money()}" else "") +
                        (if (c.labour > 0) " + Lab ${c.labour.money()}" else ""),
                    color = jc.sub, fontSize = 12.sp, modifier = Modifier.weight(1f),
                )
                Text(c.total.money(), color = Gold.Text, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun NetBar(net: Double, label: String = "Net Weight") {
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(Color(0xFFFFF4C2).copy(alpha = if (jc.dark) 0.2f else 1f)).padding(14.dp)) {
        Text(label, color = jc.text, modifier = Modifier.weight(1f))
        Text(net.wt(), color = jc.text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun MetalCard(index: Int, m: MetalItem, d: AppData, unitGrams: Double, onChange: (MetalItem) -> Unit, onDelete: () -> Unit) {
    JCard {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Diamond, null, tint = Gold.Main)
                Spacer(Modifier.width(8.dp))
                Text("Metal ${index + 1}", fontWeight = FontWeight.Medium, color = jc.text, modifier = Modifier.weight(1f))
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFFFDECEC)).clickable(onClick = onDelete),
                    contentAlignment = Alignment.Center,
                ) { Icon(Icons.Default.Delete, null, tint = Color(0xFFD9363E)) }
            }
            SegTabs(listOf("Gold", "Silver"), if (m.metal == Metal.GOLD) 0 else 1, height = 42.dp) { i ->
                val metal = if (i == 0) Metal.GOLD else Metal.SILVER
                val stock = d.metalStock.firstOrNull { it.metal == metal && it.name.startsWith("Old") } ?: d.metalStock.firstOrNull { it.metal == metal }
                onChange(m.copy(metal = metal, stockName = stock?.name ?: m.stockName, rate = d.rates.fineRate(metal), tunch = if (metal == Metal.GOLD) 91.6 else 92.5))
            }
            DropField("Metal Type", m.stockName, d.metalStock.filter { it.metal == m.metal }, { it.name }, { onChange(m.copy(stockName = it.name)) })
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DNum(m.givenWt, { onChange(m.copy(givenWt = it)) }, "Given Wt", Modifier.weight(1f), suffix = "g")
                DNum(m.tunch, { onChange(m.copy(tunch = it)) }, "Tunch", Modifier.weight(1f), suffix = "%", decimals = 2)
            }
            NetBar(m.fineWt, "Net (Fine) Wt")
            DNum(m.rate * unitGrams, { onChange(m.copy(rate = it / unitGrams)) }, "Rate (fine)", icon = Icons.Default.CurrencyRupee, decimals = 2)
            JField(m.description, { onChange(m.copy(description = it)) }, "Description")
            ValueRow("Amount", m.amount.money())
        }
    }
}

@Composable
private fun RoundOffRow(value: Double, base: Double, onChange: (Double) -> Unit) {
    var text by remember { mutableStateOf("%.2f".format(value)) }
    androidx.compose.runtime.LaunchedEffect(value) { if ((text.toDoubleOrNull() ?: 0.0) != value) text = "%.2f".format(value) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Close, "Clear", tint = Gold.Red, modifier = Modifier.clickable { text = "0.00"; onChange(0.0) })
        Spacer(Modifier.width(6.dp))
        Text("Round Off", color = jc.text, fontSize = 16.sp, modifier = Modifier.weight(1f))
        RoundBtn("+", true) { val v = (ceil(base) - base).round2(); text = "%.2f".format(v); onChange(v) }
        Spacer(Modifier.width(6.dp))
        RoundBtn("-", false) { val v = (floor(base) - base).round2(); text = "%.2f".format(v); onChange(v) }
        Spacer(Modifier.width(8.dp))
        TextField(
            text, { s -> text = com.jeweldesk.app.ui.cleanNum(s, allowNegative = true); onChange(text.toDoubleOrNull() ?: 0.0) },
            modifier = Modifier.width(90.dp).heightIn(min = 48.dp), singleLine = true,
            textStyle = TextStyle(color = Gold.Text, textAlign = TextAlign.End, fontSize = 16.sp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Text),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent, unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Gold.Main, unfocusedIndicatorColor = Gold.Main,
            ),
        )
    }
}

@Composable
private fun RoundBtn(label: String, gold: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.size(34.dp).clip(RoundedCornerShape(8.dp)).background(if (gold) jc.tile else jc.surface)
            .border(1.dp, if (gold) Gold.Main else jc.border, RoundedCornerShape(8.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = if (gold) Gold.Text else jc.sub, fontWeight = FontWeight.Bold) }
}
