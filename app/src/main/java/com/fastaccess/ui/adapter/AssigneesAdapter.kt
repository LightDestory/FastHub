package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.User
import com.fastaccess.ui.adapter.viewholder.AssigneesViewHolder
import com.fastaccess.ui.adapter.viewholder.AssigneesViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class AssigneesAdapter(
    data: MutableList<User>,
    private val onSelectAssignee: OnSelectAssignee?
) :
    BaseRecyclerAdapter<User, AssigneesViewHolder, BaseViewHolder.OnItemClickListener<User>>(
        data
    ) {
    interface OnSelectAssignee {
        fun isAssigneeSelected(position: Int): Boolean
        fun onToggleSelection(position: Int, select: Boolean)
    }

    override fun viewHolder(parent: ViewGroup, viewType: Int): AssigneesViewHolder {
        return newInstance(parent, onSelectAssignee, this)
    }

    override fun onBindView(holder: AssigneesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}