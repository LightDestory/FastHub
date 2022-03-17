package com.fastaccess.ui.modules.main.orgs

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.User
import com.fastaccess.ui.adapter.UsersAdapter
import com.fastaccess.ui.base.BaseDialogFragment
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 15 Apr 2017, 1:57 PM
 */
class OrgListDialogFragment : BaseDialogFragment<OrgListDialogMvp.View, OrgListDialogPresenter>(),
    OrgListDialogMvp.View {
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
    @BindView(R.id.toolbar)
    var toolbar: Toolbar? = null

    @JvmField
    @BindView(R.id.fastScroller)
    var fastScroller: RecyclerViewFastScroller? = null
    private var adapter: UsersAdapter? = null
    override fun onNotifyAdapter(items: List<User>?) {
        hideProgress()
        if (items == null || items.isEmpty()) {
            adapter!!.clear()
            return
        }
        adapter!!.insertItems(items)
    }

    override fun fragmentLayout(): Int {
        return R.layout.milestone_dialog_layout
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        toolbar!!.setTitle(R.string.organizations)
        toolbar!!.inflateMenu(R.menu.add_menu)
        toolbar!!.menu.findItem(R.id.add).setIcon(R.drawable.ic_info_outline)
            .setTitle(R.string.no_orgs_dialog_title)
        toolbar!!.setOnMenuItemClickListener {
            newInstance(
                getString(R.string.no_orgs_dialog_title),
                getString(R.string.no_orgs_description),
                false,
                hideCancel = true
            )
                .show(childFragmentManager, MessageDialogView.TAG)
            true
        }
        toolbar!!.setNavigationIcon(R.drawable.ic_clear)
        toolbar!!.setNavigationOnClickListener { dismiss() }
        stateLayout!!.setEmptyText(R.string.no_orgs)
        stateLayout!!.setOnReloadListener { presenter!!.onLoadOrgs() }
        refresh!!.setOnRefreshListener { presenter!!.onLoadOrgs() }
        recycler!!.setEmptyView(stateLayout!!, refresh)
        adapter = UsersAdapter(presenter!!.orgs)
        recycler!!.adapter = adapter
        recycler!!.addKeyLineDivider()
        if (savedInstanceState == null) {
            presenter!!.onLoadOrgs()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
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

    override fun providePresenter(): OrgListDialogPresenter {
        return OrgListDialogPresenter()
    }

    private fun showReload() {
        hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }

    companion object {
        fun newInstance(): OrgListDialogFragment {
            return OrgListDialogFragment()
        }
    }
}