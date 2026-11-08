package com.jeweldesk.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalOffer
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.Book
import com.jeweldesk.app.data.EXPENSE_CATEGORIES
import com.jeweldesk.app.data.Expense
import com.jeweldesk.app.data.Ops
import com.jeweldesk.app.data.PAYMENT_MODES
import com.jeweldesk.app.data.PartyType
import com.jeweldesk.app.data.dateStr
import com.jeweldesk.app.data.money
import com.jeweldesk.app.data.monthKey
import com.jeweldesk.app.data.newId
import com.jeweldesk.app.data.now
import com.jeweldesk.app.data.partyAdvance
import com.jeweldesk.app.data.partyDue
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
import com.jeweldesk.app.ui.MonthSwitcher
import com.jeweldesk.app.ui.OutlineGoldButton
import com.jeweldesk.app.ui.Page
import com.jeweldesk.app.ui.SearchBar
import com.jeweldesk.app.ui.SegTabs
import com.jeweldesk.app.ui.ValueRow
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.toast

@Composable
fun KhataScreen(initialTab: Int, openAdd: Boolean) {
    val nav = LocalNav.current
    val d = appData()
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    var side by rememberSaveable { mutableIntStateOf(0) }
    var q by rememberSaveable { mutableStateOf("") }
    var add by rememberSaveable { mutableStateOf(openAdd) }
    val type = if (side == 0) PartyType.CUSTOMER else PartyType.SUPPLIER
    val rows = d.parties.filter { it.type == type && (q.isBlank() || it.name.contains(q, true) || it.mobile.contains(q)) }
        .map { Triple(it, d.partyDue(it.id), d.partyAdvance(it.id)) }
        .filter { (_, due, adv) -> if (tab == 0) kotlin.math.abs(due) > 0.5 else adv > 0.5 }
        .sortedByDescending { (_, due, adv) -> if (tab == 0) due else adv }
    Page(
        header = { HeaderCard(if (tab == 0) "Udhar Khata" else "Advance", if (tab == 0) "Credit ledger" else "Customer advances", back = true) },
        fab = { GoldFab(if (tab == 0) "Add Udhar" else "Add Advance", Icons.Default.Add) { add = true } },
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { SegTabs(listOf("Udhar", "Advance"), tab) { tab = it } }
            item { SegTabs(listOf("Customers", "Suppliers"), side, height = 40.dp) { side = it } }
            item {
                JCard {
                    Column(Modifier.padding(14.dp)) {
                        if (tab == 0) {
                            ValueRow(if (side == 0) "Total to receive" else "Total to pay", rows.sumOf { it.second.coerceAtLeast(0.0) }.money(), Gold.Red, bold = true)
                            val neg = rows.sumOf { (-it.second).coerceAtLeast(0.0) }
                            if (neg > 0) ValueRow(if (side == 0) "Payable to customers" else "Receivable from suppliers", neg.money(), Gold.Green)
                        } else ValueRow("Total advance", rows.sumOf { it.third }.money(), Gold.Green, bold = true)
                    }
                }
            }
            item { SearchBar(q, { q = it }, "Search by name") }
            if (rows.isEmpty()) item { EmptyState(Icons.Default.AccountBalanceWallet, if (tab == 0) "No udhar records" else "No advances") }
            items(rows, key = { it.first.id }) { (p, due, adv) ->
                JCard(onClick = { nav.navigate("party/${p.id}") }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        PartyAvatar(p.name)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontWeight = FontWeight.SemiBold, color = jc.text)
                            if (p.mobile.isNotBlank()) Text(p.mobile, color = jc.sub, fontSize = 13.sp)
                        }
                        if (tab == 0) Text(kotlin.math.abs(due).money(), color = if (due > 0) Gold.Red else Gold.Green, fontWeight = FontWeight.Bold)
                        else Text(adv.money(), color = Gold.Green, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (add) KhataEntryDialog(type, "", tab, { add = false }) { add = false }
}

@Composable
fun ExpenseScreen(openAdd: Boolean) {
    val d = appData()
    var month by rememberSaveable { mutableIntStateOf(monthKey(now())) }
    var edit by remember { mutableStateOf<Expense?>(null) }
    var add by rememberSaveable { mutableStateOf(openAdd) }
    var del by remember { mutableStateOf<Expense?>(null) }
    var bookFilter by rememberSaveable { mutableIntStateOf(0) }
    val list = d.expenses.filter { monthKey(it.date) == month && (bookFilter == 0 || it.book.ordinal == bookFilter - 1) }.sortedByDescending { it.date }
    Page(
        header = { HeaderCard("Expense", "View expenses", back = true) },
        fab = { GoldFab("Add Expense", Icons.Default.Add) { add = true } },
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp, 0.dp, 16.dp, 110.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Month", color = jc.sub, modifier = Modifier.weight(1f))
                    MonthSwitcher(month) { month = it }
                }
            }
            item { SegTabs(listOf("All", "GST", "Estimate"), bookFilter, height = 40.dp) { bookFilter = it } }
            item {
                JCard {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        ValueRow("Total Expense", list.sumOf { it.amount }.money(), Gold.Red, bold = true)
                        list.groupBy { it.category }.forEach { (c, l) -> ValueRow(c, l.sumOf { it.amount }.money(), jc.sub) }
                    }
                }
            }
            if (list.isEmpty()) item { EmptyState(Icons.Default.AccountBalanceWallet, "No expenses found") }
            items(list, key = { it.id }) { e ->
                JCard(onClick = { edit = e }) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(e.category, fontWeight = FontWeight.SemiBold, color = jc.text)
                            Text("${e.date.dateStr()} • ${e.mode} • ${e.book.label}" + if (e.note.isNotBlank()) " • ${e.note}" else "", color = jc.sub, fontSize = 12.sp)
                        }
                        Text(e.amount.money(), color = Gold.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
    if (add) ExpenseDialog(null, { add = false }, onDelete = {})
    edit?.let { e -> ExpenseDialog(e, { edit = null }, onDelete = { edit = null; del = e }) }
    del?.let { e -> ConfirmDialog("Delete expense?", "${e.category} • ${e.amount.money()}", onDismiss = { del = null }) { Ops.deleteExpense(e.id) } }
}

@Composable
private fun ExpenseDialog(existing: Expense?, onDismiss: () -> Unit, onDelete: () -> Unit) {
    val ctx = LocalContext.current
    var category by remember { mutableStateOf(existing?.category ?: EXPENSE_CATEGORIES[0]) }
    var custom by remember { mutableStateOf(if (existing != null && existing.category !in EXPENSE_CATEGORIES) existing.category else "") }
    var amount by remember { mutableStateOf(existing?.amount ?: 0.0) }
    var mode by remember { mutableStateOf(existing?.mode ?: "Cash") }
    var book by remember { mutableStateOf(existing?.book ?: Book.ESTIMATE) }
    var date by remember { mutableStateOf(existing?.date ?: now()) }
    var note by remember { mutableStateOf(existing?.note ?: "") }
    val isOther = category == "Other" || custom.isNotEmpty()
    JDialog(if (existing == null) "Add Expense" else "Edit Expense", Icons.Default.LocalOffer, onDismiss) {
        DropField("Category", if (custom.isNotEmpty()) "Other" else category, EXPENSE_CATEGORIES, { it }, { category = it; if (it != "Other") custom = "" }, icon = Icons.Default.Category)
        if (isOther) JField(custom, { custom = it }, "Expense name")
        DNum(amount, { amount = it }, "Amount", icon = Icons.Default.CurrencyRupee, decimals = 2, trailing = { InlineDrop(mode, PAYMENT_MODES, { it }) { mode = it } })
        SegTabs(listOf("GST Book", "Estimate Book"), book.ordinal, height = 42.dp) { book = Book.entries[it] }
        DateField("Date", date, { date = it })
        JField(note, { note = it }, "Note", icon = Icons.AutoMirrored.Filled.Notes)
        GoldButton("Save Expense") {
            if (amount <= 0) { ctx.toast("Enter amount"); return@GoldButton }
            val cat = if (isOther && custom.isNotBlank()) custom.trim() else category
            Ops.saveExpense(Expense(existing?.id ?: newId(), date, cat, amount, mode, book, note.trim()))
            onDismiss()
        }
        if (existing != null) OutlineGoldButton("Delete", icon = Icons.Default.Delete, color = Gold.Red) { onDelete() }
    }
}
