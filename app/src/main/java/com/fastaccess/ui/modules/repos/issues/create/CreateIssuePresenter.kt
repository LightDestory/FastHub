package com.fastaccess.ui.modules.repos.issues.create

import android.app.Activity
import android.content.Intent
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.data.dao.*
import com.fastaccess.data.dao.IssueRequestModel.Companion.clone
import com.fastaccess.data.dao.model.*
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.InputHelper.toString
import com.fastaccess.helper.RxHelper.getObservable
import com.fastaccess.provider.rest.RestProvider.getIssueService
import com.fastaccess.provider.rest.RestProvider.getPullRequestService
import com.fastaccess.provider.rest.RestProvider.getRepoService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import net.grandcentrix.thirtyinch.ViewAction

/**
 * Created by Kosh on 19 Feb 2017, 12:18 PM
 */
class CreateIssuePresenter : BasePresenter<CreateIssueMvp.View>(), CreateIssueMvp.Presenter {
    @com.evernote.android.state.State
    override var isCollaborator = false
    override fun checkAuthority(login: String, repoId: String) {
        manageViewDisposable(
            getObservable(
                getRepoService(isEnterprise).isCollaborator(
                    login,
                    repoId,
                    Login.getUser().login
                )
            )
                .subscribe({ booleanResponse ->
                    isCollaborator = booleanResponse.code() == 204
                    sendToView { it.onShowIssueMisc() }
                }) { obj -> obj.printStackTrace() })
    }

    override fun onActivityForResult(resultCode: Int, requestCode: Int, intent: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == BundleConstant.REQUEST_CODE) {
            if (intent != null && intent.extras != null) {
                val charSequence = intent.extras!!.getCharSequence(BundleConstant.EXTRA)
                if (!isEmpty(charSequence)) {
                    sendToView { view: CreateIssueMvp.View ->
                        view.onSetCode(
                            charSequence!!
                        )
                    }
                }
            }
        }
    }

    override fun onSubmit(
        title: String, description: CharSequence, login: String,
        repo: String, issueModel: Issue?, pullRequestModel: PullRequest?,
        labels: java.util.ArrayList<LabelModel>?, milestoneModel: MilestoneModel?,
        users: java.util.ArrayList<User>?
    ) {
        val isEmptyTitle = isEmpty(title)
        if (view != null) {
            view!!.onTitleError(isEmptyTitle)
        }
        if (!isEmptyTitle) {
            if (issueModel == null && pullRequestModel == null) {
                val createIssue = CreateIssueModel()
                createIssue.body = toString(description)
                createIssue.title = title
                if (isCollaborator) {
                    if (labels != null && labels.isNotEmpty()) {
                        createIssue.labels = ArrayList(labels.mapNotNull { it.name })
                    }
                    if (users != null && users.isNotEmpty()) {
                        createIssue.assignees = ArrayList(users.map { it.login })
                    }
                    if (milestoneModel != null) {
                        createIssue.milestone = milestoneModel.number.toLong()
                    }
                }
                makeRestCall(
                    getIssueService(isEnterprise).createIssue(login, repo, createIssue),
                    { issue: Issue? ->
                        if (issue != null) {
                            sendToView { view ->
                                view.onSuccessSubmission(
                                    issue
                                )
                            }
                        } else {
                            sendToView { view ->
                                view.showMessage(
                                    R.string.error,
                                    R.string.error_creating_issue
                                )
                            }
                        }
                    }, false
                )
            } else {
                if (issueModel != null) {
                    issueModel.body = toString(description)
                    issueModel.title = title
                    val number = issueModel.number
                    if (isCollaborator) {
                        if (labels != null) {
                            val labelModels = LabelListModel()
                            labelModels.addAll(labels)
                            issueModel.labels = labelModels
                        }
                        if (milestoneModel != null) {
                            issueModel.milestone = milestoneModel
                        }
                        if (users != null) {
                            val usersListModel = UsersListModel()
                            usersListModel.addAll(users)
                            issueModel.assignees = usersListModel
                        }
                    }
                    val requestModel = clone(issueModel, false)
                    makeRestCall(
                        getIssueService(isEnterprise).editIssue(login, repo, number, requestModel),
                        { model: Issue? ->
                            if (model != null) {
                                sendToView { view ->
                                    view.onSuccessSubmission(
                                        model
                                    )
                                }
                            } else {
                                sendToView { view ->
                                    view.showMessage(
                                        R.string.error,
                                        R.string.error_creating_issue
                                    )
                                }
                            }
                        }, false
                    )
                }
                if (pullRequestModel != null) {
                    val number = pullRequestModel.number
                    pullRequestModel.body = toString(description)
                    pullRequestModel.title = title
                    if (isCollaborator) {
                        if (labels != null) {
                            val labelModels = LabelListModel()
                            labelModels.addAll(labels)
                            pullRequestModel.labels = labelModels
                        }
                        if (milestoneModel != null) {
                            pullRequestModel.milestone = milestoneModel
                        }
                        if (users != null) {
                            val usersListModel = UsersListModel()
                            usersListModel.addAll(users)
                            pullRequestModel.assignees = usersListModel
                        }
                    }
                    val requestModel = clone(pullRequestModel, false)
                    makeRestCall(
                        getPullRequestService(isEnterprise).editPullRequest(
                            login,
                            repo,
                            number,
                            requestModel
                        )
                            .flatMap(
                                {
                                    getIssueService(isEnterprise).getIssue(
                                        login,
                                        repo,
                                        number
                                    )
                                }
                            ) { pullRequest1, issueReaction: Issue? ->  //hack to get reactions from issue api
                                if (issueReaction != null) {
                                    pullRequest1.reactions = issueReaction.reactions
                                }
                                pullRequest1
                            }, { pr: PullRequest? ->
                            if (pr != null) {
                                sendToView { view ->
                                    view.onSuccessSubmission(
                                        pr
                                    )
                                }
                            } else {
                                sendToView { view ->
                                    view.showMessage(
                                        R.string.error,
                                        R.string.error_creating_issue
                                    )
                                }
                            }
                        }, false
                    )
                }
            }
        }
    }

    override fun onCheckAppVersion() {
        makeRestCall(
            getRepoService(false).getLatestRelease("k0shk0sh", "FastHub"),
            { release: Release? ->
                if (release != null) {
                    if (!BuildConfig.VERSION_NAME.contains(release.tagName)) {
                        sendToView { it.onShowUpdate() }
                    } else {
                        sendToView { it.hideProgress() }
                    }
                }
            }, false
        )
    }
}