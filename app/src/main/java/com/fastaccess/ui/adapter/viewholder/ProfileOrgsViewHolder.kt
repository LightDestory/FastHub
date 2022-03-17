package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.User
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 21 May 2017, 12:27 PM
 */
class ProfileOrgsViewHolder private constructor(itemView: View) : BaseViewHolder<User>(itemView) {
    @kotlin.jvm.JvmField
    @BindView(R.id.avatarLayout)
    var avatarLayout: AvatarLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.name)
    var name: FontTextView? = null
    override fun onClick(v: View) {
        avatarLayout!!.callOnClick()
    }

    override fun bind(t: User) {
        name!!.text = t.login
        avatarLayout!!.setUrl(t.avatarUrl, t.login, true, isEnterprise(t.url))
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(parent: ViewGroup): ProfileOrgsViewHolder {
            return ProfileOrgsViewHolder(getView(parent, R.layout.profile_org_row_item))
        }
    }
}