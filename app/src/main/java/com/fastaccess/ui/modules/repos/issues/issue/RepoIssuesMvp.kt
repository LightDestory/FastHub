package com.fastaccess.ui.modules.repos.issues.issue

import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.data.dao.PullsIssuesParser
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.*

/**
 * Created by Kosh on 03 Dec 2016, 3:45 PM
 */
interface RepoIssuesMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener {
        fun onNotifyAdapter(items: List<Issue>?, page: Int)
        val loadMore: OnLoadMore<IssueState>?
        fun onAddIssue()
        fun onUpdateCount(totalCount: Int)
        fun onOpenIssue(parser: PullsIssuesParser)
        fun onRefresh(isLastUpdated: Boolean)
        fun onShowIssuePopup(item: Issue)
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<Issue>,
        PaginationListener<IssueState> {
        fun onFragmentCreated(bundle: Bundle, issueState: IssueState)
        fun onWorkOffline()
        val issues: ArrayList<Issue>
        fun repoId(): String
        fun login(): String
        fun onSetSortBy(isLastUpdated: Boolean)
    }

    companion object {
        const val ISSUE_REQUEST_CODE = 1002
    }
}