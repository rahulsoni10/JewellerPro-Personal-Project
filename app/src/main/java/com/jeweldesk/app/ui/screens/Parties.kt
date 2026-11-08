package com.jeweldesk.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.AccountBalanceWallet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.AppData
import com.jeweldesk.app.data.Book
import com.jeweldesk.app.data.INDIAN_STATES
import com.jeweldesk.app.data.KhataEntry
import com.jeweldesk.app.data.KhataType
import com.jeweldesk.app.data.Kind
import com.jeweldesk.app.data.Ops
import com.jeweldesk.app.data.PAYMENT_MODES
import com.jeweldesk.app.data.Party
import com.jeweldesk.app.data.PartyType
import com.jeweldesk.app.data.dateStr
import com.jeweldesk.app.data.money
import com.jeweldesk.app.data.newId
import com.jeweldesk.app.data.now
import com.jeweldesk.app.data.partyAdvance
import com.jeweldesk.app.data.partyDue
import com.jeweldesk.app.data.totals
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
import com.jeweldesk.app.ui.SegTabs
import com.jeweldesk.app.ui.SquareIconButton
import com.jeweldesk.app.ui.ValueRow
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.toast

@Composable
fun PartyAvatar(name: String) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(jc.tile), contentAlignment = Alignment.Center) {
        Text(name.take(1).uppercase(), color = Gold.Text, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

@Composable
fun PartiesTab() {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    var tab by rememberSaveable { mutableIntStateOf(0) }
    var q by rememberSaveable { mutableStateOf("") }
    val type = if (tab == 0) PartyType.CUSTOMER else PartyType.SUPPLIER
    val list = d.parties.filter { it.type == type && (q.isBlank() || it.name.contains(q, true) || it.mobile.contains(q)) }.sortedBy { it.name.lowercase() }
    Page(
        header = {
            HeaderCard("Parties", "Customers & suppliers", Icons.Default.Groups, actions = {
                SquareIconButton(Icons.Default.Refresh) { ctx.toast("Up to date") }
            })
        },
        navPadding = false,
        fab = { GoldFab("Add ${type.label}", Icons.Default.Add) { nav.navigate("partyedit/${type.name}/new") } },
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { SegTabs(listOf("Customers", "Suppliers"), tab) { tab = it } }
            item { SearchBar(q, { q = it }, "Search by name") }
            if (list.isEmpty()) item { EmptyState(Icons.Default.Groups, "No parties found") }
            items(list, key = { it.id }) { p -> PartyRow(d, p) { nav.navigate("party/${p.id}") } }
        }
    }
}

@Composable
fun PartyRow(d: AppData, p: Party, onClick: () -> Unit) {
    val due = d.partyDue(p.id)
    val adv = d.partyAdvance(p.id)
    JCard(onClick = onClick) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            PartyAvatar(p.name)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(p.name, fontWeight = FontWeight.SemiBold, color = jc.text)
                if (p.mobile.isNotBlank()) Text(p.mobile, color = jc.sub, fontSize = 13.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                when {
                    due > 0.5 -> Text((if (p.type == PartyType.CUSTOMER) "To receive " else "To pay ") + due.money(), color = Gold.Red, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    due < -0.5 -> Text((if (p.type == PartyType.CUSTOMER) "To pay " else "To receive ") + (-due).money(), color = Gold.Green, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    else -> Text("Settled", color = Gold.Green, fontSize = 13.sp)
                }
                if (adv > 0.5) Text("Advance ${adv.money()}", color = Gold.Text, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PartyEditScreen(type: PartyType, id: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    val existing = remember { d.party(id) }
    var name by rememberSaveable { mutableStateOf(existing?.name ?: "") }
    var mobile by rememberSaveable { mutableStateOf(existing?.mobile ?: "") }
    var email by rememberSaveable { mutableStateOf(existing?.email ?: "") }
    var gstin by rememberSaveable { mutableStateOf(existing?.gstin ?: "") }
    var pan by rememberSaveable { mutableStateOf(existing?.pan ?: "") }
    var address by rememberSaveable { mutableStateOf(existing?.address ?: "") }
    var city by rememberSaveable { mutableStateOf(existing?.city ?: "") }
    var state by rememberSaveable { mutableStateOf(existing?.state ?: d.profile?.state ?: "") }
    var pincode by rememberSaveable { mutableStateOf(existing?.pincode ?: "") }
    var opening by remember { mutableStateOf(existing?.openingDue ?: 0.0) }
    var openingSide by rememberSaveable { mutableIntStateOf(if ((existing?.openingDue ?: 0.0) < 0) 1 else 0) }
    val t = existing?.type ?: type
    Page({ HeaderCard(if (existing == null) "Add ${t.label}" else "Edit ${t.label}", if (t == PartyType.CUSTOMER) "Customer details" else "Supplier details", back = true) }) {
        ScreenColumn {
            JField(name, { name = it }, "Name *", icon = Icons.Default.Person)
            JField(mobile, { s -> mobile = s.filter { it.isDigit() }.take(10) }, "Mobile Number", icon = Icons.Default.Phone, keyboard = KeyboardType.Phone)
            JField(email, { email = it.trim() }, "Email", icon = Icons.Default.Email, keyboard = KeyboardType.Email)
            JField(gstin, { gstin = it.uppercase().take(15) }, "GSTIN", icon = Icons.Default.Receipt)
            JField(pan, { pan = it.uppercase().take(10) }, "PAN", icon = Icons.Default.Badge)
            JField(address, { address = it }, "Address", icon = Icons.Default.Home)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                JField(city, { city = it }, "City", Modifier.weight(1f), Icons.Default.LocationCity)
                JField(pincode, { s -> pincode = s.filter { it.isDigit() }.take(6) }, "Pincode", Modifier.weight(1f), Icons.Default.PinDrop, KeyboardType.Number)
            }
            DropField("State", state, INDIAN_STATES, { it }, { state = it }, icon = Icons.Default.Map)
            Text("Opening Balance", fontWeight = FontWeight.Medium, color = jc.text)
            SegTabs(listOf(if (t == PartyType.CUSTOMER) "To Receive" else "To Pay", if (t == PartyType.CUSTOMER) "To Pay" else "To Receive"), openingSide, height = 42.dp) { openingSide = it }
            DNum(kotlin.math.abs(opening), { opening = it }, "Amount", icon = Icons.Default.CurrencyRupee, decimals = 2)
            GoldButton(if (existing == null) "Save ${t.label}" else "Update ${t.label}") {
                if (name.isBlank()) { ctx.toast("Enter name"); return@GoldButton }
                if (mobile.isNotEmpty() && mobile.length != 10) { ctx.toast("Mobile must be 10 digits"); return@GoldButton }
                if (gstin.isNotBlank() && !isValidGstin(gstin)) { ctx.toast("Invalid GSTIN"); return@GoldButton }
                if (pan.isNotBlank() && !Regex("^[A-Z]{5}[0-9]{4}[A-Z]$").matches(pan)) { ctx.toast("Invalid PAN"); return@GoldButton }
                if (mobile.isNotBlank() && d.parties.any { it.id != existing?.id && it.type == t && it.mobile == mobile }) { ctx.toast("A ${t.label.lowercase()} with this mobile already exists"); return@GoldButton }
                val amt = kotlin.math.abs(opening) * if (openingSide == 0) 1 else -1
                val p = (existing ?: Party(newId(), t, "", createdAt = now())).copy(
                    name = name.trim(), mobile = mobile, email = email, gstin = gstin, pan = pan, address = address.trim(),
                    city = city.trim(), state = state, pincode = pincode, openingDue = amt,
                )
                Ops.saveParty(p)
                nav.popBackStack()
            }
        }
    }
}

data class LedgerLine(val date: Long, val title: String, val amount: Double, val invoiceId: String? = null, val khataId: String? = null)

fun partyLedger(d: AppData, p: Party): List<LedgerLine> {
    val lines = mutableListOf<LedgerLine>()
    if (p.openingDue != 0.0) lines += LedgerLine(p.createdAt, "Opening Balance", p.openingDue)
    d.invoices.filter { it.partyId == p.id }.forEach { inv ->
        val t = inv.totals()
        val ret = inv.type.kind == Kind.SALE_RETURN || inv.type.kind == Kind.PURCHASE_RETURN
        val sign = if (ret) -1 else 1
        lines += LedgerLine(inv.date, "${inv.type.short} #${inv.displayNo}", sign * t.total, invoiceId = inv.id)
        if (inv.advanceApplied > 0) lines += LedgerLine(inv.date, "Advance adjusted #${inv.displayNo}", -inv.advanceApplied, invoiceId = inv.id)
        if (t.received > 0) lines += LedgerLine(inv.date, (if (ret) "Refund" else if (inv.type.isSaleSide) "Received" else "Paid") + " #${inv.displayNo}", -sign * t.received, invoiceId = inv.id)
    }
    d.khata.filter { it.partyId == p.id && it.type.dueSign != 0 }.forEach { k ->
        lines += LedgerLine(k.date, (if (p.type == PartyType.CUSTOMER) k.type.customerLabel else k.type.supplierLabel) + if (k.note.isNotBlank()) " • ${k.note}" else "", k.type.dueSign * k.amount, khataId = k.id)
    }
    return lines.sortedBy { it.date }
}

@Composable
fun PartyViewScreen(id: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    val d = appData()
    val p = d.party(id)
    var entry by remember { mutableStateOf<Int?>(null) }
    var del by remember { mutableStateOf(false) }
    var delKhata by remember { mutableStateOf<String?>(null) }
    if (p == null) { Page({ HeaderCard("Party", back = true) }) { EmptyState(Icons.Default.Person, "Party not found") }; return }
    val due = d.partyDue(p.id)
    val adv = d.partyAdvance(p.id)
    val ledger = partyLedger(d, p)
    Page({
        HeaderCard(p.name, p.type.label + if (p.mobile.isNotBlank()) " • ${p.mobile}" else "", back = true, actions = {
            SquareIconButton(Icons.Default.Edit) { nav.navigate("partyedit/${p.type.name}/${p.id}") }
        })
    }) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 40.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                JCard {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ValueRow(if (p.type == PartyType.CUSTOMER) "Balance due (udhar)" else "Balance payable", due.money(), if (due > 0.5) Gold.Red else Gold.Green, bold = true)
                        ValueRow("Advance balance", adv.money(), Gold.Text)
                        if (p.gstin.isNotBlank()) Text("GSTIN: ${p.gstin}", color = jc.sub, fontSize = 13.sp)
                        val addr = listOf(p.address, p.city, p.state, p.pincode).filter { it.isNotBlank() }.joinToString(", ")
                        if (addr.isNotBlank()) Text(addr, color = jc.sub, fontSize = 13.sp)
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (p.mobile.isNotBlank()) {
                        OutlineGoldButton("Call", Modifier.weight(1f), Icons.Default.Call) {
                            ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${p.mobile}")))
                        }
                        OutlineGoldButton("Remind", Modifier.weight(1f), Icons.Default.Share) {
                            val shop = d.profile?.businessName ?: ""
                            val msg = "Dear ${p.name}, your outstanding balance with $shop is ${due.money()}. Kindly clear it at the earliest. Thank you."
                            val uri = Uri.parse("https://wa.me/91${p.mobile}?text=" + Uri.encode(msg))
                            runCatching { ctx.startActivity(Intent(Intent.ACTION_VIEW, uri)) }.onFailure { ctx.toast("WhatsApp not available") }
                        }
                    }
                    OutlineGoldButton("Statement", Modifier.weight(1f), Icons.Default.Description) { Pdf.share(ctx, Pdf.statement(ctx, d, p)) }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GoldButton(if (p.type == PartyType.CUSTOMER) "Udhar / Payment" else "Payable / Payment", Modifier.weight(1f), icon = null, height = 50.dp) { entry = 0 }
                    GoldButton("Advance", Modifier.weight(1f), icon = null, height = 50.dp) { entry = 1 }
                }
            }
            item {
                GoldButton(if (p.type == PartyType.CUSTOMER) "New Sale Invoice" else "New Purchase Invoice", height = 50.dp) {
                    nav.navigate("inv/new/${if (p.type == PartyType.CUSTOMER) "EST_SALE" else "EST_PURCHASE"}?party=${p.id}")
                }
            }
            item { Text("Ledger", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = jc.text, modifier = Modifier.padding(top = 8.dp)) }
            if (ledger.isEmpty()) item { Text("No transactions yet", color = jc.sub) }
            var running = 0.0
            val withBal = ledger.map { running += it.amount; it to running }
            items(withBal.reversed()) { (l, bal) ->
                JCard(onClick = { l.invoiceId?.let { nav.navigate("inv/view/$it") } }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(l.title, color = jc.text, fontWeight = FontWeight.Medium)
                            Text(l.date.dateStr() + " • Bal ${bal.money()}", color = jc.sub, fontSize = 12.sp)
                        }
                        Text((if (l.amount >= 0) "+ " else "- ") + kotlin.math.abs(l.amount).money(), color = if (l.amount >= 0) Gold.Red else Gold.Green, fontWeight = FontWeight.SemiBold)
                        if (l.khataId != null) Icon(Icons.Default.Delete, null, tint = jc.sub, modifier = Modifier.padding(start = 8.dp).size(20.dp).clickable { delKhata = l.khataId })
                    }
                }
            }
            val advEntries = d.khata.filter { it.partyId == p.id && it.type.advSign != 0 }.sortedByDescending { it.date }
            if (advEntries.isNotEmpty()) {
                item { Text("Advances", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = jc.text, modifier = Modifier.padding(top = 8.dp)) }
                items(advEntries, key = { it.id }) { k ->
                    JCard {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(if (p.type == PartyType.CUSTOMER) k.type.customerLabel else k.type.supplierLabel, color = jc.text)
                                Text("${k.date.dateStr()} • ${k.mode}" + if (k.note.isNotBlank()) " • ${k.note}" else "", color = jc.sub, fontSize = 12.sp)
                            }
                            Text(k.amount.money(), color = if (k.type.advSign > 0) Gold.Green else Gold.Red, fontWeight = FontWeight.SemiBold)
                            Icon(Icons.Default.Delete, null, tint = jc.sub, modifier = Modifier.padding(start = 8.dp).size(20.dp).clickable { delKhata = k.id })
                        }
                    }
                }
            }
            item {
                Spacer(Modifier.height(12.dp))
                OutlineGoldButton("Delete ${p.type.label}", icon = Icons.Default.Delete, color = Gold.Red) { del = true }
            }
        }
    }
    entry?.let { kind -> KhataEntryDialog(p.type, p.id, kind, { entry = null }) { entry = null } }
    if (del) ConfirmDialog("Delete ${p.name}?", "Khata entries will be removed. Invoices are kept.", onDismiss = { del = false }) {
        Ops.deleteParty(p.id); nav.popBackStack()
    }
    delKhata?.let { kid -> ConfirmDialog("Delete entry?", "This khata entry will be removed.", onDismiss = { delKhata = null }) { Ops.deleteKhata(kid) } }
}

/** kind 0 = udhar/payment, 1 = advance. */
@Composable
fun KhataEntryDialog(partyType: PartyType, partyId: String, kind: Int, onDismiss: () -> Unit, onSaved: () -> Unit) {
    val ctx = LocalContext.current
    val d = appData()
    var pt by remember { mutableStateOf(partyType) }
    var pid by remember { mutableStateOf(partyId) }
    val types = if (kind == 0) listOf(KhataType.DUE_ADD, KhataType.DUE_SETTLE) else listOf(KhataType.ADVANCE_IN, KhataType.ADVANCE_OUT)
    var type by remember { mutableStateOf(types[0]) }
    var amount by remember { mutableStateOf(0.0) }
    var mode by remember { mutableStateOf("Cash") }
    var book by remember { mutableStateOf(Book.ESTIMATE) }
    var date by remember { mutableStateOf(now()) }
    var note by remember { mutableStateOf("") }
    fun label(k: KhataType) = if (pt == PartyType.CUSTOMER) k.customerLabel else k.supplierLabel
    JDialog(if (kind == 0) (if (pt == PartyType.CUSTOMER) "Add Udhar" else "Supplier Dues") else "Advance", Icons.Default.AccountBalanceWallet, onDismiss) {
        if (partyId.isBlank()) {
            SegTabs(listOf("Customer", "Supplier"), pt.ordinal, height = 42.dp) { pt = PartyType.entries[it]; pid = "" }
            PartyPicker(d, pt, pid) { pid = it?.id ?: "" }
        }
        SegTabs(types.map { label(it) }, types.indexOf(type), height = 44.dp) { type = types[it] }
        DNum(amount, { amount = it }, "Amount", icon = Icons.Default.CurrencyRupee, decimals = 2, trailing = {
            if (type.cashSign != 0) InlineDrop(mode, PAYMENT_MODES, { it }) { mode = it }
        })
        if (type.cashSign != 0) SegTabs(listOf("GST Book", "Estimate Book"), book.ordinal, height = 42.dp) { book = Book.entries[it] }
        DateField("Date", date, { date = it })
        JField(note, { note = it }, "Note", icon = Icons.AutoMirrored.Filled.Notes)
        GoldButton("Save Entry") {
            if (pid.isBlank()) { ctx.toast("Select a party"); return@GoldButton }
            if (amount <= 0) { ctx.toast("Enter amount"); return@GoldButton }
            Ops.addKhata(KhataEntry(newId(), pid, date, type, amount, mode, book, note.trim()))
            onSaved()
        }
    }
}
