package com.fastaccess.ui.modules.main.pullrequests

import android.view.View
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import com.fastaccess.ui.base.mvp.BaseMvp.PaginationListener
import com.fastaccess.data.dao.types.MyIssuesType
import com.fastaccess.ui.base.adapter.BaseViewHolder
import java.util.ArrayList

/**
 * Created by Kosh on 25 Mar 2017, 11:39 PM
 */
interface MyPullRequestsMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener {
        fun onNotifyAdapter(items: List<PullRequest>?, page: Int)
        val loadMore: OnLoadMore<IssueState>
        fun onSetCount(totalCount: Int)
        fun onFilterIssue(issueState: IssueState)
        fun onShowPopupDetails(item: PullRequest)
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<PullRequest>,
        PaginationListener<IssueState> {
        val pullRequests: ArrayList<PullRequest>
        fun onSetPullType(issuesType: MyIssuesType)
    }
}