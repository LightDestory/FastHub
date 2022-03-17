package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.ReviewCommentModel
import com.fastaccess.ui.adapter.callback.OnToggleView
import com.fastaccess.ui.adapter.callback.ReactionsCallback
import com.fastaccess.ui.adapter.viewholder.ReviewCommentsViewHolder
import com.fastaccess.ui.adapter.viewholder.ReviewCommentsViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class ReviewCommentsAdapter(
    data: MutableList<ReviewCommentModel>,
    listener: BaseViewHolder.OnItemClickListener<ReviewCommentModel>,
    private val onToggleView: OnToggleView,
    private val reactionsCallback: ReactionsCallback,
    private val repoOwner: String,
    private val poster: String
) : BaseRecyclerAdapter<
        ReviewCommentModel,
        ReviewCommentsViewHolder,
        BaseViewHolder.OnItemClickListener<ReviewCommentModel>
        >(
    data,
    listener
) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): ReviewCommentsViewHolder {
        return newInstance(
            parent, this, onToggleView,
            reactionsCallback, repoOwner, poster
        )
    }

    override fun onBindView(holder: ReviewCommentsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}