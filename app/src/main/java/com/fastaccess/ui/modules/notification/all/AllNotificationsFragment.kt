package com.fastaccess.ui.modules.notification.all

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.annotation.StringRes
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.GroupedNotificationModel
import com.fastaccess.data.dao.model.Notification
import com.fastaccess.data.dao.model.Repo
import com.fastaccess.helper.Bundler
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.provider.tasks.notification.ReadNotificationService
import com.fastaccess.ui.adapter.NotificationsAdapter
import com.fastaccess.ui.adapter.viewholder.NotificationsViewHolder
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.notification.callback.OnNotificationChangedListener
import com.fastaccess.ui.widgets.AppbarRefreshLayout
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 20 Feb 2017, 8:50 PM
 */
class AllNotificationsFragment :
    BaseFragment<AllNotificationsMvp.View, AllNotificationsPresenter>(),
    AllNotificationsMvp.View {
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
    private var adapter: NotificationsAdapter? = null
    private var onNotificationChangedListener: OnNotificationChangedListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnNotificationChangedListener) {
            onNotificationChangedListener = context
        }
    }

    override fun onDetach() {
        onNotificationChangedListener = null
        super.onDetach()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onRefresh() {
        presenter!!.onCallApi()
    }

    override fun onUpdateReadState(item: GroupedNotificationModel, position: Int) {
        if (onNotificationChangedListener != null) onNotificationChangedListener!!.onNotificationChanged(
            item, 0
        )
        adapter!!.swapItem(item, position)
    }

    override fun onNotifyAdapter(items: List<GroupedNotificationModel>) {
        hideProgress()
        if (items.isEmpty()) {
            adapter!!.clear()
            return
        }
        adapter!!.insertItems(items)
        if (isSafe) requireActivity().invalidateOptionsMenu()
    }

    override fun onClick(url: String) {
        launchUri(requireContext(), Uri.parse(url), true)
    }

    override fun onReadNotification(notification: Notification) {
        val model = GroupedNotificationModel(notification)
        if (onNotificationChangedListener != null) onNotificationChangedListener!!.onNotificationChanged(
            model,
            0
        )
        adapter!!.swapItem(model)
        ReadNotificationService.start(requireContext(), notification.id)
    }

    override fun onMarkAllByRepo(repo: Repo) {
        presenter!!.onMarkReadByRepo(adapter!!.data.filterNotNull(), repo)
    }

    override fun onNotifyNotificationChanged(notification: GroupedNotificationModel) {
        if (adapter != null) {
            adapter!!.swapItem(notification)
        }
    }

    override fun fragmentLayout(): Int {
        return R.layout.micro_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        adapter = NotificationsAdapter(presenter!!.notifications,
            showUnreadState = true,
            hideClear = true
        )
        adapter!!.listener = presenter
        refresh!!.setOnRefreshListener(this)
        stateLayout!!.setEmptyText(R.string.no_notifications)
        stateLayout!!.setOnReloadListener { onRefresh() }
        recycler!!.setEmptyView(stateLayout!!, refresh)
        recycler!!.adapter = adapter
        recycler!!.addDivider(NotificationsViewHolder::class.java)
        if (savedInstanceState == null || !presenter!!.isApiCalled) {
            onRefresh()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun providePresenter(): AllNotificationsPresenter {
        return AllNotificationsPresenter()
    }

    override fun showProgress(@StringRes resId: Int) {
        refresh!!.isRefreshing = true
        stateLayout!!.showProgress()
    }

    override fun hideProgress() {
        refresh!!.isRefreshing = false
        stateLayout!!.hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }

    override fun showErrorMessage(msgRes: String) {
        showReload()
        super.showErrorMessage(msgRes)
    }

    override fun showMessage(titleRes: Int, msgRes: Int) {
        showReload()
        super.showMessage(titleRes, msgRes)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.notification_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.readAll) {
            if (adapter!!.data.isNotEmpty()) {
                newInstance(
                    getString(R.string.mark_all_as_read), getString(R.string.confirm_message),
                    isMarkDown = false, hideCancel = false, bundle = Bundler.start()
                        .put("primary_button", getString(R.string.yes))
                        .put("secondary_button", getString(R.string.no))
                        .end()
                )
                    .show(childFragmentManager, MessageDialogView.TAG)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        val hasUnread =
            adapter!!.data.asSequence()
                .filterNotNull()
                .filter { group: GroupedNotificationModel -> group.type == GroupedNotificationModel.ROW }
                .any { group: GroupedNotificationModel -> group.notification!!.isUnread }
        menu.findItem(R.id.readAll).isVisible = hasUnread
        super.onPrepareOptionsMenu(menu)
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
        if (recycler != null) recycler!!.scrollToPosition(0)
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (isOk) {
            presenter!!.onMarkAllAsRead(adapter!!.data.filterNotNull())
        }
    }

    private fun showReload() {
        hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }

    companion object {
        fun newInstance(): AllNotificationsFragment {
            return AllNotificationsFragment()
        }
    }
}