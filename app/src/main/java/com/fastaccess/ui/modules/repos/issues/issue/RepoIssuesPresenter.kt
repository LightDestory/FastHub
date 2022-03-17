package com.fastaccess.ui.modules.repos.issues.issue

import android.os.Bundle
import android.view.View
import com.fastaccess.data.dao.Pageable
import com.fastaccess.data.dao.PullsIssuesParser.Companion.getForIssue
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.RxHelper.getObservable
import com.fastaccess.helper.RxHelper.getSingle
import com.fastaccess.provider.rest.RepoQueryProvider
import com.fastaccess.provider.rest.RestProvider.getIssueService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import io.reactivex.Observable

/**
 * Created by Kosh on 03 Dec 2016, 3:48 PM
 */
class RepoIssuesPresenter : BasePresenter<RepoIssuesMvp.View>(), RepoIssuesMvp.Presenter {
    override val issues = ArrayList<Issue>()

    @com.evernote.android.state.State
    var login: String? = null

    @com.evernote.android.state.State
    var repoId: String? = null

    @JvmField
    @com.evernote.android.state.State
    var issueState: IssueState? = null

    @JvmField
    @com.evernote.android.state.State
    var isLastUpdated = false
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE
    override fun onError(throwable: Throwable) {
        onWorkOffline()
        super.onError(throwable)
    }

    override fun onCallApi(page: Int, parameter: IssueState?): Boolean {
        if (parameter == null) {
            sendToView { it.hideProgress() }
            return false
        }
        issueState = parameter
        if (page == 1) {
            onCallCountApi(issueState!!)
            lastPage = Int.MAX_VALUE
            sendToView { view: RepoIssuesMvp.View -> view.loadMore?.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        var sortBy = "created"
        if (isLastUpdated) {
            sortBy = "updated"
        }
        var page1 = page
        val finalSortBy = sortBy
        makeRestCall(getIssueService(isEnterprise)
            .getRepositoryIssues(login!!, repoId!!, parameter.name, sortBy, page1)
            .flatMap { issues: Pageable<Issue> ->
                lastPage = issues.last
                val filtered = issues.items
                    ?.filter { issue: Issue -> issue.pullRequest == null }
                    ?.toList()
                if (filtered != null) {
                    if (filtered.size < 10 && issues.next > 1) {
                        page1 = currentPage + 1
                        return@flatMap grabMoreIssues(
                            filtered.toMutableList(),
                            parameter.name,
                            finalSortBy,
                            currentPage
                        )
                    }
                    return@flatMap Observable.just(filtered)
                }
                Observable.just(ArrayList())
            }
            .doOnNext { filtered: List<Issue>? ->
                if (currentPage == 1) {
                    Issue.save(filtered!!, repoId!!, login!!)
                }
            }
        ) { issues ->
            sendToView { view -> view.onNotifyAdapter(issues, page1) }
        }
        return true
    }

    override fun onFragmentCreated(bundle: Bundle, issueState: IssueState) {
        repoId = bundle.getString(BundleConstant.ID)
        login = bundle.getString(BundleConstant.EXTRA)
        this.issueState = issueState
        if (!isEmpty(login) && !isEmpty(repoId)) {
            onCallApi(1, issueState)
        }
    }

    override fun onWorkOffline() {
        if (issues.isEmpty()) {
            manageDisposable(
                getSingle(
                    Issue.getIssues(
                        repoId!!, login!!, issueState!!
                    )
                )
                    .subscribe { issueModel: List<Issue> ->
                        sendToView { view: RepoIssuesMvp.View ->
                            view.onNotifyAdapter(issueModel, 1)
                            view.onUpdateCount(issueModel.size)
                        }
                    })
        } else {
            sendToView { it.hideProgress() }
        }
    }

    override fun repoId(): String {
        return repoId!!
    }

    override fun login(): String {
        return login!!
    }

    override fun onSetSortBy(isLastUpdated: Boolean) {
        this.isLastUpdated = isLastUpdated
    }

    override fun onItemClick(position: Int, v: View?, item: Issue) {
        val parser = getForIssue(item.htmlUrl)
        if (parser != null && view != null) {
            view!!.onOpenIssue(parser)
        }
    }

    override fun onItemLongClick(position: Int, v: View?, item: Issue) {
        if (view != null) view!!.onShowIssuePopup(item)
    }

    private fun onCallCountApi(issueState: IssueState) {
        manageDisposable(
            getObservable(
                getIssueService(isEnterprise)
                    .getIssuesWithCount(
                        RepoQueryProvider.getIssuesPullRequestQuery(
                            login!!,
                            repoId!!,
                            issueState,
                            false
                        ), 1
                    )
            )
                .subscribe({ pullRequestPageable ->
                    sendToView { view ->
                        view.onUpdateCount(
                            pullRequestPageable.totalCount
                        )
                    }
                }) { obj: Throwable -> obj.printStackTrace() })
    }

    private fun grabMoreIssues(
        issues: MutableList<Issue>,
        state: String,
        sortBy: String,
        page: Int
    ): Observable<List<Issue>> {
        var page1 = page
        return getIssueService(isEnterprise).getRepositoryIssues(
            login!!,
            repoId!!,
            state,
            sortBy,
            page1
        )
            .flatMap { issuePageable: Pageable<Issue>? ->
                if (issuePageable != null) {
                    lastPage = issuePageable.last
                    val filtered = issuePageable.items
                        ?.filter { issue: Issue -> issue.pullRequest == null }
                        ?.toList()
                    if (filtered != null) {
                        issues.addAll(filtered)
                        if (issues.size < 10 && issuePageable.next > 1 && this.issues.size < 10) {
                            page1 = currentPage + 1
                            return@flatMap grabMoreIssues(issues, state, sortBy, currentPage)
                        }
                        issues.addAll(filtered)
                        return@flatMap Observable.just<List<Issue>>(issues)
                    }
                }
                Observable.just<List<Issue>>(issues)
            }
    }
}