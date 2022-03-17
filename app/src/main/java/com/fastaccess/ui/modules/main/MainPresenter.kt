package com.fastaccess.ui.modules.main

import androidx.annotation.IdRes
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fastaccess.R
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.Notification
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.AppHelper
import com.fastaccess.helper.ParseDateFormat.Companion.lastWeekDate
import com.fastaccess.helper.PrefGetter
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.RestProvider.getNotificationService
import com.fastaccess.provider.rest.RestProvider.getUserService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.feeds.FeedsFragment
import com.fastaccess.ui.modules.feeds.FeedsFragment.Companion.newInstance
import com.fastaccess.ui.modules.main.MainMvp.NavigationType
import com.fastaccess.ui.modules.main.issues.pager.MyIssuesPagerFragment
import com.fastaccess.ui.modules.main.pullrequests.pager.MyPullsPagerFragment
import io.reactivex.Single

/**
 * Created by Kosh on 09 Nov 2016, 7:53 PM
 */
class MainPresenter internal constructor() : BasePresenter<MainMvp.View>(), MainMvp.Presenter {
    override fun canBackPress(drawerLayout: DrawerLayout): Boolean {
        return !drawerLayout.isDrawerOpen(GravityCompat.START)
    }

    override fun onModuleChanged(fragmentManager: FragmentManager, @NavigationType type: Int) {
        val currentVisible = ActivityHelper.getVisibleFragment(fragmentManager)
        val homeView =
            AppHelper.getFragmentByTag(fragmentManager, FeedsFragment.TAG) as FeedsFragment?
        val pullRequestView = AppHelper.getFragmentByTag(
            fragmentManager,
            MyPullsPagerFragment.TAG
        ) as MyPullsPagerFragment?
        val issuesView = AppHelper.getFragmentByTag(
            fragmentManager,
            MyIssuesPagerFragment.TAG
        ) as MyIssuesPagerFragment?
        when (type) {
            MainMvp.PROFILE -> sendToView { it.onOpenProfile() }
            MainMvp.FEEDS -> if (homeView == null) {
                onAddAndHide(
                    fragmentManager, newInstance(null),
                    currentVisible!!
                )
            } else {
                onShowHideFragment(fragmentManager, homeView, currentVisible!!)
            }
            MainMvp.PULL_REQUESTS -> if (pullRequestView == null) {
                onAddAndHide(fragmentManager, MyPullsPagerFragment.newInstance(), currentVisible!!)
            } else {
                onShowHideFragment(fragmentManager, pullRequestView, currentVisible!!)
            }
            MainMvp.ISSUES -> if (issuesView == null) {
                onAddAndHide(fragmentManager, MyIssuesPagerFragment.newInstance(), currentVisible!!)
            } else {
                onShowHideFragment(fragmentManager, issuesView, currentVisible!!)
            }
        }
    }

    override fun onShowHideFragment(
        fragmentManager: FragmentManager, toShow: Fragment,
        toHide: Fragment
    ) {
        toHide.onHiddenChanged(true)
        fragmentManager
            .beginTransaction()
            .hide(toHide)
            .show(toShow)
            .commit()
        toShow.onHiddenChanged(false)
    }

    override fun onAddAndHide(
        fragmentManager: FragmentManager, toAdd: Fragment,
        toHide: Fragment
    ) {
        toHide.onHiddenChanged(true)
        fragmentManager
            .beginTransaction()
            .hide(toHide)
            .add(R.id.container, toAdd, toAdd.javaClass.simpleName)
            .commit()
        toAdd.onHiddenChanged(false)
    }

    override fun onMenuItemSelect(@IdRes itemId: Int, position: Int, fromUser: Boolean) {
        if (view != null) {
            view!!.onNavigationChanged(position)
        }
    }

    override fun onMenuItemReselect(@IdRes itemId: Int, position: Int, fromUser: Boolean) {
        sendToView { view: MainMvp.View -> view.onScrollTop(position) }
    }

    init {
        isEnterprise = PrefGetter.isEnterprise
        manageDisposable(RxHelper.getObservable(getUserService(isEnterprise).user)
            .flatMap { login: Login ->
                val current = Login.getUser()
                current.login = login.login
                current.name = login.name
                current.avatarUrl = login.avatarUrl
                current.email = login.email
                current.bio = login.bio
                current.blog = login.blog
                current.company = current.company
                login.update(current)
            }
            .flatMap {
                RxHelper.getObservable(
                    getNotificationService(isEnterprise)
                        .getNotifications(lastWeekDate)
                )
            }
            .flatMapSingle { notificationPageable ->
                if (notificationPageable.items != null && notificationPageable.items!!.isNotEmpty()) {
                    return@flatMapSingle Notification.saveAsSingle(notificationPageable.items)
                } else {
                    Notification.deleteAll()
                }
                Single.just(true)
            }
            .subscribe({ }, { obj: Throwable -> obj.printStackTrace() }) {
                sendToView { view: MainMvp.View ->
                    view.onInvalidateNotification()
                    view.onUpdateDrawerMenuHeader()
                }
            })
    }
}