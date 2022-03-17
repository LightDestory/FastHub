package com.fastaccess.ui.modules.repos.issues.issue

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.PullsIssuesParser
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.adapter.IssuesAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.repos.RepoPagerMvp.TabsBadgeListener
import com.fastaccess.ui.modules.repos.extras.popup.IssuePopupFragment
import com.fastaccess.ui.modules.repos.issues.RepoIssuesPagerMvp
import com.fastaccess.ui.modules.repos.issues.create.CreateIssueActivity
import com.fastaccess.ui.modules.repos.issues.issue.details.IssuePagerActivity
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 03 Dec 2016, 3:56 PM
 */
class RepoOpenedIssuesFragment : BaseFragment<RepoIssuesMvp.View, RepoIssuesPresenter>(),
    RepoIssuesMvp.View {
    @JvmField
    @BindView(R.id.recycler)
    var recycler: DynamicRecyclerView? = null

    @JvmField
    @BindView(R.id.refresh)
    var refresh: SwipeRefreshLayout? = null

    @JvmField
    @BindView(R.id.stateLayout)
    var stateLayout: StateLayout? = null

    @JvmField
    @BindView(R.id.fastScroller)
    var fastScroller: RecyclerViewFastScroller? = null
    private var adapter: IssuesAdapter? = null
    private var pagerCallback: RepoIssuesPagerMvp.View? = null
    private var tabsBadgeListener: TabsBadgeListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is RepoIssuesPagerMvp.View) {
            pagerCallback = parentFragment as RepoIssuesPagerMvp.View?
        } else if (context is RepoIssuesPagerMvp.View) {
            pagerCallback = context
        }
        if (parentFragment is TabsBadgeListener) {
            tabsBadgeListener = parentFragment as TabsBadgeListener?
        } else if (context is TabsBadgeListener) {
            tabsBadgeListener = context
        }
    }

    override fun onDetach() {
        pagerCallback = null
        tabsBadgeListener = null
        super.onDetach()
    }

    override fun onNotifyAdapter(items: List<Issue>?, page: Int) {
        hideProgress()
        if (items == null || items.isEmpty()) {
            adapter!!.clear()
            return
        }
        if (page <= 1) {
            adapter!!.insertItems(items)
        } else {
            adapter!!.addItems(items)
        }
    }

    override fun fragmentLayout(): Int {
        return R.layout.micro_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        if (arguments == null) {
            throw NullPointerException("Bundle is null, therefore, issues can't be proceeded.")
        }
        stateLayout!!.setEmptyText(R.string.no_issues)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        adapter = IssuesAdapter(presenter!!.issues, true)
        adapter!!.listener = presenter
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        recycler!!.adapter = adapter
        recycler!!.addKeyLineDivider()
        recycler!!.addOnScrollListener(loadMore)
        if (savedInstanceState == null) {
            presenter!!.onFragmentCreated(requireArguments(), IssueState.open)
        } else if (presenter!!.issues.isEmpty() && !presenter!!.isApiCalled) {
            onRefresh()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == BundleConstant.REQUEST_CODE) {
                onRefresh()
                if (pagerCallback != null) pagerCallback!!.setCurrentItem(0, false)
            } else if (requestCode == RepoIssuesMvp.ISSUE_REQUEST_CODE && data != null) {
                val isClose = data.extras!!.getBoolean(BundleConstant.EXTRA)
                val isOpened = data.extras!!.getBoolean(BundleConstant.EXTRA_TWO)
                if (isClose) {
                    if (pagerCallback != null) pagerCallback!!.setCurrentItem(1, true)
                    onRefresh()
                } else if (isOpened) {
                    onRefresh()
                } //else ignore!
            }
        }
    }

    override fun providePresenter(): RepoIssuesPresenter {
        return RepoIssuesPresenter()
    }

    override fun hideProgress() {
        refresh!!.isRefreshing = false
        stateLayout!!.hideProgress()
    }

    override fun showProgress(@StringRes resId: Int) {
        refresh!!.isRefreshing = true
        stateLayout!!.showProgress()
    }

    override fun showErrorMessage(msgRes: String) {
        showReload()
        super.showErrorMessage(msgRes)
    }

    override fun showMessage(titleRes: Int, msgRes: Int) {
        showReload()
        super.showMessage(titleRes, msgRes)
    }

    override val loadMore: OnLoadMore<IssueState> by lazy {
        object : OnLoadMore<IssueState>(presenter) {
            override fun onScrolled(isUp: Boolean) {
                super.onScrolled(isUp)
                if (pagerCallback != null) pagerCallback!!.onScrolled(isUp)
            }
        }
    }

    override fun onAddIssue() {
        val login = presenter!!.login()
        val repoId = presenter!!.repoId()
        if (!isEmpty(login) && !isEmpty(repoId)) {
            CreateIssueActivity.startForResult(this, login, repoId, isEnterprise)
        }
    }

    override fun onUpdateCount(totalCount: Int) {
        if (tabsBadgeListener != null) tabsBadgeListener!!.onSetBadge(0, totalCount)
    }

    override fun onOpenIssue(parser: PullsIssuesParser) {
        startActivityForResult(
            IssuePagerActivity.createIntent(
                requireContext(), parser.repoId!!, parser.login!!,
                parser.number, false, isEnterprise
            ), RepoIssuesMvp.ISSUE_REQUEST_CODE
        )
    }

    override fun onRefresh(isLastUpdated: Boolean) {
        presenter!!.onSetSortBy(isLastUpdated)
        presenter!!.onCallApi(1, IssueState.open)
    }

    override fun onShowIssuePopup(item: Issue) {
        IssuePopupFragment.showPopup(childFragmentManager, item)
    }

    override fun onRefresh() {
        presenter!!.onCallApi(1, IssueState.open)
    }

    override fun onClick(view: View) {
        onRefresh()
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
        if (recycler != null) recycler!!.scrollToPosition(0)
    }

    private fun showReload() {
        hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }

    companion object {
        fun newInstance(repoId: String, login: String): RepoOpenedIssuesFragment {
            val view = RepoOpenedIssuesFragment()
            view.arguments = Bundler.start()
                .put(BundleConstant.ID, repoId)
                .put(BundleConstant.EXTRA, login)
                .end()
            return view
        }
    }
}