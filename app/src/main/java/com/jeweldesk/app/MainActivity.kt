package com.jeweldesk.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.jeweldesk.app.data.Store
import com.jeweldesk.app.ui.AppNav
import com.jeweldesk.app.ui.theme.JewelTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val data by Store.data.collectAsState()
            JewelTheme(data.darkMode) { AppNav() }
        }
    }
}
