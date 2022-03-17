package com.fastaccess.data.dao.converters

import com.fastaccess.provider.rest.RestProvider
import io.requery.Converter

/**
 * Created by Kosh on 15 Mar 2017, 8:02 PM
 */
abstract class BaseConverter<C> : Converter<C, String> {
    protected abstract val typeClass: Class<out C>

    @Suppress("UNCHECKED_CAST")
    override fun getMappedType(): Class<C> {
        return typeClass as Class<C>
    }

    override fun getPersistedType(): Class<String> {
        return String::class.java
    }

    override fun getPersistedSize(): Int? {
        return null
    }

    override fun convertToPersisted(value: C): String {
        return RestProvider.gson.toJson(value)
    }

    override fun convertToMapped(type: Class<out C>?, value: String?): C {
        return RestProvider.gson.fromJson(value, type)
    }
}