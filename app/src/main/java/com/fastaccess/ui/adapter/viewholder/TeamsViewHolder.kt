package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import com.fastaccess.R
import com.fastaccess.data.dao.TeamsModel
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.InputHelper.toNA
import com.fastaccess.ui.adapter.TeamsAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class TeamsViewHolder private constructor(
    itemView: View,
    adapter: TeamsAdapter
) : BaseViewHolder<TeamsModel>(itemView, adapter) {
    var title: FontTextView = itemView.findViewById(R.id.title)
    var date: FontTextView = itemView.findViewById(R.id.date)
    override fun bind(t: TeamsModel) {
        title.text = if (!isEmpty(t.name)) t.name else t.slug
        if (!isEmpty(t.description)) {
            date.text = t.description
        } else {
            date.text = toNA(t.slug)
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: TeamsAdapter
        ): TeamsViewHolder {
            return TeamsViewHolder(getView(viewGroup, R.layout.feeds_row_no_image_item), adapter)
        }
    }

}