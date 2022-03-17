package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.MilestoneModel
import com.fastaccess.ui.adapter.viewholder.MilestonesViewHolder
import com.fastaccess.ui.adapter.viewholder.MilestonesViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class MilestonesAdapter(eventsModels: ArrayList<MilestoneModel>) :
    BaseRecyclerAdapter<MilestoneModel, MilestonesViewHolder, BaseViewHolder.OnItemClickListener<MilestoneModel>>(
        eventsModels
    ) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): MilestonesViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: MilestonesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}