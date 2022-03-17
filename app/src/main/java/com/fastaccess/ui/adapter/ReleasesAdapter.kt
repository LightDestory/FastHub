package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.Release
import com.fastaccess.ui.adapter.viewholder.ReleasesViewHolder
import com.fastaccess.ui.adapter.viewholder.ReleasesViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class ReleasesAdapter(data: MutableList<Release>) :
    BaseRecyclerAdapter<Release, ReleasesViewHolder, BaseViewHolder.OnItemClickListener<Release>>(
        data
    ) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): ReleasesViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: ReleasesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}