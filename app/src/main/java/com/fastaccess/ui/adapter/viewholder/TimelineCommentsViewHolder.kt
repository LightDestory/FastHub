package com.fastaccess.ui.adapter.viewholder

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.fastaccess.R
import com.fastaccess.data.dao.ReactionsModel
import com.fastaccess.data.dao.TimelineModel
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
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import java.util.*

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class TimelineCommentsViewHolder private constructor(
    itemView: View,
    viewGroup: ViewGroup,
    adapter: IssuesTimelineAdapter,
    onToggleView: OnToggleView,
    showEmojies: Boolean,
    reactionsCallback: ReactionsCallback,
    repoOwner: String,
    poster: String
) : BaseViewHolder<TimelineModel>(itemView, adapter) {
    var avatar: AvatarLayout = itemView.findViewById(R.id.avatarView)
    var name: FontTextView = itemView.findViewById(R.id.name)
    var date: FontTextView = itemView.findViewById(R.id.date)
    var toggle: ForegroundImageView = itemView.findViewById(R.id.toggle)
    var commentMenu: ForegroundImageView = itemView.findViewById(R.id.commentMenu)
    var toggleHolder: LinearLayout = itemView.findViewById(R.id.toggleHolder)
    private var thumbsUp: FontTextView = itemView.findViewById(R.id.thumbsUp)
    private var thumbsDown: FontTextView = itemView.findViewById(R.id.thumbsDown)
    var laugh: FontTextView = itemView.findViewById(R.id.laugh)
    var hurray: FontTextView = itemView.findViewById(R.id.hurray)
    var sad: FontTextView = itemView.findViewById(R.id.sad)
    var heart: FontTextView = itemView.findViewById(R.id.heart)
    private var emojiesList: View = itemView.findViewById(R.id.emojiesList)
    var commentOptions: RelativeLayout = itemView.findViewById(R.id.commentOptions)
    var comment: FontTextView = itemView.findViewById(R.id.comment)
    var owner: FontTextView = itemView.findViewById(R.id.owner)
    private var pathText: FontTextView = itemView.findViewById(R.id.pathText)
    var reactionsList: View = itemView.findViewById(R.id.reactionsList)
    private var thumbsUpReaction: FontTextView = itemView.findViewById(R.id.thumbsUpReaction)
    private var thumbsDownReaction: FontTextView = itemView.findViewById(R.id.thumbsDownReaction)
    private var laughReaction: FontTextView = itemView.findViewById(R.id.laughReaction)
    var hurrayReaction: FontTextView = itemView.findViewById(R.id.hurrayReaction)
    var sadReaction: FontTextView = itemView.findViewById(R.id.sadReaction)
    var heartReaction: FontTextView = itemView.findViewById(R.id.heartReaction)
    var rocketReaction: FontTextView = itemView.findViewById(R.id.rocketReaction)
    var eyeReaction: FontTextView = itemView.findViewById(R.id.eyeReaction)
    var rocket: FontTextView = itemView.findViewById(R.id.rocket)
    var eyes: FontTextView = itemView.findViewById(R.id.eyes)
    private val onToggleView: OnToggleView?
    private val showEmojies: Boolean
    private val reactionsCallback: ReactionsCallback?
    private val viewGroup: ViewGroup
    private val repoOwner: String
    private val poster: String
    override fun onClick(v: View) {
        if (v.id == R.id.toggle || v.id == R.id.toggleHolder) {
            if (onToggleView != null) {
                val position = absoluteAdapterPosition
                onToggleView.onToggle(
                    position.toLong(),
                    !onToggleView.isCollapsed(position.toLong())
                )
                onToggle(onToggleView.isCollapsed(position.toLong()), true)
            }
        } else {
            super.onClick(v)
            addReactionCount(v)
        }
    }

    override fun bind(t: TimelineModel) {
        val commentsModel = t.comment
        if (commentsModel!!.user != null) {
            avatar.setUrl(
                commentsModel.user.avatarUrl, commentsModel.user.login,
                false, isEnterprise(commentsModel.htmlUrl)
            )
            name.text = if (commentsModel.user != null) commentsModel.user.login else "Anonymous"
            if (commentsModel.authorAssociation != null && !"none".equals(
                    commentsModel.authorAssociation,
                    ignoreCase = true
                )
            ) {
                owner.text = commentsModel.authorAssociation.lowercase(Locale.getDefault())
                owner.visibility = View.VISIBLE
            } else {
                val isRepoOwner = TextUtils.equals(commentsModel.user.login, repoOwner)
                if (isRepoOwner) {
                    owner.visibility = View.VISIBLE
                    owner.setText(R.string.owner)
                } else {
                    val isPoster = TextUtils.equals(commentsModel.user.login, poster)
                    if (isPoster) {
                        owner.visibility = View.VISIBLE
                        owner.setText(R.string.original_poster)
                    } else {
                        owner.text = ""
                        owner.visibility = View.GONE
                    }
                }
            }
        } else {
            avatar.setUrl(null, null, isOrg = false, isEnterprise = false)
            name.text = ""
        }
        if (!isEmpty(commentsModel.path) && commentsModel.position > 0) {
            pathText.visibility = View.VISIBLE
            pathText.text = String.format(
                "Commented on %s#L%s", commentsModel.path,
                if (commentsModel.line > 0) commentsModel.line else commentsModel.position
            )
        } else {
            pathText.text = ""
            pathText.visibility = View.GONE
        }
        if (!isEmpty(commentsModel.bodyHtml)) {
            val body = commentsModel.bodyHtml
            val width = adapter?.rowWidth ?: 0
            htmlIntoTextView(comment, body, if (width > 0) width else viewGroup.width)
        } else {
            comment.text = ""
        }
        if (commentsModel.createdAt.before(commentsModel.updatedAt)) {
            date.text = String.format(
                "%s %s", getTimeAgo(commentsModel.createdAt), itemView
                    .resources.getString(R.string.edited)
            )
        } else {
            date.text = getTimeAgo(commentsModel.createdAt)
        }
        if (showEmojies) {
            if (commentsModel.reactions != null) {
                val reaction = commentsModel.reactions
                appendEmojies(reaction)
            }
        }
        emojiesList.visibility = if (showEmojies) View.VISIBLE else View.GONE
        if (onToggleView != null) onToggle(
            onToggleView.isCollapsed(absoluteAdapterPosition.toLong()),
            false
        )
    }

    private fun addReactionCount(v: View) {
        if (adapter != null) {
            val timelineModel = adapter.getItem(absoluteAdapterPosition) ?: return
            val comment = timelineModel.comment
            if (comment != null) {
                val isReacted = reactionsCallback == null || reactionsCallback.isPreviouslyReacted(
                    comment.id,
                    v.id
                )
                reactionsCallback != null && reactionsCallback.isCallingApi(comment.id, v.id)
                //                if (isCallingApi) return;
                val reactionsModel =
                    if (comment.reactions != null) comment.reactions else ReactionsModel()
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
                comment.reactions = reactionsModel
                appendEmojies(reactionsModel)
                timelineModel.comment = comment
            }
        }
    }

    private fun appendEmojies(reaction: ReactionsModel) {
        appendEmojies(
            reaction,
            thumbsUp,
            thumbsUpReaction,
            thumbsDown,
            thumbsDownReaction,
            hurray,
            hurrayReaction,
            sad,
            sadReaction,
            laugh,
            laughReaction,
            heart,
            heartReaction,
            rocket,
            rocketReaction,
            eyes,
            eyeReaction,
            reactionsList
        )
    }

    private fun onToggle(expanded: Boolean, animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(viewGroup, ChangeBounds())
        }
        toggle.rotation = if (!expanded) 0.0f else 180f
        commentOptions.visibility = if (!expanded) View.GONE else View.VISIBLE
        reactionsList.visibility = if (expanded) View.GONE else View.VISIBLE
        reactionsList.visibility =
            if (expanded) View.GONE else if (reactionsList.tag == null || !(reactionsList.tag as Boolean)) View.GONE else View.VISIBLE
    }

    override fun onViewIsDetaching() {
        val drawableGetter = comment.getTag(R.id.drawable_callback) as DrawableGetter?
        drawableGetter?.clear(viewGroup.context, drawableGetter)
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: IssuesTimelineAdapter,
            onToggleView: OnToggleView,
            showEmojies: Boolean,
            reactionsCallback: ReactionsCallback,
            repoOwner: String,
            poster: String
        ): TimelineCommentsViewHolder {
            return TimelineCommentsViewHolder(
                getView(viewGroup, R.layout.comments_row_item), viewGroup, adapter,
                onToggleView, showEmojies, reactionsCallback, repoOwner, poster
            )
        }
    }

    init {
        if (adapter.rowWidth == 0) {
            itemView.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    itemView.viewTreeObserver.removeOnPreDrawListener(this)
                    adapter.rowWidth = itemView.width - dpToPx(itemView.context, 48f)
                    return false
                }
            })
        }
        this.viewGroup = viewGroup
        this.onToggleView = onToggleView
        this.showEmojies = showEmojies
        this.reactionsCallback = reactionsCallback
        this.repoOwner = repoOwner
        this.poster = poster
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
        commentMenu.setOnClickListener(this)
        commentMenu.setOnLongClickListener(this)
        toggleHolder.setOnClickListener(this)
        toggle.setOnClickListener(this)
        laugh.setOnClickListener(this)
        sad.setOnClickListener(this)
        thumbsDown.setOnClickListener(this)
        thumbsUp.setOnClickListener(this)
        hurray.setOnClickListener(this)
        heart.setOnClickListener(this)
        laugh.setOnLongClickListener(this)
        sad.setOnLongClickListener(this)
        thumbsDown.setOnLongClickListener(this)
        thumbsUp.setOnLongClickListener(this)
        hurray.setOnLongClickListener(this)
        heart.setOnLongClickListener(this)
        laughReaction.setOnClickListener(this)
        sadReaction.setOnClickListener(this)
        thumbsDownReaction.setOnClickListener(this)
        thumbsUpReaction.setOnClickListener(this)
        hurrayReaction.setOnClickListener(this)
        heartReaction.setOnClickListener(this)
        laughReaction.setOnLongClickListener(this)
        sadReaction.setOnLongClickListener(this)
        thumbsDownReaction.setOnLongClickListener(this)
        thumbsUpReaction.setOnLongClickListener(this)
        hurrayReaction.setOnLongClickListener(this)
        heartReaction.setOnLongClickListener(this)
        rocketReaction.setOnLongClickListener(this)
        rocketReaction.setOnClickListener(this)
        rocket.setOnLongClickListener(this)
        rocket.setOnClickListener(this)
        eyeReaction.setOnLongClickListener(this)
        eyeReaction.setOnClickListener(this)
        eyes.setOnLongClickListener(this)
        eyes.setOnClickListener(this)
    }
}