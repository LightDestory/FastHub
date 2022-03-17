package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.RepoFile
import com.fastaccess.ui.adapter.viewholder.RepoFilesViewHolder
import com.fastaccess.ui.adapter.viewholder.RepoFilesViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class RepoFilesAdapter(eventsModels: ArrayList<RepoFile>) :
    BaseRecyclerAdapter<RepoFile, RepoFilesViewHolder, BaseViewHolder.OnItemClickListener<RepoFile>>(
        eventsModels
    ) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): RepoFilesViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: RepoFilesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}