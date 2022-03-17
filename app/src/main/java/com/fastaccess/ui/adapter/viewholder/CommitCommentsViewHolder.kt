package com.fastaccess.ui.adapter.viewholder

import androidx.transition.ChangeBounds
import androidx.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import com.fastaccess.R
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.helper.InputHelper
import com.fastaccess.helper.ParseDateFormat
import com.fastaccess.helper.ViewHelper
import com.fastaccess.provider.markdown.MarkDownProvider
import com.fastaccess.provider.scheme.LinkParserHelper
import com.fastaccess.provider.timeline.handler.drawable.DrawableGetter
import com.fastaccess.ui.adapter.CommitCommentsAdapter
import com.fastaccess.ui.adapter.callback.OnToggleView
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import java.util.*

/**
 * Created by kosh on 15/08/2017.
 */
class CommitCommentsViewHolder private constructor(
    view: View, adapter: CommitCommentsAdapter,
    val viewGroup: ViewGroup, val onToggleView: OnToggleView
) : BaseViewHolder<Comment>(view, adapter) {

    val avatar: AvatarLayout = view.findViewById(R.id.avatarView)
    val name: FontTextView = view.findViewById(R.id.name)
    val date: FontTextView = view.findViewById(R.id.date)
    val toggle: ForegroundImageView = view.findViewById(R.id.toggle)
    val commentMenu: ForegroundImageView = view.findViewById(R.id.commentMenu)
    val comment: FontTextView = view.findViewById(R.id.comment)
    val commentOptions: View = view.findViewById(R.id.commentOptions)
    val owner: TextView = view.findViewById(R.id.owner)

    init {
        if (adapter.rowWidth == 0) {
            itemView.viewTreeObserver.addOnPreDrawListener(object :
                ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    itemView.viewTreeObserver.removeOnPreDrawListener(this)
                    adapter.rowWidth = itemView.width - ViewHelper.dpToPx(itemView.context, 48f)
                    return false
                }
            })
        }
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
        commentMenu.setOnClickListener(this)
        commentMenu.setOnLongClickListener(this)
        toggle.visibility = View.GONE
        commentMenu.visibility = View.GONE
        commentOptions.visibility = View.GONE
    }


    override fun onClick(v: View) {
        if (v.id == R.id.toggle || v.id == R.id.toggleHolder) {
            val position = absoluteAdapterPosition
            onToggleView.onToggle(position.toLong(), !onToggleView.isCollapsed(position.toLong()))
            onToggle(onToggleView.isCollapsed(position.toLong()), true)
        } else {
            super.onClick(v)
        }
    }

    override fun bind(t: Comment) {
        val author3 = t.user
        if (author3 != null) {
            avatar.setUrl(
                author3.avatarUrl,
                author3.login,
                false,
                LinkParserHelper.isEnterprise(author3.url)
            )
            name.text = author3.login
        } else {
            avatar.setUrl(null, null, false, false)
            name.text = ""
        }
        if (!InputHelper.isEmpty(t.body)) {
            val width = adapter?.rowWidth ?: 0
            if (width > 0) {
                MarkDownProvider.setMdText(comment, t.body, width)
            } else {
                MarkDownProvider.setMdText(comment, t.body)
            }
        } else {
            comment.text = ""
        }
        if (t.authorAssociation != null && !"none".equals(t.authorAssociation, ignoreCase = true)) {
            owner.text = t.authorAssociation.lowercase(Locale.getDefault())
            owner.visibility = View.VISIBLE
        } else {
            owner.visibility = View.GONE
        }
        if (t.createdAt == t.updatedAt) {
            date.text =
                "${ParseDateFormat.getTimeAgo(t.updatedAt)} ${itemView.resources.getString(R.string.edited)}"
        } else {
            date.text = ParseDateFormat.getTimeAgo(t.createdAt)
        }
        onToggle(onToggleView.isCollapsed(absoluteAdapterPosition.toLong()), false)
    }

    private fun onToggle(expanded: Boolean, animate: Boolean) {
        if (animate) {
            TransitionManager.beginDelayedTransition(viewGroup, ChangeBounds())
        }
        toggle.rotation = if (!expanded) 0.0f else 180f
    }

    override fun onViewIsDetaching() {
        val drawableGetter = comment.getTag(R.id.drawable_callback) as DrawableGetter?
        drawableGetter?.clear(viewGroup.context, drawableGetter)
    }

    companion object {
        fun newInstance(
            parent: ViewGroup,
            adapter: CommitCommentsAdapter,
            onToggleView: OnToggleView
        ): CommitCommentsViewHolder {
            return CommitCommentsViewHolder(
                getView(parent, R.layout.comments_row_item),
                adapter,
                parent,
                onToggleView
            )
        }
    }
}