package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.fastaccess.R
import com.fastaccess.data.dao.GroupedNotificationModel
import com.fastaccess.helper.AppHelper.isNightMode
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.helper.ViewHelper.getCardBackground
import com.fastaccess.ui.adapter.NotificationsAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class NotificationsViewHolder private constructor(
    itemView: View,
    adapter: NotificationsAdapter,
    showUnreadState: Boolean
) : BaseViewHolder<GroupedNotificationModel>(itemView, adapter) {
    val title: FontTextView = itemView.findViewById(R.id.title)
    val date: FontTextView = itemView.findViewById(R.id.date)
    val markAsRead: ForegroundImageView = itemView.findViewById(R.id.markAsRead)
    val notificationType: ForegroundImageView = itemView.findViewById(R.id.notificationType)
    val repoName: FontTextView = itemView.findViewById(R.id.repoName)
    private val showUnreadState: Boolean
    override fun bind(t: GroupedNotificationModel) {
        val thread = t.notification
        if (thread?.subject != null) {
            title.text = thread.subject!!.title
            val cardBackground = getCardBackground(itemView.context)
            val color: Int
            date.text = getTimeAgo(thread.updatedAt)
            markAsRead.visibility =
                if (thread.unread) View.VISIBLE else View.GONE
            if (thread.subject!!.type != null) {
                notificationType.setImageResource(thread.subject!!.type!!.drawableRes)
                notificationType.contentDescription = thread.subject!!.type!!.name
            } else {
                notificationType.setImageResource(R.drawable.ic_info_outline)
            }
            if (showUnreadState) {
                repoName.visibility = View.GONE
                color = if (isNightMode(itemView.resources)) {
                    ContextCompat.getColor(itemView.context, R.color.material_blue_grey_800)
                } else {
                    ContextCompat.getColor(itemView.context, R.color.material_blue_grey_200)
                }
                (itemView as CardView).setCardBackgroundColor(if (thread.unread) color else cardBackground)
            } else {
                repoName.visibility = View.VISIBLE
                repoName.text = thread.repository!!.fullName
            }
        }
    }

    companion object {
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: NotificationsAdapter,
            showUnreadState: Boolean
        ): NotificationsViewHolder {
            return NotificationsViewHolder(
                getView(viewGroup, R.layout.notifications_row_item),
                adapter,
                showUnreadState
            )
        }
    }

    init {
        markAsRead.setOnClickListener(this)
        this.showUnreadState = showUnreadState
    }
}
