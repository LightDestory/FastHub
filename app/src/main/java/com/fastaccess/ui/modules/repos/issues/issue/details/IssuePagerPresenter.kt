package com.fastaccess.ui.modules.repos.issues.issue.details

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import com.annimon.stream.Stream
import com.fastaccess.R
import com.fastaccess.data.dao.*
import com.fastaccess.data.dao.IssueRequestModel.Companion.clone
import com.fastaccess.data.dao.PullsIssuesParser.Companion.getForIssue
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.PinnedIssues
import com.fastaccess.data.dao.model.User
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.Logger.e
import com.fastaccess.helper.RxHelper.getObservable
import com.fastaccess.provider.rest.RestProvider.getErrorCode
import com.fastaccess.provider.rest.RestProvider.getIssueService
import com.fastaccess.provider.rest.RestProvider.getNotificationService
import com.fastaccess.provider.rest.RestProvider.getRepoService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import io.reactivex.Observable

/**
 * Created by Kosh on 10 Dec 2016, 9:23 AM
 */
class IssuePagerPresenter : BasePresenter<IssuePagerMvp.View>(), IssuePagerMvp.Presenter {
    @com.evernote.android.state.State
    override var issue: Issue? = null

    @JvmField
    @com.evernote.android.state.State
    var issueNumber = 0

    @com.evernote.android.state.State
    override var login: String? = null

    @com.evernote.android.state.State
    override var repoId: String? = null

    @com.evernote.android.state.State
    override var isCollaborator = false

    @com.evernote.android.state.State
    var showToRepoBtn = false

    @JvmField
    @com.evernote.android.state.State
    var commentId: Long = 0
    override fun onError(throwable: Throwable) {
        if (getErrorCode(throwable) == 404) {
            sendToView { it.onOpenUrlInBrowser() }
        } else {
            onWorkOffline(issueNumber.toLong(), login!!, repoId!!)
        }
        super.onError(throwable)
    }

    override fun onActivityCreated(intent: Intent?) {
        e(isEnterprise)
        if (intent != null && intent.extras != null) {
            issue = intent.extras!!.getParcelable(BundleConstant.ITEM)
            issueNumber = intent.extras!!.getInt(BundleConstant.ID)
            login = intent.extras!!.getString(BundleConstant.EXTRA)
            repoId = intent.extras!!.getString(BundleConstant.EXTRA_TWO)
            showToRepoBtn = intent.extras!!.getBoolean(BundleConstant.EXTRA_THREE)
            commentId = intent.extras!!.getLong(BundleConstant.EXTRA_SIX)
            if (issue != null) {
                issueNumber = issue!!.number
                sendToView { view -> view.onSetupIssue(false) }
                return
            } else if (issueNumber > 0 && !isEmpty(login) && !isEmpty(repoId)) {
                issueFromApi
                return
            }
        }
        sendToView { view -> view.onSetupIssue(false) }
    }

    override fun onWorkOffline(issueNumber: Long, repoId: String, login: String) {
        if (issue == null) {
            manageDisposable(
                getObservable(
                    Issue.getIssueByNumber(
                        issueNumber.toInt(), repoId, login
                    )
                )
                    .subscribe { issueModel1: Issue? ->
                        if (issueModel1 != null) {
                            issue = issueModel1
                            sendToView { view ->
                                view.onSetupIssue(
                                    false
                                )
                            }
                        }
                    })
        } else {
            sendToView { it.hideProgress() }
        }
    }

    override val isOwner: Boolean
        get() {
            if (issue == null) return false
            val userModel = if (issue != null) issue!!.user else null
            val me = Login.getUser()
            val parser = getForIssue(issue!!.htmlUrl)
            return (userModel != null && userModel.login.equals(me.login, ignoreCase = true)
                    || parser != null && parser.login.equals(me.login, ignoreCase = true))
        }
    override val isRepoOwner: Boolean
        get() {
            if (issue == null) return false
            val me = Login.getUser()
            return TextUtils.equals(login, me.login)
        }
    override val isLocked: Boolean
        get() = issue != null && issue!!.isLocked

    override fun showToRepoBtn(): Boolean {
        return showToRepoBtn
    }

    override fun onHandleConfirmDialog(bundle: Bundle?) {
        if (bundle != null) {
            val proceedCloseIssue = bundle.getBoolean(BundleConstant.EXTRA)
            val proceedLockUnlock = bundle.getBoolean(BundleConstant.EXTRA_TWO)
            if (proceedCloseIssue) {
                onOpenCloseIssue()
            } else if (proceedLockUnlock) {
                onLockUnlockIssue(null)
            }
        }
    }

    override fun onOpenCloseIssue() {
        val currentIssue = issue
        if (currentIssue != null) {
            val requestModel = clone(currentIssue, true)
            manageDisposable(
                getObservable(
                    getIssueService(isEnterprise).editIssue(
                        login!!, repoId!!,
                        issueNumber, requestModel
                    )
                )
                    .doOnSubscribe {
                        sendToView { view -> view.showProgress(0) }
                    }
                    .subscribe({ issue: Issue? ->
                        if (issue != null) {
                            sendToView { view ->
                                view.showSuccessIssueActionMsg(
                                    currentIssue.state === IssueState.open
                                )
                            }
                            issue.repoId = issue.repoId
                            issue.login = issue.login
                            this.issue = issue
                            sendToView { view ->
                                view.onSetupIssue(
                                    false
                                )
                            }
                        }
                    }) { throwable -> onError(throwable) })
        }
    }

    override fun onLockUnlockIssue(reason: String?) {
        val currentIssue = issue ?: return
        val login = login
        val repoId = repoId
        val number = currentIssue.number
        var model: LockIssuePrModel? = null
        if (!isLocked && !isEmpty(reason)) {
            model = LockIssuePrModel(true, reason)
        }
        val issueService = getIssueService(isEnterprise)
        val observable = getObservable(
            if (model == null) issueService.unlockIssue(
                login!!, repoId!!, number
            ) else issueService.lockIssue(model, login!!, repoId!!, number)
        )
        makeRestCall(
            observable
        ) { booleanResponse ->
            val code = booleanResponse.code()
            if (code == 204) {
                issue!!.isLocked = !isLocked
                sendToView { view -> view.onSetupIssue(true) }
            }
            sendToView { it.hideProgress() }
        }
    }

    override fun onPutMilestones(milestone: MilestoneModel) {
        issue!!.milestone = milestone
        val issueRequestModel = clone(issue!!, false)
        makeRestCall(
            getIssueService(isEnterprise).editIssue(
                login!!, repoId!!, issueNumber, issueRequestModel
            )
        ) { issue: Issue ->
            this.issue!!.milestone = issue.milestone
            manageObservable(issue.save(this.issue).toObservable())
            sendToView { view: IssuePagerMvp.View ->
                updateTimeline(
                    view,
                    R.string.labels_added_successfully
                )
            }
        }
    }

    override fun onPutLabels(labels: ArrayList<LabelModel>) {
        makeRestCall(getIssueService(isEnterprise).putLabels(
            login!!, repoId!!, issueNumber,
            labels.filter { value: LabelModel? -> value?.name != null }
                .map { it.name!! }
        )
        ) {
            sendToView { view ->
                updateTimeline(
                    view,
                    R.string.labels_added_successfully
                )
            }
            val listModel = LabelListModel()
            listModel.addAll(labels)
            issue!!.labels = listModel
            manageObservable(issue!!.save(issue).toObservable())
        }
    }

    override fun onPutAssignees(users: ArrayList<User>) {
        val assigneesRequestModel = AssigneesRequestModel()
        val assignees = ArrayList<String>()
        Stream.of(users).forEach { userModel: User -> assignees.add(userModel.login) }
        assigneesRequestModel.assignees = if (assignees.isEmpty())
            issue!!.assignees.map { obj -> obj!!.login }.toList() else assignees
        makeRestCall(
            if (assignees.isNotEmpty()) getIssueService(isEnterprise).putAssignees(
                login!!, repoId!!, issueNumber, assigneesRequestModel
            ) else getIssueService(isEnterprise).deleteAssignees(
                login!!, repoId!!, issueNumber, assigneesRequestModel
            )
        ) { issue: Issue? ->
            val assignee = UsersListModel()
            assignee.addAll(users)
            issue!!.assignees = assignee
            manageObservable(issue.save(this.issue).toObservable())
            sendToView { view: IssuePagerMvp.View ->
                updateTimeline(
                    view,
                    R.string.assignee_added
                )
            }
        }
    }

    override fun onUpdateIssue(issueModel: Issue) {
        this.issue!!.body = issueModel.body
        this.issue!!.bodyHtml = issueModel.bodyHtml
        this.issue!!.title = issueModel.title
        this.issue!!.login = login
        this.issue!!.repoId = repoId
        manageObservable(issueModel.save(this.issue).toObservable())
        sendToView { view -> view.onSetupIssue(true) }
    }

    override fun onSubscribeOrMute(mute: Boolean) {
        if (issue == null) return
        makeRestCall(
            if (mute) getNotificationService(isEnterprise).subscribe(
                issue!!.id,
                NotificationSubscriptionBodyModel(subscribed = false, ignored = true)
            ) else getNotificationService(isEnterprise).subscribe(
                issue!!.id,
                NotificationSubscriptionBodyModel(subscribed = true, ignored = false)
            )
        ) { booleanResponse ->
            if (booleanResponse.code() == 204 || booleanResponse.code() == 200) {
                sendToView { view ->
                    view.showMessage(
                        R.string.success,
                        R.string.successfully_submitted
                    )
                }
            } else {
                sendToView { view ->
                    view.showMessage(
                        R.string.error,
                        R.string.network_error
                    )
                }
            }
        }
    }

    override fun onPinUnpinIssue() {
        if (issue == null) return
        PinnedIssues.pinUpin(issue!!)
        sendToView { it.onUpdateMenu() }
    }

    private val issueFromApi: Unit
        get() {
            val loginUser = Login.getUser() ?: return
            makeRestCall<Issue>(getObservable(
                Observable.zip(
                    getIssueService(isEnterprise).getIssue(
                        login!!, repoId!!, issueNumber
                    ),
                    getRepoService(isEnterprise).isCollaborator(
                        login!!,
                        repoId!!,
                        loginUser.login
                    )
                ) { issue, booleanResponse ->
                    isCollaborator = booleanResponse.code() == 204
                    issue
                }
            )) { issue: Issue -> setupIssue(issue) }
        }

    private fun setupIssue(issue: Issue) {
        this.issue = issue
        issue.repoId = repoId
        issue.login = login
        sendToView { view -> view.onSetupIssue(false) }
        manageDisposable(PinnedIssues.updateEntry(issue.id))
    }

    private fun updateTimeline(view: IssuePagerMvp.View, assignee_added: Int) {
        view.showMessage(R.string.success, assignee_added)
        view.onUpdateTimeline()
    }
}