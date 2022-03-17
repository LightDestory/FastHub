package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.Commit
import com.fastaccess.helper.InputHelper.toNA
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.adapter.CommitsAdapter
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class CommitsViewHolder private constructor(
    itemView: View,
    adapter: CommitsAdapter?
) : BaseViewHolder<Commit>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.title)
    var title: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.avatarLayout)
    var avatarLayout: AvatarLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.details)
    var details: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.commentsNo)
    var commentsNo: FontTextView? = null
    override fun bind(t: Commit) {
        title!!.text = t.gitCommit.message
        val login =
            if (t.author != null) t.author.login else t.gitCommit.author!!.login
        val avatar = if (t.author != null) t.author.avatarUrl else null
        val date = t.gitCommit.author!!.date
        details!!.text = builder()
            .bold(toNA(login))
            .append(" ")
            .append(getTimeAgo(date))
        avatarLayout!!.setUrl(
            avatar,
            login,
            false,
            isEnterprise(if (t.author != null) t.author.url else t.gitCommit.author!!.htmlUrl)
        )
        avatarLayout!!.visibility = View.VISIBLE
        if (t.gitCommit != null && t.gitCommit.commentCount > 0) {
            commentsNo!!.text = if (t.gitCommit != null) t.gitCommit.commentCount.toString() else 0.toString()
            commentsNo!!.visibility = View.VISIBLE
        } else {
            commentsNo!!.visibility = View.GONE
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: CommitsAdapter?
        ): CommitsViewHolder {
            return CommitsViewHolder(getView(viewGroup!!, R.layout.issue_row_item), adapter)
        }
    }
}