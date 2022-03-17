package com.fastaccess.ui.modules.filter.issues.fragment

import android.annotation.SuppressLint
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
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.adapter.IssuesAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.modules.filter.issues.FilterIssuesActivityMvp
import com.fastaccess.ui.modules.repos.extras.popup.IssuePopupFragment
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 09 Apr 2017, 7:13 PM
 */
class FilterIssueFragment : BaseFragment<FilterIssuesMvp.View, FilterIssuePresenter>(),
    FilterIssuesMvp.View {
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
    private var onLoadMore: OnLoadMore<String>? = null
    private var adapter: IssuesAdapter? = null

    @JvmField
    @State
    var issueState = IssueState.open

    @JvmField
    @State
    var isIssue = false

    @JvmField
    @State
    var query: String? = null
    override var callback: BaseMvp.FAView? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as FilterIssuesActivityMvp.View
    }

    override fun onDetach() {
        callback = null
        super.onDetach()
    }

    override fun onRefresh() {
        if (!isEmpty(query)) {
            presenter!!.onCallApi(1, query)
        }
    }

    override fun onClick(v: View) {
        onRefresh()
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onClear() {
        hideProgress()
        presenter!!.issues.clear()
        adapter!!.notifyDataSetChanged()
    }

    override fun onSearch(query: String, isOpen: Boolean, isIssue: Boolean, isEnterprise: Boolean) {
        presenter!!.isEnterprise = isEnterprise
        this.query = query
        issueState = if (isOpen) IssueState.open else IssueState.closed
        this.isIssue = isIssue
        onClear()
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

    override val loadMore: OnLoadMore<String>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter)
            }
            onLoadMore!!.parameter = query
            return onLoadMore!!
        }

    override fun onSetCount(totalCount: Int) {
        if (callback != null) {
            (callback!! as FilterIssuesActivityMvp.View).onSetCount(totalCount, issueState === IssueState.open)
        }
    }

    override fun onItemClicked(item: Issue) {
        launchUri(requireContext(), item.htmlUrl)
    }

    override fun fragmentLayout(): Int {
        return R.layout.micro_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        stateLayout!!.setEmptyText(R.string.no_search_results)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        adapter = IssuesAdapter(presenter!!.issues, true, false, true)
        adapter!!.listener = presenter
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        recycler!!.adapter = adapter
        recycler!!.addOnScrollListener(loadMore)
        recycler!!.addKeyLineDivider()
        if (savedInstanceState != null) {
            if (!isEmpty(query) && presenter!!.issues.isEmpty()) {
                onRefresh()
            }
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun providePresenter(): FilterIssuePresenter {
        return FilterIssuePresenter()
    }

    override fun onShowPopupDetails(item: Issue) {
        IssuePopupFragment.showPopup(childFragmentManager, item)
    }

    private fun showReload() {
        hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }
}