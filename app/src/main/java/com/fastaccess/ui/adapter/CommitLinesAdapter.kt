package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.CommitLinesModel
import com.fastaccess.ui.adapter.viewholder.CommitLinesViewHolder
import com.fastaccess.ui.adapter.viewholder.CommitLinesViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

class CommitLinesAdapter(
    data: MutableList<CommitLinesModel>,
    listener: BaseViewHolder.OnItemClickListener<CommitLinesModel>?
) : BaseRecyclerAdapter<CommitLinesModel, CommitLinesViewHolder, BaseViewHolder.OnItemClickListener<CommitLinesModel>>(
    data,
    listener
) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): CommitLinesViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: CommitLinesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}