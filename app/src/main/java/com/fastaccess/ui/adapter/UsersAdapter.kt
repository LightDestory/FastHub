package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.User
import com.fastaccess.ui.adapter.viewholder.UsersViewHolder
import com.fastaccess.ui.adapter.viewholder.UsersViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class UsersAdapter @JvmOverloads constructor(
    list: ArrayList<User>,
    private val isContributor: Boolean = false,
    private val isFilter: Boolean = false
) : BaseRecyclerAdapter<User, UsersViewHolder, BaseViewHolder.OnItemClickListener<User>>(list) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): UsersViewHolder {
        return newInstance(parent, this, isFilter)
    }

    override fun onBindView(holder: UsersViewHolder, position: Int) {
        holder.bind(getItem(position)!!, isContributor)
    }
}