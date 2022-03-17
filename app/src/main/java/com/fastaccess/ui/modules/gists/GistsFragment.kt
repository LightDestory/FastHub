package com.fastaccess.ui.modules.gists

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.adapter.GistsAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 11 Nov 2016, 12:36 PM
 */
class GistsFragment : BaseFragment<GistsMvp.View, GistsPresenter>(), GistsMvp.View {
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
    private var adapter: GistsAdapter? = null
    private var onLoadMore: OnLoadMore<Gist>? = null
    override fun fragmentLayout(): Int {
        return R.layout.small_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        refresh!!.setOnRefreshListener(this)
        stateLayout!!.setOnReloadListener(this)
        stateLayout!!.setEmptyText(R.string.no_gists)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        adapter = GistsAdapter(presenter!!.gists)
        adapter!!.listener = presenter
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        recycler!!.adapter = adapter
        recycler!!.addKeyLineDivider()
        recycler!!.addOnScrollListener(loadMore)
        if (presenter!!.gists.isEmpty() && !presenter!!.isApiCalled) {
            onRefresh()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun onRefresh() {
        presenter!!.onCallApi(1, null)
    }

    override fun onNotifyAdapter(items: List<Gist>?, page: Int) {
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

    override fun showProgress(@StringRes resId: Int) {
        refresh!!.isRefreshing = true
        stateLayout!!.showProgress()
    }

    override fun hideProgress() {
        refresh!!.isRefreshing = false
        stateLayout!!.hideProgress()
    }

    override fun showErrorMessage(msgRes: String) {
        showReload()
        super.showErrorMessage(msgRes)
    }

    override fun showMessage(titleRes: Int, msgRes: Int) {
        showReload()
        super.showMessage(titleRes, msgRes)
    }

    override fun providePresenter(): GistsPresenter {
        return GistsPresenter()
    }

    override val loadMore: OnLoadMore<Gist>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore((presenter as GistsPresenter))
            }
            return onLoadMore!!
        }

    override fun onDestroyView() {
        recycler!!.removeOnScrollListener(loadMore)
        super.onDestroyView()
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
        val TAG: String = GistsFragment::class.java.simpleName
        fun newInstance(): GistsFragment {
            return GistsFragment()
        }
    }
}