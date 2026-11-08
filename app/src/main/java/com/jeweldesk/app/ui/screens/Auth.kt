package com.jeweldesk.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jeweldesk.app.data.BUSINESS_TYPES
import com.jeweldesk.app.data.INDIAN_STATES
import com.jeweldesk.app.data.Profile
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.ui.DropField
import com.jeweldesk.app.ui.GoldButton
import com.jeweldesk.app.ui.JField
import com.jeweldesk.app.ui.LocalNav
import com.jeweldesk.app.ui.RadioCard
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.theme.Gold
import com.jeweldesk.app.ui.toast

private val GSTIN_RE = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")

@Composable
fun LoginScreen() {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    var mobile by rememberSaveable { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().background(jc.bg).statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(60.dp))
        Box(Modifier.size(96.dp).clip(RoundedCornerShape(28.dp)).background(Gold.brush), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Diamond, null, tint = Color.Black, modifier = Modifier.size(52.dp))
        }
        Spacer(Modifier.height(20.dp))
        Text("JewelDesk", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = jc.text)
        Text("Billing, stock, khata & girvi for jewellers", color = jc.sub, textAlign = TextAlign.Center)
        Spacer(Modifier.height(48.dp))
        Text("Login with your mobile number", fontSize = 18.sp, fontWeight = FontWeight.Medium, color = jc.text, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        JField(mobile, { s -> mobile = s.filter { it.isDigit() }.take(10) }, "Mobile Number", icon = Icons.Default.Phone, keyboard = KeyboardType.Phone)
        Spacer(Modifier.height(20.dp))
        GoldButton("Continue", enabled = mobile.length == 10) {
            val d = Store.value
            if (d.profile != null && d.profile.mobile == mobile) {
                Store.update { it.copy(loggedIn = true) }
                nav.navigate("main") { popUpTo(0) }
            } else if (d.profile != null) {
                ctx.toast("This device is registered to another number. Delete the account from Settings to switch.")
            } else nav.navigate("setup/$mobile")
        }
        Spacer(Modifier.height(16.dp))
        Text(
            "Your data is stored securely on this device. Cloud sync can be enabled later.",
            color = jc.sub, fontSize = 13.sp, textAlign = TextAlign.Center,
        )
    }
}

@Composable
fun SetupScreen(mobile: String) {
    val nav = LocalNav.current
    val ctx = LocalContext.current
    var owner by rememberSaveable { mutableStateOf("") }
    var business by rememberSaveable { mutableStateOf("") }
    var gstin by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var pincode by rememberSaveable { mutableStateOf("") }
    var city by rememberSaveable { mutableStateOf("") }
    var state by rememberSaveable { mutableStateOf("") }
    var type by rememberSaveable { mutableStateOf(BUSINESS_TYPES[0]) }
    var gold by rememberSaveable { mutableStateOf(true) }
    var silver by rememberSaveable { mutableStateOf(true) }
    Column(
        Modifier.fillMaxSize().background(jc.bg).statusBarsPadding().navigationBarsPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text("Set up your business", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = jc.text)
        Text("These details appear on your invoices. You can change them later.", color = jc.sub)
        JField(owner, { owner = it }, "Owner Name *", icon = Icons.Default.Person)
        JField(business, { business = it }, "Business Name *", icon = Icons.Default.Storefront)
        JField(gstin, { gstin = it.uppercase().take(15) }, "GSTIN (optional)", icon = Icons.Default.Receipt)
        JField(address, { address = it }, "Address", icon = Icons.Outlined.Home)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            JField(pincode, { s -> pincode = s.filter { it.isDigit() }.take(6) }, "Pincode", Modifier.weight(1f), Icons.Default.PinDrop, KeyboardType.Number)
            JField(city, { city = it }, "City", Modifier.weight(1f), Icons.Default.LocationCity)
        }
        DropField("State", state, INDIAN_STATES, { it }, { state = it }, icon = Icons.Default.Map)
        DropField("Business Type", type, BUSINESS_TYPES, { it }, { type = it }, icon = Icons.Default.Business)
        Text("I deal in", color = jc.text, fontWeight = FontWeight.Medium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RadioCard("Gold", gold, Modifier.weight(1f)) { gold = !gold || !silver }
            RadioCard("Silver", silver, Modifier.weight(1f)) { silver = !silver || !gold }
        }
        Spacer(Modifier.height(4.dp))
        GoldButton("Get Started", enabled = owner.isNotBlank() && business.isNotBlank()) {
            if (gstin.isNotEmpty() && !GSTIN_RE.matches(gstin)) { ctx.toast("Invalid GSTIN format"); return@GoldButton }
            val p = Profile(
                mobile = mobile, ownerName = owner.trim(), businessName = business.trim(), gstin = gstin, address = address.trim(),
                pincode = pincode, city = city.trim(), state = state, businessType = type, dealsGold = gold, dealsSilver = silver,
            )
            val initial = business.trim().take(1).uppercase()
            Store.update {
                it.copy(
                    profile = p, loggedIn = true,
                    gstInvoice = it.gstInvoice.copy(companyInitial = initial, invoicePhone = mobile),
                    estInvoice = it.estInvoice.copy(companyInitial = initial, invoicePhone = mobile),
                )
            }
            nav.navigate("main") { popUpTo(0) }
        }
    }
}

fun isValidGstin(s: String) = GSTIN_RE.matches(s)
