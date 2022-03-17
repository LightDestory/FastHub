package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import butterknife.BindString
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.PullsIssuesParser.Companion.getForIssue
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.adapter.IssuesAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import java.util.*

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class IssuesViewHolder private constructor(
    itemView: View,
    adapter: IssuesAdapter,
    private val withAvatar: Boolean,
    private val showRepoName: Boolean,
    private val showState: Boolean = false
) : BaseViewHolder<Issue>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.title)
    var title: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.avatarLayout)
    var avatarLayout: AvatarLayout? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.issue_state)
    var issueState: AppCompatImageView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.details)
    var details: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.commentsNo)
    var commentsNo: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindString(R.string.by)
    var by: String? = null
    override fun bind(t: Issue) {
        title!!.text = t.title
        if (t.state != null) {
            val data =
                getTimeAgo(if (t.state === IssueState.open) t.createdAt else t.closedAt)
            val builder = builder()
            if (showRepoName) {
                val parser = getForIssue(t.htmlUrl)
                if (parser != null) builder.bold(parser.login!!)
                    .append("/")
                    .bold(parser.repoId!!)
                    .bold("#")
                    .bold(t.number.toString()).append(" ")
                    .append(" ")
            }
            if (!showRepoName) {
                if (t.state === IssueState.closed) {
                    if (t.closedBy == null) {
                        builder.bold("#")
                            .bold(t.number.toString()).append(" ")
                            .append(" ")
                    } else {
                        builder.append("#")
                            .append(t.number.toString()).append(" ")
                            .append(t.closedBy.login)
                            .append(" ")
                    }
                } else {
                    builder.bold("#")
                        .bold(t.number.toString()).append(" ")
                        .append(t.user.login)
                        .append(" ")
                }
            }
            details!!.text = builder
                .append(itemView.resources.getString(t.state.status)
                    .lowercase(Locale.getDefault()))
                .append(" ")
                .append(data)
            if (t.comments > 0) {
                commentsNo!!.text = t.comments.toString()
                commentsNo!!.visibility = View.VISIBLE
            } else {
                commentsNo!!.visibility = View.GONE
            }
        }
        if (showState) {
            issueState!!.visibility = View.VISIBLE
            issueState!!.setImageResource(if (t.state === IssueState.open) R.drawable.ic_issue_opened_small else R.drawable.ic_issue_closed_small)
        } else {
            issueState!!.visibility = View.GONE
        }
        if (withAvatar && avatarLayout != null) {
            avatarLayout!!.setUrl(
                t.user.avatarUrl, t.user.login, false,
                isEnterprise(t.user.htmlUrl)
            )
            avatarLayout!!.visibility = View.VISIBLE
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        @JvmOverloads
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: IssuesAdapter,
            withAvatar: Boolean,
            showRepoName: Boolean,
            showState: Boolean = false
        ): IssuesViewHolder {
            return if (withAvatar) {
                IssuesViewHolder(
                    getView(viewGroup!!, R.layout.issue_row_item),
                    adapter,
                    true,
                    showRepoName,
                    showState
                )
            } else {
                IssuesViewHolder(
                    getView(
                        viewGroup!!,
                        R.layout.issue_no_image_row_item
                    ), adapter, false, showRepoName, showState
                )
            }
        }
    }
}