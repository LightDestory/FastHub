package com.fastaccess.ui.modules.main.issues

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.data.dao.types.MyIssuesType
import com.fastaccess.data.entity.Issue
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.*

/**
 * Created by Kosh on 25 Mar 2017, 11:39 PM
 */
interface MyIssuesMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener {
        fun onNotifyAdapter(items: List<Issue>?, page: Int)
        val loadMore: OnLoadMore<IssueState>
        fun onSetCount(totalCount: Int)
        fun onFilterIssue(issueState: IssueState)
        fun onShowPopupDetails(item: Issue)
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<Issue>,
        PaginationListener<IssueState> {
        val issues: ArrayList<Issue>
        fun onSetIssueType(issuesType: MyIssuesType)
    }
}