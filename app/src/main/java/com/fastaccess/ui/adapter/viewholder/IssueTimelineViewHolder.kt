package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.TimelineModel
import com.fastaccess.data.dao.types.IssueEventType
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.provider.timeline.TimelineProvider.getStyledEvents
import com.fastaccess.provider.timeline.handler.drawable.DrawableGetter
import com.fastaccess.ui.adapter.IssuesTimelineAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView

/**
 * Created by Kosh on 13 Dec 2016, 1:42 AM
 */
class IssueTimelineViewHolder private constructor(
    itemView: View,
    private val viewGroup: ViewGroup,
    adapter: IssuesTimelineAdapter,
    private val isMerged: Boolean
) : BaseViewHolder<TimelineModel>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.stateImage)
    var stateImage: ForegroundImageView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.avatarLayout)
    var avatarLayout: AvatarLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.stateText)
    var stateText: FontTextView? = null
    override fun bind(timelineModel: TimelineModel) {
        val issueEventModel = timelineModel.genericEvent
        val event = issueEventModel!!.event
        if (issueEventModel.assignee != null && issueEventModel.assigner != null) {
            avatarLayout!!.setUrl(
                issueEventModel.assigner!!.avatarUrl, issueEventModel.assigner!!.login,
                false, isEnterprise(issueEventModel.url)
            )
        } else {
            if (event !== IssueEventType.committed) {
                avatarLayout!!.visibility = View.VISIBLE
                if (issueEventModel.actor != null) {
                    avatarLayout!!.setUrl(
                        issueEventModel.actor!!.avatarUrl, issueEventModel.actor!!.login,
                        false, isEnterprise(issueEventModel.url)
                    )
                } else if (issueEventModel.author != null) {
                    avatarLayout!!.setUrl(
                        issueEventModel.author!!.avatarUrl, issueEventModel.author!!.login,
                        false, isEnterprise(issueEventModel.url)
                    )
                }
            } else {
                avatarLayout!!.visibility = View.GONE
            }
        }
        if (event != null) {
            stateImage!!.contentDescription = event.name
            stateImage!!.setImageResource(event.iconResId)
        }
        if (event != null) {
            stateText!!.text = getStyledEvents(issueEventModel, itemView.context, isMerged)
        } else {
            stateText!!.text = ""
            stateImage!!.setImageResource(R.drawable.ic_label)
        }
    }

    override fun onViewIsDetaching() {
        val drawableGetter = stateText!!.getTag(R.id.drawable_callback) as DrawableGetter?
        drawableGetter?.clear(viewGroup.context, drawableGetter)
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: IssuesTimelineAdapter,
            isMerged: Boolean
        ): IssueTimelineViewHolder {
            return IssueTimelineViewHolder(
                getView(viewGroup, R.layout.issue_timeline_row_item),
                viewGroup,
                adapter,
                isMerged
            )
        }
    }
}