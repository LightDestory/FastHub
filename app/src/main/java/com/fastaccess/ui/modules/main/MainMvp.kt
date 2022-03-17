package com.fastaccess.ui.modules.main

import androidx.annotation.IntDef
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import it.sephiroth.android.library.bottomnavigation.BottomNavigation

/**
 * Created by Kosh on 09 Nov 2016, 7:51 PM
 */
interface MainMvp {
    @IntDef(FEEDS, ISSUES, PULL_REQUESTS, PROFILE)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class NavigationType
    interface View : FAView {
        fun onNavigationChanged(@NavigationType navType: Int)
        fun onUpdateDrawerMenuHeader()
        fun onOpenProfile()
        fun onInvalidateNotification()
        fun onUserIsBlackListed()
    }

    interface Presenter : FAPresenter, BottomNavigation.OnMenuItemSelectionListener {
        fun canBackPress(drawerLayout: DrawerLayout): Boolean
        fun onModuleChanged(fragmentManager: FragmentManager, @NavigationType type: Int)
        fun onShowHideFragment(fragmentManager: FragmentManager, toShow: Fragment, toHide: Fragment)
        fun onAddAndHide(fragmentManager: FragmentManager, toAdd: Fragment, toHide: Fragment)
    }

    companion object {
        const val FEEDS = 0
        const val ISSUES = 1
        const val PULL_REQUESTS = 2
        const val PROFILE = 3
    }
}