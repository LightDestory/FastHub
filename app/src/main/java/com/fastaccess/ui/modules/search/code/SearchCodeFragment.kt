package com.fastaccess.ui.modules.search.code

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.SearchCodeModel
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.adapter.SearchCodeAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.search.SearchMvp
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 03 Dec 2016, 3:56 PM
 */
class SearchCodeFragment : BaseFragment<SearchCodeMvp.View, SearchCodePresenter>(),
    SearchCodeMvp.View {
    @JvmField
    @State
    var searchQuery = ""

    @JvmField
    @State
    var showRepoName = false

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
    private var adapter: SearchCodeAdapter? = null
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

    override fun onNotifyAdapter(items: List<SearchCodeModel>?, page: Int) {
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
        if (countCallback != null) countCallback!!.onSetCount(count, 3)
    }

    override fun fragmentLayout(): Int {
        return R.layout.small_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        stateLayout!!.setEmptyText(R.string.no_search_results)
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        adapter = SearchCodeAdapter(presenter!!.codes)
        adapter!!.showRepoName(showRepoName)
        adapter!!.listener = presenter
        recycler!!.adapter = adapter
        recycler!!.addDivider()
        if (!isEmpty(searchQuery) && presenter!!.codes.isEmpty() && !presenter!!.isApiCalled) {
            onRefresh()
        }
        if (isEmpty(searchQuery)) {
            stateLayout!!.showEmptyState()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun providePresenter(): SearchCodePresenter {
        return SearchCodePresenter()
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

    override fun onSetSearchQuery(query: String, showRepoName: Boolean) {
        searchQuery = query
        this.showRepoName = showRepoName
        loadMore.reset()
        adapter!!.clear()
        adapter!!.showRepoName(showRepoName)
        if (!isEmpty(query)) {
            recycler!!.removeOnScrollListener(loadMore)
            recycler!!.addOnScrollListener(loadMore)
            onRefresh()
        }
    }

    override fun onQueueSearch(query: String) {
        searchQuery = query
        if (view != null) onSetSearchQuery(query, false)
    }

    override fun onQueueSearch(query: String, showRepoName: Boolean) {
        searchQuery = query
        if (view != null) onSetSearchQuery(query, showRepoName)
    }

    override val loadMore: OnLoadMore<String>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter, searchQuery)
            }
            onLoadMore!!.parameter = searchQuery
            return onLoadMore!!
        }

    override fun onItemClicked(item: SearchCodeModel) {
        if (item.url != null) {
            launchUri(requireContext(), item.htmlUrl!!)
        } else {
            showErrorMessage(getString(R.string.no_url))
        }
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
        fun newInstance(): SearchCodeFragment {
            return SearchCodeFragment()
        }
    }
}