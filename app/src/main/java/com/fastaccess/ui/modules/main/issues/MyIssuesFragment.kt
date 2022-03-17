package com.fastaccess.ui.modules.main.issues

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.data.dao.types.MyIssuesType
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.adapter.IssuesAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.repos.RepoPagerMvp.TabsBadgeListener
import com.fastaccess.ui.modules.repos.extras.popup.IssuePopupFragment
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 25 Mar 2017, 11:48 PM
 */
class MyIssuesFragment : BaseFragment<MyIssuesMvp.View, MyIssuesPresenter>(), MyIssuesMvp.View {
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

    @JvmField
    @State
    var issueState: IssueState? = null
    private var onLoadMore: OnLoadMore<IssueState>? = null
    private var adapter: IssuesAdapter? = null
    private var issuesType: MyIssuesType? = null
        get() {
            if (field == null) {
                field = requireArguments().getSerializable(BundleConstant.EXTRA_TWO) as MyIssuesType?
            }
            return field
        }
    private var tabsBadgeListener: TabsBadgeListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
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

    override fun onRefresh() {
        presenter!!.onCallApi(1, issueState)
    }

    override fun onClick(view: View) {
        onRefresh()
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

    override val loadMore: OnLoadMore<IssueState>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter)
            }
            onLoadMore!!.parameter = issueState
            return onLoadMore!!
        }

    override fun fragmentLayout(): Int {
        return R.layout.micro_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            issueState = requireArguments().getSerializable(BundleConstant.EXTRA) as IssueState?
        }
        presenter!!.onSetIssueType(issuesType!!)
        stateLayout!!.setEmptyText(R.string.no_issues)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        adapter = IssuesAdapter(presenter!!.issues, false, true)
        adapter!!.listener = presenter
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        recycler!!.adapter = adapter
        recycler!!.addDivider()
        recycler!!.addOnScrollListener(loadMore)
        if (savedInstanceState == null || presenter!!.issues.isEmpty() && !presenter!!.isApiCalled) {
            onRefresh()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun providePresenter(): MyIssuesPresenter {
        return MyIssuesPresenter()
    }

    override fun onSetCount(totalCount: Int) {
        if (tabsBadgeListener != null) {
            when (issuesType) {
                MyIssuesType.CREATED -> tabsBadgeListener!!.onSetBadge(0, totalCount)
                MyIssuesType.ASSIGNED -> tabsBadgeListener!!.onSetBadge(1, totalCount)
                MyIssuesType.MENTIONED -> tabsBadgeListener!!.onSetBadge(2, totalCount)
                MyIssuesType.PARTICIPATED -> tabsBadgeListener!!.onSetBadge(3, totalCount)
                else -> {}
            }
        }
    }

    override fun onFilterIssue(issueState: IssueState) {
        if (this.issueState != null && this.issueState !== issueState) {
            this.issueState = issueState
            requireArguments().putSerializable(BundleConstant.ITEM, issueState)
            loadMore.reset()
            adapter!!.clear()
            onRefresh()
        }
    }

    override fun onShowPopupDetails(item: Issue) {
        IssuePopupFragment.showPopup(childFragmentManager, item)
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
        fun newInstance(issueState: IssueState, issuesType: MyIssuesType): MyIssuesFragment {
            val view = MyIssuesFragment()
            view.arguments = Bundler.start()
                .put(BundleConstant.EXTRA, issueState)
                .put(BundleConstant.EXTRA_TWO, issuesType)
                .end()
            return view
        }
    }
}