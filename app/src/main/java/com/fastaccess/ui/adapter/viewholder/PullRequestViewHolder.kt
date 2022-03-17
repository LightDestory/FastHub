package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import butterknife.BindString
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.adapter.PullRequestAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class PullRequestViewHolder private constructor(
    itemView: View,
    adapter: PullRequestAdapter,
    private val withAvatar: Boolean,
    private val showRepoName: Boolean
) : BaseViewHolder<PullRequest>(itemView, adapter) {
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

    @kotlin.jvm.JvmField
    @BindString(R.string.by)
    var by: String? = null
    override fun bind(t: PullRequest) {
        title!!.text = t.title
        details!!.text = PullRequest.getMergeBy(
            t,
            details!!.context,
            showRepoName
        )
        if (t.comments > 0) {
            commentsNo!!.text = t.comments.toString()
            commentsNo!!.visibility = View.VISIBLE
        } else {
            commentsNo!!.visibility = View.GONE
        }
        if (withAvatar && avatarLayout != null) {
            avatarLayout!!.setUrl(
                t.user.avatarUrl, t.user.login,
                false, isEnterprise(t.htmlUrl)
            )
            avatarLayout!!.visibility = View.VISIBLE
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: PullRequestAdapter,
            withAvatar: Boolean,
            showRepoName: Boolean
        ): PullRequestViewHolder {
            return if (withAvatar) {
                PullRequestViewHolder(
                    getView(
                        viewGroup!!,
                        R.layout.issue_row_item
                    ), adapter, true, showRepoName
                )
            } else PullRequestViewHolder(
                getView(
                    viewGroup!!,
                    R.layout.issue_no_image_row_item
                ), adapter, false, showRepoName
            )
        }
    }
}