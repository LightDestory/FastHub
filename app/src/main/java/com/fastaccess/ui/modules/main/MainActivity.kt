package com.fastaccess.ui.modules.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.GravityCompat
import com.evernote.android.state.State
import com.fastaccess.App
import com.fastaccess.R
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.Notification
import com.fastaccess.helper.*
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.modules.feeds.FeedsFragment
import com.fastaccess.ui.modules.feeds.FeedsFragment.Companion.newInstance
import com.fastaccess.ui.modules.main.MainMvp.NavigationType
import com.fastaccess.ui.modules.main.issues.pager.MyIssuesPagerFragment
import com.fastaccess.ui.modules.main.pullrequests.pager.MyPullsPagerFragment
import com.fastaccess.ui.modules.notification.NotificationActivity
import com.fastaccess.ui.modules.search.SearchActivity
import com.fastaccess.ui.modules.settings.SlackBottomSheetDialog
import com.fastaccess.ui.modules.user.UserPagerActivity.Companion.startActivity
import com.fastaccess.utils.setOnThrottleClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import it.sephiroth.android.library.bottomnavigation.BottomNavigation

class MainActivity : BaseActivity<MainMvp.View, MainPresenter>(), MainMvp.View {
    @JvmField
    @State
    @NavigationType
    var navType = MainMvp.FEEDS
    var bottomNavigation: BottomNavigation? = null
    var fab: FloatingActionButton? = null
    fun onFilter() {}
    override fun providePresenter(): MainPresenter {
        return MainPresenter()
    }

    override fun layout(): Int {
        return R.layout.activity_main_view
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBack(): Boolean {
        return false
    }

    override val isSecured: Boolean
        get() = false

    public override fun onCreate(savedInstanceState: Bundle?) {
        val intent = intent
        navType = handleIntent(intent)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            if (intent.getBooleanExtra(SlackBottomSheetDialog.TAG, false)) {
                SlackBottomSheetDialog().show(supportFragmentManager, SlackBottomSheetDialog.TAG)
            }
        }
        val root = window.decorView
        bottomNavigation = root.findViewById(R.id.bottomNavigation)
        fab = root.findViewById(R.id.fab)
        fab?.setOnThrottleClickListener { onFilter() }
        presenter!!.isEnterprise = PrefGetter.isEnterprise
        selectHome(false)
        hideShowShadow(navType == MainMvp.FEEDS)
        setToolbarIcon(R.drawable.ic_menu)
        onInit(savedInstanceState)
        fab?.setImageResource(R.drawable.ic_filter)
        onNewIntent(intent)
    }

    private fun handleIntent(intent: Intent): Int {
        var nav = navType
        val action = intent.action
        if (action != null && action == Intent.ACTION_VIEW) {
            val uri = intent.data
            if (uri != null) {
                val host = uri.host
                if (host.equals("myPulls", ignoreCase = true)) {
                    nav = MainMvp.PULL_REQUESTS
                } else if (host.equals("myIssues", ignoreCase = true)) {
                    nav = MainMvp.ISSUES
                }
            }
        }
        return nav
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.extras != null) {
            val recreate = intent.extras!!.getBoolean(BundleConstant.YES_NO_EXTRA)
            if (recreate) recreate()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (drawer != null) drawer!!.openDrawer(GravityCompat.START)
                return true
            }
            R.id.search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                return true
            }
            R.id.notifications -> {
                ViewHelper.tintDrawable(
                    item.setIcon(R.drawable.ic_notifications_none).icon,
                    ViewHelper.getIconColor(this)
                )
                startActivity(Intent(this, NotificationActivity::class.java))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        if (isLoggedIn && Notification.hasUnreadNotifications()) {
            ViewHelper.tintDrawable(
                menu.findItem(R.id.notifications).setIcon(R.drawable.ic_ring).icon,
                ViewHelper.getAccentColor(this)
            )
        } else {
            ViewHelper.tintDrawable(
                menu.findItem(R.id.notifications)
                    .setIcon(R.drawable.ic_notifications_none).icon, ViewHelper.getIconColor(this)
            )
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onNavigationChanged(@NavigationType navType: Int) {
        if (navType == MainMvp.PROFILE) {
            presenter!!.onModuleChanged(supportFragmentManager, navType)
            bottomNavigation!!.setSelectedIndex(this.navType, true)
            return
        }
        this.navType = navType
        if (bottomNavigation!!.selectedIndex != navType) bottomNavigation!!.setSelectedIndex(
            navType,
            true
        )
        hideShowShadow(navType == MainMvp.FEEDS)
        presenter!!.onModuleChanged(supportFragmentManager, navType)
    }

    override fun onUpdateDrawerMenuHeader() {
        setupNavigationView()
    }

    override fun onOpenProfile() {
        startActivity(this, Login.getUser().login, false, PrefGetter.isEnterprise, -1)
    }

    override fun onInvalidateNotification() {
        invalidateOptionsMenu()
    }

    override fun onUserIsBlackListed() {
        Toast.makeText(
            App.getInstance(),
            "You are blacklisted, please contact the dev",
            Toast.LENGTH_LONG
        ).show()
        finish()
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
        val fragmentManager = supportFragmentManager
        when (index) {
            0 -> {
                val homeView =
                    AppHelper.getFragmentByTag(fragmentManager, FeedsFragment.TAG) as FeedsFragment?
                homeView?.onScrollTop(index)
            }
            1 -> {
                val issuesView = AppHelper.getFragmentByTag(
                    fragmentManager,
                    MyIssuesPagerFragment.TAG
                ) as MyIssuesPagerFragment?
                issuesView?.onScrollTop(index)
            }
            2 -> {
                val pullRequestView = AppHelper.getFragmentByTag(
                    fragmentManager,
                    MyPullsPagerFragment.TAG
                ) as MyPullsPagerFragment?
                pullRequestView?.onScrollTop(0)
            }
        }
    }

    private fun onInit(savedInstanceState: Bundle?) {
        if (isLoggedIn) {
            if (savedInstanceState == null) {
                var attachFeeds = true
                when (navType) {
                    MainMvp.PULL_REQUESTS -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.container,
                                MyPullsPagerFragment.newInstance(),
                                MyPullsPagerFragment.TAG
                            )
                            .commit()
                        bottomNavigation!!.setSelectedIndex(2, true)
                        attachFeeds = false
                    }
                    MainMvp.ISSUES -> {
                        supportFragmentManager
                            .beginTransaction()
                            .replace(
                                R.id.container,
                                MyIssuesPagerFragment.newInstance(),
                                MyIssuesPagerFragment.TAG
                            )
                            .commit()
                        bottomNavigation!!.setSelectedIndex(1, true)
                        attachFeeds = false
                    }
                    MainMvp.FEEDS, MainMvp.PROFILE -> {}
                }
                hideShowShadow(navType == MainMvp.FEEDS)
                if (attachFeeds) {
                    supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.container, newInstance(null), FeedsFragment.TAG)
                        .commit()
                }
            }
            val myTypeface = TypeFaceHelper.getTypeface()
            bottomNavigation!!.setDefaultTypeface(myTypeface)
            bottomNavigation!!.menuItemSelectionListener = presenter
        }
    }

    companion object {
        @JvmStatic
        fun launchMain(context: Context?, clearTop: Boolean): Intent {
            val intent = Intent(context, MainActivity::class.java)
            intent.action = Intent.ACTION_VIEW
            if (clearTop) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            return intent
        }

        @JvmStatic
        fun launchMainActivity(context: Context, clearTop: Boolean) {
            val intent = launchMain(context, clearTop)
            context.startActivity(intent)
        }
    }
}