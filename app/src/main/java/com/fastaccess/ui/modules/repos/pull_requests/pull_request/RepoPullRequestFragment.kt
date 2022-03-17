package com.fastaccess.ui.modules.repos.pull_requests.pull_request

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.PullsIssuesParser
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.adapter.PullRequestAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.repos.RepoPagerMvp.TabsBadgeListener
import com.fastaccess.ui.modules.repos.extras.popup.IssuePopupFragment
import com.fastaccess.ui.modules.repos.pull_requests.RepoPullRequestPagerMvp
import com.fastaccess.ui.modules.repos.pull_requests.pull_request.details.PullRequestPagerActivity
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 03 Dec 2016, 3:56 PM
 */
class RepoPullRequestFragment : BaseFragment<RepoPullRequestMvp.View, RepoPullRequestPresenter>(),
    RepoPullRequestMvp.View {
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
    private var adapter: PullRequestAdapter? = null
    private var pagerCallback: RepoPullRequestPagerMvp.View? = null
    private var tabsBadgeListener: TabsBadgeListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (parentFragment is RepoPullRequestPagerMvp.View) {
            pagerCallback = parentFragment as RepoPullRequestPagerMvp.View?
        } else if (context is RepoPullRequestPagerMvp.View) {
            pagerCallback = context
        }
        if (parentFragment is TabsBadgeListener) {
            tabsBadgeListener = parentFragment as TabsBadgeListener?
        } else if (context is TabsBadgeListener) {
            tabsBadgeListener = context
        }
    }

    override fun onDetach() {
        tabsBadgeListener = null
        super.onDetach()
    }

    override fun onNotifyAdapter(items: List<PullRequest>?, page: Int) {
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
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        adapter = PullRequestAdapter(presenter!!.pullRequests, true)
        adapter!!.listener = presenter
        loadMore.initialize(presenter.currentPage, presenter.previousTotal)
        recycler!!.adapter = adapter
        recycler!!.addKeyLineDivider()
        recycler!!.addOnScrollListener(loadMore)
        if (savedInstanceState == null) {
            presenter!!.onFragmentCreated(requireArguments())
        } else if (presenter!!.pullRequests.isEmpty() && !presenter!!.isApiCalled) {
            onRefresh()
        }
        stateLayout!!.setEmptyText(R.string.no_pull_requests)
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun providePresenter(): RepoPullRequestPresenter {
        return RepoPullRequestPresenter()
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

    override fun getLoadMore(): OnLoadMore<IssueState> {
        if (onLoadMore == null) {
            onLoadMore = object : OnLoadMore<IssueState>(presenter) {
                override fun onScrolled(isUp: Boolean) {
                    super.onScrolled(isUp)
                    if (pagerCallback != null) pagerCallback!!.onScrolled(isUp)
                }
            }
        }
        onLoadMore!!.parameter = issueState
        return onLoadMore!!
    }

    override fun onUpdateCount(totalCount: Int) {
        if (tabsBadgeListener != null) tabsBadgeListener!!.onSetBadge(
            if (presenter!!.getIssueState() === IssueState.open) 0 else 1,
            totalCount
        )
    }

    private val openPullLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it?.data?.let { data ->
            val isClose = data.extras!!.getBoolean(BundleConstant.EXTRA)
            val isOpened = data.extras!!.getBoolean(BundleConstant.EXTRA_TWO)
            if (isClose || isOpened) {
                onRefresh()
            }
        }
    }

    override fun onOpenPullRequest(parser: PullsIssuesParser) {
        val intent = PullRequestPagerActivity.createIntent(
            requireContext(), parser.repoId!!, parser.login!!,
            parser.number, false, isEnterprise
        )
        openPullLauncher.launch(intent)
    }

    override fun onShowPullRequestPopup(item: PullRequest) {
        IssuePopupFragment.showPopup(childFragmentManager, item)
    }

    override fun onRefresh() {
        presenter!!.onCallApi(1, issueState)
    }

    override fun onClick(view: View) {
        onRefresh()
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
        if (recycler != null) recycler!!.scrollToPosition(0)
    }

    private val issueState: IssueState?
        get() = requireArguments().getSerializable(BundleConstant.EXTRA_TWO) as IssueState?

    private fun showReload() {
        hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }

    companion object {
        fun newInstance(
            repoId: String,
            login: String,
            issueState: IssueState
        ): RepoPullRequestFragment {
            val view = RepoPullRequestFragment()
            view.arguments = Bundler.start()
                .put(BundleConstant.ID, repoId)
                .put(BundleConstant.EXTRA, login)
                .put(BundleConstant.EXTRA_TWO, issueState)
                .end()
            return view
        }
    }
}