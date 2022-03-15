package com.fastaccess.helper

import android.content.Context
import com.fastaccess.helper.InputHelper.isEmpty
import com.tencent.mmkv.MMKV

/**
 * Created by kosh20111 on 19 Feb 2017, 2:01 AM
 */
object PrefHelper {
    @JvmStatic
    var instance: MMKV? = null
        private set

    @JvmStatic
    fun init(context: Context?) {
        MMKV.initialize(context)
        instance = MMKV.mmkvWithID("preferences")
    }

    @JvmStatic
    fun getAll(): MutableMap<String, *> {
        val keys = instance!!.allKeys()
        val map = mutableMapOf<String, Any>()
        keys?.forEach {
            if (it.contains("@")) {
                val all = it.split("@")
                val type = all.first()
                val key = all.last()
                val value: Any? = when (type) {
                    "s" -> instance!!.getString(it, "") ?: ""
                    "i" -> instance!!.getInt(it, 0)
                    "l" -> instance!!.getLong(it, 0L)
                    "f" -> instance!!.getFloat(it, 0f)
                    "b" -> instance!!.getBoolean(it, false)
                    "ss" -> instance!!.getStringSet(it, setOf())
                    else -> null
                }
                value?.let {
                    map[key] = value
                }
            }
        }
        return map
    }


    @JvmStatic
    inline fun <reified T> getTypeKey(key: String?): String {
        key ?: return ""
        if (key.contains("@")) {
            return key
        }
        val type = when (T::class.simpleName) {
            String::class.simpleName -> "s"
            Int::class.simpleName -> "i"
            Long::class.simpleName -> "l"
            Float::class.simpleName -> "f"
            Boolean::class.simpleName -> "b"
            Set::class.simpleName -> "ss"
            else -> "?"
        } + "@"
        return type + key
    }

    /**
     * @param key   ( the Key to used to retrieve this data later  )
     * @param value ( any kind of primitive values  )
     *
     *
     * non can be null!!!
     */
    fun <T> putAny(key: String, value: T?) {
        if (isEmpty(key)) {
            throw NullPointerException("Key must not be null! (key = $key), (value = $value)")
        }
        val edit = instance!!
        if (value == null || isEmpty(value.toString())) {
            return
        }
        when (value) {
            is String -> {
                edit.putString(getTypeKey<String>(key), value as String?)
            }
            is Int -> {
                edit.putInt(getTypeKey<Int>(key), (value as Int?)!!)
            }
            is Long -> {
                edit.putLong(getTypeKey<Long>(key), (value as Long?)!!)
            }
            is Boolean -> {
                edit.putBoolean(getTypeKey<Boolean>(key), (value as Boolean?)!!)
            }
            is Float -> {
                edit.putFloat(getTypeKey<Float>(key), (value as Float?)!!)
            }
            else -> {
                edit.putString(getTypeKey<String>(key), value.toString())
            }
        }
    }

    fun getString(key: String): String? {
        return instance!!.getString(getTypeKey<String>(key), null)
    }

    fun getBoolean(key: String): Boolean {
        return instance!!.getBoolean(getTypeKey<Boolean>(key), false)
    }

    fun getInt(key: String): Int {
        return instance!!.getInt(getTypeKey<Int>(key), 0)
    }

    fun getLong(key: String): Long {
        return instance!!.getLong(getTypeKey<Long>(key), 0)
    }

    fun getFloat(key: String): Float {
        return instance!!.getFloat(getTypeKey<Float>(key), 0f)
    }

    inline fun <reified T> clearKey(key: String) {
        instance!!.remove(getTypeKey<T>(key))
    }

    inline fun <reified T> isExist(key: String): Boolean {
        return instance!!.contains(getTypeKey<T>(key))
    }

    fun clearPrefs() {
        instance!!.clear()
    }
}