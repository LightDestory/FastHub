package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.CommitFileChanges
import com.fastaccess.ui.adapter.callback.OnToggleView
import com.fastaccess.ui.adapter.viewholder.PullRequestFilesViewHolder
import com.fastaccess.ui.adapter.viewholder.PullRequestFilesViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.modules.repos.pull_requests.pull_request.details.files.PullRequestFilesMvp.OnPatchClickListener

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class CommitFilesAdapter(
    eventsModels: ArrayList<CommitFileChanges>,
    private val onToggleView: OnToggleView,
    private val onPatchClickListener: OnPatchClickListener?
) : BaseRecyclerAdapter<CommitFileChanges, PullRequestFilesViewHolder, BaseViewHolder.OnItemClickListener<CommitFileChanges>>(
    eventsModels
) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): PullRequestFilesViewHolder {
        return newInstance(parent, this, onToggleView, onPatchClickListener)
    }

    override fun onBindView(holder: PullRequestFilesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}