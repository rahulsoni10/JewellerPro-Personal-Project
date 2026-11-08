package com.jeweldesk.app.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

/**
 * Local document store. Everything lives in one JSON document that is kept in memory and
 * persisted atomically. The shape intentionally mirrors what a Firestore sync would use later.
 */
object Store {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        coerceInputValues = true
        allowStructuredMapKeys = true
    }

    private lateinit var file: File
    private val _data = MutableStateFlow(AppData())
    val data: StateFlow<AppData> = _data
    val value: AppData get() = _data.value

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val lock = Mutex()

    fun init(context: Context) {
        file = File(context.filesDir, "jeweldesk.json")
        val backup = File(context.filesDir, "jeweldesk.json.bak")
        val loaded = listOf(file, backup).firstNotNullOfOrNull { f ->
            if (!f.exists()) null else runCatching { json.decodeFromString(AppData.serializer(), f.readText()) }.getOrNull()
        }
        if (loaded != null) _data.value = loaded
    }

    fun update(block: (AppData) -> AppData) {
        _data.update(block)
        persist()
    }

    fun replaceAll(newData: AppData) {
        _data.value = newData
        persist()
    }

    fun exportJson(): String = json.encodeToString(AppData.serializer(), _data.value)

    fun importJson(text: String): Boolean {
        val parsed = runCatching { json.decodeFromString(AppData.serializer(), text) }.getOrNull() ?: return false
        replaceAll(parsed)
        return true
    }

    private fun persist() {
        scope.launch {
            lock.withLock {
                val text = json.encodeToString(AppData.serializer(), _data.value)
                val tmp = File(file.parentFile, file.name + ".tmp")
                tmp.writeText(text)
                if (file.exists()) file.copyTo(File(file.parentFile, file.name + ".bak"), overwrite = true)
                if (!tmp.renameTo(file)) {
                    file.writeText(text)
                    tmp.delete()
                }
            }
        }
    }
}

fun newId(): String = UUID.randomUUID().toString()
fun now(): Long = System.currentTimeMillis()
