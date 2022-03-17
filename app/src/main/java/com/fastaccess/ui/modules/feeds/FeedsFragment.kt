package com.fastaccess.ui.modules.feeds

import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.view.View
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.fastaccess.R
import com.fastaccess.data.dao.GitCommitModel
import com.fastaccess.data.dao.NameParser
import com.fastaccess.data.dao.SimpleUrlsModel
import com.fastaccess.data.dao.model.Event
import com.fastaccess.databinding.MicroGridRefreshListBinding
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.adapter.FeedsAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.delegate.viewBinding
import com.fastaccess.ui.modules.repos.code.commit.details.CommitPagerActivity
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.dialog.ListDialogView
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 11 Nov 2016, 12:36 PM
 */
class FeedsFragment : BaseFragment<FeedsMvp.View, FeedsPresenter>(), FeedsMvp.View {
    val binding: MicroGridRefreshListBinding by viewBinding()

    val recycler: DynamicRecyclerView? by lazy { binding.recycler }
    val refresh: SwipeRefreshLayout? by lazy { binding.refresh }
    val stateLayout: StateLayout? by lazy { binding.root.findViewById(R.id.stateLayout) }
    val fastScroller: RecyclerViewFastScroller? by lazy { binding.fastScroller }

    private var adapter: FeedsAdapter? = null
    private var onLoadMore: OnLoadMore<String>? = null
    override fun fragmentLayout(): Int {
        return R.layout.micro_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        stateLayout!!.setEmptyText(R.string.no_feeds)
        stateLayout!!.setOnReloadListener(this)
        refresh!!.setOnRefreshListener(this)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        adapter = FeedsAdapter(presenter!!.events, isProfile)
        adapter!!.listener = presenter
        loadMore.initialize(
            presenter!!.currentPage, presenter!!.previousTotal
        )
        recycler!!.adapter = adapter
        if (isProfile) {
            recycler!!.addDivider()
        }
        recycler!!.addOnScrollListener(loadMore)
        fastScroller!!.attachRecyclerView(recycler!!)
        if (presenter!!.events.isEmpty() && !presenter!!.isApiCalled) {
            presenter!!.onFragmentCreated(requireArguments())
        }
    }

    override fun onRefresh() {
        presenter!!.onCallApi(1)
    }

    override fun onNotifyAdapter(events: List<Event>, page: Int) {
        hideProgress()
        if (events.isEmpty()) {
            adapter!!.clear()
            return
        }
        if (page <= 1) {
            adapter!!.insertItems(events)
        } else {
            adapter!!.addItems(events)
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

    override fun onOpenRepoChooser(models: List<SimpleUrlsModel>) {
        val dialogView = ListDialogView<SimpleUrlsModel>()
        dialogView.initArguments(getString(R.string.repo_chooser), models)
        dialogView.show(childFragmentManager, "ListDialogView")
    }

    override fun providePresenter(): FeedsPresenter {
        return FeedsPresenter()
    }

    override val loadMore: OnLoadMore<String>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter)
            }
            return onLoadMore!!
        }

    override fun onOpenCommitChooser(commits: List<GitCommitModel>) {
        val dialogView = ListDialogView<GitCommitModel>()
        dialogView.initArguments(getString(R.string.commits), commits)
        dialogView.show(childFragmentManager, "ListDialogView")
    }

    override fun onDestroyView() {
        recycler!!.removeOnScrollListener(loadMore)
        super.onDestroyView()
    }

    override fun onClick(view: View) {
        onRefresh()
    }

    override fun onItemSelected(item: Parcelable) {
        if (item is SimpleUrlsModel) {
            launchUri(requireContext(), Uri.parse(item.item))
        } else if (item is GitCommitModel) {
            val nameParser = NameParser(item.url)
            val intent = CommitPagerActivity.createIntent(
                requireContext(), nameParser.name!!,
                nameParser.username!!, item.sha!!, true, isEnterprise(item.url)
            )
            requireContext().startActivity(intent)
        }
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
        if (recycler != null) {
            recycler!!.scrollToPosition(0)
        }
    }

    private fun showReload() {
        hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }

    val isProfile: Boolean
        get() = !isEmpty(requireArguments().getString(BundleConstant.EXTRA)) &&
                !requireArguments().getBoolean(BundleConstant.EXTRA_TWO)

    companion object {
        @JvmField
        val TAG: String = FeedsFragment::class.java.simpleName

        @JvmOverloads
        @JvmStatic
        fun newInstance(user: String?, isOrg: Boolean = false): FeedsFragment {
            val feedsFragment = FeedsFragment()
            feedsFragment.arguments = Bundler.start()
                .put(BundleConstant.EXTRA, user)
                .put(BundleConstant.EXTRA_TWO, isOrg)
                .end()
            return feedsFragment
        }
    }
}