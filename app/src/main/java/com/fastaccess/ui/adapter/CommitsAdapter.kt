package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.Commit
import com.fastaccess.ui.adapter.viewholder.CommitsViewHolder
import com.fastaccess.ui.adapter.viewholder.CommitsViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class CommitsAdapter(data: MutableList<Commit>) :
    BaseRecyclerAdapter<Commit, CommitsViewHolder, BaseViewHolder.OnItemClickListener<Commit>>(
        data
    ) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): CommitsViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: CommitsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}