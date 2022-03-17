package com.fastaccess.helper

import androidx.preference.PreferenceDataStore
import com.tencent.mmkv.MMKV

class SettingsDataStore(val mmkv: MMKV) : PreferenceDataStore() {
    companion object {
        var instance: SettingsDataStore? = null
        @JvmStatic
        fun init() {
            instance = SettingsDataStore(PrefHelper.instance!!)
        }
    }

    private inline fun <reified T> getTypeKey(key: String?): String {
        return PrefHelper.getTypeKey<T>(key)
    }

    fun getAll(): MutableMap<String, *> {
        val keys = mmkv.allKeys()
        val map = mutableMapOf<String, Any>()
        keys?.forEach {
            if (it.contains("@")) {
                val all = it.split("@")
                val type = all.first()
                val key = all.last()
                val value: Any? = when (type) {
                    "s" -> mmkv.getString(it, "") ?: ""
                    "i" -> mmkv.getInt(it, 0)
                    "l" -> mmkv.getLong(it, 0L)
                    "f" -> mmkv.getFloat(it, 0f)
                    "b" -> mmkv.getBoolean(it, false)
                    "ss" -> mmkv.getStringSet(it, setOf())
                    else -> null
                }
                value?.let {
                    map[key] = value
                }
            }
        }
        return map
    }

    @Suppress("UNCHECKED_CAST")
    fun putAny(key: String, value: Any?) {
        when (value) {
            is String -> putString(key, value)
            is Int -> putInt(key, value)
            is Long -> putLong(key, value)
            is Float -> putFloat(key, value)
            is Boolean -> putBoolean(key, value)
            is Set<*> -> putStringSet(key, value as Set<String>)
            is List<*> -> putStringSet(key, (value as List<String>).toSet())
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return mmkv.getString(getTypeKey<String>(key), defValue)
    }

    override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? {
        return mmkv.getStringSet(getTypeKey<Set<String>>(key), defValues)
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return mmkv.getInt(getTypeKey<Int>(key), defValue)
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return mmkv.getLong(getTypeKey<Long>(key), defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return mmkv.getFloat(getTypeKey<Float>(key), defValue)
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return mmkv.getBoolean(getTypeKey<Boolean>(key), defValue)
    }

    override fun putString(key: String, value: String?) {
        mmkv.putString(getTypeKey<String>(key), value)
    }

    override fun putStringSet(key: String?, values: Set<String>?) {
        mmkv.putStringSet(getTypeKey<Set<String>>(key), values)
    }

    override fun putInt(key: String?, value: Int) {
        mmkv.putInt(getTypeKey<Int>(key), value)
    }

    override fun putLong(key: String?, value: Long) {
        mmkv.putLong(getTypeKey<Long>(key), value)
    }

    override fun putFloat(key: String?, value: Float) {
        mmkv.putFloat(getTypeKey<Float>(key), value)
    }

    override fun putBoolean(key: String?, value: Boolean) {
        mmkv.putBoolean(getTypeKey<Boolean>(key), value)
    }
}