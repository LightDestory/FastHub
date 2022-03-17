package com.fastaccess.data.dao

import android.os.Parcelable
import android.os.Parcel
import com.fastaccess.data.dao.CreateIssueModel
import java.util.ArrayList

/**
 * Created by Kosh on 19 Feb 2017, 12:13 PM
 */
open class CreateIssueModel : Parcelable {
    var title: String? = null
    var body: String? = null
    var labels: ArrayList<String>? = null
    var assignees: ArrayList<String>? = null
    var milestone: Long? = null

    constructor()

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(title)
        dest.writeString(body)
        dest.writeStringList(labels)
        dest.writeStringList(assignees)
        dest.writeValue(milestone)
    }

    protected constructor(`in`: Parcel) {
        title = `in`.readString()
        body = `in`.readString()
        labels = `in`.createStringArrayList()
        assignees = `in`.createStringArrayList()
        milestone = `in`.readValue(Long::class.java.classLoader) as Long?
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CreateIssueModel> =
            object : Parcelable.Creator<CreateIssueModel> {
                override fun createFromParcel(source: Parcel): CreateIssueModel {
                    return CreateIssueModel(source)
                }

                override fun newArray(size: Int): Array<CreateIssueModel?> {
                    return arrayOfNulls(size)
                }
            }
    }
}