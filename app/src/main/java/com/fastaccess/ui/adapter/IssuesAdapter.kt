package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.ui.adapter.viewholder.IssuesViewHolder
import com.fastaccess.ui.adapter.viewholder.IssuesViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class IssuesAdapter :
    BaseRecyclerAdapter<Issue, IssuesViewHolder, BaseViewHolder.OnItemClickListener<Issue>> {
    private var withAvatar: Boolean
    private var showRepoName = false
    private var showState = false

    @JvmOverloads
    constructor(data: MutableList<Issue>, withAvatar: Boolean = false) : super(data) {
        this.withAvatar = withAvatar
    }

    constructor(data: MutableList<Issue>, withAvatar: Boolean, showRepoName: Boolean) : super(data) {
        this.withAvatar = withAvatar
        this.showRepoName = showRepoName
    }

    constructor(
        data: MutableList<Issue>,
        withAvatar: Boolean,
        showRepoName: Boolean,
        showState: Boolean
    ) : super(data) {
        this.withAvatar = withAvatar
        this.showRepoName = showRepoName
        this.showState = showState
    }

    override fun viewHolder(parent: ViewGroup, viewType: Int): IssuesViewHolder {
        return newInstance(parent, this, withAvatar, showRepoName, showState)
    }

    override fun onBindView(holder: IssuesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}