package com.fastaccess.ui.modules.main.issues

import android.view.View
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.data.dao.types.MyIssuesType
import com.fastaccess.helper.PrefGetter
import com.fastaccess.provider.rest.RepoQueryProvider
import com.fastaccess.provider.rest.RestProvider.getIssueService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 25 Mar 2017, 11:39 PM
 */
class MyIssuesPresenter internal constructor() : BasePresenter<MyIssuesMvp.View>(),
    MyIssuesMvp.Presenter {
    override val issues = ArrayList<Issue>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE

    @JvmField
    @com.evernote.android.state.State
    var issuesType: MyIssuesType? = null
    private val login = Login.getUser().login
    override fun onItemClick(position: Int, v: View?, item: Issue) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Issue) {
        if (view != null) view!!.onShowPopupDetails(item)
    }

    override fun onSetIssueType(issuesType: MyIssuesType) {
        this.issuesType = issuesType
    }

    override fun onCallApi(page: Int, parameter: IssueState?): Boolean {
        if (parameter == null) {
            throw NullPointerException("parameter is null")
        }
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        makeRestCall(
            getIssueService(isEnterprise).getIssuesWithCount(getUrl(parameter), page)
        ) { issues ->
            lastPage = issues.last
            if (currentPage == 1) {
                sendToView { view -> view.onSetCount(issues.totalCount) }
            }
            sendToView { view ->
                view.onNotifyAdapter(
                    issues.items,
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
                false
            )
            MyIssuesType.ASSIGNED -> return RepoQueryProvider.getAssigned(login, parameter, false)
            MyIssuesType.MENTIONED -> return RepoQueryProvider.getMentioned(login, parameter, false)
            MyIssuesType.PARTICIPATED -> return RepoQueryProvider.getParticipated(
                login,
                parameter,
                false
            )
            else -> {}
        }
        return RepoQueryProvider.getMyIssuesPullRequestQuery(login, parameter, false)
    }

    init {
        isEnterprise = PrefGetter.isEnterprise
    }
}