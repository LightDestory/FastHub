package com.fastaccess.ui.modules.repos

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.format.Formatter
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import androidx.core.widget.TextViewCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.TransitionManager
import butterknife.BindView
import butterknife.OnCheckedChanged
import butterknife.OnClick
import butterknife.OnLongClick
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.NameParser
import com.fastaccess.data.dao.model.AbstractPinnedRepos
import com.fastaccess.data.dao.model.Login
import com.fastaccess.helper.*
import com.fastaccess.helper.AnimHelper.mimicFabVisibility
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.InputHelper.toLong
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.provider.colors.ColorsProvider.getColorAsColor
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.provider.tasks.git.GithubActionService
import com.fastaccess.provider.tasks.git.GithubActionService.Companion.startForRepo
import com.fastaccess.ui.adapter.TopicsAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.modules.filter.issues.FilterIssuesActivity
import com.fastaccess.ui.modules.main.MainActivity.Companion.launchMainActivity
import com.fastaccess.ui.modules.repos.RepoPagerActivity
import com.fastaccess.ui.modules.repos.RepoPagerMvp.RepoNavigationType
import com.fastaccess.ui.modules.repos.code.RepoCodePagerFragment
import com.fastaccess.ui.modules.repos.extras.labels.LabelsDialogFragment
import com.fastaccess.ui.modules.repos.extras.license.RepoLicenseBottomSheet.Companion.newInstance
import com.fastaccess.ui.modules.repos.extras.milestone.create.MilestoneDialogFragment
import com.fastaccess.ui.modules.repos.extras.misc.RepoMiscDialogFragment
import com.fastaccess.ui.modules.repos.extras.misc.RepoMiscMVp
import com.fastaccess.ui.modules.repos.issues.RepoIssuesPagerFragment
import com.fastaccess.ui.modules.repos.pull_requests.RepoPullRequestPagerFragment
import com.fastaccess.ui.modules.repos.wiki.WikiActivity.Companion.getWiki
import com.fastaccess.ui.modules.user.UserPagerActivity.Companion.startActivity
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.floatingactionbutton.FloatingActionButton.OnVisibilityChangedListener
import it.sephiroth.android.library.bottomnavigation.BottomNavigation
import java.text.NumberFormat

/**
 * Created by Kosh on 09 Dec 2016, 4:17 PM
 */
class RepoPagerActivity : BaseActivity<RepoPagerMvp.View, RepoPagerPresenter>(),
    RepoPagerMvp.View {
    @JvmField
    @BindView(R.id.avatarLayout)
    var avatarLayout: AvatarLayout? = null

    @JvmField
    @BindView(R.id.headerTitle)
    var title: FontTextView? = null

    @JvmField
    @BindView(R.id.size)
    var size: FontTextView? = null

    @JvmField
    @BindView(R.id.date)
    var date: FontTextView? = null

    @JvmField
    @BindView(R.id.forkRepo)
    var forkRepo: FontTextView? = null

    @JvmField
    @BindView(R.id.starRepo)
    var starRepo: FontTextView? = null

    @JvmField
    @BindView(R.id.watchRepo)
    var watchRepo: FontTextView? = null

    @JvmField
    @BindView(R.id.license)
    var license: FontTextView? = null

    @JvmField
    @BindView(R.id.bottomNavigation)
    var bottomNavigation: BottomNavigation? = null

    @JvmField
    @BindView(R.id.fab)
    var fab: FloatingActionButton? = null

    @JvmField
    @BindView(R.id.language)
    var language: FontTextView? = null

    @JvmField
    @BindView(R.id.detailsIcon)
    var detailsIcon: View? = null

    @JvmField
    @BindView(R.id.tagsIcon)
    var tagsIcon: View? = null

    @JvmField
    @BindView(R.id.watchRepoImage)
    var watchRepoImage: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.starRepoImage)
    var starRepoImage: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.forkRepoImage)
    var forkRepoImage: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.licenseLayout)
    var licenseLayout: View? = null

    @JvmField
    @BindView(R.id.watchRepoLayout)
    var watchRepoLayout: View? = null

    @JvmField
    @BindView(R.id.starRepoLayout)
    var starRepoLayout: View? = null

    @JvmField
    @BindView(R.id.forkRepoLayout)
    var forkRepoLayout: View? = null

    @JvmField
    @BindView(R.id.pinImage)
    var pinImage: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.pinLayout)
    var pinLayout: View? = null

    @JvmField
    @BindView(R.id.pinText)
    var pinText: FontTextView? = null

    @JvmField
    @BindView(R.id.filterLayout)
    var filterLayout: View? = null

    @JvmField
    @BindView(R.id.topicsList)
    var topicsList: RecyclerView? = null

    @JvmField
    @BindView(R.id.sortByUpdated)
    var sortByUpdated: CheckBox? = null

    @JvmField
    @BindView(R.id.wikiLayout)
    var wikiLayout: View? = null

    @JvmField
    @State
    @RepoNavigationType
    var navType = 0

    @JvmField
    @State
    var login: String? = null

    @JvmField
    @State
    var repoId: String? = null

    @JvmField
    @State
    var showWhich = -1
    private val numberFormat = NumberFormat.getNumberInstance()
    private var userInteracted = false
    private var accentColor = 0
    private var iconColor = 0
    @OnLongClick(R.id.date)
    fun onShowDateHint(): Boolean {
        showMessage(R.string.creation_date, R.string.creation_date_hint)
        return true
    }

    @OnLongClick(R.id.size)
    fun onShowLastUpdateDateHint(): Boolean {
        showMessage(R.string.last_updated, R.string.last_updated_hint)
        return true
    }

    @OnLongClick(R.id.fab)
    fun onFabLongClick(): Boolean {
        if (navType == RepoPagerMvp.ISSUES) {
            onAddSelected()
            return true
        }
        return false
    }

    @OnClick(R.id.fab)
    fun onFabClicked() {
        if (navType == RepoPagerMvp.ISSUES) {
            fab!!.hide(object : OnVisibilityChangedListener() {
                override fun onHidden(fab: FloatingActionButton) {
                    super.onHidden(fab)
                    if (appbar != null) appbar!!.setExpanded(false, true)
                    bottomNavigation!!.setExpanded(false, true)
                    mimicFabVisibility(true, filterLayout!!, null)
                }
            })
        } else if (navType == RepoPagerMvp.PULL_REQUEST) {
            val pullRequestPagerView = AppHelper.getFragmentByTag(
                supportFragmentManager,
                RepoPullRequestPagerFragment.TAG
            ) as RepoPullRequestPagerFragment?
            if (pullRequestPagerView != null) {
                FilterIssuesActivity.startActivity(
                    this, presenter!!.login(), presenter!!.repoId(), false,
                    pullRequestPagerView.currentItem == 0, isEnterprise
                )
            }
        } else {
            fab!!.hide()
        }
    }

    @OnClick(R.id.add)
    fun onAddIssues() {
        hideFilterLayout()
        onAddSelected()
    }

    @OnClick(R.id.search)
    fun onSearch() {
        hideFilterLayout()
        onSearchSelected()
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (navType == RepoPagerMvp.ISSUES && filterLayout!!.isShown) {
            val viewRect = ViewHelper.getLayoutPosition(filterLayout!!)
            if (!viewRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                hideFilterLayout()
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    @OnClick(R.id.detailsIcon)
    fun onTitleClick() {
        val repoModel = presenter!!.getRepo()
        if (repoModel != null && !isEmpty(repoModel.description)) {
            newInstance(repoModel.fullName, repoModel.description, false, true)
                .show(supportFragmentManager, MessageDialogView.TAG)
        }
    }

    @OnClick(R.id.tagsIcon)
    fun onTagsClick() {
        if (topicsList!!.adapter!!.itemCount > 0) {
            TransitionManager.beginDelayedTransition(topicsList!!)
            topicsList!!.visibility =
                if (topicsList!!.visibility == View.VISIBLE) View.GONE else View.VISIBLE
        }
    }

    @OnClick(
        R.id.forkRepoLayout,
        R.id.starRepoLayout,
        R.id.watchRepoLayout,
        R.id.pinLayout,
        R.id.wikiLayout,
        R.id.licenseLayout
    )
    fun onClick(view: View) {
        when (view.id) {
            R.id.forkRepoLayout -> newInstance(
                getString(R.string.fork),
                String.format("%s %s/%s?", getString(R.string.fork), login, repoId),
                Bundler.start().put(BundleConstant.EXTRA, true)
                    .put(BundleConstant.YES_NO_EXTRA, true).end()
            )
                .show(supportFragmentManager, MessageDialogView.TAG)
            R.id.starRepoLayout -> if (!isEmpty(presenter!!.login()) && !isEmpty(
                    presenter!!.repoId()
                )
            ) {
                startForRepo(
                    this,
                    presenter!!.login(),
                    presenter!!.repoId(),
                    if (presenter!!.isStarred()) GithubActionService.UNSTAR_REPO else GithubActionService.STAR_REPO,
                    isEnterprise
                )
                presenter!!.onStar()
            }
            R.id.watchRepoLayout -> if (!isEmpty(presenter!!.login()) && !isEmpty(
                    presenter!!.repoId()
                )
            ) {
                startForRepo(
                    this,
                    presenter!!.login(),
                    presenter!!.repoId(),
                    if (presenter!!.isWatched()) GithubActionService.UNWATCH_REPO else GithubActionService.WATCH_REPO,
                    isEnterprise
                )
                presenter!!.onWatch()
            }
            R.id.pinLayout -> {
                pinLayout!!.isEnabled = false
                presenter!!.onPinUnpinRepo()
            }
            R.id.wikiLayout -> ActivityHelper.startReveal(
                this,
                getWiki(this, repoId, login),
                wikiLayout!!
            )
            R.id.licenseLayout -> if (presenter!!.getRepo() != null) {
                val licenseModel = presenter!!.getRepo()!!.license
                val license =
                    if (!isEmpty(licenseModel.spdxId)) licenseModel.spdxId else licenseModel.name
                newInstance(presenter!!.login(), presenter!!.repoId(), license!!)
                    .show(supportFragmentManager, "RepoLicenseBottomSheet")
            }
        }
    }

    @OnLongClick(R.id.forkRepoLayout, R.id.starRepoLayout, R.id.watchRepoLayout)
    fun onLongClick(view: View?): Boolean {
        when (view!!.id) {
            R.id.forkRepoLayout -> {
                RepoMiscDialogFragment.show(
                    supportFragmentManager,
                    login!!,
                    repoId!!,
                    RepoMiscMVp.FORKS
                )
                return true
            }
            R.id.starRepoLayout -> {
                RepoMiscDialogFragment.show(
                    supportFragmentManager,
                    login!!,
                    repoId!!,
                    RepoMiscMVp.STARS
                )
                return true
            }
            R.id.watchRepoLayout -> {
                RepoMiscDialogFragment.show(
                    supportFragmentManager,
                    login!!,
                    repoId!!,
                    RepoMiscMVp.WATCHERS
                )
                return true
            }
        }
        return false
    }

    @OnCheckedChanged(R.id.sortByUpdated)
    fun onSortIssues(isChecked: Boolean) {
        val pagerView = AppHelper.getFragmentByTag(
            supportFragmentManager,
            RepoIssuesPagerFragment.TAG
        ) as RepoIssuesPagerFragment?
        pagerView?.onChangeIssueSort(isChecked)
        hideFilterLayout()
    }

    override fun layout(): Int {
        return R.layout.repo_pager_activity
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = false

    override fun providePresenter(): RepoPagerPresenter {
        return RepoPagerPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            if (intent == null || intent.extras == null) {
                finish()
                return
            }
            val extras = intent.extras
            repoId = extras!!.getString(BundleConstant.ID)
            login = extras.getString(BundleConstant.EXTRA_TWO)
            navType = extras.getInt(BundleConstant.EXTRA_TYPE)
            showWhich = extras.getInt(BundleConstant.EXTRA_THREE)
            presenter!!.onUpdatePinnedEntry(repoId!!, login!!)
        }
        presenter!!.onActivityCreate(repoId!!, login!!, navType)
        setTitle("")
        accentColor = ViewHelper.getAccentColor(this)
        iconColor = ViewHelper.getIconColor(this)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.container, DummyFragment(), "DummyFragment")
                .commit()
        }
        val myTypeface = TypeFaceHelper.getTypeface()
        bottomNavigation!!.setDefaultTypeface(myTypeface)
        fab!!.imageTintList = ColorStateList.valueOf(Color.WHITE)
        showHideFab()
    }

    override fun onNavigationChanged(@RepoNavigationType navType: Int) {
        if (navType == RepoPagerMvp.PROFILE) {
            presenter!!.onModuleChanged(supportFragmentManager, navType)
            bottomNavigation!!.setSelectedIndex(this.navType, true)
            return
        }
        this.navType = navType
        try {
            if (bottomNavigation!!.selectedIndex != navType) bottomNavigation!!.setSelectedIndex(
                navType,
                true
            )
        } catch (ignored: Exception) {
        }
        showHideFab()
        presenter!!.onModuleChanged(supportFragmentManager, navType)
    }

    override fun onFinishActivity() {
        //do nothing here, github might return 404 if even the repo don't have anything but issues.
    }

    override fun onInitRepo() {
        hideProgress()
        if (presenter!!.getRepo() == null) {
            return
        }
        when (showWhich) {
            1 -> onLongClick(watchRepoLayout)
            2 -> onLongClick(starRepoLayout)
            3 -> onLongClick(forkRepoLayout)
            4 -> MilestoneDialogFragment.newInstance(
                login!!, repoId!!
            )
                .show(supportFragmentManager, "MilestoneDialogFragment")
            5 -> LabelsDialogFragment.newInstance(null, repoId!!, login!!)
                .show(supportFragmentManager, "LabelsDialogFragment")
        }
        showWhich = -1
        setTaskName(presenter!!.getRepo()!!.fullName)
        val repoModel = presenter!!.getRepo()
        if (repoModel!!.isHasProjects) {
            bottomNavigation!!.inflateMenu(R.menu.repo_with_project_bottom_nav_menu)
        }
        bottomNavigation!!.menuItemSelectionListener = presenter
        if (repoModel.topics != null && !repoModel.topics.isEmpty()) {
            tagsIcon!!.visibility = View.VISIBLE
            topicsList!!.adapter = TopicsAdapter(repoModel.topics)
        } else {
            topicsList!!.visibility = View.GONE
        }
        onRepoPinned(AbstractPinnedRepos.isPinned(repoModel.fullName))
        wikiLayout!!.visibility = if (repoModel.isHasWiki) View.VISIBLE else View.GONE
        pinText!!.setText(R.string.pin)
        detailsIcon!!.visibility = if (isEmpty(repoModel.description)) View.GONE else View.VISIBLE
        language!!.visibility = if (isEmpty(repoModel.language)) View.GONE else View.VISIBLE
        if (!isEmpty(repoModel.language)) {
            language!!.text = repoModel.language
            language!!.setTextColor(getColorAsColor(repoModel.language, language!!.context))
        }
        forkRepo!!.text = numberFormat.format(repoModel.forksCount)
        starRepo!!.text = numberFormat.format(repoModel.stargazersCount)
        watchRepo!!.text = numberFormat.format(repoModel.subsCount.toLong())
        if (repoModel.owner != null) {
            avatarLayout!!.setUrl(
                repoModel.owner.avatarUrl, repoModel.owner.login,
                repoModel.owner.isOrganizationType, isEnterprise(repoModel.htmlUrl)
            )
        } else if (repoModel.organization != null) {
            avatarLayout!!.setUrl(
                repoModel.organization.avatarUrl, repoModel.organization.login, true,
                isEnterprise(repoModel.htmlUrl)
            )
        }
        val repoSize = if (repoModel.size > 0) repoModel.size * 1000 else repoModel.size
        date!!.text = builder()
            .append(getTimeAgo(repoModel.pushedAt))
            .append(" ,")
            .append(" ")
            .append(Formatter.formatFileSize(this, repoSize))
        size!!.visibility = View.GONE
        title!!.text = repoModel.fullName
        TextViewCompat.setTextAppearance(title!!, R.style.TextAppearance_AppCompat_Medium)
        title!!.setTextColor(ViewHelper.getPrimaryTextColor(this))
        if (repoModel.license != null) {
            licenseLayout!!.visibility = View.VISIBLE
            val licenseModel = repoModel.license
            license!!.text =
                if (!isEmpty(licenseModel.spdxId)) licenseModel.spdxId else licenseModel.name
        }
        invalidateOptionsMenu()
        onRepoWatched(presenter!!.isWatched())
        onRepoStarred(presenter!!.isStarred())
        onRepoForked(presenter!!.isForked())
    }

    override fun onRepoWatched(isWatched: Boolean) {
        watchRepoImage!!.tintDrawableColor(if (isWatched) accentColor else iconColor)
        onEnableDisableWatch(true)
    }

    override fun onRepoStarred(isStarred: Boolean) {
        starRepoImage!!.setImageResource(if (isStarred) R.drawable.ic_star_filled else R.drawable.ic_star)
        starRepoImage!!.tintDrawableColor(if (isStarred) accentColor else iconColor)
        onEnableDisableStar(true)
    }

    override fun onRepoForked(isForked: Boolean) {
        forkRepoImage!!.tintDrawableColor(if (isForked) accentColor else iconColor)
        onEnableDisableFork(true)
    }

    override fun onRepoPinned(isPinned: Boolean) {
        pinImage!!.setImageResource(if (isPinned) R.drawable.ic_pin_filled else R.drawable.ic_pin)
        pinLayout!!.isEnabled = true
    }

    override fun onEnableDisableWatch(isEnabled: Boolean) {
        watchRepoLayout!!.isEnabled = isEnabled
    }

    override fun onEnableDisableStar(isEnabled: Boolean) {
        starRepoLayout!!.isEnabled = isEnabled
    }

    override fun onEnableDisableFork(isEnabled: Boolean) {
        forkRepoLayout!!.isEnabled = isEnabled
    }

    override fun onChangeWatchedCount(isWatched: Boolean) {
        val count = toLong(watchRepo!!)
        watchRepo!!.text =
            numberFormat.format(if (isWatched) count + 1 else if (count > 0) count - 1 else 0)
        updatePinnedRepo()
    }

    override fun onChangeStarCount(isStarred: Boolean) {
        val count = toLong(starRepo!!)
        starRepo!!.text =
            numberFormat.format(if (isStarred) count + 1 else if (count > 0) count - 1 else 0)
        updatePinnedRepo()
    }

    override fun onChangeForkCount(isForked: Boolean) {
        val count = toLong(forkRepo!!)
        forkRepo!!.text =
            numberFormat.format(if (isForked) count + 1 else if (count > 0) count - 1 else 0)
        updatePinnedRepo()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userInteracted = true
    }

    override fun hasUserInteractedWithView(): Boolean {
        return userInteracted
    }

    override fun disableIssueTab() {
        showMessage(R.string.error, R.string.repo_issues_is_disabled)
        bottomNavigation!!.setMenuItemEnabled(1, false)
        bottomNavigation!!.setSelectedIndex(navType, true)
    }

    override fun openUserProfile() {
        startActivity(this, Login.getUser().login, false, PrefGetter.isEnterprise, -1)
    }

    override fun onScrolled(isUp: Boolean) {
        if (fab != null) {
            if (isUp) {
                fab!!.hide()
            } else {
                fab!!.show()
            }
        }
    }

    override fun isCollaborator(): Boolean {
        return presenter!!.isRepoOwner
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.repo_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val repoModel = presenter!!.getRepo()
        if (repoModel != null && repoModel.isFork && repoModel.parent != null) {
            val menuItem = menu.findItem(R.id.originalRepo)
            menuItem.isVisible = true
            menuItem.title = repoModel.parent.fullName
        }
        //        menu.findItem(R.id.deleteRepo).setVisible(getPresenter().isRepoOwner());
        if (menu.findItem(R.id.deleteRepo) != null) menu.findItem(R.id.deleteRepo).isVisible =
            false //removing delete permission.
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                launchMainActivity(this, true)
                finish()
            }
            R.id.share -> {
                if (presenter!!.getRepo() != null) ActivityHelper.shareUrl(
                    this, presenter!!.getRepo()!!
                        .htmlUrl
                )
                return true
            }
            R.id.browser -> {
                if (presenter!!.getRepo() != null) ActivityHelper.startCustomTab(
                    this, presenter!!.getRepo()!!
                        .htmlUrl
                )
                return true
            }
            R.id.copy -> {
                if (presenter!!.getRepo() != null) AppHelper.copyToClipboard(
                    this, presenter!!.getRepo()!!
                        .htmlUrl
                )
                return true
            }
            R.id.originalRepo -> {
                if (presenter!!.getRepo() != null && presenter!!.getRepo()!!.parent != null) {
                    val parent = presenter!!.getRepo()!!.parent
                    launchUri(this, parent.htmlUrl)
                }
                return true
            }
            R.id.deleteRepo -> {
                newInstance(
                    getString(R.string.delete_repo), getString(R.string.delete_repo_warning),
                    Bundler.start().put(BundleConstant.EXTRA_TWO, true)
                        .put(BundleConstant.YES_NO_EXTRA, true)
                        .end()
                ).show(supportFragmentManager, MessageDialogView.TAG)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (isOk && bundle != null) {
            val isDelete = bundle.getBoolean(BundleConstant.EXTRA_TWO)
            val fork = bundle.getBoolean(BundleConstant.EXTRA)
            if (fork) {
                if (!presenter!!.isForked()) {
                    startForRepo(
                        this, presenter!!.login(), presenter!!.repoId(),
                        GithubActionService.FORK_REPO, isEnterprise
                    )
                    presenter!!.onFork()
                }
            }
            if (isDelete) presenter!!.onDeleteRepo()
        }
    }

    override fun onBackPressed() {
        if (navType == RepoPagerMvp.CODE) {
            val codePagerView = AppHelper.getFragmentByTag(
                supportFragmentManager,
                RepoCodePagerFragment.TAG
            ) as RepoCodePagerFragment?
            if (codePagerView != null) {
                if (codePagerView.canPressBack()) {
                    super.onBackPressed()
                } else {
                    codePagerView.onBackPressed()
                    return
                }
            }
        } else if (navType == RepoPagerMvp.ISSUES && filterLayout!!.isShown) {
            hideFilterLayout()
            return
        }
        super.onBackPressed()
    }

    override fun onAddSelected() {
        val pagerView = AppHelper.getFragmentByTag(
            supportFragmentManager,
            RepoIssuesPagerFragment.TAG
        ) as RepoIssuesPagerFragment?
        pagerView?.onAddIssue()
    }

    override fun onSearchSelected() {
        var isOpen = true
        val pagerView = AppHelper.getFragmentByTag(
            supportFragmentManager,
            RepoIssuesPagerFragment.TAG
        ) as RepoIssuesPagerFragment?
        if (pagerView != null) {
            isOpen = pagerView.currentItem == 0
        }
        FilterIssuesActivity.startActivity(
            this,
            presenter!!.login(),
            presenter!!.repoId(),
            true,
            isOpen,
            isEnterprise
        )
    }

    private fun showHideFab() {
        if (navType == RepoPagerMvp.ISSUES) {
            fab!!.setImageResource(R.drawable.ic_menu)
            fab!!.show()
        } else if (navType == RepoPagerMvp.PULL_REQUEST) {
            fab!!.setImageResource(R.drawable.ic_search)
            fab!!.show()
        } else {
            fab!!.hide()
        }
    }

    private fun hideFilterLayout() {
        mimicFabVisibility(false, filterLayout!!, object : OnVisibilityChangedListener() {
            override fun onHidden(actionButton: FloatingActionButton) {
                fab!!.show()
            }
        })
    }

    private fun updatePinnedRepo() {
        presenter!!.updatePinned(
            toLong(forkRepo!!).toInt(), toLong(starRepo!!).toInt(), toLong(
                watchRepo!!
            ).toInt()
        )
    }

    companion object {
        @JvmStatic
        fun startRepoPager(context: Context, nameParser: NameParser) {
            if (!isEmpty(nameParser.name) && !isEmpty(nameParser.username)) {
                val intent = Intent(context, RepoPagerActivity::class.java)
                intent.putExtras(
                    Bundler.start()
                        .put(BundleConstant.ID, nameParser.name)
                        .put(BundleConstant.EXTRA_TWO, nameParser.username)
                        .put(BundleConstant.EXTRA_TYPE, RepoPagerMvp.CODE)
                        .put(BundleConstant.IS_ENTERPRISE, nameParser.isEnterprise)
                        .end()
                )
                context.startActivity(intent)
            }
        }

        @JvmOverloads
        fun createIntent(
            context: Context, repoId: String, login: String,
            @RepoNavigationType navType: Int = RepoPagerMvp.CODE, showWhat: Int = -1
        ): Intent {
            val intent = Intent(context, RepoPagerActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.ID, repoId)
                    .put(BundleConstant.EXTRA_TWO, login)
                    .put(BundleConstant.EXTRA_TYPE, navType)
                    .put(BundleConstant.EXTRA_THREE, showWhat)
                    .end()
            )
            return intent
        }
    }
}