package com.fastaccess.ui.modules.gists.starred

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.helper.BundleConstant
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.adapter.GistsAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.gists.gist.GistActivity.Companion.createIntent
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 11 Nov 2016, 12:36 PM
 */
class StarredGistsFragment : BaseFragment<StarredGistsMvp.View, StarredGistsPresenter>(),
    StarredGistsMvp.View {
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
    private var onLoadMore: OnLoadMore<String>? = null
    override fun fragmentLayout(): Int {
        return R.layout.small_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        stateLayout!!.setEmptyText(R.string.no_gists)
        refresh!!.setOnRefreshListener(this)
        stateLayout!!.setOnReloadListener(this)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        adapter = GistsAdapter(presenter!!.gists, true)
        adapter!!.listener = presenter
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        recycler!!.adapter = adapter
        recycler!!.addOnScrollListener(loadMore)
        recycler!!.addDivider()
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

    override fun providePresenter(): StarredGistsPresenter {
        return StarredGistsPresenter()
    }

    override val loadMore: OnLoadMore<String>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter, null)
            }
            return onLoadMore!!
        }

    override fun onStartGistView(gistId: String) {
        startActivityForResult(
            createIntent(requireContext(), gistId, isEnterprise),
            BundleConstant.REQUEST_CODE
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == BundleConstant.REQUEST_CODE) {
            if (data != null && data.extras != null) {
                val gistsModel: Gist? = data.extras!!.getParcelable(BundleConstant.ITEM)
                if (gistsModel != null && adapter != null) {
                    adapter!!.removeItem(gistsModel)
                }
            } else {
                onRefresh()
            }
        }
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
        fun newInstance(): StarredGistsFragment {
            return StarredGistsFragment()
        }
    }
}