package com.fastaccess.ui.adapter.viewholder

import android.graphics.Color
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.LabelModel
import com.fastaccess.data.dao.ReactionsModel
import com.fastaccess.data.dao.TimelineModel
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.data.dao.model.User
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.helper.ViewHelper.dpToPx
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.provider.timeline.CommentsHelper.appendEmojies
import com.fastaccess.provider.timeline.HtmlHelper.htmlIntoTextView
import com.fastaccess.provider.timeline.handler.drawable.DrawableGetter
import com.fastaccess.ui.adapter.IssuesTimelineAdapter
import com.fastaccess.ui.adapter.callback.OnToggleView
import com.fastaccess.ui.adapter.callback.ReactionsCallback
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.LabelSpan
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import java.util.*

/**
 * Created by Kosh on 13 Dec 2016, 1:03 AM
 */
class IssueDetailsViewHolder private constructor(
    itemView: View, viewGroup: ViewGroup,
    adapter: IssuesTimelineAdapter,
    onToggleView: OnToggleView, reactionsCallback: ReactionsCallback,
    repoOwner: String, poster: String
) : BaseViewHolder<TimelineModel>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.avatarView)
    var avatar: AvatarLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.date)
    var date: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.name)
    var name: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.comment)
    var comment: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.thumbsUp)
    var thumbsUp: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.thumbsDown)
    var thumbsDown: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.laugh)
    var laugh: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.sad)
    var sad: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.hurray)
    var hooray: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.heart)
    var heart: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.toggle)
    var toggle: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.commentMenu)
    var commentMenu: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.commentOptions)
    var commentOptions: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.toggleHolder)
    var toggleHolder: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.emojiesList)
    var emojiesList: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.owner)
    var owner: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.labels)
    var labels: TextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.labelsHolder)
    var labelsHolder: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.reactionsList)
    var reactionsList: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.thumbsUpReaction)
    var thumbsUpReaction: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.thumbsDownReaction)
    var thumbsDownReaction: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.laughReaction)
    var laughReaction: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.hurrayReaction)
    var hurrayReaction: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.sadReaction)
    var sadReaction: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.heartReaction)
    var heartReaction: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.rocketReaction)
    var rocketReaction: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.eyeReaction)
    var eyeReaction: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.rocket)
    var rocket: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.eyes)
    var eyes: FontTextView? = null
    private val onToggleView: OnToggleView?
    private val reactionsCallback: ReactionsCallback?
    private val viewGroup: ViewGroup
    private val repoOwner: String
    private val poster: String
    override fun bind(t: TimelineModel) {
        if (t.issue != null) {
            bind(t.issue!!)
        } else if (t.pullRequest != null) {
            bind(t.pullRequest!!)
        }
        if (onToggleView != null) onToggle(
            onToggleView.isCollapsed(adapterPosition.toLong()),
            false
        )
    }

    override fun onClick(v: View) {
        if (v.id == R.id.toggle || v.id == R.id.toggleHolder) {
            if (onToggleView != null) {
                val position = adapterPosition
                onToggleView.onToggle(
                    position.toLong(),
                    !onToggleView.isCollapsed(position.toLong())
                )
                onToggle(onToggleView.isCollapsed(position.toLong()), true)
            }
        } else {
            addReactionCount(v)
            super.onClick(v)
        }
    }

    private fun addReactionCount(v: View) {
        if (adapter != null) {
            val timelineModel = adapter.getItem(adapterPosition) ?: return
            var reactionsModel: ReactionsModel? = null
            val pullRequest = timelineModel.pullRequest
            val issue = timelineModel.issue
            var number = 0
            if (pullRequest != null) {
                reactionsModel = pullRequest.reactions
                number = pullRequest.number
            } else if (issue != null) {
                reactionsModel = issue.reactions
                number = issue.number
            }
            if (reactionsModel == null) reactionsModel = ReactionsModel()
            val isReacted = reactionsCallback == null || reactionsCallback.isPreviouslyReacted(
                number.toLong(),
                v.id
            )
            val isCallingApi =
                reactionsCallback != null && reactionsCallback.isCallingApi(number.toLong(), v.id)
            when (v.id) {
                R.id.heart, R.id.heartReaction -> reactionsModel.heart =
                    if (!isReacted) reactionsModel.heart + 1 else reactionsModel.heart - 1
                R.id.sad, R.id.sadReaction -> reactionsModel.confused =
                    if (!isReacted) reactionsModel.confused + 1 else reactionsModel.confused - 1
                R.id.thumbsDown, R.id.thumbsDownReaction -> reactionsModel.minusOne =
                    if (!isReacted) reactionsModel.minusOne + 1 else reactionsModel.minusOne - 1
                R.id.thumbsUp, R.id.thumbsUpReaction -> reactionsModel.plusOne =
                    if (!isReacted) reactionsModel.plusOne + 1 else reactionsModel.plusOne - 1
                R.id.laugh, R.id.laughReaction -> reactionsModel.laugh =
                    if (!isReacted) reactionsModel.laugh + 1 else reactionsModel.laugh - 1
                R.id.hurray, R.id.hurrayReaction -> reactionsModel.hooray =
                    if (!isReacted) reactionsModel.hooray + 1 else reactionsModel.hooray - 1
                R.id.rocket, R.id.rocketReaction -> reactionsModel.rocket =
                    if (!isReacted) reactionsModel.rocket + 1 else reactionsModel.rocket - 1
                R.id.eyes, R.id.eyeReaction -> reactionsModel.eyes =
                    if (!isReacted) reactionsModel.eyes + 1 else reactionsModel.eyes - 1
            }
            if (pullRequest != null) {
                pullRequest.reactions = reactionsModel
                appendEmojies(reactionsModel)
                timelineModel.pullRequest = pullRequest
            } else if (issue != null) {
                issue.reactions = reactionsModel
                appendEmojies(reactionsModel)
                timelineModel.issue = issue
            }
        }
    }

    private fun bind(issueModel: Issue) {
        setup(issueModel.user, issueModel.bodyHtml, issueModel.reactions)
        setupDate(issueModel.createdAt, issueModel.updatedAt)
        setupLabels(issueModel.labels.filterNotNull())
    }

    private fun bind(pullRequest: PullRequest) {
        setup(pullRequest.user, pullRequest.bodyHtml, pullRequest.reactions)
        setupDate(pullRequest.createdAt, pullRequest.updatedAt)
        setupLabels(pullRequest.labels.filterNotNull())
    }

    private fun setup(user: User, description: String, reactionsModel: ReactionsModel?) {
        avatar!!.setUrl(
            user.avatarUrl,
            user.login,
            user.isOrganizationType,
            isEnterprise(user.htmlUrl)
        )
        name!!.text = user.login
        val isOwner = TextUtils.equals(repoOwner, user.login)
        if (isOwner) {
            owner!!.visibility = View.VISIBLE
            owner!!.setText(R.string.owner)
        } else {
            owner!!.text = ""
            owner!!.visibility = View.GONE
        }
        reactionsModel?.let { appendEmojies(it) }
        if (!isEmpty(description)) {
            htmlIntoTextView(
                comment!!,
                description,
                viewGroup.width - dpToPx(itemView.context, 24f)
            )
        } else {
            comment!!.setText(R.string.no_description_provided)
        }
    }

    private fun setupDate(createdDate: Date, updated: Date) {
        date!!.text = getTimeAgo(createdDate)
    }

    private fun setupLabels(labelList: List<LabelModel>?) {
        if (labelList != null && labelList.isNotEmpty()) {
            val builder = builder()
            for (labelModel in labelList) {
                val color = Color.parseColor("#" + labelModel.color)
                builder.append(" ").append(" " + labelModel.name + " ", LabelSpan(color))
            }
            labels!!.text = builder
            labelsHolder!!.visibility = View.VISIBLE
        } else {
            labels!!.text = ""
            labelsHolder!!.visibility = View.GONE
        }
    }

    private fun appendEmojies(reaction: ReactionsModel) {
        appendEmojies(
            reaction,
            thumbsUp!!,
            thumbsUpReaction!!,
            thumbsDown!!,
            thumbsDownReaction!!,
            hooray!!,
            hurrayReaction!!,
            sad!!,
            sadReaction!!,
            laugh!!,
            laughReaction!!,
            heart!!,
            heartReaction!!,
            rocket!!,
            rocketReaction!!,
            eyes!!,
            eyeReaction!!,
            reactionsList!!
        )
    }

    private fun onToggle(expanded: Boolean, animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(viewGroup, ChangeBounds())
        }
        toggle!!.rotation = if (!expanded) 0.0f else 180f
        commentOptions!!.visibility = if (!expanded) View.GONE else View.VISIBLE
        reactionsList!!.visibility =
            if (expanded) View.GONE else if (reactionsList!!.tag == null || !(reactionsList!!.tag as Boolean)) View.GONE else View.VISIBLE
    }

    override fun onViewIsDetaching() {
        val drawableGetter = comment!!.getTag(R.id.drawable_callback) as DrawableGetter?
        drawableGetter?.clear(viewGroup.context, drawableGetter)
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: IssuesTimelineAdapter,
            onToggleView: OnToggleView,
            reactionsCallback: ReactionsCallback,
            repoOwner: String,
            poster: String
        ): IssueDetailsViewHolder {
            return IssueDetailsViewHolder(
                getView(viewGroup, R.layout.issue_detail_header_row_item), viewGroup,
                adapter, onToggleView, reactionsCallback, repoOwner, poster
            )
        }
    }

    init {
        this.onToggleView = onToggleView
        this.viewGroup = viewGroup
        this.reactionsCallback = reactionsCallback
        this.repoOwner = repoOwner
        this.poster = poster
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
        commentMenu!!.setOnClickListener(this)
        toggle!!.setOnClickListener(this)
        toggleHolder!!.setOnClickListener(this)
        laugh!!.setOnClickListener(this)
        sad!!.setOnClickListener(this)
        thumbsDown!!.setOnClickListener(this)
        thumbsUp!!.setOnClickListener(this)
        hooray!!.setOnClickListener(this)
        laugh!!.setOnLongClickListener(this)
        sad!!.setOnLongClickListener(this)
        thumbsDown!!.setOnLongClickListener(this)
        thumbsUp!!.setOnLongClickListener(this)
        hooray!!.setOnLongClickListener(this)
        heart!!.setOnLongClickListener(this)
        heart!!.setOnClickListener(this)
        laughReaction!!.setOnClickListener(this)
        sadReaction!!.setOnClickListener(this)
        thumbsDownReaction!!.setOnClickListener(this)
        thumbsUpReaction!!.setOnClickListener(this)
        hurrayReaction!!.setOnClickListener(this)
        heartReaction!!.setOnClickListener(this)
        laughReaction!!.setOnLongClickListener(this)
        sadReaction!!.setOnLongClickListener(this)
        thumbsDownReaction!!.setOnLongClickListener(this)
        thumbsUpReaction!!.setOnLongClickListener(this)
        hurrayReaction!!.setOnLongClickListener(this)
        heartReaction!!.setOnLongClickListener(this)
        rocketReaction!!.setOnLongClickListener(this)
        rocketReaction!!.setOnClickListener(this)
        rocket!!.setOnLongClickListener(this)
        rocket!!.setOnClickListener(this)
        eyeReaction!!.setOnLongClickListener(this)
        eyeReaction!!.setOnClickListener(this)
        eyes!!.setOnLongClickListener(this)
        eyes!!.setOnClickListener(this)
    }
}