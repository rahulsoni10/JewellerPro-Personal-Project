package com.jeweldesk.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonPin
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.Book
import com.jeweldesk.app.data.CalcMethod
import com.jeweldesk.app.data.Girvi
import com.jeweldesk.app.data.GirviItem
import com.jeweldesk.app.data.GirviPayment
import com.jeweldesk.app.data.GirviTransfer
import com.jeweldesk.app.data.INDIAN_STATES
import com.jeweldesk.app.data.InterestType
import com.jeweldesk.app.data.Metal
import com.jeweldesk.app.data.Ops
import com.jeweldesk.app.data.PAYMENT_MODES
import com.jeweldesk.app.data.PartyType
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.data.computeInterest
import com.jeweldesk.app.data.dateStr
import com.jeweldesk.app.data.interestAsOf
import com.jeweldesk.app.data.money
import com.jeweldesk.app.data.newId
import com.jeweldesk.app.data.now
import com.jeweldesk.app.data.round2
import com.jeweldesk.app.data.wt
import com.jeweldesk.app.pdf.Pdf
import com.jeweldesk.app.ui.ConfirmDialog
import com.jeweldesk.app.ui.DNum
import com.jeweldesk.app.ui.DateField
import com.jeweldesk.app.ui.DropField
import com.jeweldesk.app.ui.EmptyState
import com.jeweldesk.app.ui.GoldButton
import com.jeweldesk.app.ui.GoldFab
import com.jeweldesk.app.ui.HeaderCard
import com.jeweldesk.app.ui.InlineDrop
import com.jeweldesk.app.ui.JCard
import com.jeweldesk.app.ui.JDialog
import com.jeweldesk.app.ui.JField
import com.jeweldesk.app.ui.LocalNav
import com.jeweldesk.app.ui.OutlineGoldButton
import com.jeweldesk.app.ui.Page
import com.jeweldesk.app.ui.ScreenColumn
import com.jeweldesk.app.ui.SearchBar
import com.jeweldesk.app.ui.SectionCard
import com.jeweldesk.app.ui.SegTabs
import com.jeweldesk.app.ui.SquareIconButton
import com.jeweldesk.app.ui.ValueRow
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.toast

@Composable
fun GirviListScreen(mode: String) {
    val nav = LocalNav.current
    val d = appData()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var q by rememberSaveable { mutableStateOf("") }
    var transferFor by remember { mutableStateOf<Girvi?>(null) }
    val transferMode = mode == "transfer"
    val list = d.girvis.filter {
        if (transferMode) !it.released && it.transfer == null
        else when (tab) { 0 -> !it.released && it.transfer == null; 1 -> it.transfer != null && !it.released; else -> it.released }
    }.filter { g ->
        q.isBlank() || g.customerName.contains(q, true) || g.mobile.contains(q) || g.number.toString() == q.trim() ||
            g.items.any { it.name.contains(q, true) }
    }.sortedByDescending { it.date }
    Page(
        header = {
            HeaderCard(
                when (mode) { "search" -> "Search Girvi"; "transfer" -> "Transfer Girvi"; else -> "Girvi" },
                when (mode) { "search" -> "Find pledge record"; "transfer" -> "Select a pledge to transfer"; else -> "Jewellery pledge records" },
                back = true,
            )
        },
        fab = if (transferMode) null else ({ GoldFab("Add Girvi", Icons.Default.Add) { nav.navigate("girviedit/new") } }),
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            if (!transferMode) item { SegTabs(listOf("Active", "Transferred", "Released"), tab) { tab = it } }
            item { SearchBar(q, { q = it }, "Search by name, mobile, girvi no. or item") }
            if (!transferMode && tab == 0) item {
                val active = d.girvis.filter { !it.released }
                JCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ValueRow("Active girvi", active.size.toString(), jc.text)
                        ValueRow("Total principal", active.sumOf { it.principal }.money())
                        ValueRow("Interest accrued", active.sumOf { it.interestAsOf().interest }.money(), Gold.Green)
                    }
                }
            }
            if (list.isEmpty()) item { EmptyState(Icons.Default.AccountBalanceWallet, "No girvi records found") }
            items(list, key = { it.id }) { g ->
                val r = g.interestAsOf()
                JCard(onClick = { if (transferMode) transferFor = g else nav.navigate("girviview/${g.id}") }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        PartyAvatar(g.customerName)
                        Column(Modifier.weight(1f).padding(horizontal = 12.dp)) {
                            Text("#${g.number}  ${g.customerName}", fontWeight = FontWeight.SemiBold, color = jc.text)
                            Text("${g.date.dateStr()} • ${g.items.joinToString { it.name }.ifBlank { "No items" }}", color = jc.sub, fontSize = 12.sp, maxLines = 1)
                            Text("${g.rate}% p.m. • ${r.days} days", color = jc.sub, fontSize = 12.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(g.principal.money(), fontWeight = FontWeight.Bold, color = jc.text)
                            Text("+ ${r.interest.money()}", color = Gold.Green, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
    transferFor?.let { g -> TransferDialog(g, { transferFor = null }) { transferFor = null } }
}

@Composable
fun TransferDialog(g: Girvi, onDismiss: () -> Unit, onDone: () -> Unit) {
    val ctx = LocalContext.current
    var to by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf(g.principal) }
    var rate by remember { mutableStateOf(g.rate) }
    var date by remember { mutableStateOf(now()) }
    var note by remember { mutableStateOf("") }
    JDialog("Transfer Girvi #${g.number}", Icons.Default.SwapHoriz, onDismiss) {
        Text("Re-pledge ${g.customerName}'s items with another lender. The amount you receive is added to your cash.", color = jc.sub, fontSize = 13.sp)
        JField(to, { to = it }, "Transfer to (lender name)", icon = Icons.Default.Person)
        DNum(amount, { amount = it }, "Amount received", icon = Icons.Default.CurrencyRupee, decimals = 2)
        DNum(rate, { rate = it }, "Interest (% / month)", icon = Icons.Default.Percent, decimals = 2)
        DateField("Transfer date", date, { date = it })
        JField(note, { note = it }, "Note", icon = Icons.AutoMirrored.Filled.Notes)
        GoldButton("Transfer") {
            if (to.isBlank() || amount <= 0) { ctx.toast("Enter lender and amount"); return@GoldButton }
            Ops.saveGirvi(g.copy(transfer = GirviTransfer(to.trim(), date, amount, rate, note.trim())))
            onDone()
        }
    }
}

@Composable
fun GirviEditScreen(id: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    val gs = d.girviSettings
    val existing = remember { d.girvis.firstOrNull { it.id == id } }
    var partyId by remember { mutableStateOf(existing?.partyId ?: "") }
    var name by remember { mutableStateOf(existing?.customerName ?: "") }
    var mobile by remember { mutableStateOf(existing?.mobile ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }
    var city by remember { mutableStateOf(existing?.city ?: "") }
    var state by remember { mutableStateOf(existing?.state ?: d.profile?.state ?: "") }
    var pincode by remember { mutableStateOf(existing?.pincode ?: "") }
    val items = remember { mutableStateListOf<GirviItem>().apply { addAll(existing?.items ?: listOf(GirviItem())) } }
    var desc by remember { mutableStateOf(existing?.description ?: "") }
    var approx by remember { mutableStateOf(existing?.approxValue ?: 0.0) }
    var principal by remember { mutableStateOf(existing?.principal ?: 0.0) }
    var rate by remember { mutableStateOf(existing?.rate ?: gs.defaultRate) }
    var itype by remember { mutableStateOf(existing?.interestType ?: gs.defaultType) }
    var method by remember { mutableStateOf(existing?.method ?: gs.defaultMethod) }
    var mode by remember { mutableStateOf(existing?.mode ?: "Cash") }
    var book by remember { mutableStateOf(existing?.book ?: Book.ESTIMATE) }
    var date by remember { mutableStateOf(existing?.date ?: now()) }
    var number by remember { mutableStateOf((existing?.number ?: Ops.nextNumber(d, "girvi")).toDouble()) }
    Page({ HeaderCard(if (existing == null) "Add Girvi" else "Edit Girvi", "Create pledge record", back = true) }) {
        ScreenColumn {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                DNum(number, { number = it }, "Girvi No.", Modifier.weight(1f), integer = true)
                DateField("Date", date, { date = it }, Modifier.weight(1.3f))
            }
            SectionCard("Customer Details", Icons.Default.PersonPin) {
                PartyPicker(d, PartyType.CUSTOMER, partyId) { p ->
                    partyId = p?.id ?: ""
                    if (p != null) { name = p.name; mobile = p.mobile; address = p.address; city = p.city; state = p.state.ifBlank { state }; pincode = p.pincode }
                }
                if (partyId.isBlank()) JField(name, { name = it }, "Customer Name *", icon = Icons.Default.Person)
                JField(mobile, { s -> mobile = s.filter { it.isDigit() }.take(10) }, "Mobile", icon = Icons.Default.Phone, keyboard = KeyboardType.Phone)
                JField(address, { address = it }, "Address", icon = Icons.Default.Home)
                if (gs.city || gs.pincode) Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (gs.city) JField(city, { city = it }, "City", Modifier.weight(1f), Icons.Default.LocationCity)
                    if (gs.pincode) JField(pincode, { s -> pincode = s.filter { it.isDigit() }.take(6) }, "Pincode", Modifier.weight(1f), Icons.Default.PinDrop, KeyboardType.Number)
                }
                if (gs.state) DropField("State", state, INDIAN_STATES, { it }, { state = it }, icon = Icons.Default.Map)
            }
            SectionCard("Product Details", Icons.Outlined.Diamond) {
                items.forEachIndexed { i, it ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Item ${i + 1}", fontWeight = FontWeight.Medium, color = jc.text, modifier = Modifier.weight(1f))
                        if (items.size > 1) Icon(Icons.Default.Delete, null, tint = Gold.Red, modifier = Modifier.clickable { items.removeAt(i) })
                    }
                    SegTabs(listOf("Gold", "Silver"), it.metal.ordinal, height = 40.dp) { m -> items[i] = it.copy(metal = Metal.entries[m]) }
                    JField(it.name, { v -> items[i] = it.copy(name = v) }, "Item name (e.g. Necklace)")
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (gs.pcs) DNum(it.pcs.toDouble(), { v -> items[i] = it.copy(pcs = v.toInt()) }, "PCS", Modifier.weight(1f), integer = true)
                        if (gs.grossWt) DNum(it.grossWt, { v -> items[i] = it.copy(grossWt = v) }, "Gross Wt", Modifier.weight(1f), suffix = "g")
                        if (gs.lessWt) DNum(it.lessWt, { v -> items[i] = it.copy(lessWt = v) }, "Less Wt", Modifier.weight(1f), suffix = "g")
                    }
                    if (gs.grossWt) NetBar(it.netWt)
                    if (gs.productDescription) JField(it.description, { v -> items[i] = it.copy(description = v) }, "Product Description")
                }
                OutlineGoldButton("Add Item", icon = Icons.Default.Add) { items.add(GirviItem()) }
            }
            SectionCard("Girvi Details", Icons.Default.Payments) {
                if (gs.girviDescription) JField(desc, { desc = it }, "Girvi Description", icon = Icons.AutoMirrored.Filled.Notes)
                if (gs.approxValue) DNum(approx, { approx = it }, "Approx Value", icon = Icons.Default.CurrencyRupee, decimals = 2)
                DNum(principal, { principal = it }, "Loan Amount *", icon = Icons.Default.CurrencyRupee, decimals = 2, trailing = {
                    InlineDrop(mode, PAYMENT_MODES, { it }) { mode = it }
                })
                DNum(rate, { rate = it }, "Interest (% / month)", icon = Icons.Default.Percent, decimals = 2)
                DropField("Interest Type", itype.label, InterestType.entries, { it.label }, { itype = it })
                DropField("Calculation Method", method.label, CalcMethod.entries, { it.label }, { method = it }, icon = Icons.Default.Calculate)
                SegTabs(listOf("GST Book", "Estimate Book"), book.ordinal, height = 42.dp) { book = Book.entries[it] }
            }
            GoldButton(if (existing == null) "Save Girvi" else "Update Girvi") {
                val n = name.trim()
                if (n.isBlank()) { ctx.toast("Enter customer name"); return@GoldButton }
                if (principal <= 0) { ctx.toast("Enter loan amount"); return@GoldButton }
                val no = number.toInt()
                if (no <= 0 || Store.value.girvis.any { it.id != existing?.id && it.number == no }) { ctx.toast("Girvi number already used"); return@GoldButton }
                val g = (existing ?: Girvi(newId(), no, date, customerName = n, principal = principal, rate = rate)).copy(
                    number = no, date = date, partyId = partyId, customerName = n, mobile = mobile, address = address, city = city, state = state,
                    pincode = pincode, items = items.filter { it.name.isNotBlank() || it.grossWt > 0 }, description = desc, approxValue = approx,
                    principal = principal, rate = rate, interestType = itype, method = method, mode = mode, book = book,
                )
                Ops.saveGirvi(g)
                nav.popBackStack()
                if (existing == null) nav.navigate("girviview/${g.id}")
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
fun GirviViewScreen(id: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    val g = d.girvis.firstOrNull { it.id == id }
    var asOf by remember { mutableStateOf(now()) }
    var pay by remember { mutableStateOf(false) }
    var release by remember { mutableStateOf(false) }
    var transfer by remember { mutableStateOf(false) }
    var del by remember { mutableStateOf(false) }
    if (g == null) { Page({ HeaderCard("Girvi", back = true) }) { EmptyState(Icons.Default.AccountBalanceWallet, "Girvi not found") }; return }
    val r = g.interestAsOf(asOf)
    val outstanding = r.outstanding(g.principal)
    Page({
        HeaderCard("Girvi #${g.number}", g.customerName, back = true, actions = {
            SquareIconButton(Icons.Default.Share) { Pdf.share(ctx, Pdf.girviSlip(ctx, d, g)) }
        })
    }) {
        ScreenColumn {
            SectionCard("Customer", Icons.Default.Person) {
                Text(g.customerName, fontWeight = FontWeight.SemiBold, color = jc.text, fontSize = 17.sp)
                if (g.mobile.isNotBlank()) Text(g.mobile, color = jc.sub)
                val addr = listOf(g.address, g.city, g.state, g.pincode).filter { it.isNotBlank() }.joinToString(", ")
                if (addr.isNotBlank()) Text(addr, color = jc.sub)
                ValueRow("Status", if (g.released) "Released on ${g.releasedAt.dateStr()}" else if (g.transfer != null) "Transferred" else "Active", if (g.released) Gold.Green else Gold.Text)
            }
            SectionCard("Items", Icons.Outlined.Diamond) {
                g.items.forEach { Text("${it.name} (${it.metal.label}) • ${it.pcs} pcs • GW ${it.grossWt.wt()} • NW ${it.netWt.wt()}" + if (it.description.isNotBlank()) "\n${it.description}" else "", color = jc.text) }
                if (g.description.isNotBlank()) Text(g.description, color = jc.sub)
                if (g.approxValue > 0) ValueRow("Approx Value", g.approxValue.money())
            }
            SectionCard("Interest", Icons.Default.Calculate) {
                ValueRow("Loan date", g.date.dateStr(), jc.text)
                ValueRow("Principal", g.principal.money())
                ValueRow("Rate", "${g.rate}% / month • ${g.interestType.label}", jc.text)
                ValueRow("Method", g.method.label, jc.text)
                if (!g.released) DateField("Calculate as of", asOf, { asOf = it })
                ValueRow("Duration", "${r.days} days (${"%.2f".format(r.months)} months)", jc.text)
                ValueRow("Interest", r.interest.money(), Gold.Green)
                ValueRow("Paid", r.paid.money())
                ValueRow(if (g.released) "Settled" else "Outstanding", outstanding.money(), if (outstanding > 0.5) Gold.Red else Gold.Green, bold = true)
            }
            g.transfer?.let { t ->
                val (days, int) = computeInterest(t.amount, t.rate, t.date, if (g.released) g.releasedAt else asOf, InterestType.SIMPLE, g.method)
                SectionCard("Transferred", Icons.Default.SwapHoriz) {
                    ValueRow("To", t.toName, jc.text)
                    ValueRow("Date", t.date.dateStr(), jc.text)
                    ValueRow("Amount", t.amount.money())
                    ValueRow("Rate", "${t.rate}% / month", jc.text)
                    ValueRow("Interest payable ($days days)", int.money(), Gold.Red)
                    if (t.note.isNotBlank()) Text(t.note, color = jc.sub)
                }
            }
            if (g.payments.isNotEmpty()) SectionCard("Payments", Icons.Default.Payments) {
                g.payments.forEach { ValueRow("${it.date.dateStr()} • ${it.mode}" + if (it.note.isNotBlank()) " • ${it.note}" else "", it.amount.money()) }
            }
            if (!g.released) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    GoldButton("Add Payment", Modifier.weight(1f), icon = null) { pay = true }
                    GoldButton("Release", Modifier.weight(1f), leading = Icons.Default.CheckCircle, icon = null) { release = true }
                }
                if (g.transfer == null && d.girviSettings.activeTransfer) OutlineGoldButton("Transfer Girvi", icon = Icons.Default.SwapHoriz) { transfer = true }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlineGoldButton("Print Slip", Modifier.weight(1f), Icons.Default.Print) { Pdf.print(ctx, Pdf.girviSlip(ctx, d, g), "Girvi ${g.number}") }
                OutlineGoldButton("Edit", Modifier.weight(1f), Icons.Default.Edit) { nav.navigate("girviedit/${g.id}") }
            }
            OutlineGoldButton("Delete", icon = Icons.Default.Delete, color = Gold.Red) { del = true }
            Spacer(Modifier.height(20.dp))
        }
    }
    if (pay) {
        var amt by remember { mutableStateOf(0.0) }
        var mode by remember { mutableStateOf("Cash") }
        var date by remember { mutableStateOf(now()) }
        var note by remember { mutableStateOf("") }
        JDialog("Add Payment", Icons.Default.Payments, { pay = false }) {
            DNum(amt, { amt = it }, "Amount", icon = Icons.Default.CurrencyRupee, decimals = 2, trailing = { InlineDrop(mode, PAYMENT_MODES, { it }) { mode = it } })
            DateField("Date", date, { date = it })
            JField(note, { note = it }, "Note")
            GoldButton("Save") {
                if (amt <= 0) { ctx.toast("Enter amount"); return@GoldButton }
                Ops.saveGirvi(g.copy(payments = g.payments + GirviPayment(date, amt, mode, note.trim()))); pay = false
            }
        }
    }
    if (release) {
        var date by remember { mutableStateOf(now()) }
        var mode by remember { mutableStateOf("Cash") }
        val rr = g.interestAsOf(date)
        val due = rr.outstanding(g.principal).round2()
        var amt by remember(due) { mutableStateOf(due.coerceAtLeast(0.0)) }
        JDialog("Release Girvi", Icons.Default.CheckCircle, { release = false }) {
            DateField("Release date", date, { date = it })
            ValueRow("Interest", rr.interest.money(), Gold.Green)
            ValueRow("Outstanding", due.money(), Gold.Red, bold = true)
            DNum(amt, { amt = it }, "Amount received", icon = Icons.Default.CurrencyRupee, decimals = 2, trailing = { InlineDrop(mode, PAYMENT_MODES, { it }) { mode = it } })
            if (amt < due - 0.5) Text("Discount of ${(due - amt).money()} will be given.", color = jc.sub, fontSize = 13.sp)
            GoldButton("Release & Return Items") {
                val pays = if (amt > 0) g.payments + GirviPayment(date, amt, mode, "Release") else g.payments
                Ops.saveGirvi(g.copy(payments = pays, released = true, releasedAt = date)); release = false
            }
        }
    }
    if (transfer) TransferDialog(g, { transfer = false }) { transfer = false }
    if (del) ConfirmDialog("Delete Girvi #${g.number}?", "All its payments will also be removed.", onDismiss = { del = false }) {
        Ops.deleteGirvi(g.id); nav.popBackStack()
    }
}
