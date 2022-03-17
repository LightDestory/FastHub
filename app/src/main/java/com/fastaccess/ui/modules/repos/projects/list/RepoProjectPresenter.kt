package com.fastaccess.ui.modules.repos.projects.list

import android.os.Bundle
import android.view.View
import com.apollographql.apollo3.api.Optional
import com.apollographql.apollo3.rx2.rxFlowable
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.github.OrgProjectsClosedQuery
import com.fastaccess.github.OrgProjectsOpenQuery
import com.fastaccess.github.RepoProjectsClosedQuery
import com.fastaccess.github.RepoProjectsOpenQuery
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Logger
import com.fastaccess.provider.rest.ApolloProdivder
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.repos.projects.details.ProjectPagerActivity
import io.reactivex.Observable
import io.reactivex.disposables.Disposable

/**
 * Created by kosh on 09/09/2017.
 */
class RepoProjectPresenter : BasePresenter<RepoProjectMvp.View>(), RepoProjectMvp.Presenter {
    private val projects = arrayListOf<RepoProjectsOpenQuery.Node>()
    override var currentPage: Int = 0
    override var previousTotal: Int = 0
    private var lastPage = Integer.MAX_VALUE

    @com.evernote.android.state.State
    var login: String = ""

    @com.evernote.android.state.State
    var repoId: String? = null
    var count: Int = 0
    val pages = arrayListOf<String>()

    override fun onItemClick(position: Int, v: View?, item: RepoProjectsOpenQuery.Node) {
        v ?: return
        item.databaseId?.let {
            ProjectPagerActivity.startActivity(v.context, login, repoId, it.toLong(), isEnterprise)
        }
    }

    override fun onItemLongClick(position: Int, v: View?, item: RepoProjectsOpenQuery.Node) {}

    override fun onFragmentCreate(bundle: Bundle?) {
        bundle?.let {
            repoId = it.getString(BundleConstant.ID)
            login = it.getString(BundleConstant.EXTRA)!!
        }
    }

    override fun getProjects(): ArrayList<RepoProjectsOpenQuery.Node> = projects

    override fun onCallApi(page: Int, parameter: IssueState?): Boolean {
        if (page == 1) {
            lastPage = Integer.MAX_VALUE
            sendToView { view -> view.getLoadMore().reset() }
        }
        if (page > lastPage || lastPage == 0 || parameter == null) {
            sendToView { it.hideProgress() }
            return false
        }
        currentPage = page
        Logger.e(login)
        val repoId = repoId
        val apollo = ApolloProdivder.getApollo(isEnterprise)
        val task: Disposable?
        if (repoId != null && !repoId.isNullOrBlank()) {
            if (parameter == IssueState.open) {
                val query = RepoProjectsOpenQuery(
                    login,
                    repoId,
                    getPage()
                )
                task = apollo.query(query).rxFlowable().map { it ->
                    val list = arrayListOf<RepoProjectsOpenQuery.Node>()
                    val repo = it.data?.repository!!
                    repo.projects.let { projects1 ->
                        lastPage = if (projects1.pageInfo.hasNextPage) Int.MAX_VALUE else 0
                        pages.clear()
                        count = projects1.totalCount
                        projects1.edges?.let { it1 ->
                            pages.addAll(it1.map { it?.cursor.toString() })
                        }
                        projects1.nodes?.let { it1 ->
                            list.addAll(it1.map { it!! })
                        }
                    }
                    Observable.just(list)
                }.subscribe {
                    makeRestCall(it) {
                        sendToView { v ->
                            v.onNotifyAdapter(it, page)
                            if (page == 1) v.onChangeTotalCount(count)
                        }
                    }
                }
            } else {
                val query = RepoProjectsClosedQuery(
                    login,
                    repoId,
                    getPage()
                )
                task = apollo.query(query).rxFlowable().map { apolloResponse ->
                    val list = arrayListOf<RepoProjectsOpenQuery.Node>()
                    val repos = apolloResponse.data?.repository!!
                    repos.projects.let { projects1 ->
                        lastPage = if (projects1.pageInfo.hasNextPage) Int.MAX_VALUE else 0
                        pages.clear()
                        count = projects1.totalCount
                        projects1.edges?.let { list1 ->
                            pages.addAll(list1.map { it?.cursor!! })
                        }
                        projects1.nodes?.let { list1 ->
                            val toConvert = arrayListOf<RepoProjectsOpenQuery.Node>()
                            list1.onEach {
                                val columns = RepoProjectsOpenQuery.Columns(
                                    it?.columns?.totalCount!!
                                )
                                val node = RepoProjectsOpenQuery.Node(
                                    it.name,
                                    it.number,
                                    it.body,
                                    it.createdAt,
                                    it.id,
                                    it.viewerCanUpdate,
                                    columns,
                                    it.databaseId
                                )
                                toConvert.add(node)
                            }
                            list.addAll(toConvert)
                        }
                    }
                    Observable.just(list)
                }.subscribe {
                    makeRestCall(it) {
                        sendToView { v ->
                            v.onNotifyAdapter(it, page)
                            if (page == 1) v.onChangeTotalCount(count)
                        }
                    }
                }
            }
        } else {
            if (parameter == IssueState.open) {
                val query = OrgProjectsOpenQuery(login, getPage())
                task = apollo.query(query).rxFlowable().map { response ->
                    val list = arrayListOf<RepoProjectsOpenQuery.Node>()
                    response.data?.organization?.let { organization ->
                        organization.projects.let { projects1 ->
                            lastPage = if (projects1.pageInfo.hasNextPage) Int.MAX_VALUE else 0
                            pages.clear()
                            count = projects1.totalCount
                            projects1.edges?.let { list1 ->
                                pages.addAll(list1.map { it?.cursor!! })
                            }
                            projects1.nodes?.let { list1 ->
                                val toConvert = arrayListOf<RepoProjectsOpenQuery.Node>()
                                list1.onEach {
                                    val columns = RepoProjectsOpenQuery.Columns(
                                        it?.columns?.totalCount!!
                                    )
                                    val node = RepoProjectsOpenQuery.Node(
                                        it.name,
                                        it.number,
                                        it.body,
                                        it.createdAt,
                                        it.id,
                                        it.viewerCanUpdate,
                                        columns,
                                        it.databaseId
                                    )
                                    toConvert.add(node)
                                }
                                list.addAll(toConvert)
                            }
                        }
                    }
                    Observable.just(list)
                }.subscribe {
                    makeRestCall(it) {
                        sendToView { v ->
                            v.onNotifyAdapter(it, page)
                            if (page == 1) v.onChangeTotalCount(count)
                        }
                    }
                }
            } else {
                val query = OrgProjectsClosedQuery(login, getPage())
                task = apollo.query(query).rxFlowable().map { response ->
                    val list = arrayListOf<RepoProjectsOpenQuery.Node>()
                    val organization = response.data?.organization!!
                    organization.projects.let { projects1 ->
                        lastPage = if (projects1.pageInfo.hasNextPage) Int.MAX_VALUE else 0
                        pages.clear()
                        count = projects1.totalCount
                        projects1.edges?.let { list1 ->
                            pages.addAll(list1.map { it?.cursor!! })
                        }
                        projects1.nodes?.let { list1 ->
                            val toConvert = arrayListOf<RepoProjectsOpenQuery.Node>()
                            list1.onEach {
                                val columns = RepoProjectsOpenQuery.Columns(
                                    it?.columns?.totalCount!!
                                )
                                val node = RepoProjectsOpenQuery.Node(
                                    it.name,
                                    it.number,
                                    it.body,
                                    it.createdAt,
                                    it.id,
                                    it.viewerCanUpdate,
                                    columns,
                                    it.databaseId
                                )
                                toConvert.add(node)
                            }
                            list.addAll(toConvert)
                        }
                    }
                    Observable.just(list)
                }.subscribe {
                    makeRestCall(it) {
                        sendToView { v ->
                            v.onNotifyAdapter(it, page)
                            if (page == 1) v.onChangeTotalCount(count)
                        }
                    }
                }
            }
        }
        manageDisposable(task)
        return true
    }

    private fun getPage(): Optional<String> {
        val result = if (pages.isNotEmpty()) pages.last() else null
        return Optional.presentIfNotNull(result)
    }
}