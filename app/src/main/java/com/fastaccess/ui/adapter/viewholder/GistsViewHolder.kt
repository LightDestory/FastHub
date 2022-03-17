package com.fastaccess.ui.adapter.viewholder

import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.base.adapter.BaseViewHolder
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import android.view.ViewGroup
import android.view.View
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.ui.adapter.*

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class GistsViewHolder private constructor(
    itemView: View,
    adapter: GistsAdapter?,
    isFromProfile: Boolean
) : BaseViewHolder<Gist>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.avatarLayout)
    var avatar: AvatarLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.title)
    var title: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.date)
    var date: FontTextView? = null
    private val isFromProfile: Boolean
    override fun bind(t: Gist) {
        if (!isFromProfile) {
            if (avatar != null) {
                val url =
                    if (t.owner != null) t.owner.avatarUrl else (if (t.user != null) t.user.avatarUrl else null)!!
                val login =
                    if (t.owner != null) t.owner.login else (if (t.user != null) t.user.login else null)!!
                avatar!!.setUrl(
                    url, login, false, isEnterprise(
                        if (t.owner != null) t.owner.htmlUrl else if (t.user != null) t.user.htmlUrl else null
                    )
                )
            }
        }
        title!!.text = t.getDisplayTitle(isFromProfile)
        date!!.text = getTimeAgo(t.createdAt)
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: GistsAdapter?,
            isFromProfile: Boolean
        ): GistsViewHolder {
            return if (!isFromProfile) {
                GistsViewHolder(
                    getView(viewGroup, R.layout.feeds_row_item),
                    adapter,
                    false
                )
            } else {
                GistsViewHolder(
                    getView(
                        viewGroup,
                        R.layout.feeds_row_no_image_item
                    ), adapter, true
                )
            }
        }
    }

    init {
        title!!.maxLines = 2
        this.isFromProfile = isFromProfile
    }
}