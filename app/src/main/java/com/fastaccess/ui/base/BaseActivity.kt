package com.fastaccess.ui.base

import android.app.ActivityManager.TaskDescription
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.SimpleDrawerListener
import androidx.viewpager.widget.ViewPager
import butterknife.ButterKnife
import com.bumptech.glide.Glide
import com.evernote.android.state.State
import com.evernote.android.state.StateSaver
import com.fastaccess.App
import com.fastaccess.R
import com.fastaccess.data.dao.model.FastHubNotification
import com.fastaccess.data.dao.model.Login
import com.fastaccess.helper.*
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.PrefGetter.isNavDrawerHintShowed
import com.fastaccess.helper.PrefGetter.isTwiceBackButtonDisabled
import com.fastaccess.helper.PrefGetter.otpCode
import com.fastaccess.helper.PrefGetter.resetEnterprise
import com.fastaccess.helper.PrefGetter.showWhatsNew
import com.fastaccess.helper.PrefGetter.token
import com.fastaccess.provider.markdown.CachedComments.Companion.instance
import com.fastaccess.provider.theme.ThemeEngine.apply
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.changelog.ChangelogBottomSheetDialog
import com.fastaccess.ui.modules.gists.gist.GistActivity
import com.fastaccess.ui.modules.login.chooser.LoginChooserActivity
import com.fastaccess.ui.modules.main.MainActivity
import com.fastaccess.ui.modules.main.drawer.MainDrawerFragment
import com.fastaccess.ui.modules.main.notifications.FastHubNotificationDialog.Companion.show
import com.fastaccess.ui.modules.main.orgs.OrgListDialogFragment
import com.fastaccess.ui.modules.main.playstore.PlayStoreWarningActivity
import com.fastaccess.ui.modules.repos.code.commit.details.CommitPagerActivity
import com.fastaccess.ui.modules.repos.issues.issue.details.IssuePagerActivity
import com.fastaccess.ui.modules.repos.pull_requests.pull_request.details.PullRequestPagerActivity
import com.fastaccess.ui.modules.settings.SettingsActivity
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.fastaccess.ui.widgets.dialog.ProgressDialogFragment
import com.fastaccess.utils.setOnThrottleClickListener
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import net.grandcentrix.thirtyinch.TiActivity

/**
 * Created by Kosh on 24 May 2016, 8:48 PM
 */
abstract class BaseActivity<V : FAView, P : BasePresenter<V>> : TiActivity<P, V>(), FAView,
    MainDrawerFragment.OnDrawerMenuCreatedListener {

    @State
    open var isProgressShowing = false
    protected val toolbar: Toolbar? by lazy { window.decorView.findViewById(R.id.toolbar) }
    protected val appbar: AppBarLayout? by lazy { window.decorView.findViewById(R.id.appbar) }
    protected val drawer: DrawerLayout? by lazy { window.decorView.findViewById(R.id.drawer) }
    private val extraNav: NavigationView? by lazy { window.decorView.findViewById(R.id.extrasNav) }
    val drawerViewPager: ViewPager? by lazy { window.decorView.findViewById(R.id.drawerViewPager) }

    @JvmField
    @State
    var schemeUrl: String? = null

    @JvmField
    @State
    var presenterStateBundle = Bundle()
    private var mainNavDrawer: MainNavDrawer? = null
    private var backPressTimer: Long = 0
    private var toast: Toast? = null

    @LayoutRes
    protected abstract fun layout(): Int
    protected abstract val isTransparent: Boolean
    protected abstract fun canBack(): Boolean
    protected abstract val isSecured: Boolean
    private val menuCallback: MutableList<(menu: Menu) -> Unit> = mutableListOf()
    private var drawerMenu: Menu? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(this, outState)
        presenter!!.onSaveInstanceState(presenterStateBundle)
    }

    override fun onDrawerCreated(menu: Menu) {
        this.drawerMenu = menu
        if (this.menuCallback.isNotEmpty()) {
            this.menuCallback.forEach {
                it(menu)
            }
            this.menuCallback.clear()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTaskName(null)
        setupTheme()
        AppHelper.updateAppLanguage(this)
        super.onCreate(savedInstanceState)

        if (layout() != 0) {
            setContentView(layout())
            ButterKnife.bind(this)
            window.decorView.findViewById<View>(R.id.logout)?.setOnThrottleClickListener {
                onLogoutClicked()
            }
        }
        if (savedInstanceState == null) {
            val now = System.currentTimeMillis() / 1000
            val old = PrefHelper.getLong("github_status_check")
            if (now - old >= 600) {
                // 10m check
                presenter!!.onCheckGitHubStatus()
                PrefHelper.putAny("github_status_check", now)
            }
            if (intent != null) {
                schemeUrl = intent.getStringExtra(BundleConstant.SCHEME_URL)
            }
        }
        if (!validateAuth()) return
        if (savedInstanceState == null) {
            if (showInAppNotifications()) {
                show(supportFragmentManager)
            }
        }
        showChangelog()
        initPresenterBundle(savedInstanceState)
        setupToolbarAndStatusBar(toolbar)
        initEnterpriseExtra(savedInstanceState)
        mainNavDrawer = MainNavDrawer(this, extraNav)
        setupDrawer()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (canBack()) {
            if (item.itemId == android.R.id.home) {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDialogDismissed() {} //pass
    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        if (isOk && bundle != null) {
            val logout = bundle.getBoolean("logout")
            if (logout) {
                onRequireLogin()
            }
        }
    } //pass

    override fun showMessage(@StringRes titleRes: Int, @StringRes msgRes: Int) {
        showMessage(getString(titleRes), getString(msgRes))
    }

    override fun showMessage(titleRes: String, msgRes: String) {
        hideProgress()
        if (toast != null) toast!!.cancel()
        val context: Context = App.getInstance() // WindowManager$BadTokenException
        toast = if (titleRes == context.getString(R.string.error)) Toasty.error(
            context,
            msgRes,
            Toast.LENGTH_LONG
        ) else Toasty.info(context, msgRes, Toast.LENGTH_LONG)
        toast?.show()
    }

    override fun showErrorMessage(msgRes: String) {
        showMessage(getString(R.string.error), msgRes)
    }

    override val isLoggedIn: Boolean
        get() = Login.getUser() != null

    override fun showProgress(@StringRes resId: Int) {
        showProgress(resId, true)
    }

    override fun showBlockingProgress(resId: Int) {
        showProgress(resId, false)
    }

    override fun hideProgress() {
        val fragment = AppHelper.getFragmentByTag(
            supportFragmentManager,
            ProgressDialogFragment.TAG
        ) as ProgressDialogFragment?
        if (fragment != null) {
            isProgressShowing = false
            fragment.dismiss()
        }
    }

    override fun onRequireLogin() {
        Toasty.warning(App.getInstance(), getString(R.string.unauthorized_user), Toast.LENGTH_LONG)
            .show()
        val glide = Glide.get(App.getInstance())
        presenter!!.manageViewDisposable(RxHelper.getObservable(Observable.fromCallable {
            glide.clearDiskCache()
            token = null
            otpCode = null
            resetEnterprise()
            Login.logout()
            true
        }).subscribe {
            glide.clearMemory()
            val intent = Intent(this, LoginChooserActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finishAffinity()
        })
    }

    override fun onBackPressed() {
        if (drawer != null && drawer!!.isDrawerOpen(GravityCompat.START)) {
            closeDrawer()
        } else {
            val clickTwiceToExit = !isTwiceBackButtonDisabled
            superOnBackPressed(clickTwiceToExit)
        }
    }

    override fun onLogoutPressed() {
        newInstance(
            getString(R.string.logout), getString(R.string.confirm_message),
            Bundler.start()
                .put(BundleConstant.YES_NO_EXTRA, true)
                .put("logout", true)
                .end()
        )
            .show(supportFragmentManager, MessageDialogView.TAG)
    }

    override fun onThemeChanged() {
        if (this is MainActivity) {
            recreate()
        } else {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtras(Bundler.start().put(BundleConstant.YES_NO_EXTRA, true).end())
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)
            finish()
        }
    }

    private val openSettingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        onThemeChanged()
    }

    override fun onOpenSettings() {
        val intent = Intent(this, SettingsActivity::class.java)
        openSettingsLauncher.launch(intent)
    }

    override fun onScrollTop(index: Int) {}
    override val isEnterprise: Boolean
        get() = presenter != null && presenter!!.isEnterprise

    override fun onOpenUrlInBrowser() {
        if (!isEmpty(schemeUrl)) {
            ActivityHelper.startCustomTab(this, schemeUrl!!)
            try {
                finish()
            } catch (ignored: Exception) {
            } // fragment might be committed and calling finish will crash the app.
        }
    }

    private fun onLogoutClicked() {
        closeDrawer()
        onLogoutPressed()
    }

    override fun onDestroy() {
        clearCachedComments()
        super.onDestroy()
    }

    protected fun setTaskName(name: String?) {
        val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) TaskDescription(
            name,
            0,
            ViewHelper.getPrimaryDarkColor(this)
        ) else TaskDescription(name, null, ViewHelper.getPrimaryDarkColor(this))
        setTaskDescription(description)
    }

    private fun callMenu(call: (menu: Menu) -> Unit) {
        if (drawerMenu == null) {
            this.menuCallback.add(call)
            return
        }
        call(drawerMenu!!)
    }

    protected fun selectHome(hideRepo: Boolean) {
        callMenu { menu ->
            if (hideRepo) {
                menu.findItem(R.id.navToRepo).isVisible = false
                menu.findItem(R.id.mainView).isVisible = true
                return@callMenu
            }
            menu.findItem(R.id.navToRepo).isVisible = false
            menu.findItem(R.id.mainView).isCheckable = true
            menu.findItem(R.id.mainView).isChecked = true
        }

    }

    protected fun selectProfile() {
        selectHome(true)
        selectMenuItem(R.id.profile)
    }

    protected fun selectPinned() {
        selectMenuItem(R.id.pinnedMenu)
    }

    protected fun onSelectNotifications() {
        selectMenuItem(R.id.notifications)
    }

    protected fun onSelectTrending() {
        selectMenuItem(R.id.trending)
    }

    fun onOpenOrgsDialog() {
        OrgListDialogFragment.newInstance().show(supportFragmentManager, "OrgListDialogFragment")
    }

    protected fun showNavToRepoItem() {
        callMenu { menu ->
            menu.findItem(R.id.navToRepo).isVisible = true
        }
    }

    private fun selectMenuItem(@IdRes id: Int) {
        callMenu { menu ->
            menu.findItem(id).isCheckable = true
            menu.findItem(id).isChecked = true
        }
    }

    open fun onNavToRepoClicked() {}
    private fun setupToolbarAndStatusBar(toolbar: Toolbar?) {
        changeStatusBarColor(isTransparent)
        if (toolbar != null) {
            setSupportActionBar(toolbar)
            if (canBack()) {
                if (supportActionBar != null) {
                    supportActionBar!!.setHomeAsUpIndicator(R.drawable.ic_back)
                    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
                    if (canBack()) {
                        val navIcon = getToolbarNavigationIcon(toolbar)
                        navIcon?.setOnLongClickListener {
                            MainActivity.launchMainActivity(this, true)
                            finish()
                            true
                        }
                        navIcon?.setOnThrottleClickListener {
                            this.onNavBack()
                        }
                    }
                }
            }
        }
    }

    open fun onNavBack() {
        finish()
    }

    protected fun setToolbarIcon(@DrawableRes res: Int) {
        if (supportActionBar != null) {
            supportActionBar!!.setHomeAsUpIndicator(res)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
    }

    protected fun hideShowShadow(show: Boolean) {
        if (appbar != null) {
            appbar!!.elevation = if (show) resources.getDimension(R.dimen.spacing_micro) else 0.0f
        }
    }

    private fun changeStatusBarColor(isTransparent: Boolean) {
        if (!isTransparent) {
            window.statusBarColor = ViewHelper.getPrimaryDarkColor(this)
        }
    }

    private fun setupTheme() {
        apply(this)
    }

    protected fun setupNavigationView() {
        if (mainNavDrawer != null) {
            mainNavDrawer!!.setupView()
        }
    }

    fun closeDrawer() {
        if (drawer != null) {
            if (drawer!!.isDrawerOpen(GravityCompat.START)) {
                drawer!!.closeDrawer(GravityCompat.START)
            }
        }
    }

    private fun setupDrawer() {
        if (drawer != null && this !is MainActivity) {
            if (!isNavDrawerHintShowed) {
                drawer!!.viewTreeObserver.addOnPreDrawListener(object :
                    ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        drawer!!.openDrawer(GravityCompat.START)
                        drawer!!.addDrawerListener(object : SimpleDrawerListener() {
                            override fun onDrawerOpened(drawerView: View) {
                                super.onDrawerOpened(drawerView)
                                drawerView.postDelayed({
                                    if (drawer != null) {
                                        closeDrawer()
                                        drawer!!.removeDrawerListener(this)
                                    }
                                }, 1000)
                            }
                        })
                        drawer!!.viewTreeObserver.removeOnPreDrawListener(this)
                        return true
                    }
                })
            }
        }
    }

    private fun superOnBackPressed(didClickTwice: Boolean) {
        if (this is MainActivity) {
            if (didClickTwice) {
                if (canExit()) {
                    super.onBackPressed()
                }
            } else {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun canExit(): Boolean {
        if (backPressTimer + 2000 > System.currentTimeMillis()) {
            return true
        } else {
            showMessage(R.string.press_again_to_exit, R.string.press_again_to_exit)
        }
        backPressTimer = System.currentTimeMillis()
        return false
    }

    private fun getToolbarNavigationIcon(toolbar: Toolbar): View? {
        val hadContentDescription = TextUtils.isEmpty(toolbar.navigationContentDescription)
        val contentDescription =
            if (!hadContentDescription) toolbar.navigationContentDescription.toString() else "navigationIcon"
        toolbar.navigationContentDescription = contentDescription
        val potentialViews = ArrayList<View>()
        toolbar.findViewsWithText(
            potentialViews,
            contentDescription,
            View.FIND_VIEWS_WITH_CONTENT_DESCRIPTION
        )
        var navIcon: View? = null
        if (potentialViews.size > 0) {
            navIcon = potentialViews[0]
        }
        if (hadContentDescription) toolbar.navigationContentDescription = null
        return navIcon
    }

    fun onRestartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun showProgress(resId: Int, cancelable: Boolean) {
        var msg = getString(R.string.in_progress)
        if (resId != 0) {
            msg = getString(resId)
        }
        if (!isProgressShowing && !isFinishing) {
            var fragment = AppHelper.getFragmentByTag(
                supportFragmentManager,
                ProgressDialogFragment.TAG
            ) as ProgressDialogFragment?
            if (fragment == null) {
                isProgressShowing = true
                fragment = ProgressDialogFragment.newInstance(msg, cancelable)
                fragment.show(supportFragmentManager, ProgressDialogFragment.TAG)
            }
        }
    }

    /**
     * not really needed but meh.
     */
    private fun clearCachedComments() {
        if (this is IssuePagerActivity || this is CommitPagerActivity ||
            this is PullRequestPagerActivity || this is GistActivity
        ) {
            instance.clear()
        }
    }

    private fun validateAuth(): Boolean {
        if (!isSecured) {
            if (!isLoggedIn) {
                onRequireLogin()
                return false
            }
        }
        return true
    }

    private fun initEnterpriseExtra(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            if (intent != null) {
                if (intent.extras != null) {
                    presenter!!.isEnterprise =
                        intent.extras!!.getBoolean(BundleConstant.IS_ENTERPRISE)
                } else if (intent.hasExtra(BundleConstant.IS_ENTERPRISE)) {
                    presenter!!.isEnterprise =
                        intent.getBooleanExtra(BundleConstant.IS_ENTERPRISE, false)
                }
            }
        }
    }

    private fun initPresenterBundle(savedInstanceState: Bundle?) {
        if (savedInstanceState != null && !savedInstanceState.isEmpty) {
            StateSaver.restoreInstanceState(this, savedInstanceState)
            presenter!!.onRestoreInstanceState(presenterStateBundle)
        }
    }

    private fun showChangelog() {
        if (showWhatsNew() && this !is PlayStoreWarningActivity) {
            ChangelogBottomSheetDialog().show(supportFragmentManager, "ChangelogBottomSheetDialog")
        }
    }

    private fun showInAppNotifications(): Boolean {
        return FastHubNotification.hasNotifications()
    }

//    private val mainDrawerMenu: Menu?
//        private get() {
//            if (drawerViewPager != null) {
//                val adapter = drawerViewPager!!.adapter as FragmentsPagerAdapter?
//                if (adapter != null) {
//                    val fragment =
//                        adapter.instantiateItem(drawerViewPager!!, 0) as MainDrawerFragment
//                    return fragment.getMenu()
//                }
//            }
//            return null
//        }
}