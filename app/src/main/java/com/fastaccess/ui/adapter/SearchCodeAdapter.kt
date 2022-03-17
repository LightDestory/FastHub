package com.fastaccess.ui.adapter

import android.annotation.SuppressLint
import android.view.ViewGroup
import com.fastaccess.data.dao.SearchCodeModel
import com.fastaccess.ui.adapter.viewholder.SearchCodeViewHolder
import com.fastaccess.ui.adapter.viewholder.SearchCodeViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class SearchCodeAdapter(data: MutableList<SearchCodeModel>) :
    BaseRecyclerAdapter<SearchCodeModel, SearchCodeViewHolder, BaseViewHolder.OnItemClickListener<SearchCodeModel>>(
        data
    ) {
    private var showRepoName = false
    override fun viewHolder(parent: ViewGroup, viewType: Int): SearchCodeViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: SearchCodeViewHolder, position: Int) {
        holder.bind(getItem(position)!!, showRepoName)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun showRepoName(showRepoName: Boolean) {
        this.showRepoName = showRepoName
        notifyDataSetChanged()
    }
}