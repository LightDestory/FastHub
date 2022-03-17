package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.Repo
import com.fastaccess.ui.adapter.viewholder.ReposViewHolder
import com.fastaccess.ui.adapter.viewholder.ReposViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class ReposAdapter @JvmOverloads constructor(
    data: MutableList<Repo>,
    private val isStarred: Boolean,
    private val withImage: Boolean = false
) : BaseRecyclerAdapter<Repo, ReposViewHolder, BaseViewHolder.OnItemClickListener<Repo>>(data) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): ReposViewHolder {
        return newInstance(parent, this, isStarred, withImage)
    }

    override fun onBindView(holder: ReposViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}