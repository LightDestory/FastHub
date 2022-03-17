package com.fastaccess.ui.modules.gists.gist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.format.Formatter
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import butterknife.BindView
import butterknife.OnClick
import com.fastaccess.R
import com.fastaccess.data.dao.FragmentPagerAdapterModel.Companion.buildForGist
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.PinnedGists
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.helper.PrefGetter.isAllFeaturesUnlocked
import com.fastaccess.helper.PrefGetter.isProEnabled
import com.fastaccess.helper.ViewHelper
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.provider.tasks.git.GithubActionService
import com.fastaccess.provider.tasks.git.GithubActionService.Companion.startForGist
import com.fastaccess.ui.adapter.FragmentsPagerAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.editor.comment.CommentEditorFragment
import com.fastaccess.ui.modules.gists.GistsListActivity
import com.fastaccess.ui.modules.gists.create.CreateGistActivity.Companion.launcher
import com.fastaccess.ui.modules.gists.gist.comments.GistCommentsFragment
import com.fastaccess.ui.modules.main.premium.PremiumActivity.Companion.startActivity
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.ViewPagerView
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

/**
 * Created by Kosh on 12 Nov 2016, 12:18 PM
 */
class GistActivity : BaseActivity<GistMvp.View, GistPresenter>(), GistMvp.View {
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
    @BindView(R.id.pager)
    var pager: ViewPagerView? = null

    @JvmField
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null

    @JvmField
    @BindView(R.id.fab)
    var fab: FloatingActionButton? = null

    @JvmField
    @BindView(R.id.startGist)
    var startGist: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.forkGist)
    var forkGist: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.detailsIcon)
    var detailsIcon: View? = null

    @JvmField
    @BindView(R.id.edit)
    var edit: View? = null

    @JvmField
    @BindView(R.id.pinUnpin)
    var pinUnpin: ForegroundImageView? = null
    private var accentColor = 0
    private var iconColor = 0
    private var commentEditorFragment: CommentEditorFragment? = null

    @OnClick(R.id.detailsIcon)
    fun onTitleClick() {
        if (presenter!!.gist != null && !isEmpty(presenter!!.gist!!.description)) newInstance(
            getString(R.string.details),
            presenter!!.gist!!.description,
            isMarkDown = false,
            hideCancel = true
        )
            .show(supportFragmentManager, MessageDialogView.TAG)
    }

    @OnClick(R.id.startGist, R.id.forkGist, R.id.browser)
    fun onGistActions(view: View) {
        if (presenter!!.gist == null) return
        if (view.id != R.id.browser) {
            view.isEnabled = false
        }
        when (view.id) {
            R.id.startGist -> {
                startForGist(
                    this,
                    presenter!!.gist!!.gistId,
                    if (presenter!!.isStarred) GithubActionService.UNSTAR_GIST else GithubActionService.STAR_GIST,
                    isEnterprise
                )
                presenter!!.onStarGist()
            }
            R.id.forkGist -> {
                startForGist(
                    this, presenter!!.gist!!.gistId,
                    GithubActionService.FORK_GIST, isEnterprise
                )
                presenter!!.onForkGist()
            }
            R.id.browser -> ActivityHelper.startCustomTab(this, presenter!!.gist!!.htmlUrl)
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        presenter!!.callApi()
    }

    @OnClick(R.id.edit)
    fun onEdit() {
        if (isProEnabled || isAllFeaturesUnlocked) {
            if (presenter!!.gist != null) launcher(this, launcher, presenter!!.gist!!)
        } else {
            startActivity(this)
        }
    }

    @OnClick(R.id.pinUnpin)
    fun pinUpin() {
        if (isProEnabled) {
            presenter!!.onPinUnpinGist()
        } else {
            startActivity(this)
        }
    }

    override fun layout(): Int {
        return R.layout.gists_pager_layout
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = false

    override fun providePresenter(): GistPresenter {
        return GistPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fab!!.hide()
        commentEditorFragment =
            supportFragmentManager.findFragmentById(R.id.commentFragment) as CommentEditorFragment?
        // default
        hideShowFab()
        accentColor = ViewHelper.getAccentColor(this)
        iconColor = ViewHelper.getIconColor(this)
        if (savedInstanceState == null) {
            presenter!!.onActivityCreated(intent)
        } else {
            if (presenter!!.gist != null) {
                onSetupDetails()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.gist_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.share -> {
                if (presenter!!.gist != null) ActivityHelper.shareUrl(
                    this,
                    presenter!!.gist!!.htmlUrl
                )
                return true
            }
            R.id.deleteGist -> {
                newInstance(
                    getString(R.string.delete_gist), getString(R.string.confirm_message),
                    Bundler.start()
                        .put(BundleConstant.YES_NO_EXTRA, true)
                        .put(BundleConstant.EXTRA, true).end()
                )
                    .show(supportFragmentManager, MessageDialogView.TAG)
                return true
            }
            android.R.id.home -> {
                GistsListActivity.startActivity(this)
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        menu.findItem(R.id.deleteGist).isVisible = presenter!!.isOwner
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (bundle != null) {
            val isDelete = bundle.getBoolean(BundleConstant.EXTRA) && isOk
            if (isDelete) {
                presenter!!.onDeleteGist()
            }
        }
    }

    override fun onSuccessDeleted() {
        hideProgress()
        if (presenter!!.gist != null) {
            val intent = Intent()
            val gistsModel = Gist()
            gistsModel.url = presenter!!.gist!!.htmlUrl
            intent.putExtras(Bundler.start().put(BundleConstant.ITEM, gistsModel).end())
            setResult(RESULT_OK, intent)
        }
        finish()
    }

    override fun onErrorDeleting() {
        showErrorMessage(getString(R.string.error_deleting_gist))
    }

    override fun onGistStarred(isStarred: Boolean) {
        startGist!!.setImageResource(if (isStarred) R.drawable.ic_star_filled else R.drawable.ic_star)
        startGist!!.tintDrawableColor(if (isStarred) accentColor else iconColor)
        startGist!!.isEnabled = true
    }

    override fun onGistForked(isForked: Boolean) {
        forkGist!!.tintDrawableColor(if (isForked) accentColor else iconColor)
        forkGist!!.isEnabled = true
    }

    override fun onSetupDetails() {
        hideProgress()
        val gistsModel = presenter!!.gist ?: return
        onUpdatePinIcon(gistsModel)
        val url =
            if (gistsModel.owner != null) gistsModel.owner.avatarUrl else if (gistsModel.user != null) gistsModel.user.avatarUrl else ""
        val login =
            if (gistsModel.owner != null) gistsModel.owner.login else if (gistsModel.user != null) gistsModel.user.login else ""
        avatarLayout!!.setUrl(url, login, false, isEnterprise(gistsModel.htmlUrl))
        title!!.text = gistsModel.getDisplayTitle(false, true)
        setTaskName(gistsModel.getDisplayTitle(false, true).toString())
        edit!!.visibility = if (Login.getUser().login == login) View.VISIBLE else View.GONE
        detailsIcon!!.visibility = if (isEmpty(gistsModel.description) || !ViewHelper.isEllipsed(
                title!!
            )
        ) View.GONE else View.VISIBLE
        if (gistsModel.createdAt.before(gistsModel.updatedAt)) {
            date!!.text =
                String.format("%s %s", getTimeAgo(gistsModel.createdAt), getString(R.string.edited))
        } else {
            date!!.text = getTimeAgo(gistsModel.createdAt)
        }
        size!!.text = Formatter.formatFileSize(this, gistsModel.size)
        pager!!.adapter =
            FragmentsPagerAdapter(supportFragmentManager, buildForGist(this, gistsModel))
        pager!!.currentItem = 0
        tabs!!.setupWithViewPager(pager)
        pager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                hideShowFab()
            }
        })
        invalidateOptionsMenu()
        onGistForked(presenter!!.isForked)
        onGistStarred(presenter!!.isStarred)
        hideShowFab()
        tabs!!.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(pager) {
            override fun onTabReselected(tab: TabLayout.Tab) {
                super.onTabReselected(tab)
                onScrollTop(tab.position)
            }
        })
    }

    override fun onUpdatePinIcon(gist: Gist) {
        pinUnpin!!.setImageDrawable(
            if (PinnedGists.isPinned(
                    gist.gistId.hashCode().toLong()
                )
            ) ContextCompat.getDrawable(
                this,
                R.drawable.ic_pin_filled
            ) else ContextCompat.getDrawable(
                this,
                R.drawable.ic_pin
            )
        )
    }

    override fun onScrollTop(index: Int) {
        if (pager == null || pager!!.adapter == null) return
        val fragment: Fragment = pager!!.adapter!!
            .instantiateItem(pager!!, index) as BaseFragment<*, *>
        if (fragment is BaseFragment<*, *>) {
            fragment.onScrollTop(index)
        }
    }

    override fun onSendActionClicked(text: String, bundle: Bundle?) {
        val view = gistCommentsFragment
        view?.onHandleComment(text, bundle!!)
    }

    override fun onTagUser(username: String) {
        commentEditorFragment!!.onAddUserName(username)
    }

    override fun onCreateComment(text: String, bundle: Bundle?) {}
    override fun onClearEditText() {
        if (commentEditorFragment != null) commentEditorFragment!!.commentText.setText(
            ""
        )
    }

    override fun getNamesToTag(): ArrayList<String> {
        val view = gistCommentsFragment
        return view?.namesToTag ?: ArrayList()
    }

    private val gistCommentsFragment: GistCommentsFragment?
        get() = if (pager == null || pager!!.adapter == null) null else pager!!.adapter!!.instantiateItem(
            pager!!,
            1
        ) as GistCommentsFragment

    private fun hideShowFab() {
        if (pager!!.currentItem == 1) {
            supportFragmentManager.beginTransaction().show(commentEditorFragment!!).commit()
        } else {
            supportFragmentManager.beginTransaction().hide(commentEditorFragment!!).commit()
        }
    }

    companion object {
        @JvmStatic
        fun createIntent(context: Context, gistId: String, isEnterprise: Boolean): Intent {
            val intent = Intent(context, GistActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.EXTRA, gistId)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .end()
            )
            return intent
        }
    }
}