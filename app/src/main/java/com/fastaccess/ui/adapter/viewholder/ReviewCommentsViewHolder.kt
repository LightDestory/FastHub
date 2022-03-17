package com.fastaccess.ui.adapter.viewholder

import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import com.fastaccess.R
import com.fastaccess.data.dao.ReactionsModel
import com.fastaccess.data.dao.ReviewCommentModel
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.helper.ViewHelper.dpToPx
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.provider.timeline.CommentsHelper.appendEmojies
import com.fastaccess.provider.timeline.HtmlHelper.htmlIntoTextView
import com.fastaccess.provider.timeline.handler.drawable.DrawableGetter
import com.fastaccess.ui.adapter.ReviewCommentsAdapter
import com.fastaccess.ui.adapter.callback.OnToggleView
import com.fastaccess.ui.adapter.callback.ReactionsCallback
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import java.util.*

class ReviewCommentsViewHolder private constructor(
    itemView: View,
    viewGroup: ViewGroup,
    adapter: ReviewCommentsAdapter,
    onToggleView: OnToggleView,
    reactionsCallback: ReactionsCallback,
    repoOwner: String,
    poster: String
) : BaseViewHolder<ReviewCommentModel>(itemView, adapter) {
    private var avatarView: AvatarLayout = itemView.findViewById(R.id.avatarView)
    private var name: FontTextView = itemView.findViewById(R.id.name)
    private var date: FontTextView = itemView.findViewById(R.id.date)
    private var comment: FontTextView = itemView.findViewById(R.id.comment)
    private var toggle: AppCompatImageView = itemView.findViewById(R.id.toggle)
    private var toggleHolder: LinearLayout = itemView.findViewById(R.id.toggleHolder)
    private var thumbsUp: FontTextView = itemView.findViewById(R.id.thumbsUp)
    private var thumbsDown: FontTextView = itemView.findViewById(R.id.thumbsDown)
    private var laugh: FontTextView = itemView.findViewById(R.id.laugh)
    private var hurray: FontTextView = itemView.findViewById(R.id.hurray)
    private var sad: FontTextView = itemView.findViewById(R.id.sad)
    private var heart: FontTextView = itemView.findViewById(R.id.heart)
    private var commentMenu: ImageView = itemView.findViewById(R.id.commentMenu)
    private var commentOptions: RelativeLayout = itemView.findViewById(R.id.commentOptions)
    private var owner: FontTextView = itemView.findViewById(R.id.owner)
    private var reactionsList: View = itemView.findViewById(R.id.reactionsList)
    private var thumbsUpReaction: FontTextView = itemView.findViewById(R.id.thumbsUpReaction)
    private var thumbsDownReaction: FontTextView = itemView.findViewById(R.id.thumbsDownReaction)
    private var laughReaction: FontTextView = itemView.findViewById(R.id.laughReaction)
    private var hurrayReaction: FontTextView = itemView.findViewById(R.id.hurrayReaction)
    private var sadReaction: FontTextView = itemView.findViewById(R.id.sadReaction)
    private var heartReaction: FontTextView = itemView.findViewById(R.id.heartReaction)
    private var rocketReaction: FontTextView = itemView.findViewById(R.id.rocketReaction)
    private var eyeReaction: FontTextView = itemView.findViewById(R.id.eyeReaction)
    private var rocket: FontTextView = itemView.findViewById(R.id.rocket)
    private var eyes: FontTextView = itemView.findViewById(R.id.eyes)
    private val onToggleView: OnToggleView?
    private val reactionsCallback: ReactionsCallback?
    private val viewGroup: ViewGroup
    private val repoOwner: String
    private val poster: String
    override fun onClick(v: View) {
        if (v.id == R.id.toggle || v.id == R.id.toggleHolder) {
            if (onToggleView != null) {
                val id = id
                onToggleView.onToggle(id, !onToggleView.isCollapsed(id))
                onToggle(onToggleView.isCollapsed(id), true)
            }
        } else {
            addReactionCount(v)
            super.onClick(v)
        }
    }

    override fun bind(t: ReviewCommentModel) {
        if (t.user != null) {
            avatarView.setUrl(
                t.user!!.avatarUrl, t.user!!.login, t.user!!
                    .isOrganizationType, isEnterprise(t.htmlUrl)
            )
            name.text = t.user!!.login
            if (t.authorAssociation != null && !"none".equals(
                    t.authorAssociation,
                    ignoreCase = true
                )
            ) {
                owner.text = t.authorAssociation!!.lowercase(Locale.getDefault())
                owner.visibility = View.VISIBLE
            } else {
                val isRepoOwner = TextUtils.equals(t.user!!.login, repoOwner)
                if (isRepoOwner) {
                    owner.visibility = View.VISIBLE
                    owner.setText(R.string.owner)
                } else {
                    val isPoster = TextUtils.equals(t.user!!.login, poster)
                    if (isPoster) {
                        owner.visibility = View.VISIBLE
                        owner.setText(R.string.original_poster)
                    } else {
                        owner.text = ""
                        owner.visibility = View.GONE
                    }
                }
            }
        }
        date.text = getTimeAgo(t.createdAt)
        if (!isEmpty(t.bodyHtml)) {
            val width = adapter?.rowWidth ?: 0
            htmlIntoTextView(
                comment,
                t.bodyHtml!!,
                if (width > 0) width else viewGroup.width
            )
        } else {
            comment.text = ""
        }
        if (t.reactions != null) {
            val reaction = t.reactions
            appendEmojies(reaction)
        }
        if (onToggleView != null) onToggle(onToggleView.isCollapsed(id), false)
    }

    private fun addReactionCount(v: View) {
        if (adapter != null) {
            val comment = adapter.getItem(absoluteAdapterPosition)
            if (comment != null) {
                val isReacted = reactionsCallback == null || reactionsCallback.isPreviouslyReacted(
                    comment.id,
                    v.id
                )
                val reactionsModel =
                    if (comment.reactions != null) comment.reactions else ReactionsModel()
                when (v.id) {
                    R.id.heart, R.id.heartReaction -> reactionsModel!!.heart =
                        if (!isReacted) reactionsModel.heart + 1 else reactionsModel.heart - 1
                    R.id.sad, R.id.sadReaction -> reactionsModel!!.confused =
                        if (!isReacted) reactionsModel.confused + 1 else reactionsModel.confused - 1
                    R.id.thumbsDown, R.id.thumbsDownReaction -> reactionsModel!!.minusOne =
                        if (!isReacted) reactionsModel.minusOne + 1 else reactionsModel.minusOne - 1
                    R.id.thumbsUp, R.id.thumbsUpReaction -> reactionsModel!!.plusOne =
                        if (!isReacted) reactionsModel.plusOne + 1 else reactionsModel.plusOne - 1
                    R.id.laugh, R.id.laughReaction -> reactionsModel!!.laugh =
                        if (!isReacted) reactionsModel.laugh + 1 else reactionsModel.laugh - 1
                    R.id.hurray, R.id.hurrayReaction -> reactionsModel!!.hooray =
                        if (!isReacted) reactionsModel.hooray + 1 else reactionsModel.hooray - 1
                    R.id.rocket, R.id.rocketReaction -> reactionsModel!!.rocket =
                        if (!isReacted) reactionsModel.rocket + 1 else reactionsModel.rocket - 1
                    R.id.eyes, R.id.eyeReaction -> reactionsModel!!.eyes =
                        if (!isReacted) reactionsModel.eyes + 1 else reactionsModel.eyes - 1
                }
                comment.reactions = reactionsModel
                appendEmojies(reactionsModel)
            }
        }
    }

    private fun appendEmojies(reaction: ReactionsModel?) {
        appendEmojies(
            reaction!!,
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

    private val id: Long
        get() {
            if (adapter != null) {
                return adapter.getItem(absoluteAdapterPosition)!!.id
            }
            return -1
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
            adapter: ReviewCommentsAdapter,
            onToggleView: OnToggleView,
            reactionsCallback: ReactionsCallback,
            repoOwner: String,
            poster: String
        ): ReviewCommentsViewHolder {
            return ReviewCommentsViewHolder(
                getView(viewGroup, R.layout.review_comments_row_item),
                viewGroup, adapter, onToggleView, reactionsCallback, repoOwner, poster
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
        this.onToggleView = onToggleView
        this.viewGroup = viewGroup
        this.reactionsCallback = reactionsCallback
        this.repoOwner = repoOwner
        this.poster = poster
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
        toggle.setOnClickListener(this)
        commentMenu.setOnClickListener(this)
        toggleHolder.setOnClickListener(this)
        laugh.setOnClickListener(this)
        sad.setOnClickListener(this)
        thumbsDown.setOnClickListener(this)
        thumbsUp.setOnClickListener(this)
        hurray.setOnClickListener(this)
        laugh.setOnLongClickListener(this)
        sad.setOnLongClickListener(this)
        thumbsDown.setOnLongClickListener(this)
        thumbsUp.setOnLongClickListener(this)
        hurray.setOnLongClickListener(this)
        heart.setOnLongClickListener(this)
        heart.setOnClickListener(this)
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