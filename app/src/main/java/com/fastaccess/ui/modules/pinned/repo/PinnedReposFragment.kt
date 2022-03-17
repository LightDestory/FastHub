package com.fastaccess.ui.modules.pinned.repo

import android.os.Bundle
import android.view.View
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.AbstractPinnedRepos
import com.fastaccess.data.dao.model.PinnedRepos
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.ui.adapter.PinnedReposAdapter
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
class PinnedReposFragment : BaseFragment<PinnedReposMvp.View, PinnedReposPresenter>(),
    PinnedReposMvp.View {
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
    private var adapter: PinnedReposAdapter? = null
    override fun onNotifyAdapter(items: List<PinnedRepos>?) {
        refresh!!.isRefreshing = false
        stateLayout!!.hideProgress()
        if (items != null) adapter!!.insertItems(items) else adapter!!.clear()
    }

    override fun onDeletePinnedRepo(id: Long, position: Int) {
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
        adapter = PinnedReposAdapter(presenter!!.pinnedRepos, presenter)
        stateLayout!!.setEmptyText(R.string.empty_pinned_repos)
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

    override fun providePresenter(): PinnedReposPresenter {
        return PinnedReposPresenter()
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (bundle != null && isOk) {
            val id = bundle.getLong(BundleConstant.ID)
            val position = bundle.getInt(BundleConstant.EXTRA)
            AbstractPinnedRepos.delete(id)
            adapter!!.removeItem(position)
        }
    }

    companion object {
        val TAG: String = PinnedReposFragment::class.java.simpleName
        fun newInstance(): PinnedReposFragment {
            return PinnedReposFragment()
        }
    }
}