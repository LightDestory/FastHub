package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.ui.adapter.viewholder.PullRequestViewHolder
import com.fastaccess.ui.adapter.viewholder.PullRequestViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class PullRequestAdapter :
    BaseRecyclerAdapter<PullRequest, PullRequestViewHolder, BaseViewHolder.OnItemClickListener<PullRequest>> {
    private var showRepoName = false
    private var withAvatar: Boolean

    @JvmOverloads
    constructor(data: MutableList<PullRequest>, withAvatar: Boolean = false) : super(data) {
        this.withAvatar = withAvatar
    }

    constructor(
        data: MutableList<PullRequest>,
        withAvatar: Boolean,
        showRepoName: Boolean
    ) : super(data) {
        this.withAvatar = withAvatar
        this.showRepoName = showRepoName
    }

    override fun viewHolder(parent: ViewGroup, viewType: Int): PullRequestViewHolder {
        return newInstance(parent, this, withAvatar, showRepoName)
    }

    override fun onBindView(holder: PullRequestViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}