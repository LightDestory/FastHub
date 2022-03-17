package com.fastaccess.ui.adapter

import android.view.ViewGroup
import com.fastaccess.ui.adapter.viewholder.LabelColorsViewHolder
import com.fastaccess.ui.adapter.viewholder.LabelColorsViewHolder.Companion.newInstance
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 02 Apr 2017, 5:19 PM
 */
class LabelColorsAdapter(
    data: MutableList<String>,
    listener: BaseViewHolder.OnItemClickListener<String>?
) : BaseRecyclerAdapter<String, LabelColorsViewHolder, BaseViewHolder.OnItemClickListener<String>>(
    data,
    listener
) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): LabelColorsViewHolder {
        return newInstance(parent, this)
    }

    override fun onBindView(holder: LabelColorsViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }
}