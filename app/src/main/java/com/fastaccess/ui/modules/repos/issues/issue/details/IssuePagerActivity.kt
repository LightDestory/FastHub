package com.fastaccess.ui.modules.repos.issues.issue.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import butterknife.BindView
import butterknife.OnClick
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.FragmentPagerAdapterModel.Companion.buildForIssues
import com.fastaccess.data.dao.LabelModel
import com.fastaccess.data.dao.MilestoneModel
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.PinnedIssues
import com.fastaccess.data.dao.model.User
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.Logger.e
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.helper.PrefGetter.isProEnabled
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.adapter.FragmentsPagerAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.modules.editor.comment.CommentEditorFragment
import com.fastaccess.ui.modules.main.premium.PremiumActivity.Companion.startActivity
import com.fastaccess.ui.modules.repos.RepoPagerActivity
import com.fastaccess.ui.modules.repos.RepoPagerMvp
import com.fastaccess.ui.modules.repos.extras.assignees.AssigneesDialogFragment
import com.fastaccess.ui.modules.repos.extras.labels.LabelsDialogFragment
import com.fastaccess.ui.modules.repos.extras.locking.LockIssuePrBottomSheetDialog.Companion.newInstance
import com.fastaccess.ui.modules.repos.extras.milestone.create.MilestoneDialogFragment
import com.fastaccess.ui.modules.repos.issues.create.CreateIssueActivity
import com.fastaccess.ui.modules.repos.issues.issue.details.timeline.IssueTimelineFragment
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import com.fastaccess.ui.widgets.ViewPagerView
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

/**
 * Created by Kosh on 10 Dec 2016, 9:23 AM
 */
class IssuePagerActivity : BaseActivity<IssuePagerMvp.View, IssuePagerPresenter>(),
    IssuePagerMvp.View {
    @JvmField
    @BindView(R.id.startGist)
    var startGist: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.forkGist)
    var forkGist: ForegroundImageView? = null

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
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null

    @JvmField
    @BindView(R.id.pager)
    var pager: ViewPagerView? = null

    @JvmField
    @BindView(R.id.fab)
    var fab: FloatingActionButton? = null

    @JvmField
    @BindView(R.id.detailsIcon)
    var detailsIcon: View? = null

    @JvmField
    @State
    var isClosed = false

    @JvmField
    @State
    var isOpened = false
    private var commentEditorFragment: CommentEditorFragment? = null

    override val data: Issue?
        get() = presenter?.issue

    @OnClick(R.id.detailsIcon)
    fun onTitleClick() {
        if (presenter!!.issue != null && !isEmpty(
                presenter!!.issue!!.title
            )
        ) newInstance(
            String.format("%s/%s", presenter!!.login, presenter!!.repoId),
            presenter!!.issue!!.title, isMarkDown = false, hideCancel = true
        )
            .show(supportFragmentManager, MessageDialogView.TAG)
    }

    override fun layout(): Int {
        return R.layout.issue_pager_activity
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = false

    override fun providePresenter(): IssuePagerPresenter {
        return IssuePagerPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        commentEditorFragment =
            supportFragmentManager.findFragmentById(R.id.commentFragment) as CommentEditorFragment?
        tabs!!.visibility = View.GONE
        if (savedInstanceState == null) {
            presenter!!.onActivityCreated(intent)
        } else {
            if (presenter!!.issue != null) onSetupIssue(false)
        }
        startGist!!.visibility = View.GONE
        forkGist!!.visibility = View.GONE
        fab!!.hide()
        if (presenter!!.showToRepoBtn()) showNavToRepoItem()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == BundleConstant.REQUEST_CODE) {
                val bundle = data.extras
                if (bundle != null) {
                    val issueModel: Issue? = bundle.getParcelable(BundleConstant.ITEM)
                    if (issueModel != null) {
                        presenter!!.onUpdateIssue(issueModel)
                    }
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.issue_menu, menu)
        menu.findItem(R.id.closeIssue).isVisible = presenter!!.isOwner
        menu.findItem(R.id.lockIssue).isVisible =
            presenter!!.isRepoOwner || presenter!!.isCollaborator
        menu.findItem(R.id.labels).isVisible = presenter!!.isRepoOwner || presenter!!.isCollaborator
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onNavToRepoClicked()
            return true
        }
        val issueModel = presenter!!.issue ?: return false
        when (item.itemId) {
            R.id.share -> {
                ActivityHelper.shareUrl(this, presenter!!.issue!!.htmlUrl)
                return true
            }
            R.id.closeIssue -> {
                newInstance(
                    if (issueModel.state === IssueState.open) getString(R.string.close_issue) else getString(
                        R.string.re_open_issue
                    ),
                    getString(R.string.confirm_message),
                    Bundler.start().put(BundleConstant.EXTRA, true)
                        .put(BundleConstant.YES_NO_EXTRA, true).end()
                )
                    .show(supportFragmentManager, MessageDialogView.TAG)
                return true
            }
            R.id.lockIssue -> {
                if (!presenter!!.isLocked) {
                    newInstance()
                        .show(supportFragmentManager, MessageDialogView.TAG)
                } else {
                    newInstance(
                        getString(R.string.unlock_issue), getString(R.string.unlock_issue_details),
                        Bundler.start().put(BundleConstant.EXTRA_TWO, true)
                            .put(BundleConstant.YES_NO_EXTRA, true)
                            .end()
                    )
                        .show(supportFragmentManager, MessageDialogView.TAG)
                }
                return true
            }
            R.id.labels -> {
                LabelsDialogFragment.newInstance(
                    if (presenter!!.issue != null) presenter!!.issue!!
                        .labels else null,
                    presenter!!.repoId!!, presenter!!.login!!
                )
                    .show(supportFragmentManager, "LabelsDialogFragment")
                return true
            }
            R.id.edit -> {
                CreateIssueActivity.startForResult(
                    this, presenter!!.login!!, presenter!!.repoId!!,
                    presenter!!.issue, isEnterprise
                )
                return true
            }
            R.id.milestone -> {
                MilestoneDialogFragment.newInstance(
                    presenter!!.login!!, presenter!!.repoId!!
                )
                    .show(supportFragmentManager, "MilestoneDialogFragment")
                return true
            }
            R.id.assignees -> {
                AssigneesDialogFragment.newInstance(
                    presenter!!.login!!,
                    presenter!!.repoId!!,
                    true
                )
                    .show(supportFragmentManager, "AssigneesDialogFragment")
                return true
            }
            R.id.subscribe -> {
                presenter!!.onSubscribeOrMute(false)
                return true
            }
            R.id.mute -> {
                presenter!!.onSubscribeOrMute(true)
                return true
            }
            R.id.browser -> {
                ActivityHelper.startCustomTab(this, issueModel.htmlUrl)
                return true
            }
            R.id.pinUnpin -> {
                if (isProEnabled) {
                    presenter!!.onPinUnpinIssue()
                } else {
                    startActivity(this)
                }
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val closeIssue = menu.findItem(R.id.closeIssue)
        val lockIssue = menu.findItem(R.id.lockIssue)
        val milestone = menu.findItem(R.id.milestone)
        val labels = menu.findItem(R.id.labels)
        val assignees = menu.findItem(R.id.assignees)
        val edit = menu.findItem(R.id.edit)
        val editMenu = menu.findItem(R.id.editMenu)
        val pinUnpin = menu.findItem(R.id.pinUnpin)
        val isOwner = presenter!!.isOwner
        val isLocked = presenter!!.isLocked
        val isCollaborator = presenter!!.isCollaborator
        val isRepoOwner = presenter!!.isRepoOwner
        editMenu.isVisible = isOwner || isCollaborator || isRepoOwner
        milestone.isVisible = isCollaborator || isRepoOwner
        labels.isVisible = isCollaborator || isRepoOwner
        assignees.isVisible = isCollaborator || isRepoOwner
        edit.isVisible = isCollaborator || isRepoOwner || isOwner
        lockIssue.isVisible = isRepoOwner || isCollaborator
        labels.isVisible = presenter!!.isRepoOwner || isCollaborator
        closeIssue.isVisible = isOwner || isCollaborator
        if (presenter!!.issue != null) {
            val isPinned = PinnedIssues.isPinned(presenter!!.issue!!.id)
            pinUnpin.icon = if (isPinned) ContextCompat.getDrawable(
                this,
                R.drawable.ic_pin_filled
            ) else ContextCompat.getDrawable(this, R.drawable.ic_pin)
            closeIssue.title =
                if (presenter!!.issue!!.state === IssueState.closed) getString(
                    R.string.re_open
                ) else getString(R.string.close)
            lockIssue.title =
                if (isLocked) getString(R.string.unlock_issue) else getString(R.string.lock_issue)
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onSetupIssue(isUpdate: Boolean) {
        hideProgress()
        if (presenter!!.issue == null) {
            return
        }
        onUpdateMenu()
        val issueModel = presenter!!.issue
        setTaskName(issueModel!!.repoId + " - " + issueModel.title)
        setTitle(String.format("#%s", issueModel.number))
        if (supportActionBar != null) {
            supportActionBar!!.subtitle = issueModel.repoId
        }
        updateViews(issueModel)
        if (isUpdate) {
            val issueDetailsView = issueTimelineFragment
            if (issueDetailsView != null && presenter!!.issue != null) {
                issueDetailsView.onUpdateHeader()
            }
        } else {
            if (pager!!.adapter == null) {
                e(presenter!!.commentId)
                pager!!.adapter = FragmentsPagerAdapter(
                    supportFragmentManager, buildForIssues(this, presenter!!.commentId)
                )
            } else {
                onUpdateTimeline()
            }
        }
        if (!presenter!!.isLocked || presenter!!.isOwner) {
            pager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    hideShowFab()
                }
            })
        }
        hideShowFab()
    }

    override fun showSuccessIssueActionMsg(isClose: Boolean) {
        hideProgress()
        if (isClose) {
            isOpened = false
            isClosed = true
            showMessage(getString(R.string.success), getString(R.string.success_closed))
        } else {
            isOpened = true
            isClosed = false
            showMessage(getString(R.string.success), getString(R.string.success_re_opened))
        }
    }

    override fun showErrorIssueActionMsg(isClose: Boolean) {
        hideProgress()
        if (isClose) {
            showMessage(getString(R.string.error), getString(R.string.error_closing_issue))
        } else {
            showMessage(getString(R.string.error), getString(R.string.error_re_opening_issue))
        }
    }

    override fun onUpdateTimeline() {
        val issueDetailsView = issueTimelineFragment
        if (issueDetailsView != null && presenter!!.issue != null) {
            issueDetailsView.onRefresh()
        }
    }

    override fun onUpdateMenu() {
        invalidateOptionsMenu()
    }

    override fun onMileStoneSelected(milestoneModel: MilestoneModel) {
        presenter!!.onPutMilestones(milestoneModel)
    }

    override fun onFinishActivity() {
        hideProgress()
        finish()
    }


    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (isOk) {
            presenter!!.onHandleConfirmDialog(bundle)
        }
    }

    override fun onSelectedLabels(labels: ArrayList<LabelModel>) {
        presenter!!.onPutLabels(labels)
    }

    override fun onSelectedAssignees(users: ArrayList<User>, isAssignee: Boolean) {
        presenter!!.onPutAssignees(users)
    }

    override fun onNavToRepoClicked() {
        val intent = ActivityHelper.editBundle(
            RepoPagerActivity.createIntent(
                this, presenter!!.repoId!!,
                presenter!!.login!!, RepoPagerMvp.ISSUES
            ), isEnterprise
        )
        startActivity(intent)
        finish()
    }


    override fun finish() {
        val intent = Intent()
        intent.putExtras(
            Bundler.start()
                .put(BundleConstant.EXTRA, isClosed)
                .put(BundleConstant.EXTRA_TWO, isOpened)
                .end()
        )
        setResult(RESULT_OK, intent)
        super.finish()
    }

    override fun onSendActionClicked(text: String, bundle: Bundle?) {
        val fragment = issueTimelineFragment
        fragment?.onHandleComment(text, bundle)
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
        val fragment = issueTimelineFragment
        return fragment?.namesToTag ?: ArrayList()
    }

    override fun onLock(reason: String) {
        presenter!!.onLockUnlockIssue(reason)
    }

    private val issueTimelineFragment: IssueTimelineFragment?
        get() = if (pager == null || pager!!.adapter == null) null else pager!!.adapter!!.instantiateItem(
            pager!!,
            0
        ) as IssueTimelineFragment

    private fun hideShowFab() {
        if (presenter!!.isLocked && !presenter!!.isOwner && !presenter!!.isCollaborator) {
            supportFragmentManager.beginTransaction().hide(commentEditorFragment!!).commit()
            return
        }
        supportFragmentManager.beginTransaction().show(commentEditorFragment!!).commit()
    }

    private fun updateViews(issueModel: Issue) {
        val userModel = issueModel.user
        title!!.text = issueModel.title
        detailsIcon!!.visibility = View.VISIBLE
        if (userModel != null) {
            size!!.visibility = View.GONE
            val username: String
            val parsedDate: CharSequence
            if (issueModel.state === IssueState.closed) {
                username = if (issueModel.closedBy != null) issueModel.closedBy.login else "N/A"
                parsedDate =
                    if (issueModel.closedAt != null) getTimeAgo(issueModel.closedAt) else "N/A"
            } else {
                parsedDate = getTimeAgo(issueModel.createdAt)
                username = if (issueModel.user != null) issueModel.user.login else "N/A"
            }
            date!!.text = builder()
                .append(
                    ContextCompat.getDrawable(
                        this,
                        if (issueModel.state === IssueState.open)
                            R.drawable.ic_issue_opened_small else
                            R.drawable.ic_issue_closed_small
                    )
                )
                .append(" ")
                .append(getString(issueModel.state.status))
                .append(" ").append(getString(R.string.by)).append(" ").append(username).append(" ")
                .append(parsedDate)
            avatarLayout!!.setUrl(
                userModel.avatarUrl, userModel.login, false,
                isEnterprise(issueModel.htmlUrl)
            )
        }
    }

    companion object {
        @JvmOverloads
        fun createIntent(
            context: Context, repoId: String,
            login: String, number: Int, showToRepoBtn: Boolean = false,
            isEnterprise: Boolean = false, commentId: Long = 0
        ): Intent {
            val intent = Intent(context, IssuePagerActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.ID, number)
                    .put(BundleConstant.EXTRA, login)
                    .put(BundleConstant.EXTRA_TWO, repoId)
                    .put(BundleConstant.EXTRA_THREE, showToRepoBtn)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .put(BundleConstant.EXTRA_SIX, commentId)
                    .end()
            )
            return intent
        }
    }
}