package com.fastaccess.ui.modules.repos.issues.issue

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.PullsIssuesParser
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.adapter.IssuesAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.repos.RepoPagerMvp.TabsBadgeListener
import com.fastaccess.ui.modules.repos.extras.popup.IssuePopupFragment
import com.fastaccess.ui.modules.repos.issues.RepoIssuesPagerMvp
import com.fastaccess.ui.modules.repos.issues.issue.details.IssuePagerActivity
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 03 Dec 2016, 3:56 PM
 */
class RepoClosedIssuesFragment : BaseFragment<RepoIssuesMvp.View, RepoIssuesPresenter>(),
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
    private var onLoadMore: OnLoadMore<IssueState>? = null
    private var adapter: IssuesAdapter? = null
    private var tabsBadgeListener: TabsBadgeListener? = null
    private var pagerCallback: RepoIssuesPagerMvp.View? = null
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
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        adapter = IssuesAdapter(presenter!!.issues, true)
        adapter!!.listener = presenter
        loadMore!!.initialize(presenter.currentPage, presenter.previousTotal)
        recycler!!.adapter = adapter
        recycler!!.addKeyLineDivider()
        recycler!!.addOnScrollListener(loadMore!!)
        if (savedInstanceState == null) {
            presenter!!.onFragmentCreated(requireArguments(), IssueState.closed)
        } else if (presenter!!.issues.isEmpty() && !presenter!!.isApiCalled) {
            onRefresh()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
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

    override var loadMore: OnLoadMore<IssueState>? = null
        get() {
            if (field == null) {
                field = object : OnLoadMore<IssueState>(presenter) {
                    override fun onScrolled(isUp: Boolean) {
                        super.onScrolled(isUp)
                        if (pagerCallback != null) pagerCallback!!.onScrolled(isUp)
                    }
                }
            }
            return field
        }
//    override fun getLoadMore(): OnLoadMore<IssueState> {
//        if (onLoadMore == null) {
//            onLoadMore = object : OnLoadMore<IssueState>(presenter) {
//                override fun onScrolled(isUp: Boolean) {
//                    super.onScrolled(isUp)
//                    if (pagerCallback != null) pagerCallback!!.onScrolled(isUp)
//                }
//            }
//        }
//        onLoadMore!!.parameter = IssueState.closed
//        return onLoadMore!!
//    }

    override fun onAddIssue() {
        //DO NOTHING
    }

    override fun onUpdateCount(totalCount: Int) {
        if (tabsBadgeListener != null) tabsBadgeListener!!.onSetBadge(1, totalCount)
    }

    private val openIssueLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it?.data?.let { data ->
            val isClose = data.extras!!.getBoolean(BundleConstant.EXTRA)
            val isOpened = data.extras!!.getBoolean(BundleConstant.EXTRA_TWO)
            if (isClose) {
                onRefresh()
            } else if (isOpened) {
                if (pagerCallback != null) pagerCallback!!.setCurrentItem(0, true)
                onRefresh()
            }
        }

    }

    override fun onOpenIssue(parser: PullsIssuesParser) {
        val intent = IssuePagerActivity.createIntent(
            requireContext(), parser.repoId!!, parser.login!!,
            parser.number, false, isEnterprise
        )
        openIssueLauncher.launch(intent)
    }

    override fun onRefresh(isLastUpdated: Boolean) {
        presenter!!.onSetSortBy(isLastUpdated)
        presenter!!.onCallApi(1, IssueState.closed)
    }

    override fun onShowIssuePopup(item: Issue) {
        IssuePopupFragment.showPopup(childFragmentManager, item)
    }

    override fun onRefresh() {
        presenter!!.onCallApi(1, IssueState.closed)
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
        fun newInstance(repoId: String, login: String): RepoClosedIssuesFragment {
            val view = RepoClosedIssuesFragment()
            view.arguments = Bundler.start()
                .put(BundleConstant.ID, repoId)
                .put(BundleConstant.EXTRA, login)
                .end()
            return view
        }
    }
}