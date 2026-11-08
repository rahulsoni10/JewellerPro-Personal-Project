package com.jeweldesk.app

import android.app.Application
import com.jeweldesk.app.data.Store

class JewelApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Store.init(this)
    }
}
