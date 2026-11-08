package com.jeweldesk.app.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.data.sha256
import com.jeweldesk.app.ui.GoldButton
import com.jeweldesk.app.ui.JDialog
import com.jeweldesk.app.ui.JField
import com.jeweldesk.app.ui.jc
import com.jeweldesk.app.ui.toast

object GstGate {
    /** Unlocked for the lifetime of the process once the password is entered. */
    var unlocked = false
}

/**
 * Returns a runner that executes the given action immediately when no GST password is set (or it
 * was already entered), otherwise asks for the password first.
 */
@Composable
fun rememberGstGate(): (() -> Unit) -> Unit {
    val ctx = LocalContext.current
    var pending by remember { mutableStateOf<(() -> Unit)?>(null) }
    var pass by remember { mutableStateOf("") }
    pending?.let { action ->
        JDialog("GST Password", Icons.Default.Lock, { pending = null; pass = "" }) {
            Text("Enter your GST password to continue.", color = jc.sub)
            JField(pass, { pass = it }, "Password", icon = Icons.Default.Lock, keyboard = KeyboardType.NumberPassword, password = true)
            GoldButton("Unlock") {
                if (sha256(pass) == Store.value.gstPasswordHash) {
                    GstGate.unlocked = true; pending = null; pass = ""; action()
                } else ctx.toast("Wrong password")
            }
        }
    }
    return { action ->
        if (Store.value.gstPasswordHash.isBlank() || GstGate.unlocked) action() else pending = action
    }
}
