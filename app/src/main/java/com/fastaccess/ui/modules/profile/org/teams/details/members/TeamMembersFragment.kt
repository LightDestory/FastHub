package com.fastaccess.ui.modules.profile.org.teams.details.members

import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.User
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler.Companion.start
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.adapter.UsersAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 03 Dec 2016, 3:56 PM
 */
class TeamMembersFragment : BaseFragment<TeamMembersMvp.View, TeamMembersPresenter>(),
    TeamMembersMvp.View {
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
    private var onLoadMore: OnLoadMore<Long>? = null
    private var adapter: UsersAdapter? = null
    override fun onNotifyAdapter(items: List<User>?, page: Int) {
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

    override fun fragmentLayout(): Int {
        return R.layout.small_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        if (arguments == null) {
            throw NullPointerException("Bundle is null, username is required")
        }
        stateLayout!!.setEmptyText(R.string.no_members)
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        adapter = UsersAdapter(presenter!!.followers)
        adapter!!.listener = presenter
        recycler!!.adapter = adapter
        recycler!!.addOnScrollListener(loadMore)
        recycler!!.addKeyLineDivider()
        if (presenter!!.followers.isEmpty() && !presenter!!.isApiCalled) {
            onRefresh()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun providePresenter(): TeamMembersPresenter {
        return TeamMembersPresenter()
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

    override val loadMore: OnLoadMore<Long>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter, requireArguments().getLong(BundleConstant.EXTRA))
            }
            return onLoadMore!!
        }

    override fun onRefresh() {
        presenter!!.onCallApi(1, requireArguments().getLong(BundleConstant.EXTRA))
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
        fun newInstance(id: Long): TeamMembersFragment {
            val view = TeamMembersFragment()
            view.arguments = start().put(BundleConstant.EXTRA, id).end()
            return view
        }
    }
}