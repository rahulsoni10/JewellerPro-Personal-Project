package com.jeweldesk.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TurnRight
import androidx.compose.material.icons.outlined.Diamond
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.InvoiceType
import com.jeweldesk.app.data.Metal
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.data.balance
import com.jeweldesk.app.data.Book
import com.jeweldesk.app.data.money
import com.jeweldesk.app.data.monthKey
import com.jeweldesk.app.data.monthTotal
import com.jeweldesk.app.data.now
import com.jeweldesk.app.ui.BigTitle
import com.jeweldesk.app.ui.ChipRow
import com.jeweldesk.app.ui.GoldFab
import com.jeweldesk.app.ui.HeaderCard
import com.jeweldesk.app.ui.IconTile
import com.jeweldesk.app.ui.JCard
import com.jeweldesk.app.ui.JDialog
import com.jeweldesk.app.ui.LocalNav
import com.jeweldesk.app.ui.MonthSwitcher
import com.jeweldesk.app.ui.Page
import com.jeweldesk.app.ui.ScrollTabs
import com.jeweldesk.app.ui.SplitRow
import com.jeweldesk.app.ui.appData
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold

@Composable
fun MainScreen(initialTab: Int) {
    var tab by rememberSaveable { mutableIntStateOf(initialTab) }
    BackHandler(enabled = tab != 0) { tab = 0 }
    Column(Modifier.fillMaxSize().background(jc.bg)) {
        Box(Modifier.weight(1f)) {
            when (tab) {
                0 -> HomeTab()
                1 -> InvoicesTab()
                2 -> ProductsTab()
                3 -> PartiesTab()
                else -> SettingsTab()
            }
        }
        NavigationBar(containerColor = jc.nav) {
            val items = listOf(
                Triple("Home", Icons.Outlined.Home, Icons.Outlined.Home),
                Triple("Invoice", Icons.Default.Description, Icons.Default.Description),
                Triple("Products", Icons.Outlined.Diamond, Icons.Default.Diamond),
                Triple("Parties", Icons.Default.Groups, Icons.Default.Groups),
                Triple("More", Icons.Default.GridView, Icons.Default.GridView),
            )
            items.forEachIndexed { i, (label, icon, sel) ->
                NavigationBarItem(
                    selected = tab == i, onClick = { tab = i },
                    icon = { Icon(if (tab == i) sel else icon, label) },
                    label = { Text(label) },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = jc.navPill, selectedIconColor = jc.text, selectedTextColor = jc.text,
                        unselectedIconColor = jc.sub, unselectedTextColor = jc.sub,
                    ),
                )
            }
        }
    }
}

data class QuickAction(val title: String, val sub: String, val icon: ImageVector, val route: String)

@Composable
fun HomeTab() {
    val nav = LocalNav.current
    val d = appData()
    val p = d.profile
    var chip by rememberSaveable { mutableIntStateOf(0) }
    var recent by rememberSaveable { mutableIntStateOf(0) }
    var month by rememberSaveable { mutableIntStateOf(monthKey(now())) }
    var pickType by rememberSaveable { mutableStateOf(false) }
    var addProduct by rememberSaveable { mutableStateOf(false) }

    val groups = buildList {
        add("Sale" to listOf(
            QuickAction("GST Sale Invoice", "Create GST sale bill", Icons.Default.ShoppingBag, "inv/new/GST_SALE"),
            QuickAction("Estimate Sale Invoice", "Sale quotation", Icons.Default.Receipt, "inv/new/EST_SALE"),
            QuickAction("GST Sale Return", "Reverse sale bill", Icons.AutoMirrored.Filled.Undo, "inv/new/GST_SALE_RETURN"),
            QuickAction("Fix. Amt. Invoice", "Fixed amount bill", Icons.Default.LocalOffer, "fixed"),
        ))
        add("Purchase" to listOf(
            QuickAction("GST Purchase Invoice", "Supplier purchase entry", Icons.Default.ShoppingBag, "inv/new/GST_PURCHASE"),
            QuickAction("GST Purchase Metal", "Supplier old metal purchase", Icons.Outlined.Diamond, "inv/new/GST_PURCHASE_METAL"),
            QuickAction("Estimate Purchase Invoice", "Draft purchase", Icons.AutoMirrored.Filled.Assignment, "inv/new/EST_PURCHASE"),
            QuickAction("Estimate Purchase Metal", "Draft old metal purchase", Icons.Outlined.Diamond, "inv/new/EST_PURCHASE_METAL"),
            QuickAction("GST Purchase Return", "Supplier return", Icons.Default.TurnRight, "inv/new/GST_PURCHASE_RETURN"),
        ))
        add("Parties" to listOf(
            QuickAction("Add Customer", "New party", Icons.Default.PersonAdd, "partyedit/CUSTOMER/new"),
            QuickAction("Add Supplier", "Supplier ledger", Icons.Default.Storefront, "partyedit/SUPPLIER/new"),
        ))
        if (d.addons.khata) add("Khata" to listOf(
            QuickAction("Udhar Khata", "Credit ledger", Icons.Default.AccountBalanceWallet, "khata?tab=0"),
            QuickAction("Add Udhar", "Create credit entry", Icons.Default.AccountBalanceWallet, "khata?tab=0&add=true"),
            QuickAction("Advance", "Customer advances", Icons.Default.AccountBalanceWallet, "khata?tab=1"),
        ))
        if (d.addons.expense) add("Expense" to listOf(
            QuickAction("Expense", "View expenses", Icons.Default.AccountBalanceWallet, "expense"),
            QuickAction("Add Expense", "Create expense entry", Icons.Default.LocalOffer, "expense?add=true"),
        ))
        if (d.addons.girvi) add("Girvi" to listOf(
            QuickAction("Girvi", "Jewellery pledge records", Icons.Default.AccountBalanceWallet, "girvi"),
            QuickAction("Add Girvi", "Create pledge record", Icons.Outlined.Diamond, "girviedit/new"),
            QuickAction("Search Girvi", "Find pledge record", Icons.AutoMirrored.Filled.Assignment, "girvi?mode=search"),
            QuickAction("Transfer Girvi", "Transfer pledge", Icons.Default.SwapHoriz, "girvi?mode=transfer"),
        ))
        add("Extras" to listOf(
            QuickAction("Update Rates", "Gold & silver rate", Icons.AutoMirrored.Filled.TrendingUp, "rates"),
            QuickAction("Add Product", "Stock item", Icons.Outlined.Diamond, "#product"),
            QuickAction("Interest Calculator", "Outstanding khata", Icons.Default.Calculate, "interest"),
            QuickAction("Reports", "View business reports", Icons.AutoMirrored.Filled.ShowChart, "reports"),
        ))
        add("Help" to listOf(
            QuickAction("Tutorial", "App walkthrough", Icons.Default.PlayCircle, "tours"),
            QuickAction("Contact Us", "Help, support and product assistance", Icons.Default.HeadsetMic, "info/contact"),
        ))
    }
    if (chip >= groups.size) chip = 0
    val recentTypes = listOf(InvoiceType.GST_SALE, InvoiceType.EST_SALE, InvoiceType.GST_PURCHASE, InvoiceType.EST_PURCHASE)
    val recentSet = when (recent) {
        2 -> setOf(InvoiceType.GST_PURCHASE, InvoiceType.GST_PURCHASE_METAL)
        3 -> setOf(InvoiceType.EST_PURCHASE, InvoiceType.EST_PURCHASE_METAL)
        else -> setOf(recentTypes[recent])
    }
    val recentList = d.invoices.filter { it.type in recentSet }.sortedWith(compareByDescending<com.jeweldesk.app.data.Invoice> { it.date }.thenByDescending { it.number }).take(10)

    Page(
        header = {
            HeaderCard(
                title = p?.ownerName ?: "", subtitle = (p?.businessName ?: "").uppercase(),
                letter = (p?.businessName?.take(1) ?: "J").uppercase(),
            )
        },
        fab = { GoldFab("Invoice", Icons.Default.Add) { pickType = true } },
        navPadding = false,
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)) {
            item { BigTitle("Quick actions", Modifier.padding(start = 16.dp, top = 8.dp, bottom = 12.dp)) }
            item { ChipRow(groups.map { it.first }, chip) { chip = it } }
            item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    groups[chip].second.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            row.forEach { qa ->
                                ActionCard(qa, Modifier.weight(1f)) {
                                    if (qa.route == "#product") addProduct = true else nav.navigate(qa.route)
                                }
                            }
                            if (row.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                SummaryCard("Sale / Purchase", Icons.Default.ShoppingCart, { nav.navigate("reports?tab=0") }, {
                    MonthSwitcher(month) { month = it }
                }) {
                    SplitRow(
                        "GST Sale: " + d.monthTotal(setOf(InvoiceType.GST_SALE), month).money(),
                        "GST Purchase: " + d.monthTotal(setOf(InvoiceType.GST_PURCHASE, InvoiceType.GST_PURCHASE_METAL), month).money(), jc.mint,
                    )
                    SplitRow(
                        "Estimate Sale: " + d.monthTotal(setOf(InvoiceType.EST_SALE), month).money(),
                        "Estimate Purchase: " + d.monthTotal(setOf(InvoiceType.EST_PURCHASE, InvoiceType.EST_PURCHASE_METAL), month).money(), jc.pink,
                    )
                }
            }
            item {
                SummaryCard("Balance", Icons.Default.AccountBalanceWallet, { nav.navigate("balance") }) {
                    SplitRow("GST Cash: " + d.balance(Book.GST, true).money(), "GST Bank: " + d.balance(Book.GST, false).money(), jc.mint)
                    SplitRow("Estimate Cash: " + d.balance(Book.ESTIMATE, true).money(), "Estimate Bank: " + d.balance(Book.ESTIMATE, false).money(), jc.pink)
                }
            }
            item {
                SummaryCard("Rates", Icons.Default.Scale, { nav.navigate("rates") }) {
                    val unit = d.gstInvoice.metalRateUnit
                    fun r(v: Double) = if (v > 0) (v * unit.grams).money() + if (unit.grams == 1.0) "/g" else if (unit.grams == 10.0) "/10g" else "/kg" else "—"
                    SplitRow("Gold 24K: " + r(d.rates.gold24), "Silver 999: " + r(d.rates.silver999), jc.cream)
                }
            }
            item { BigTitle("Recent Invoices", Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp)) }
            item { ScrollTabs(recentTypes.map { it.short }, recent) { recent = it } }
            if (recentList.isEmpty()) item {
                Text("No invoices found", color = jc.sub, fontSize = 17.sp, modifier = Modifier.fillMaxWidth().padding(32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            items(recentList, key = { it.id }) { inv ->
                Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) { InvoiceRow(inv) { nav.navigate("inv/view/${inv.id}") } }
            }
        }
    }

    if (pickType) InvoiceTypeDialog({ pickType = false }) { route -> pickType = false; nav.navigate(route) }
    if (addProduct) ProductDialog(null, Metal.GOLD, onDismiss = { addProduct = false }) { addProduct = false }
    if (!d.firstMoveShown) FirstMoveDialog()
}

@Composable
private fun ActionCard(qa: QuickAction, modifier: Modifier, onClick: () -> Unit) {
    JCard(modifier.height(130.dp), onClick = onClick) {
        Column(Modifier.padding(14.dp)) {
            Row {
                IconTile(qa.icon, 46.dp)
                Spacer(Modifier.weight(1f))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = jc.sub, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(10.dp))
            Text(qa.title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = jc.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(qa.sub, color = jc.sub, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    icon: ImageVector,
    onOpen: () -> Unit,
    extra: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    JCard(Modifier.padding(horizontal = 16.dp, vertical = 7.dp)) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onOpen).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, 40.dp)
            Spacer(Modifier.width(10.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = jc.text, modifier = Modifier.weight(1f))
            extra()
            Icon(Icons.Default.ChevronRight, null, tint = Gold.Text)
        }
        Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

@Composable
fun InvoiceTypeDialog(onDismiss: () -> Unit, onPick: (String) -> Unit) {
    JDialog("Create Invoice", Icons.Default.Description, onDismiss) {
        val entries = InvoiceType.entries.map { it.title to "inv/new/${it.name}" } + ("Fixed Amount Invoice" to "fixed")
        entries.forEach { (title, route) ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(jc.surface).clickable { onPick(route) }.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconTile(Icons.Default.Receipt, 38.dp)
                Spacer(Modifier.width(12.dp))
                Text(title, color = jc.text, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null, tint = Gold.Text)
            }
        }
    }
}

@Composable
fun FirstMoveDialog() {
    val nav = LocalNav.current
    fun close() = Store.update { it.copy(firstMoveShown = true) }
    JDialog("Make your first move", Icons.Default.PlayCircle, { close() }) {
        Text("Pick a quick tour to get started. You can always find these under More → Tours.", color = jc.sub)
        TOURS.forEach { t ->
            JCard(onClick = { close(); nav.navigate("tours") }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(t.icon, 42.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t.title, fontWeight = FontWeight.Medium, color = jc.text)
                        Text(t.subtitle, fontSize = 13.sp, color = jc.sub)
                    }
                    Icon(Icons.Default.ChevronRight, null, tint = Gold.Text)
                }
            }
        }
        HorizontalDivider(color = jc.border)
        Text("Skip for now", color = Gold.Text, fontWeight = FontWeight.Medium, modifier = Modifier.clickable { close() }.padding(8.dp))
    }
}
