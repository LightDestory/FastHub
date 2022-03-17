package com.fastaccess.ui.adapter.viewholder

import android.annotation.SuppressLint
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import com.fastaccess.R
import com.fastaccess.data.dao.model.Login
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 09 Jul 2017, 4:54 PM
 */

class LoginViewHolder private constructor(itemView: View, adapter: BaseRecyclerAdapter<Login, LoginViewHolder, OnItemClickListener<Login>>) :
        BaseViewHolder<Login>(itemView, adapter) {

    val avatarLayout: AvatarLayout? by lazy { itemView.findViewById(R.id.avatarLayout) }
    val title: FontTextView by lazy { itemView.findViewById(R.id.title) }

    @SuppressLint("SetTextI18n")
    override fun bind(t: Login) {
        avatarLayout?.setUrl(t.avatarUrl, null, false, false)
        title.text = if (t.isIsEnterprise) {
            val uri: String? = Uri.parse(t.enterpriseUrl).authority
            "${t.login} ${if (uri.isNullOrBlank()) t.enterpriseUrl else uri}"
        } else {
            t.login
        }
    }

    companion object {
        fun newInstance(parent: ViewGroup, adapter: BaseRecyclerAdapter<Login, LoginViewHolder, OnItemClickListener<Login>>, small: Boolean): LoginViewHolder {
            return LoginViewHolder(getView(parent, if (small) R.layout.login_row_item_menu else R.layout.login_row_item), adapter)
        }
    }
}
