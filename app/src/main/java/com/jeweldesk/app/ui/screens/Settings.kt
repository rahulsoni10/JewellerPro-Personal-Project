@file:OptIn(ExperimentalMaterial3Api::class)

package com.jeweldesk.app.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.Addons
import com.jeweldesk.app.data.BUSINESS_TYPES
import com.jeweldesk.app.data.Book
import com.jeweldesk.app.data.CalcMethod
import com.jeweldesk.app.data.CashEntry
import com.jeweldesk.app.data.ChargeUnit
import com.jeweldesk.app.data.GirviSettings
import com.jeweldesk.app.data.INDIAN_STATES
import com.jeweldesk.app.data.InterestResult
import com.jeweldesk.app.data.InterestType
import com.jeweldesk.app.data.InvoiceSettings
import com.jeweldesk.app.data.Metal
import com.jeweldesk.app.data.Ops
import com.jeweldesk.app.data.PAYMENT_MODES
import com.jeweldesk.app.data.Profile
import com.jeweldesk.app.data.RateUnit
import com.jeweldesk.app.data.Rates
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.data.balanceRows
import com.jeweldesk.app.data.balance
import com.jeweldesk.app.data.computeInterest
import com.jeweldesk.app.data.dateStr
import com.jeweldesk.app.data.money
import com.jeweldesk.app.data.monthKey
import com.jeweldesk.app.data.monthTotal
import com.jeweldesk.app.data.newId
import com.jeweldesk.app.data.now
import com.jeweldesk.app.data.plain
import com.jeweldesk.app.data.sha256
import com.jeweldesk.app.data.stockProducts
import com.jeweldesk.app.data.totalStockWeight
import com.jeweldesk.app.data.wt
import com.jeweldesk.app.ui.BigTitle
import com.jeweldesk.app.ui.ConfirmDialog
import com.jeweldesk.app.ui.DatePick
import com.jeweldesk.app.ui.DropField
import com.jeweldesk.app.ui.DropRow
import com.jeweldesk.app.ui.GoldButton
import com.jeweldesk.app.ui.HeaderCard
import com.jeweldesk.app.ui.IconTile
import com.jeweldesk.app.ui.JCard
import com.jeweldesk.app.ui.JDialog
import com.jeweldesk.app.ui.JField
import com.jeweldesk.app.ui.LocalNav
import com.jeweldesk.app.ui.MonthSwitcher
import com.jeweldesk.app.ui.NumField
import com.jeweldesk.app.ui.Page
import com.jeweldesk.app.ui.RadioCard
import com.jeweldesk.app.ui.ScreenColumn
import com.jeweldesk.app.ui.SegTabs
import com.jeweldesk.app.ui.SectionCard
import com.jeweldesk.app.ui.SplitRow
import com.jeweldesk.app.ui.ToggleRow
import com.jeweldesk.app.ui.ValueRow
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.data.InvoiceType

// ═════════════════════════════════════════════════════════════════
// TOURS data
// ═════════════════════════════════════════════════════════════════

data class TourItem(val icon: ImageVector, val title: String, val subtitle: String, val steps: List<String>)

val TOURS = listOf(
    TourItem(
        Icons.Default.Description, "Create your first invoice",
        "Learn how to make a GST or Estimate invoice",
        listOf(
            "Tap Invoice in the bottom bar or Quick Actions.",
            "Choose GST Sale or Estimate Sale.",
            "Pick a party (or walk-in) and add products.",
            "Set rates, charges and payments.",
            "Hit Create Invoice. Done!",
        )
    ),
    TourItem(
        Icons.Default.Diamond, "Add products to stock",
        "Manage your gold & silver inventory",
        listOf(
            "Tap Products in the bottom bar.",
            "Choose a metal tab (Gold, Silver, Metals).",
            "Tap an existing category or + Add.",
            "Fill in name, purity, weight and charges.",
            "Save. Products appear in invoice picker.",
        )
    ),
    TourItem(
        Icons.Default.AccountBalanceWallet, "Track Udhar & Advance",
        "Keep credit ledger for each customer",
        listOf(
            "Tap More → Khata / Udhar.",
            "Find the party or search by name.",
            "Tap + to add an Udhar or Advance entry.",
            "Running balance is shown per party.",
            "Settlement clears the ledger.",
        )
    ),
    TourItem(
        Icons.Default.Diamond, "Create a Girvi (pledge)",
        "Record jewellery loans and calculate interest",
        listOf(
            "Tap More → Girvi.",
            "Tap + to add a new girvi.",
            "Enter customer, pledged items and principal.",
            "Set interest rate, type and calculation method.",
            "View or release girvi at any time.",
        )
    ),
)

// ═════════════════════════════════════════════════════════════════
// SETTINGS TAB
// ═════════════════════════════════════════════════════════════════

@Composable
fun SettingsTab() {
    val nav = LocalNav.current
    val d = appData()
    val p = d.profile

    Page(
        header = {
            HeaderCard(
                title = "More",
                subtitle = p?.businessName?.uppercase() ?: "",
                letter = (p?.businessName?.take(1) ?: "M").uppercase(),
            )
        },
        navPadding = false,
    ) {
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp),
        ) {
            item { BigTitle("Business", Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)) }
            item {
                JCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SettingsRow("Business Profile", Icons.Default.Business, "Name, GSTIN, address") { nav.navigate("set/business") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Addons", Icons.Default.Extension, "Khata, Expense, Girvi modules") { nav.navigate("set/addons") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Language", Icons.Default.Language, "Display language") { nav.navigate("set/language") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Dark Mode", Icons.Default.DarkMode, if (d.darkMode) "On" else "Off") {
                        Store.update { it.copy(darkMode = !it.darkMode) }
                    }
                }
            }

            item { BigTitle("Invoice", Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            item {
                JCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SettingsRow("GST Invoice", Icons.Default.Description, "Columns, charges, logo, bank") { nav.navigate("set/invoice/GST") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Estimate Invoice", Icons.Default.Description, "Estimate template settings") { nav.navigate("set/invoice/ESTIMATE") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Product Tag", Icons.Default.Tag, "Tag & label layout") { nav.navigate("set/tag") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("GST Password", Icons.Default.Lock, "Lock GST bill editing") { nav.navigate("set/gstpass") }
                }
            }

            if (d.addons.girvi) {
                item { BigTitle("Girvi", Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
                item {
                    JCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        SettingsRow("Girvi Settings", Icons.Default.Diamond, "Default rate, fields, slip format") { nav.navigate("set/girvi") }
                    }
                }
            }

            item { BigTitle("Reports & Tools", Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            item {
                JCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SettingsRow("Rates", Icons.Default.Scale, "Gold & silver market rates") { nav.navigate("rates") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Balance", Icons.Default.AccountBalanceWallet, "Cash & bank balance") { nav.navigate("balance") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Reports", Icons.AutoMirrored.Filled.ShowChart, "Sales, stock & profit reports") { nav.navigate("reports?tab=0") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Interest Calculator", Icons.Default.Calculate, "Compute interest") { nav.navigate("interest") }
                }
            }

            item { BigTitle("Data", Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            item {
                JCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SettingsRow("Backup", Icons.Default.Backup, "Export data to JSON") { nav.navigate("set/backup") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Import", Icons.Default.SyncAlt, "Restore from JSON / import CSV") { nav.navigate("set/import") }
                }
            }

            item { BigTitle("Help & About", Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)) }
            item {
                JCard(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    SettingsRow("Tours / Tutorial", Icons.Default.PlayCircle, "Quick app walkthrough") { nav.navigate("tours") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Terms of Service", Icons.AutoMirrored.Filled.Article, "Legal terms") { nav.navigate("info/terms") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Privacy Policy", Icons.Default.Policy, "Privacy information") { nav.navigate("info/privacy") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("Contact Us", Icons.Default.ContactSupport, "Help & support") { nav.navigate("info/contact") }
                    HorizontalDivider(color = jc.border)
                    SettingsRow("About", Icons.Default.Info, "Version info") { nav.navigate("info/about") }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun SettingsRow(title: String, icon: ImageVector, subtitle: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconTile(icon, 38.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, color = jc.text, fontSize = 15.sp)
            Text(subtitle, color = jc.sub, fontSize = 12.sp)
        }
        Icon(Icons.Default.ChevronRight, null, tint = Gold.Text, modifier = Modifier.size(20.dp))
    }
}

// ═════════════════════════════════════════════════════════════════
// RATES SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun RatesScreen() {
    val nav = LocalNav.current
    val d = appData()
    var r by remember { mutableStateOf(d.rates) }
    var unit by remember { mutableIntStateOf(0) }

    fun multiplier() = when (unit) { 1 -> 10.0; 2 -> 1000.0; else -> 1.0 }
    fun display(v: Double) = if (v > 0) (v * multiplier()).plain(0) else ""
    fun store(text: String) = text.trim().toDoubleOrNull()?.div(multiplier()) ?: 0.0

    Page(
        header = { HeaderCard("Rates", "Market rates", Icons.Default.Scale, back = true) },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SegTabs(listOf("Per Gram", "Per 10g", "Per Kg"), unit) { unit = it }
            SectionCard("Gold Rates", Icons.Default.Diamond) {
                NumField(display(r.gold24), { r = r.copy(gold24 = store(it)) }, "24K / 999 fine")
                NumField(display(r.gold22), { r = r.copy(gold22 = store(it)) }, "22K / 916")
                NumField(display(r.gold18), { r = r.copy(gold18 = store(it)) }, "18K / 750")
            }
            SectionCard("Silver Rates", Icons.Default.Diamond) {
                NumField(display(r.silver999), { r = r.copy(silver999 = store(it)) }, "999 fine")
                NumField(display(r.silver925), { r = r.copy(silver925 = store(it)) }, "925 / Sterling")
            }
            GoldButton("Save Rates") {
                Store.update { it.copy(rates = r) }
                nav.popBackStack()
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// BALANCE SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun BalanceScreen() {
    val nav = LocalNav.current
    val d = appData()
    val rows = remember(d) { d.balanceRows() }
    var tab by rememberSaveable { mutableIntStateOf(0) } // 0=GST Cash, 1=GST Bank, 2=Est Cash, 3=Est Bank
    var addAdj by rememberSaveable { mutableStateOf(false) }
    var confirmDel by rememberSaveable { mutableStateOf<String?>(null) }

    val (book, cash) = when (tab) {
        1 -> Book.GST to false
        2 -> Book.ESTIMATE to true
        3 -> Book.ESTIMATE to false
        else -> Book.GST to true
    }
    val filtered = rows.filter { it.book == book && it.cash == cash }
    val total = filtered.sumOf { it.amount }

    Page(
        header = { HeaderCard("Balance", "Cash & Bank", Icons.Default.AccountBalanceWallet, back = true) },
        fab = { GoldButton("+ Adjustment", icon = Icons.Default.Add) { addAdj = true } },
    ) {
        Column(Modifier.fillMaxSize()) {
            SegTabs(listOf("GST Cash", "GST Bank", "Est. Cash", "Est. Bank"), tab) { tab = it }
            Spacer(Modifier.height(4.dp))
            ValueRow("Total Balance", total.money(), if (total >= 0) Gold.Green else Gold.Red, bold = true)
            HorizontalDivider(color = jc.border, modifier = Modifier.padding(horizontal = 16.dp))
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 100.dp)) {
                items(filtered.size, key = { idx -> filtered[idx].let { "${it.date}${it.title}${it.amount}" } }) { idx ->
                    val row = filtered[idx]
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .then(if (row.adjustmentId.isNotBlank()) Modifier.clickable { confirmDel = row.adjustmentId } else Modifier)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(row.title, color = jc.text, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text(row.date.dateStr(), color = jc.sub, fontSize = 12.sp)
                        }
                        val color = if (row.amount >= 0) Gold.Green else Gold.Red
                        Text(row.amount.money(), color = color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                    HorizontalDivider(color = jc.border)
                }
            }
        }
    }

    if (addAdj) AdjustmentDialog(book, cash) { addAdj = false }
    confirmDel?.let { cid ->
        ConfirmDialog("Delete Adjustment", "Remove this cash/bank entry?", onDismiss = { confirmDel = null }) {
            Ops.deleteCashEntry(cid)
            confirmDel = null
        }
    }
}

@Composable
private fun AdjustmentDialog(book: Book, cash: Boolean, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(now()) }
    var mode by remember { mutableStateOf(if (cash) "Cash" else "Bank") }

    JDialog("Add Adjustment", Icons.Default.AccountBalanceWallet, onDismiss) {
        NumField(amount, { amount = it }, "Amount (+ credit, - debit)")
        JField(note, { note = it }, "Note")
        DropField("Mode", mode, PAYMENT_MODES, { it }, { mode = it })
        GoldButton("Save") {
            val amt = amount.toDoubleOrNull() ?: return@GoldButton
            Ops.addCashEntry(CashEntry(id = newId(), date = date, book = book, cash = cash, amount = amt, note = note))
            onDismiss()
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// REPORTS SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun ReportsScreen(initialTab: Int) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    Page(
        header = { HeaderCard("Reports", "Business analytics", Icons.AutoMirrored.Filled.ShowChart, back = true) },
    ) {
        Column(Modifier.fillMaxSize()) {
            SegTabs(listOf("Sales", "Stock", "Profit/Loss", "Metal"), tab) { tab = it }
            when (tab) {
                0 -> SalesReport()
                1 -> StockReport()
                2 -> ProfitReport()
                else -> MetalReport()
            }
        }
    }
}

@Composable
private fun SalesReport() {
    val d = appData()
    var month by rememberSaveable { mutableIntStateOf(monthKey(now())) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)) {
        item { MonthSwitcher(month) { month = it } }
        item {
            SectionCard("GST Summary", Icons.Default.Description) {
                ValueRow("GST Sales", d.monthTotal(setOf(InvoiceType.GST_SALE), month).money())
                ValueRow("GST Sales Return", d.monthTotal(setOf(InvoiceType.GST_SALE_RETURN), month).money())
                ValueRow("GST Purchase", d.monthTotal(setOf(InvoiceType.GST_PURCHASE, InvoiceType.GST_PURCHASE_METAL), month).money())
                ValueRow("GST Purchase Return", d.monthTotal(setOf(InvoiceType.GST_PURCHASE_RETURN), month).money())
            }
        }
        item {
            SectionCard("Estimate Summary", Icons.Default.Description) {
                ValueRow("Estimate Sales", d.monthTotal(setOf(InvoiceType.EST_SALE), month).money())
                ValueRow("Estimate Purchase", d.monthTotal(setOf(InvoiceType.EST_PURCHASE, InvoiceType.EST_PURCHASE_METAL), month).money())
            }
        }
    }
}

@Composable
private fun StockReport() {
    val d = appData()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)) {
        item {
            SectionCard("Gold Stock", Icons.Default.Diamond) {
                val cats = d.categories.filter { it.metal == Metal.GOLD }
                cats.forEach { cat ->
                    val prods = d.products.filter { it.categoryId == cat.id && it.inStock }
                    if (prods.isNotEmpty()) {
                        Text(cat.name, fontWeight = FontWeight.SemiBold, color = jc.text, modifier = Modifier.padding(bottom = 4.dp))
                        ValueRow("Products", prods.size.toString())
                        ValueRow("Gross Wt", prods.sumOf { it.grossWt }.wt())
                        ValueRow("Net Wt", prods.sumOf { it.netWt }.wt())
                    }
                }
                ValueRow("Total Gold Net Wt", d.totalStockWeight(Metal.GOLD).wt(), Gold.Main)
                ValueRow("GST Book Weight", d.stockProducts(Metal.GOLD).sumOf { it.netWt }.wt())
            }
        }
        item {
            SectionCard("Silver Stock", Icons.Default.Diamond) {
                val cats = d.categories.filter { it.metal == Metal.SILVER }
                cats.forEach { cat ->
                    val prods = d.products.filter { it.categoryId == cat.id && it.inStock }
                    if (prods.isNotEmpty()) {
                        Text(cat.name, fontWeight = FontWeight.SemiBold, color = jc.text, modifier = Modifier.padding(bottom = 4.dp))
                        ValueRow("Net Wt", prods.sumOf { it.netWt }.wt())
                    }
                }
                ValueRow("Total Silver Net Wt", d.totalStockWeight(Metal.SILVER).wt(), Gold.Main)
            }
        }
    }
}

@Composable
private fun ProfitReport() {
    val d = appData()
    var month by rememberSaveable { mutableIntStateOf(monthKey(now())) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)) {
        item { MonthSwitcher(month) { month = it } }
        item {
            SectionCard("Profit / Loss", Icons.AutoMirrored.Filled.ShowChart) {
                val sales = d.monthTotal(setOf(InvoiceType.GST_SALE, InvoiceType.EST_SALE), month)
                val purchase = d.monthTotal(setOf(InvoiceType.GST_PURCHASE, InvoiceType.GST_PURCHASE_METAL, InvoiceType.EST_PURCHASE, InvoiceType.EST_PURCHASE_METAL), month)
                val expenses = d.expenses.filter { monthKey(it.date) == month }.sumOf { it.amount }
                val profit = sales - purchase - expenses
                ValueRow("Sales", sales.money())
                ValueRow("Purchase", purchase.money())
                ValueRow("Expenses", expenses.money())
                HorizontalDivider(color = jc.border)
                ValueRow("Net Profit", profit.money(), if (profit >= 0) Gold.Green else Gold.Red, bold = true)
            }
        }
    }
}

@Composable
private fun MetalReport() {
    val d = appData()
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 80.dp, top = 8.dp)) {
        item {
            SectionCard("Metal Stock (GST Book)", Icons.Default.Scale) {
                d.metalStock.forEach { ms ->
                    ValueRow(ms.name, "${ms.direct.plain(3)}g direct + ${ms.indirect.plain(3)}g indirect = ${(ms.direct + ms.indirect).plain(3)}g total")
                }
            }
        }
        item {
            SectionCard("Physical Stock Weight", Icons.Default.Diamond) {
                ValueRow("Gold (physical)", d.totalStockWeight(Metal.GOLD).wt())
                ValueRow("Silver (physical)", d.totalStockWeight(Metal.SILVER).wt())
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// INTEREST CALCULATOR SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun InterestCalcScreen() {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(now() - 30L * 24 * 3600 * 1000) }
    var interestType by remember { mutableIntStateOf(0) }
    var method by remember { mutableIntStateOf(0) }
    var result by remember { mutableStateOf<com.jeweldesk.app.data.InterestResult?>(null) }

    Page(
        header = { HeaderCard("Interest Calculator", "Compute accrued interest", Icons.Default.Calculate, back = true) },
    ) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard("Loan Details", Icons.Default.AccountBalanceWallet) {
                NumField(principal, { principal = it }, "Principal Amount (₹)")
                NumField(rate, { rate = it }, "Interest Rate (% per month)")
                SegTabs(listOf("Simple", "Compound"), interestType) { interestType = it }
                SegTabs(CalcMethod.entries.map { it.label }, method) { method = it }
                val dateStr = date.dateStr()
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("From Date: ", color = jc.sub, fontSize = 14.sp)
                    Text(dateStr, color = Gold.Main, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            }

            GoldButton("Calculate") {
                val p2 = principal.toDoubleOrNull() ?: return@GoldButton
                val r2 = rate.toDoubleOrNull() ?: return@GoldButton
                val iType = if (interestType == 0) InterestType.SIMPLE else InterestType.COMPOUND
                val m = CalcMethod.entries.getOrElse(method) { CalcMethod.ONE_MONTH_PLUS_DAILY }
                val (days, interest) = computeInterest(p2, r2, date, now(), iType, m)
                result = InterestResult(days, days / 30.0, interest, 0.0)
            }

            result?.let { res ->
                val principalVal = principal.toDoubleOrNull() ?: 0.0
                SectionCard("Result", Icons.Default.Calculate) {
                    ValueRow("Principal", principalVal.money())
                    ValueRow("Duration", "${res.days} days (${res.months.plain(2)} months)")
                    ValueRow("Interest", res.interest.money(), Gold.Main)
                    ValueRow("Total Due", res.outstanding(principalVal).money(), Gold.Red, bold = true)
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// FIXED AMOUNT SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun FixedAmountScreen() {
    val nav = LocalNav.current
    var totalStr by remember { mutableStateOf("") }
    var rateStr by remember { mutableStateOf("") }
    var makingStr by remember { mutableStateOf("") }
    var gstRate by remember { mutableDoubleStateOf(3.0) }
    var isGst by remember { mutableStateOf(true) }
    var solve by remember { mutableIntStateOf(0) } // 0=Weight, 1=Rate, 2=Making
    var result by remember { mutableStateOf<Double?>(null) }

    Page(header = { HeaderCard("Fixed Amount Invoice", "Reverse calculate", Icons.Default.Calculate, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard("Bill Settings", Icons.Default.Description) {
                ToggleRow("GST Invoice", isGst) { isGst = it }
                if (isGst) NumField(gstRate.plain(1), { gstRate = it.toDoubleOrNull() ?: 3.0 }, "GST Rate %")
            }
            SectionCard("Solve for", Icons.Default.Calculate) {
                SegTabs(listOf("Weight", "Rate", "Making"), solve) { solve = it }
            }
            SectionCard("Known Values", Icons.Default.Description) {
                NumField(totalStr, { totalStr = it }, "Total Bill Amount (₹)")
                if (solve != 1) NumField(rateStr, { rateStr = it }, "Rate per gram (₹)")
                if (solve != 2) NumField(makingStr, { makingStr = it }, "Making per gram (₹)")
            }
            GoldButton("Calculate") {
                val total = totalStr.toDoubleOrNull() ?: return@GoldButton
                val gstFactor = if (isGst) 1 + gstRate / 100 else 1.0
                val taxable = total / gstFactor
                val rate = rateStr.toDoubleOrNull() ?: 0.0
                val making = makingStr.toDoubleOrNull() ?: 0.0
                result = when (solve) {
                    0 -> if (rate + making > 0) taxable / (rate + making) else null   // Weight
                    1 -> {                                                               // Rate
                        val wt = makingStr.toDoubleOrNull() ?: 0.0  // making field holds weight here
                        if (wt > 0 && making > 0) (taxable - making * wt) / wt else null
                    }
                    else -> if (rate > 0) {                                             // Making
                        val wt = rateStr.toDoubleOrNull() ?: 0.0
                        if (wt > 0) (taxable - rate * wt) / wt else null
                    } else null
                }
            }
            result?.let { r ->
                SectionCard("Result", Icons.Default.CheckCircle) {
                    val label = listOf("Weight (grams)", "Rate per gram (₹)", "Making per gram (₹)")[solve]
                    ValueRow(label, r.plain(3), Gold.Main, bold = true)
                    ValueRow("Bill Total", totalStr.toDoubleOrNull()?.money() ?: "—")
                }
                GoldButton("Use in Invoice") {
                    Prefill.item = Prefill.item?.copy() // keep existing or null
                    nav.navigate("inv/new/GST_SALE")
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// INVOICE SETTINGS SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun InvoiceSettingsScreen(book: Book) {
    val d = appData()
    var s by remember { mutableStateOf(d.invoiceSettings(book)) }

    fun save() = Store.update {
        if (book == Book.GST) it.copy(gstInvoice = s) else it.copy(estInvoice = s)
    }

    Page(header = { HeaderCard("${book.label} Invoice Settings", "Template configuration", Icons.Default.Settings, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard("General", Icons.Default.Description) {
                JField(s.prefix, { s = s.copy(prefix = it); save() }, "Invoice Prefix")
                JField(s.tagline, { s = s.copy(tagline = it); save() }, "Tagline")
                JField(s.invoicePhone, { s = s.copy(invoicePhone = it); save() }, "Display Phone")
                JField(s.companyInitial, { s = s.copy(companyInitial = it); save() }, "Company Initial (logo letter)")
                if (book == Book.GST) {
                    JField(s.bisNo, { s = s.copy(bisNo = it); save() }, "BIS Registration No.")
                }
            }
            SectionCard("Show on Invoice", Icons.Default.Description) {
                ToggleRow("Hallmark logo", s.showHallmarkLogo) { s = s.copy(showHallmarkLogo = it); save() }
                if (book == Book.ESTIMATE) ToggleRow("Estimate title", s.showEstimateTitle) { s = s.copy(showEstimateTitle = it); save() }
                ToggleRow("Invoice description / note", s.showInvoiceDescription) { s = s.copy(showInvoiceDescription = it); save() }
                ToggleRow("Payment mode", s.showPaymentMode) { s = s.copy(showPaymentMode = it); save() }
                ToggleRow("Gold rate", s.showGoldRate) { s = s.copy(showGoldRate = it); save() }
                ToggleRow("Silver rate", s.showSilverRate) { s = s.copy(showSilverRate = it); save() }
                ToggleRow("Customer signature", s.showCustomerSignature) { s = s.copy(showCustomerSignature = it); save() }
                ToggleRow("Collect old metal section", s.collectOldMetal) { s = s.copy(collectOldMetal = it); save() }
            }
            SectionCard("Columns (item table)", Icons.Default.GridView) {
                ToggleRow("Purity", s.colPurity) { s = s.copy(colPurity = it); save() }
                ToggleRow("Gross / Less Weight", s.colGrossLess) { s = s.copy(colGrossLess = it); save() }
                ToggleRow("Pcs", s.colPcs) { s = s.copy(colPcs = it); save() }
                ToggleRow("Size", s.colSize) { s = s.copy(colSize = it); save() }
                ToggleRow("Description", s.colDescription) { s = s.copy(colDescription = it); save() }
                ToggleRow("HUID", s.colHuid) { s = s.copy(colHuid = it); save() }
            }
            SectionCard("Charges", Icons.Default.Percent) {
                ToggleRow("Making charge", s.makingCharge) { s = s.copy(makingCharge = it); save() }
                ToggleRow("Wastage charge", s.wastageCharge) { s = s.copy(wastageCharge = it); save() }
                ToggleRow("Hallmark charge", s.hallmarkCharge) { s = s.copy(hallmarkCharge = it); save() }
                ToggleRow("Stone / Diamond charge", s.stoneCharge) { s = s.copy(stoneCharge = it); save() }
                DropRow("Metal rate unit", s.metalRateUnit.label, RateUnit.entries, { it.label }) {
                    s = s.copy(metalRateUnit = it); save()
                }
                DropRow("Making unit", s.makingUnit.label, ChargeUnit.entries, { it.label }) {
                    s = s.copy(makingUnit = it); save()
                }
                DropRow("Hallmark unit", s.hallmarkUnit.label, ChargeUnit.entries, { it.label }) {
                    s = s.copy(hallmarkUnit = it); save()
                }
            }
            SectionCard("Bank / Payment", Icons.Default.AccountBalance) {
                ToggleRow("Show bank details", s.showBank) { s = s.copy(showBank = it); save() }
                if (s.showBank) {
                    JField(s.bankName, { s = s.copy(bankName = it); save() }, "Bank Name")
                    JField(s.accountNumber, { s = s.copy(accountNumber = it); save() }, "Account Number")
                    JField(s.bankBranch, { s = s.copy(bankBranch = it); save() }, "Branch")
                    JField(s.ifsc, { s = s.copy(ifsc = it); save() }, "IFSC Code")
                    JField(s.upiId, { s = s.copy(upiId = it); save() }, "UPI ID")
                }
            }
            SectionCard("Terms & Declaration", Icons.AutoMirrored.Filled.Article) {
                JField(s.declaration, { s = s.copy(declaration = it); save() }, "Declaration", singleLine = false, minLines = 3)
                JField(s.terms1, { s = s.copy(terms1 = it); save() }, "Term 1")
                JField(s.terms2, { s = s.copy(terms2 = it); save() }, "Term 2")
                JField(s.terms3, { s = s.copy(terms3 = it); save() }, "Term 3")
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// TAG SETTINGS SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun TagSettingsScreen() {
    val d = appData()
    var isLabel by rememberSaveable { mutableStateOf(false) }
    var layout by remember(isLabel) { mutableStateOf(if (isLabel) d.label else d.tag) }

    fun save() = Store.update { if (isLabel) it.copy(label = layout) else it.copy(tag = layout) }

    Page(header = { HeaderCard("Tag & Label Settings", "Print layout", Icons.Default.Tag, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SegTabs(listOf("Tag", "Label"), if (isLabel) 1 else 0) { isLabel = it == 1 }
            SectionCard("Size (mm)", Icons.Default.Description) {
                NumField(layout.lengthMm.plain(1), { layout = layout.copy(lengthMm = it.toDoubleOrNull() ?: layout.lengthMm); save() }, "Length (mm)")
                NumField(layout.breadthMm.plain(1), { layout = layout.copy(breadthMm = it.toDoubleOrNull() ?: layout.breadthMm); save() }, "Breadth (mm)")
                NumField(layout.tailLengthMm.plain(1), { layout = layout.copy(tailLengthMm = it.toDoubleOrNull() ?: layout.tailLengthMm); save() }, "Tail Length (mm)")
                NumField(layout.tailBreadthMm.plain(1), { layout = layout.copy(tailBreadthMm = it.toDoubleOrNull() ?: layout.tailBreadthMm); save() }, "Tail Breadth (mm)")
            }
            SectionCard("Fields Preview", Icons.Default.GridView) {
                val allFields = listOf("productName", "productCode", "grossWt", "netWt", "purity", "huid", "category", "size", "firmName", "diamondWt", "qr")
                Text("Available fields to include:", color = jc.sub, fontSize = 13.sp)
                allFields.forEach { f ->
                    val included = layout.columns.any { col -> col.any { it.key == f } }
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            if (!included) {
                                val updated = layout.columns.toMutableList()
                                val newField = com.jeweldesk.app.data.TagField(f)
                                if (updated.isNotEmpty()) updated[0] = updated[0] + newField
                                layout = layout.copy(columns = updated)
                                save()
                            }
                        }.padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(f, color = jc.text, modifier = Modifier.weight(1f))
                        if (included) Text("✓", color = Gold.Main)
                    }
                }
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// GIRVI SETTINGS SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun GirviSettingsScreen() {
    val d = appData()
    var gs by remember { mutableStateOf(d.girviSettings) }

    fun save() = Store.update { it.copy(girviSettings = gs) }

    Page(header = { HeaderCard("Girvi Settings", "Pledge configuration", Icons.Default.Diamond, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard("Fields on Slip", Icons.Default.Description) {
                ToggleRow("Pincode", gs.pincode) { gs = gs.copy(pincode = it); save() }
                ToggleRow("City", gs.city) { gs = gs.copy(city = it); save() }
                ToggleRow("State", gs.state) { gs = gs.copy(state = it); save() }
                ToggleRow("Pcs", gs.pcs) { gs = gs.copy(pcs = it); save() }
                ToggleRow("Gross Weight", gs.grossWt) { gs = gs.copy(grossWt = it); save() }
                ToggleRow("Less / Net Weight", gs.lessWt) { gs = gs.copy(lessWt = it); save() }
                ToggleRow("Product Description", gs.productDescription) { gs = gs.copy(productDescription = it); save() }
                ToggleRow("Girvi Description", gs.girviDescription) { gs = gs.copy(girviDescription = it); save() }
                ToggleRow("Approx Value", gs.approxValue) { gs = gs.copy(approxValue = it); save() }
                ToggleRow("Interest on Slip", gs.slipRate) { gs = gs.copy(slipRate = it); save() }
                ToggleRow("Interest Type on Slip", gs.slipType) { gs = gs.copy(slipType = it); save() }
                ToggleRow("Calc. Method on Slip", gs.slipMethod) { gs = gs.copy(slipMethod = it); save() }
            }
            SectionCard("Defaults", Icons.Default.Settings) {
                DropRow("Default Interest Type", gs.defaultType.label, InterestType.entries, { it.label }) {
                    gs = gs.copy(defaultType = it); save()
                }
                NumField(gs.defaultRate.plain(2), { gs = gs.copy(defaultRate = it.toDoubleOrNull() ?: 0.0); save() }, "Default Rate (% / month)")
                DropRow("Default Calc. Method", gs.defaultMethod.label, CalcMethod.entries, { it.label }) {
                    gs = gs.copy(defaultMethod = it); save()
                }
            }
            SectionCard("Slip Terms", Icons.AutoMirrored.Filled.Article) {
                JField(gs.declaration, { gs = gs.copy(declaration = it); save() }, "Declaration", singleLine = false, minLines = 3)
                JField(gs.terms1, { gs = gs.copy(terms1 = it); save() }, "Term 1")
                JField(gs.terms2, { gs = gs.copy(terms2 = it); save() }, "Term 2")
                JField(gs.terms3, { gs = gs.copy(terms3 = it); save() }, "Term 3")
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// ADDONS SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun AddonsScreen() {
    val d = appData()
    var a by remember { mutableStateOf(d.addons) }

    fun save() = Store.update { it.copy(addons = a) }

    Page(header = { HeaderCard("Addons", "Feature modules", Icons.Default.Extension, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard("Modules", Icons.Default.Extension) {
                ToggleRow("Khata (Udhar / Advance ledger)", a.khata) { a = a.copy(khata = it); save() }
                HorizontalDivider(color = jc.border)
                ToggleRow("Expense tracker", a.expense) { a = a.copy(expense = it); save() }
                HorizontalDivider(color = jc.border)
                ToggleRow("Girvi (pledge / loan system)", a.girvi) { a = a.copy(girvi = it); save() }
            }
            Text(
                "Turning off a module hides it from the app. Your data is preserved.",
                color = jc.sub, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// BUSINESS SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun BusinessScreen() {
    val nav = LocalNav.current
    val d = appData()
    val existing = d.profile ?: Profile()
    var p by remember { mutableStateOf(existing) }
    var confirmLogout by rememberSaveable { mutableStateOf(false) }
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Page(header = { HeaderCard("Business Profile", "Company information", Icons.Default.Business, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard("Owner & Business", Icons.Default.Person) {
                JField(p.ownerName, { p = p.copy(ownerName = it) }, "Owner Name")
                JField(p.businessName, { p = p.copy(businessName = it) }, "Business Name")
                DropField("Business Type", p.businessType, BUSINESS_TYPES, { it }, { p = p.copy(businessType = it) })
                JField(p.gstin, { p = p.copy(gstin = it) }, "GSTIN")
                JField(p.email, { p = p.copy(email = it) }, "Email")
            }
            SectionCard("Address", Icons.Default.Business) {
                JField(p.address, { p = p.copy(address = it) }, "Address")
                JField(p.city, { p = p.copy(city = it) }, "City")
                DropField("State", p.state, INDIAN_STATES, { it }, { p = p.copy(state = it) })
                JField(p.pincode, { p = p.copy(pincode = it) }, "Pincode", keyboard = KeyboardType.Number)
            }
            GoldButton("Save Profile") {
                Store.update { it.copy(profile = p) }
                nav.popBackStack()
            }
            Spacer(Modifier.height(8.dp))
            com.jeweldesk.app.ui.OutlineGoldButton("Log Out", color = Gold.Red) { confirmLogout = true }
            com.jeweldesk.app.ui.OutlineGoldButton("Delete Account", color = Gold.Red) { confirmDelete = true }
        }
    }

    if (confirmLogout) ConfirmDialog("Log Out", "You will need to re-enter your mobile number.", confirm = "Log Out", danger = false, onDismiss = { confirmLogout = false }) {
        Ops.logout(); confirmLogout = false
        nav.navigate("login") { popUpTo(0) }
    }
    if (confirmDelete) ConfirmDialog("Delete Account", "All data will be permanently erased. This cannot be undone.", onDismiss = { confirmDelete = false }) {
        Ops.deleteAccount(); confirmDelete = false
        nav.navigate("login") { popUpTo(0) }
    }
}

// ═════════════════════════════════════════════════════════════════
// GST PASSWORD SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun GstPasswordScreen() {
    val nav = LocalNav.current
    val d = appData()
    val hasPassword = d.gstPasswordHash.isNotBlank()
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Page(header = { HeaderCard("GST Password", "Lock GST bill editing", Icons.Default.Lock, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                if (hasPassword) "Change or remove the GST password. Leave new password blank to remove lock."
                else "Set a password to protect GST invoices from unauthorised editing.",
                color = jc.sub, fontSize = 14.sp,
            )
            if (hasPassword) {
                JField(current, { current = it }, "Current Password", password = true)
            }
            JField(newPass, { newPass = it }, "New Password", password = true)
            JField(confirm, { confirm = it }, "Confirm Password", password = true)
            if (error.isNotBlank()) Text(error, color = Gold.Red, fontSize = 13.sp)
            GoldButton("Save Password") {
                if (hasPassword && sha256(current) != d.gstPasswordHash) {
                    error = "Current password is incorrect"; return@GoldButton
                }
                if (newPass != confirm) { error = "Passwords do not match"; return@GoldButton }
                Store.update { it.copy(gstPasswordHash = if (newPass.isBlank()) "" else sha256(newPass)) }
                nav.popBackStack()
            }
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// LANGUAGE SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun LanguageScreen() {
    Page(header = { HeaderCard("Language", "Display language", Icons.Default.Language, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            listOf("English", "Hindi", "Gujarati", "Marathi", "Tamil", "Telugu", "Kannada", "Bengali").forEach { lang ->
                val selected = lang == "English"
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                        .background(if (selected) jc.mint else jc.surface)
                        .clickable { /* future multi-language */ }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(lang, color = jc.text, modifier = Modifier.weight(1f))
                    if (selected) Icon(Icons.Default.CheckCircle, null, tint = Gold.Main)
                }
            }
            Text("Additional languages coming soon.", color = jc.sub, fontSize = 13.sp, modifier = Modifier.padding(4.dp))
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// BACKUP SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun BackupScreen() {
    val ctx = LocalContext.current
    var done by rememberSaveable { mutableStateOf(false) }

    Page(header = { HeaderCard("Backup", "Export your data", Icons.Default.Backup, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard("Export JSON", Icons.Default.Share) {
                Text(
                    "Export all your invoices, parties, products, girvi and settings as a JSON file. " +
                        "You can share it via WhatsApp, email, or save to Google Drive for safekeeping.",
                    color = jc.sub, fontSize = 14.sp,
                )
                Spacer(Modifier.height(8.dp))
                GoldButton("Export & Share") {
                    try {
                        val json = Store.exportJson()
                        val file = java.io.File(ctx.cacheDir, "shared/jeweldesk_backup_${now()}.json").also {
                            it.parentFile?.mkdirs(); it.writeText(json)
                        }
                        val uri = androidx.core.content.FileProvider.getUriForFile(ctx, "${ctx.packageName}.files", file)
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/json"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        ctx.startActivity(Intent.createChooser(intent, "Share Backup").also { it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) })
                        done = true
                    } catch (e: Exception) {
                        // silently ignore for now
                    }
                }
                if (done) Text("Backup shared successfully!", color = Gold.Green)
            }
            Text("Tip: Save your backup to Google Drive so it's accessible from any device.", color = jc.sub, fontSize = 12.sp)
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// IMPORT SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun ImportScreen() {
    val ctx = LocalContext.current
    var status by rememberSaveable { mutableStateOf("") }
    var confirmImport by rememberSaveable { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        try {
            val json = ctx.contentResolver.openInputStream(uri)?.bufferedReader()?.readText() ?: ""
            confirmImport = json
        } catch (e: Exception) {
            status = "Failed to read file: ${e.message}"
        }
    }

    Page(header = { HeaderCard("Import", "Restore from backup", Icons.Default.SyncAlt, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            SectionCard("Restore from JSON", Icons.Default.Backup) {
                Text(
                    "Import a JewelDesk backup JSON file. This will REPLACE all current data.",
                    color = jc.sub, fontSize = 14.sp,
                )
                Spacer(Modifier.height(8.dp))
                GoldButton("Choose Backup File") { launcher.launch("application/json") }
            }
            if (status.isNotBlank()) {
                Text(status, color = if (status.startsWith("Success")) Gold.Green else Gold.Red, fontSize = 13.sp)
            }
        }
    }

    confirmImport?.let { json ->
        ConfirmDialog(
            "Replace All Data",
            "This will permanently replace all current data with the backup. Continue?",
            confirm = "Replace",
            onDismiss = { confirmImport = null },
        ) {
            if (Store.importJson(json)) status = "Success! Data restored." else status = "Invalid backup file."
            confirmImport = null
        }
    }
}

// ═════════════════════════════════════════════════════════════════
// INFO / ABOUT SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun InfoScreen(key: String) {
    val ctx = LocalContext.current
    val (title, content) = when (key) {
        "terms" -> "Terms of Service" to TERMS_TEXT
        "privacy" -> "Privacy Policy" to PRIVACY_TEXT
        "contact" -> "Contact Us" to CONTACT_TEXT
        "about" -> "About JewelDesk" to ABOUT_TEXT
        else -> key.replaceFirstChar { it.uppercase() } to "No content available."
    }
    Page(header = { HeaderCard(title, "", Icons.Default.Info, back = true) }) {
        Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp)) {
            Text(content, color = jc.text, fontSize = 14.sp, lineHeight = 22.sp)
            if (key == "contact") {
                Spacer(Modifier.height(16.dp))
                GoldButton("Send Email") {
                    val i = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:support@jeweldesk.in")
                        putExtra(Intent.EXTRA_SUBJECT, "JewelDesk Support")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    ctx.startActivity(i)
                }
            }
        }
    }
}

private const val TERMS_TEXT = """TERMS OF SERVICE

By using JewelDesk you agree to these terms.

1. The app is provided for internal business use. Data is stored locally on your device.
2. We are not responsible for data loss due to device failure. Please backup regularly.
3. All calculations are for informational purposes. Consult a CA for tax compliance.
4. The app may not be redistributed, modified or reverse-engineered without permission.

Last updated: 2024"""

private const val PRIVACY_TEXT = """PRIVACY POLICY

JewelDesk stores all data locally on your device. We do not collect, transmit or sell any personal information.

No analytics or tracking SDKs are used. Your business data never leaves your device unless you explicitly export and share it.

For questions, contact support@jeweldesk.in."""

private const val CONTACT_TEXT = """CONTACT US

We are here to help!

Email: support@jeweldesk.in
Hours: Mon–Sat, 10am–6pm IST

For feature requests, bug reports, or general questions, send us an email and we will respond within 1–2 business days."""

private const val ABOUT_TEXT = """ABOUT JEWELDESK

JewelDesk is a professional invoicing and management solution built for Indian jewellers.

Features:
• GST & Estimate invoicing
• Product & stock management  
• Party ledger (Khata / Udhar)
• Girvi (pledge/pawn) management
• Expense tracking
• PDF invoice generation
• Balance & reports

Version: 1.0.0
Package: com.jeweldesk.app"""

// ═════════════════════════════════════════════════════════════════
// TOURS SCREEN
// ═════════════════════════════════════════════════════════════════

@Composable
fun ToursScreen() {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val tour = TOURS[selected]

    Page(header = { HeaderCard("App Tours", "Quick walkthroughs", Icons.Default.PlayCircle, back = true) }) {
        Column(Modifier.fillMaxSize()) {
            SegTabs(TOURS.map { it.title.take(10) }, selected) { selected = it }
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                JCard {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        IconTile(tour.icon, 50.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(tour.title, fontWeight = FontWeight.Bold, color = jc.text, fontSize = 16.sp)
                            Text(tour.subtitle, color = jc.sub, fontSize = 13.sp)
                        }
                    }
                }
                tour.steps.forEachIndexed { i, step ->
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        androidx.compose.foundation.layout.Box(
                            Modifier.size(28.dp).clip(CircleShape).background(Gold.Main),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("${i + 1}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(step, color = jc.text, fontSize = 14.sp, lineHeight = 20.sp, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
