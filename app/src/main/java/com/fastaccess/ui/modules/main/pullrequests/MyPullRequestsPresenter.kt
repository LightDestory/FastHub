package com.fastaccess.ui.modules.main.pullrequests

import android.view.View
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.data.dao.types.MyIssuesType
import com.fastaccess.helper.PrefGetter
import com.fastaccess.provider.rest.RepoQueryProvider
import com.fastaccess.provider.rest.RestProvider.getPullRequestService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 25 Mar 2017, 11:53 PM
 */
class MyPullRequestsPresenter internal constructor() : BasePresenter<MyPullRequestsMvp.View>(),
    MyPullRequestsMvp.Presenter {
    override val pullRequests = ArrayList<PullRequest>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE

    @JvmField
    @com.evernote.android.state.State
    var issuesType: MyIssuesType? = null
    private val login = Login.getUser().login
    override fun onItemClick(position: Int, v: View?, item: PullRequest) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: PullRequest) {
        if (view != null) view!!.onShowPopupDetails(item)
    }

    override fun onSetPullType(issuesType: MyIssuesType) {
        this.issuesType = issuesType
    }

    override fun onCallApi(page: Int, parameter: IssueState?): Boolean {
        if (parameter == null) {
            throw NullPointerException("Parameter is null")
        }
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view: MyPullRequestsMvp.View -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        makeRestCall(
            getPullRequestService(isEnterprise).getPullsWithCount(getUrl(parameter), page)
        ) { response ->
            lastPage = response.last
            if (currentPage == 1) {
                sendToView { view -> view.onSetCount(response.totalCount) }
            }
            sendToView { view ->
                view.onNotifyAdapter(
                    response.items,
                    page
                )
            }
        }
        return true
    }

    private fun getUrl(parameter: IssueState): String {
        when (issuesType) {
            MyIssuesType.CREATED -> return RepoQueryProvider.getMyIssuesPullRequestQuery(
                login,
                parameter,
                true
            )
            MyIssuesType.ASSIGNED -> return RepoQueryProvider.getAssigned(login, parameter, true)
            MyIssuesType.MENTIONED -> return RepoQueryProvider.getMentioned(login, parameter, true)
            MyIssuesType.REVIEW -> return RepoQueryProvider.getReviewRequests(login, parameter)
            else -> {}
        }
        return RepoQueryProvider.getMyIssuesPullRequestQuery(login, parameter, false)
    }

    init {
        isEnterprise = PrefGetter.isEnterprise
    }
}