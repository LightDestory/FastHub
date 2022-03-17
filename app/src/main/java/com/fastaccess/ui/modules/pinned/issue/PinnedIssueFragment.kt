package com.fastaccess.ui.modules.pinned.issue

import android.os.Bundle
import android.view.View
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.PinnedIssues
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.ui.adapter.IssuesAdapter
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
class PinnedIssueFragment : BaseFragment<PinnedIssueMvp.View, PinnedIssuePresenter>(),
    PinnedIssueMvp.View {
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
    private var adapter: IssuesAdapter? = null
    override fun onNotifyAdapter(items: List<Issue>?) {
        refresh!!.isRefreshing = false
        stateLayout!!.hideProgress()
        if (items != null) adapter!!.insertItems(items) else adapter!!.clear()
    }

    override fun onDeletePinnedIssue(id: Long, position: Int) {
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
        adapter = IssuesAdapter(presenter!!.pinnedIssue, true, true, true)
        adapter!!.listener = presenter
        stateLayout!!.setEmptyText(R.string.no_issues)
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

    override fun providePresenter(): PinnedIssuePresenter {
        return PinnedIssuePresenter()
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (bundle != null && isOk) {
            val id = bundle.getLong(BundleConstant.ID)
            val position = bundle.getInt(BundleConstant.EXTRA)
            PinnedIssues.delete(id)
            adapter!!.removeItem(position)
        }
    }

    companion object {
        val TAG: String = PinnedIssueFragment::class.java.simpleName
        fun newInstance(): PinnedIssueFragment {
            return PinnedIssueFragment()
        }
    }
}