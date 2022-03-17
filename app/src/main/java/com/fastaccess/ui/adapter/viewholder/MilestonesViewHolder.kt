package com.fastaccess.ui.adapter.viewholder

import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.ui.base.adapter.BaseViewHolder
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.ui.widgets.FontTextView
import android.view.ViewGroup
import com.fastaccess.data.dao.MilestoneModel
import android.view.View
import com.fastaccess.ui.adapter.*

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class MilestonesViewHolder private constructor(
    itemView: View,
    adapter: MilestonesAdapter
) : BaseViewHolder<MilestoneModel>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.title)
    var title: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.date)
    var date: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.notificationTitle)
    var notificationTitle: FontTextView? = null
    override fun bind(t: MilestoneModel) {
        title!!.text = t.title
        notificationTitle!!.text = t.description
        if (t.dueOn != null) {
            date!!.text = getTimeAgo(t.dueOn)
        } else if (t.createdAt != null) {
            date!!.text = getTimeAgo(t.createdAt)
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: MilestonesAdapter
        ): MilestonesViewHolder {
            return MilestonesViewHolder(getView(viewGroup, R.layout.milestone_row_item), adapter)
        }
    }
}