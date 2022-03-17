package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.ui.adapter.viewholder.GistsViewHolder
import com.fastaccess.ui.adapter.viewholder.GistsViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class GistsAdapter(gistsModels: ArrayList<Gist>, private val isForProfile: Boolean) :
    BaseRecyclerAdapter<Gist, GistsViewHolder, BaseViewHolder.OnItemClickListener<Gist>>(
        gistsModels
    ) {
    constructor(gistModels: ArrayList<Gist>) : this(gistModels, false)

    override fun viewHolder(parent: ViewGroup, viewType: Int): GistsViewHolder {
        return newInstance(parent, this, isForProfile)
    }

    override fun onBindView(holder: GistsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}