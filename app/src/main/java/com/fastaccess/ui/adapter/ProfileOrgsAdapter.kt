package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.User
import com.fastaccess.ui.adapter.viewholder.ProfileOrgsViewHolder
import com.fastaccess.ui.adapter.viewholder.ProfileOrgsViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 21 May 2017, 12:30 PM
 */
class ProfileOrgsAdapter :
    BaseRecyclerAdapter<User, ProfileOrgsViewHolder, BaseViewHolder.OnItemClickListener<User>>() {
    override fun viewHolder(parent: ViewGroup, viewType: Int): ProfileOrgsViewHolder {
        return newInstance(parent)
    }

    override fun onBindView(holder: ProfileOrgsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}