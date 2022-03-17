package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.TeamsModel
import com.fastaccess.ui.adapter.viewholder.TeamsViewHolder
import com.fastaccess.ui.adapter.viewholder.TeamsViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 03 Apr 2017, 7:52 PM
 */
class TeamsAdapter(data: MutableList<TeamsModel>) :
    BaseRecyclerAdapter<TeamsModel, TeamsViewHolder, BaseViewHolder.OnItemClickListener<TeamsModel>>(
        data
    ) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): TeamsViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: TeamsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}