package com.fastaccess.data.dao.wiki

import android.os.Parcel
import com.fastaccess.helper.KotlinParcelable
import com.fastaccess.helper.parcelableCreator

/**
 * Created by Kosh on 13 Jun 2017, 8:03 PM
 */
data class WikiSideBarModel(
    val title: String = "",
    val link: String = "",
    var hash: String? = null,
    var level: Int = 0,
) : KotlinParcelable {
    val id: Int
        get() = title.hashCode()

    companion object {
        @JvmField
        val CREATOR = parcelableCreator(::WikiSideBarModel)
    }

    constructor(source: Parcel) : this(
        source.readString() ?: "",
        source.readString() ?: "",
        source.readString(),
        source.readInt(),
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(title)
        writeString(link)
        writeString(hash)
        writeInt(level)
    }
}
