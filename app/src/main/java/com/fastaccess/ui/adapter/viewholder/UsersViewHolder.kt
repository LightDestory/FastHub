package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import com.fastaccess.R
import com.fastaccess.data.dao.model.User
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.adapter.UsersAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class UsersViewHolder private constructor(
    itemView: View,
    adapter: UsersAdapter,
    private val isFilter: Boolean
) : BaseViewHolder<User>(itemView, adapter) {
    var avatar: AvatarLayout = itemView.findViewById(R.id.avatarLayout)
    var title: FontTextView = itemView.findViewById(R.id.title)
    var date: FontTextView = itemView.findViewById(R.id.date)
    override fun onClick(v: View) {
        if (isFilter) {
            super.onClick(v)
        } else {
            avatar.findViewById<View>(R.id.avatar).callOnClick()
        }
    }

    override fun bind(t: User) {}
    fun bind(user: User, isContributor: Boolean) {
        avatar.setUrl(
            user.avatarUrl, user.login, user.isOrganizationType,
            isEnterprise(user.htmlUrl)
        )
        title.text = user.login
        date.visibility = if (!isContributor) View.GONE else View.VISIBLE
        if (isContributor) {
            date.text = String.format(
                "%s (%s)",
                date.resources.getString(R.string.commits),
                user.contributions
            )
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            parent: ViewGroup,
            adapter: UsersAdapter,
            isFilter: Boolean
        ): UsersViewHolder {
            return UsersViewHolder(
                getView(
                    parent,
                    if (isFilter) R.layout.users_small_row_item else R.layout.feeds_row_item
                ), adapter, isFilter
            )
        }
    }

}