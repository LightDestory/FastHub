package com.fastaccess.ui.modules.notification

import android.os.Bundle
import androidx.fragment.app.Fragment
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.FragmentPagerAdapterModel.Companion.buildForNotifications
import com.fastaccess.data.dao.GroupedNotificationModel
import com.fastaccess.helper.AppHelper
import com.fastaccess.ui.adapter.FragmentsPagerAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.main.MainActivity.Companion.launchMainActivity
import com.fastaccess.ui.modules.notification.all.AllNotificationsFragment
import com.fastaccess.ui.modules.notification.callback.OnNotificationChangedListener
import com.fastaccess.ui.modules.notification.unread.UnreadNotificationsFragment
import com.fastaccess.ui.widgets.ViewPagerView
import com.google.android.material.tabs.TabLayout

/**
 * Created by Kosh on 27 Feb 2017, 12:36 PM
 */
class NotificationActivity : BaseActivity<BaseMvp.FAView, BasePresenter<BaseMvp.FAView>>(), OnNotificationChangedListener {
    @JvmField
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null

    @JvmField
    @BindView(R.id.notificationContainer)
    var pager: ViewPagerView? = null
    override fun layout(): Int {
        return R.layout.notification_activity_layout
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = false

    override fun providePresenter(): BasePresenter<BaseMvp.FAView> {
        return BasePresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppHelper.cancelNotification(this)
        onSelectNotifications()
        setupTabs()
        tabs!!.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(pager) {
            override fun onTabReselected(tab: TabLayout.Tab) {
                super.onTabReselected(tab)
                onScrollTop(tab.position)
            }
        })
    }

    override fun onScrollTop(index: Int) {
        if (pager == null || pager!!.adapter == null) return
        val fragment: Fragment = pager!!.adapter!!
            .instantiateItem(pager!!, index) as BaseFragment<*, *>
        if (fragment is BaseFragment<*, *>) {
            fragment.onScrollTop(index)
        }
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            launchMainActivity(this, true)
        }
        super.onBackPressed()
    }

    override fun onNotificationChanged(notification: GroupedNotificationModel, index: Int) {
        if (pager != null && pager!!.adapter != null) {
            if (index == 0) {
                val fragment = pager!!.adapter!!
                    .instantiateItem(pager!!, 0) as UnreadNotificationsFragment
                fragment.onNotifyNotificationChanged(notification)
            } else {
                val fragment = pager!!.adapter!!
                    .instantiateItem(pager!!, 1) as AllNotificationsFragment
                fragment.onNotifyNotificationChanged(notification)
            }
        }
    }

    private fun setupTabs() {
        pager!!.adapter = FragmentsPagerAdapter(
            supportFragmentManager,
            buildForNotifications(this)
        )
        tabs!!.setupWithViewPager(pager)
    }
}