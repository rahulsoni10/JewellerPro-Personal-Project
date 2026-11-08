package com.jeweldesk.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jeweldesk.app.data.AppData
import com.jeweldesk.app.data.Book
import com.jeweldesk.app.data.InvoiceType
import com.jeweldesk.app.data.Metal
import com.jeweldesk.app.data.PartyType
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.ui.screens.*

@Composable
fun appData(): AppData = Store.data.collectAsState().value

/** Standard page: pinned header, scrollable body, optional floating action at bottom-end. */
@Composable
fun Page(
    header: @Composable () -> Unit,
    fab: (@Composable () -> Unit)? = null,
    navPadding: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    Column(Modifier.fillMaxSize().background(jc.bg).then(if (navPadding) Modifier.navigationBarsPadding() else Modifier).imePadding()) {
        header()
        Box(Modifier.weight(1f)) {
            content()
            if (fab != null) Box(Modifier.align(Alignment.BottomEnd).padding(18.dp)) { fab() }
        }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    val start = remember { val d = Store.value; if (d.loggedIn && d.profile != null) "main" else "login" }
    CompositionLocalProvider(LocalNav provides nav) {
        NavHost(nav, startDestination = start) {
            composable("login") { LoginScreen() }
            composable("setup/{mobile}") { SetupScreen(it.arguments?.getString("mobile") ?: "") }
            composable("main?tab={tab}", listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })) {
                MainScreen(it.arguments?.getInt("tab") ?: 0)
            }
            composable("inv/new/{type}?party={party}", listOf(navArgument("party") { defaultValue = "" })) {
                InvoiceFormScreen(InvoiceType.valueOf(it.arguments?.getString("type")!!), null, it.arguments?.getString("party") ?: "")
            }
            composable("inv/edit/{id}") { InvoiceFormScreen(null, it.arguments?.getString("id"), "") }
            composable("inv/view/{id}") { InvoiceViewScreen(it.arguments?.getString("id")!!) }
            composable("fixed") { FixedAmountScreen() }
            composable("prodcat/{metal}/{cat}") {
                CategoryProductsScreen(Metal.valueOf(it.arguments?.getString("metal")!!), it.arguments?.getString("cat")!!)
            }
            composable("party/{id}") { PartyViewScreen(it.arguments?.getString("id")!!) }
            composable("partyedit/{type}/{id}") {
                PartyEditScreen(PartyType.valueOf(it.arguments?.getString("type")!!), it.arguments?.getString("id")!!)
            }
            composable(
                "khata?tab={tab}&add={add}",
                listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 }, navArgument("add") { type = NavType.BoolType; defaultValue = false }),
            ) { KhataScreen(it.arguments?.getInt("tab") ?: 0, it.arguments?.getBoolean("add") ?: false) }
            composable("expense?add={add}", listOf(navArgument("add") { type = NavType.BoolType; defaultValue = false })) {
                ExpenseScreen(it.arguments?.getBoolean("add") ?: false)
            }
            composable("girvi?mode={mode}", listOf(navArgument("mode") { defaultValue = "list" })) {
                GirviListScreen(it.arguments?.getString("mode") ?: "list")
            }
            composable("girviedit/{id}") { GirviEditScreen(it.arguments?.getString("id")!!) }
            composable("girviview/{id}") { GirviViewScreen(it.arguments?.getString("id")!!) }
            composable("rates") { RatesScreen() }
            composable("balance") { BalanceScreen() }
            composable("reports?tab={tab}", listOf(navArgument("tab") { type = NavType.IntType; defaultValue = 0 })) {
                ReportsScreen(it.arguments?.getInt("tab") ?: 0)
            }
            composable("interest") { InterestCalcScreen() }
            composable("set/addons") { AddonsScreen() }
            composable("set/business") { BusinessScreen() }
            composable("set/invoice/{book}") { InvoiceSettingsScreen(Book.valueOf(it.arguments?.getString("book")!!)) }
            composable("set/tag") { TagSettingsScreen() }
            composable("set/girvi") { GirviSettingsScreen() }
            composable("set/gstpass") { GstPasswordScreen() }
            composable("set/language") { LanguageScreen() }
            composable("set/import") { ImportScreen() }
            composable("set/backup") { BackupScreen() }
            composable("info/{key}") { InfoScreen(it.arguments?.getString("key")!!) }
            composable("tours") { ToursScreen() }
        }
    }
}
