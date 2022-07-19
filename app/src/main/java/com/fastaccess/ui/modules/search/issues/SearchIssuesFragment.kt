package com.fastaccess.ui.modules.search.issues

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.entity.Issue
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.adapter.IssuesAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.delegate.viewFind
import com.fastaccess.ui.modules.repos.extras.popup.IssuePopupFragment
import com.fastaccess.ui.modules.search.SearchMvp
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 03 Dec 2016, 3:56 PM
 */
class SearchIssuesFragment : BaseFragment<SearchIssuesMvp.View, SearchIssuesPresenter>(),
    SearchIssuesMvp.View {
    @State
    var searchQuery = ""

    val recycler: DynamicRecyclerView? by viewFind(R.id.recycler)
    val refresh: SwipeRefreshLayout? by viewFind(R.id.refresh)
    val stateLayout: StateLayout? by viewFind(R.id.stateLayout)
    val fastScroller: RecyclerViewFastScroller? by viewFind(R.id.fastScroller)
    private var onLoadMore: OnLoadMore<String>? = null
    private var adapter: IssuesAdapter? = null
    private var countCallback: SearchMvp.View? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SearchMvp.View) {
            countCallback = context
        }
    }

    override fun onDetach() {
        countCallback = null
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

    override fun onSetTabCount(count: Int) {
        if (countCallback != null) countCallback!!.onSetCount(count, 2)
    }

    override fun fragmentLayout(): Int {
        return R.layout.micro_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        stateLayout!!.setEmptyText(R.string.no_search_results)
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        adapter = IssuesAdapter(presenter!!.issues,
            withAvatar = false,
            showRepoName = true,
            showState = true
        )
        adapter!!.listener = presenter
        recycler!!.adapter = adapter
        recycler!!.addDivider()
        if (!isEmpty(searchQuery) && presenter!!.issues.isEmpty() && !presenter!!.isApiCalled) {
            onRefresh()
        }
        if (isEmpty(searchQuery)) {
            stateLayout!!.showEmptyState()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun providePresenter(): SearchIssuesPresenter {
        return SearchIssuesPresenter()
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

    override fun onSetSearchQuery(query: String) {
        searchQuery = query
        loadMore.reset()
        adapter!!.clear()
        if (!isEmpty(query)) {
            recycler!!.removeOnScrollListener(loadMore)
            recycler!!.addOnScrollListener(loadMore)
            onRefresh()
        }
    }

    override fun onQueueSearch(query: String) {
        searchQuery = query
        if (view != null) onSetSearchQuery(query)
    }

    override val loadMore: OnLoadMore<String>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter, searchQuery)
            }
            onLoadMore!!.parameter = searchQuery
            return onLoadMore!!
        }

    override fun onShowPopupDetails(item: Issue) {
        IssuePopupFragment.showPopup(childFragmentManager, item)
    }

    override fun onRefresh() {
        if (searchQuery.isEmpty()) {
            refresh!!.isRefreshing = false
            return
        }
        presenter!!.onCallApi(1, searchQuery)
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
        fun newInstance(): SearchIssuesFragment {
            return SearchIssuesFragment()
        }
    }
}