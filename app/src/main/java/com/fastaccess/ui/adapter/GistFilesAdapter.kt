package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.FilesListModel
import com.fastaccess.ui.adapter.viewholder.GistFilesViewHolder
import com.fastaccess.ui.adapter.viewholder.GistFilesViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class GistFilesAdapter(
    data: MutableList<FilesListModel>,
    listener: BaseViewHolder.OnItemClickListener<FilesListModel>,
    private var isOwner: Boolean
) : BaseRecyclerAdapter<FilesListModel, GistFilesViewHolder, BaseViewHolder.OnItemClickListener<FilesListModel>>(
    data,
    listener
) {
    fun setOwner(owner: Boolean) {
        isOwner = owner
    }

    override fun viewHolder(parent: ViewGroup, viewType: Int): GistFilesViewHolder {
        return newInstance(parent, this, isOwner)
    }

    override fun onBindView(holder: GistFilesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}