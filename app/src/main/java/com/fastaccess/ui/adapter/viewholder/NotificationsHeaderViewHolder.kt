package com.fastaccess.ui.adapter.viewholder

import com.fastaccess.ui.base.adapter.BaseViewHolder
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.ui.widgets.FontTextView
import android.view.ViewGroup
import android.view.View
import com.fastaccess.data.dao.GroupedNotificationModel
import androidx.appcompat.widget.AppCompatImageButton
import com.fastaccess.ui.adapter.*

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class NotificationsHeaderViewHolder private constructor(
    itemView: View,
    adapter: NotificationsAdapter
) : BaseViewHolder<GroupedNotificationModel>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.headerTitle)
    var headerTitle: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.markAsRead)
    var markAsRead: AppCompatImageButton? = null
    override fun bind(t: GroupedNotificationModel) {
        val repo = t.repo
        if (repo != null && headerTitle != null) {
            headerTitle!!.text = repo.fullName
        }
    }

    companion object {
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: NotificationsAdapter
        ): NotificationsHeaderViewHolder {
            return NotificationsHeaderViewHolder(
                getView(
                    viewGroup,
                    R.layout.notification_header_row_item
                ), adapter
            )
        }
    }

    init {
        markAsRead!!.setOnClickListener(this)
    }
}