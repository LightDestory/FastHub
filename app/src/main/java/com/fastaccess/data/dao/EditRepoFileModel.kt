package com.fastaccess.data.dao

import android.os.Parcel
import androidx.core.os.ParcelCompat
import com.fastaccess.helper.*

/**
 * Created by Hashemsergani on 01/09/2017.
 */
data class EditRepoFileModel(
    val login: String,
    val repoId: String,
    val path: String?,
    val ref: String,
    val sha: String?,
    val contentUrl: String?,
    val fileName: String?,
    val isEdit: Boolean
) : KotlinParcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        ParcelCompat.readBoolean(parcel)
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(login)
        dest.writeString(repoId)
        dest.writeString(path)
        dest.writeString(ref)
        dest.writeString(sha)
        dest.writeString(contentUrl)
        dest.writeString(fileName)
        ParcelCompat.writeBoolean(dest, isEdit)
    }

    companion object {
        @JvmField val CREATOR = parcelableCreator(::EditRepoFileModel)
    }
}
