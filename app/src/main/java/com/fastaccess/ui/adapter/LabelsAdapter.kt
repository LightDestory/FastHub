package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.data.dao.LabelModel
import com.fastaccess.ui.adapter.viewholder.LabelsViewHolder
import com.fastaccess.ui.adapter.viewholder.LabelsViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 11 Nov 2016, 2:07 PM
 */
class LabelsAdapter(eventsModels: MutableList<LabelModel>, private val onSelectLabel: OnSelectLabel?) :
    BaseRecyclerAdapter<LabelModel, LabelsViewHolder, BaseViewHolder.OnItemClickListener<LabelModel>>(
        eventsModels
    ) {
    interface OnSelectLabel {
        fun isLabelSelected(labelModel: LabelModel?): Boolean
        fun onToggleSelection(labelModel: LabelModel?, select: Boolean)
    }

    override fun viewHolder(parent: ViewGroup, viewType: Int): LabelsViewHolder {
        return newInstance(parent, onSelectLabel, this)
    }

    override fun onBindView(holder: LabelsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}