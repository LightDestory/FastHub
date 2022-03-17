package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.ui.adapter.viewholder.CommentsViewHolder
import com.fastaccess.ui.adapter.viewholder.CommentsViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class CommentsAdapter(eventsModels: MutableList<Comment>) :
    BaseRecyclerAdapter<Comment, CommentsViewHolder, BaseViewHolder.OnItemClickListener<Comment>>(
        eventsModels
    ) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): CommentsViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: CommentsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}