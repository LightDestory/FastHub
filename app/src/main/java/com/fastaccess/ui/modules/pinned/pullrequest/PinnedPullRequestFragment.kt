package com.fastaccess.ui.modules.pinned.pullrequest

import android.os.Bundle
import android.view.View
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.PinnedPullRequests
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.ui.adapter.PullRequestAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.widgets.AppbarRefreshLayout
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 25 Mar 2017, 8:04 PM
 */
class PinnedPullRequestFragment :
    BaseFragment<PinnedPullRequestMvp.View, PinnedPullRequestPresenter>(),
    PinnedPullRequestMvp.View {
    @JvmField
    @BindView(R.id.recycler)
    var recycler: DynamicRecyclerView? = null

    @JvmField
    @BindView(R.id.refresh)
    var refresh: AppbarRefreshLayout? = null

    @JvmField
    @BindView(R.id.stateLayout)
    var stateLayout: StateLayout? = null

    @JvmField
    @BindView(R.id.fastScroller)
    var fastScroller: RecyclerViewFastScroller? = null
    private var adapter: PullRequestAdapter? = null
    override fun onNotifyAdapter(items: List<PullRequest>?) {
        refresh!!.isRefreshing = false
        stateLayout!!.hideProgress()
        if (items != null) adapter!!.insertItems(items) else adapter!!.clear()
    }

    override fun onDeletePinnedPullRequest(id: Long, position: Int) {
        newInstance(
            getString(R.string.delete), getString(R.string.confirm_message),
            Bundler.start().put(BundleConstant.YES_NO_EXTRA, true)
                .put(BundleConstant.EXTRA, position)
                .put(BundleConstant.ID, id)
                .end()
        )
            .show(childFragmentManager, MessageDialogView.TAG)
    }

    override fun fragmentLayout(): Int {
        return R.layout.small_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        adapter = PullRequestAdapter(presenter!!.pinnedPullRequest, true, true)
        adapter!!.listener = presenter
        stateLayout!!.setEmptyText(getString(R.string.no_pull_requests))
        recycler!!.setEmptyView(stateLayout!!, refresh)
        recycler!!.adapter = adapter
        recycler!!.addKeyLineDivider()
        refresh!!.setOnRefreshListener { presenter!!.onReload() }
        stateLayout!!.setOnReloadListener { presenter!!.onReload() }
        if (savedInstanceState == null) {
            stateLayout!!.showProgress()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun providePresenter(): PinnedPullRequestPresenter {
        return PinnedPullRequestPresenter()
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (bundle != null && isOk) {
            val id = bundle.getLong(BundleConstant.ID)
            val position = bundle.getInt(BundleConstant.EXTRA)
            PinnedPullRequests.delete(id)
            adapter!!.removeItem(position)
        }
    }

    companion object {
        val TAG: String = PinnedPullRequestFragment::class.java.simpleName
        fun newInstance(): PinnedPullRequestFragment {
            return PinnedPullRequestFragment()
        }
    }
}