package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.appcompat.widget.AppCompatImageView
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.helper.ViewHelper.dpToPx
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.provider.timeline.HtmlHelper.htmlIntoTextView
import com.fastaccess.ui.adapter.CommentsAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class CommentsViewHolder private constructor(
    itemView: View,
    adapter: CommentsAdapter,
    viewGroup: ViewGroup
) : BaseViewHolder<Comment>(itemView, adapter) {
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
    @BindView(R.id.toggleHolder)
    var toggleHolder: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.toggle)
    var toggle: AppCompatImageView? = null
    private val viewGroup: ViewGroup
    override fun onClick(v: View) {
        if (v.id == R.id.toggleHolder) {
            toggle!!.callOnClick()
        } else {
            super.onClick(v)
        }
    }

    override fun bind(t: Comment) {
        if (t.user != null) {
            avatar!!.setUrl(
                t.user.avatarUrl, t.user.login,
                t.user.isOrganizationType, isEnterprise(t.user.htmlUrl)
            )
        } else {
            avatar!!.setUrl(null, null, isOrg = false, isEnterprise = false)
        }
        if (!isEmpty(t.bodyHtml)) {
            val width = adapter?.rowWidth ?: 0
            htmlIntoTextView(
                comment!!,
                t.bodyHtml,
                if (width > 0) width else viewGroup.width
            )
        } else {
            comment!!.text = ""
        }
        name!!.text = if (t.user != null) t.user.login else "Anonymous"
        if (t.createdAt.before(t.updatedAt)) {
            date!!.text = String.format(
                "%s %s", getTimeAgo(t.createdAt),
                date!!.resources.getString(R.string.edited)
            )
        } else {
            date!!.text = getTimeAgo(t.createdAt)
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: CommentsAdapter
        ): CommentsViewHolder {
            return CommentsViewHolder(
                getView(viewGroup, R.layout.no_emojies_comments_row_item),
                adapter,
                viewGroup
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
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
        toggleHolder!!.setOnClickListener(this)
        toggle!!.setOnClickListener(this)
        toggle!!.setOnLongClickListener(this)
        this.viewGroup = viewGroup
    }
}