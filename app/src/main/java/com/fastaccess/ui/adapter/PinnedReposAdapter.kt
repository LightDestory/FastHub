package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.PinnedRepos
import com.fastaccess.ui.adapter.viewholder.PinnedReposViewHolder
import com.fastaccess.ui.adapter.viewholder.PinnedReposViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class PinnedReposAdapter :
    BaseRecyclerAdapter<PinnedRepos, PinnedReposViewHolder, BaseViewHolder.OnItemClickListener<PinnedRepos>> {
    private var singleLine = false

    constructor(singleLine: Boolean) : super() {
        this.singleLine = singleLine
    }

    constructor(
        data: MutableList<PinnedRepos>,
        listener: BaseViewHolder.OnItemClickListener<PinnedRepos>?
    ) : super(data, listener) {
    }

    override fun viewHolder(parent: ViewGroup, viewType: Int): PinnedReposViewHolder {
        return newInstance(parent, this, singleLine)
    }

    override fun onBindView(holder: PinnedReposViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}