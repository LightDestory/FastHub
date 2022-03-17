package com.fastaccess.data.dao

import android.os.Parcelable
import android.os.Parcel
import com.fastaccess.helper.InputHelper
import com.fastaccess.ui.widgets.DiffLineSpan
import java.lang.NumberFormatException
import java.util.*
import kotlin.math.abs

/**
 * Created by Kosh on 20 Jun 2017, 7:32 PM
 */
class CommitLinesModel : Parcelable {
    var text: String? = null
    var color: Int
    var leftLineNo: Int
    var rightLineNo: Int
    var isNoNewLine: Boolean
    var position: Int
    var isHasCommentedOn = false

    constructor(
        text: String?,
        color: Int,
        leftLineNo: Int,
        rightLineNo: Int,
        noNewLine: Boolean,
        position: Int, hasCommentedOn: Boolean
    ) {
        this.text = text
        this.color = color
        this.leftLineNo = leftLineNo
        this.rightLineNo = rightLineNo
        isNoNewLine = noNewLine
        this.position = position
        isHasCommentedOn = hasCommentedOn
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(text)
        dest.writeInt(color)
        dest.writeInt(leftLineNo)
        dest.writeInt(rightLineNo)
        dest.writeByte(if (isNoNewLine) 1.toByte() else 0.toByte())
        dest.writeInt(position)
    }

    private constructor(`in`: Parcel) {
        text = `in`.readString()
        color = `in`.readInt()
        leftLineNo = `in`.readInt()
        rightLineNo = `in`.readInt()
        isNoNewLine = `in`.readByte().toInt() != 0
        position = `in`.readInt()
    }

    companion object {
        const val TRANSPARENT = 0
        const val ADDITION = 1
        const val DELETION = 2
        const val PATCH = 3
        fun getLines(text: String?): List<CommitLinesModel> {
            val models = ArrayList<CommitLinesModel>()
            if (!InputHelper.isEmpty(text)) {
                val split = text!!.split("\\r?\\n|\\r".toRegex())
                if (split.size > 1) {
                    var leftLineNo = -1
                    var rightLineNo = -1
                    var position = 0
                    for (_token in split) {
                        var token = _token
                        val firstChar = if (token.isEmpty()) "" else token[0]
                        var addLeft = false
                        var addRight = false
                        var color = TRANSPARENT
                        if (token.startsWith("@@")) {
                            color = PATCH
                            val matcher = DiffLineSpan.HUNK_TITLE.matcher(token.trim { it <= ' ' })
                            if (matcher.matches()) {
                                try {
                                    leftLineNo = abs(
                                        matcher.group(1)!!.toInt()
                                    ) - 1
                                    rightLineNo =
                                        matcher.group(3)!!.toInt() - 1
                                } catch (e: NumberFormatException) {
                                    e.printStackTrace()
                                }
                            }
                        } else if (firstChar == '+') {
                            position++
                            color = ADDITION
                            ++rightLineNo
                            addRight = true
                            addLeft = false
                        } else if (firstChar == '-') {
                            position++
                            color = DELETION
                            ++leftLineNo
                            addRight = false
                            addLeft = true
                        } else {
                            position++
                            addLeft = true
                            addRight = true
                            ++rightLineNo
                            ++leftLineNo
                        }
                        val index = token.indexOf("\\ No newline at end of file")
                        if (index != -1) {
                            token = token.replace("\\ No newline at end of file", "")
                        }
                        models.add(
                            CommitLinesModel(
                                token,
                                color,
                                if (token.startsWith("@@") || !addLeft) -1 else leftLineNo,
                                if (token.startsWith("@@") || !addRight) -1 else rightLineNo,
                                index != -1,
                                position,
                                false
                            )
                        )
                    }
                }
            }
            return models
        }

        @JvmField
        val CREATOR: Parcelable.Creator<CommitLinesModel> =
            object : Parcelable.Creator<CommitLinesModel> {
                override fun createFromParcel(source: Parcel): CommitLinesModel {
                    return CommitLinesModel(source)
                }

                override fun newArray(size: Int): Array<CommitLinesModel?> {
                    return arrayOfNulls(size)
                }
            }
    }
}