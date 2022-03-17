package com.fastaccess.ui.modules.repos.pull_requests.pull_request.details.timeline.timeline

import android.net.Uri
import android.os.Bundle
import android.util.SparseArray
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.fastaccess.R
import com.fastaccess.data.dao.*
import com.fastaccess.data.dao.TimelineModel.Companion.constructComment
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.data.dao.types.IssueEventType
import com.fastaccess.data.dao.types.ReactionTypes
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.RestProvider.getIssueService
import com.fastaccess.provider.rest.RestProvider.getPullRequestService
import com.fastaccess.provider.rest.RestProvider.getRepoService
import com.fastaccess.provider.rest.RestProvider.getReviewService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.provider.timeline.CommentsHelper.isOwner
import com.fastaccess.provider.timeline.ReactionsProvider
import com.fastaccess.provider.timeline.ReactionsProvider.ReactionType
import com.fastaccess.provider.timeline.TimelineConverter.convert
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.filter.issues.FilterIssuesActivity
import com.fastaccess.ui.modules.repos.issues.create.CreateIssueActivity
import io.reactivex.Observable

/**
 * Created by Kosh on 31 Mar 2017, 7:17 PM
 */
class PullRequestTimelinePresenter : BasePresenter<PullRequestTimelineMvp.View>(),
    PullRequestTimelineMvp.Presenter {
    private val timeline = ArrayList<TimelineModel>()
    private val pages = SparseArray<String>()
    private var reactionsProvider: ReactionsProvider? = null
        get() {
            if (field == null) {
                field = ReactionsProvider()
            }
            return field
        }
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE

    @JvmField
    @com.evernote.android.state.State
    var isCollaborator = false
    override fun onItemClick(position: Int, v: View?, item: TimelineModel) {
        if (view == null) return
        val pullRequest = view!!.pullRequest
        if (pullRequest != null) {
            if (item.type == TimelineModel.COMMENT) {
                if (v!!.id == R.id.commentMenu) {
                    val popupMenu = PopupMenu(v.context, v)
                    popupMenu.inflate(R.menu.comments_menu)
                    val username = Login.getUser().login
                    val isOwner = (isOwner(username, pullRequest.login, item.comment!!.user.login)
                            || isCollaborator)
                    popupMenu.menu.findItem(R.id.delete).isVisible = isOwner
                    popupMenu.menu.findItem(R.id.edit).isVisible = isOwner
                    popupMenu.setOnMenuItemClickListener { item1: MenuItem ->
                        if (view == null) return@setOnMenuItemClickListener false
                        when (item1.itemId) {
                            R.id.delete -> {
                                view!!.onShowDeleteMsg(item.comment!!.id)
                            }
                            R.id.reply -> {
                                view!!.onReply(item.comment!!.user, item.comment!!.bodyHtml)
                            }
                            R.id.edit -> {
                                view!!.onEditComment(item.comment!!)
                            }
                            R.id.share -> {
                                ActivityHelper.shareUrl(v.context, item.comment!!.htmlUrl)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                } else {
                    onHandleReaction(v.id, item.comment!!.id, ReactionsProvider.COMMENT)
                }
            } else if (item.type == TimelineModel.EVENT) {
                val issueEventModel = item.genericEvent
                if (issueEventModel!!.commitUrl != null) {
                    launchUri(v!!.context, Uri.parse(issueEventModel.commitUrl))
                } else if (issueEventModel.label != null) {
                    FilterIssuesActivity.startActivity(
                        v!!, pullRequest.login, pullRequest.repoId, false,
                        true, isEnterprise, "label:\"" + issueEventModel.label!!.name + "\""
                    )
                } else if (issueEventModel.milestone != null) {
                    FilterIssuesActivity.startActivity(
                        v!!,
                        pullRequest.login,
                        pullRequest.repoId,
                        false,
                        true,
                        isEnterprise,
                        "milestone:\"" + issueEventModel.milestone!!.title + "\""
                    )
                } else if (issueEventModel.assignee != null) {
                    FilterIssuesActivity.startActivity(
                        v!!, pullRequest.login, pullRequest.repoId, false,
                        true, isEnterprise, "assignee:\"" + issueEventModel.assignee!!.login + "\""
                    )
                } else if (issueEventModel.event === IssueEventType.committed) {
                    launchUri(v!!.context, issueEventModel.url!!.replace("git/", ""))
                } else {
                    val sourceModel = issueEventModel.source
                    if (sourceModel != null) {
                        when {
                            sourceModel.commit != null -> {
                                launchUri(v!!.context, sourceModel.commit!!.url)
                            }
                            sourceModel.pullRequest != null -> {
                                launchUri(v!!.context, sourceModel.pullRequest!!.url)
                            }
                            sourceModel.issue != null -> {
                                launchUri(v!!.context, sourceModel.issue!!.htmlUrl)
                            }
                            sourceModel.repository != null -> {
                                launchUri(v!!.context, sourceModel.repository!!.url)
                            }
                        }
                    }
                }
            } else if (item.type == TimelineModel.HEADER) {
                if (v!!.id == R.id.commentMenu) {
                    val popupMenu = PopupMenu(v.context, v)
                    popupMenu.inflate(R.menu.comments_menu)
                    val username = Login.getUser().login
                    val isOwner = isOwner(
                        username, item.pullRequest!!.login,
                        item.pullRequest!!.user.login
                    ) || isCollaborator
                    popupMenu.menu.findItem(R.id.edit).isVisible = isOwner
                    popupMenu.setOnMenuItemClickListener { item1: MenuItem ->
                        if (view == null) return@setOnMenuItemClickListener false
                        when (item1.itemId) {
                            R.id.reply -> {
                                view!!.onReply(item.pullRequest!!.user, item.pullRequest!!.bodyHtml)
                            }
                            R.id.edit -> {
                                val activity = ActivityHelper.getActivity(v.context)
                                    ?: return@setOnMenuItemClickListener false
                                CreateIssueActivity.startForResult(
                                    activity,
                                    item.pullRequest!!.login, item.pullRequest!!.repoId,
                                    item.pullRequest, isEnterprise
                                )
                            }
                            R.id.share -> {
                                ActivityHelper.shareUrl(v.context, item.pullRequest!!.htmlUrl)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                } else {
                    onHandleReaction(
                        v.id,
                        item.pullRequest!!.number.toLong(),
                        ReactionsProvider.HEADER
                    )
                }
            } else if (item.type == TimelineModel.GROUP) {
                val reviewModel = item.groupedReviewModel
                if (v!!.id == R.id.addCommentPreview) {
                    if (view != null) {
                        val model = EditReviewCommentModel()
                        model.commentPosition = -1
                        model.groupPosition = position
                        model.inReplyTo = reviewModel!!.id
                        view!!.onReplyOrCreateReview(null, null, position, -1, model)
                    }
                }
            }
        }
    }

    override fun onItemLongClick(position: Int, v: View?, item: TimelineModel) {
        if (view == null || view!!.pullRequest == null) return
        if (item.type == TimelineModel.COMMENT || item.type == TimelineModel.HEADER) {
            if (v!!.id == R.id.commentMenu && item.type == TimelineModel.COMMENT) {
                val comment = item.comment
                if (view != null) view!!.onReply(comment!!.user, comment.body)
            } else {
                val pullRequest = view!!.pullRequest
                val login = pullRequest!!.login
                val repoId = pullRequest.repoId
                if (!isEmpty(login) && !isEmpty(repoId)) {
                    val type = ReactionTypes[v.id]
                    if (type != null) {
                        if (item.type == TimelineModel.HEADER) {
                            view!!.showReactionsPopup(
                                type,
                                login,
                                repoId,
                                item.pullRequest!!.number.toLong(),
                                ReactionsProvider.HEADER
                            )
                        } else {
                            view!!.showReactionsPopup(
                                type,
                                login,
                                repoId,
                                item.comment!!.id,
                                ReactionsProvider.COMMENT
                            )
                        }
                    } else {
                        onItemClick(position, v, item)
                    }
                }
            }
        } else {
            onItemClick(position, v, item)
        }
    }

    override fun getEvents(): ArrayList<TimelineModel> {
        return timeline
    }

    override fun onWorkOffline() {
        //TODO
    }

    override fun onHandleDeletion(bundle: Bundle?) {
        if (view == null || view!!.pullRequest == null) return
        if (bundle != null) {
            val pullRequest = view!!.pullRequest
            val login = pullRequest!!.login
            val repoId = pullRequest.repoId
            val commId = bundle.getLong(BundleConstant.EXTRA, 0)
            val isReviewComment = bundle.getBoolean(BundleConstant.YES_NO_EXTRA)
            if (commId != 0L && !isReviewComment) {
                makeRestCall(getIssueService(isEnterprise).deleteIssueComment(
                    login,
                    repoId,
                    commId
                )
                ) { booleanResponse ->
                    sendToView { view ->
                        if (booleanResponse.code() == 204) {
                            val comment = Comment()
                            comment.id = commId
                            view.onRemove(constructComment(comment))
                        } else {
                            view.showMessage(
                                R.string.error,
                                R.string.error_deleting_comment
                            )
                        }
                    }
                }
            } else {
                val groupPosition = bundle.getInt(BundleConstant.EXTRA_TWO)
                val commentPosition = bundle.getInt(BundleConstant.EXTRA_THREE)
                makeRestCall(getReviewService(isEnterprise).deleteComment(login, repoId, commId)
                ) { booleanResponse ->
                    sendToView { view ->
                        if (booleanResponse.code() == 204) {
                            view.onRemoveReviewComment(groupPosition, commentPosition)
                        } else {
                            view.showMessage(
                                R.string.error,
                                R.string.error_deleting_comment
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onHandleReaction(vId: Int, idOrNumber: Long, @ReactionType reactionType: Int) {
        if (view == null || view!!.pullRequest == null) return
        val pullRequest = view!!.pullRequest
        val login = pullRequest!!.login
        val repoId = pullRequest.repoId
        val observable = reactionsProvider!!.onHandleReaction(
            vId,
            idOrNumber,
            login,
            repoId,
            reactionType,
            isEnterprise
        )
        observable?.let { manageObservable(it) }
    }

    override fun isMerged(pullRequest: PullRequest): Boolean {
        return pullRequest.isMerged || !isEmpty(pullRequest.mergedAt)
    }

    override fun isCallingApi(id: Long, vId: Int): Boolean {
        return reactionsProvider!!.isCallingApi(id, vId)
    }

    override fun onHandleComment(text: String, bundle: Bundle?) {
        if (view == null) return
        val pullRequest = view!!.pullRequest
        if (pullRequest != null) {
            if (bundle == null) {
                val commentRequestModel = CommentRequestModel()
                commentRequestModel.body = text
                manageDisposable(RxHelper.getObservable(
                    getIssueService(isEnterprise).createIssueComment(
                        pullRequest.login,
                        pullRequest.repoId, pullRequest.number, commentRequestModel
                    )
                )
                    .doOnSubscribe {
                        sendToView { view ->
                            view.showBlockingProgress(
                                0
                            )
                        }
                    }
                    .subscribe(
                        { comment: Comment? ->
                            sendToView { view ->
                                view.addComment(
                                    constructComment(comment)
                                )
                            }
                        }
                    ) { throwable: Throwable? ->
                        onError(throwable!!)
                        sendToView { it.onHideBlockingProgress() }
                    })
            }
        }
    }

    override fun isPreviouslyReacted(commentId: Long, vId: Int): Boolean {
        return reactionsProvider!!.isPreviouslyReacted(commentId, vId)
    }

    override fun onClick(
        groupPosition: Int,
        commentPosition: Int,
        v: View,
        comment: ReviewCommentModel
    ) {
        if (view == null || view!!.pullRequest == null) return
        if (v.id == R.id.commentMenu) {
            val popupMenu = PopupMenu(v.context, v)
            popupMenu.inflate(R.menu.comments_menu)
            val username = Login.getUser().login
            val isOwner = isOwner(
                username, view!!.pullRequest!!
                    .login, comment.user!!.login
            ) || isCollaborator
            popupMenu.menu.findItem(R.id.delete).isVisible = isOwner
            popupMenu.menu.findItem(R.id.edit).isVisible = isOwner
            popupMenu.setOnMenuItemClickListener { item1: MenuItem ->
                if (view == null) return@setOnMenuItemClickListener false
                when (item1.itemId) {
                    R.id.delete -> {
                        view!!.onShowReviewDeleteMsg(comment.id, groupPosition, commentPosition)
                    }
                    R.id.reply -> {
                        val model = EditReviewCommentModel()
                        model.groupPosition = groupPosition
                        model.commentPosition = commentPosition
                        model.inReplyTo = comment.id
                        view!!.onReplyOrCreateReview(
                            comment.user,
                            comment.bodyHtml,
                            groupPosition,
                            commentPosition,
                            model
                        )
                    }
                    R.id.edit -> {
                        view!!.onEditReviewComment(comment, groupPosition, commentPosition)
                    }
                    R.id.share -> {
                        ActivityHelper.shareUrl(v.context, comment.htmlUrl!!)
                    }
                }
                true
            }
            popupMenu.show()
        } else {
            onHandleReaction(v.id, comment.id, ReactionsProvider.REVIEW_COMMENT)
        }
    }

    override fun onLongClick(
        groupPosition: Int,
        commentPosition: Int,
        v: View,
        model: ReviewCommentModel
    ) {
        if (view == null || view!!.pullRequest == null) return
        val pullRequest = view!!.pullRequest
        val login = pullRequest!!.login
        val repoId = pullRequest.repoId
        if (!isEmpty(login) && !isEmpty(repoId)) {
            val type = ReactionTypes[v.id]
            if (type != null) {
                view!!.showReactionsPopup(
                    type,
                    login,
                    repoId,
                    model.id,
                    ReactionsProvider.REVIEW_COMMENT
                )
            } else {
                onClick(groupPosition, commentPosition, v, model)
            }
        }
    }

    override fun onCallApi(page: Int, parameter: PullRequest?): Boolean {
        if (parameter == null) {
            sendToView { it.hideProgress() }
            return false
        }
        val login = parameter.login
        val repoId = parameter.repoId
        val number = parameter.number
        if (page <= 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view -> view.loadMore.reset() }
            pages.clear()
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        if (page == 1) {
            manageObservable(getRepoService(isEnterprise).isCollaborator(
                login, repoId,
                Login.getUser().login
            )
                .doOnNext { booleanResponse ->
                    isCollaborator = booleanResponse.code() == 204
                }
            )
        }
        if (parameter.head != null) {
            val observable: Observable<List<TimelineModel>> =
                Observable.zip(
                    getIssueService(isEnterprise).getTimeline(login, repoId, number, page),
                    getReviewService(isEnterprise).getPrReviewComments(
                        login,
                        repoId,
                        number.toLong()
                    ),
                    getPullRequestService(isEnterprise).getPullStatus(
                        login,
                        repoId,
                        parameter.head.sha
                    )
                    .onErrorReturn {
                        getPullRequestService(isEnterprise).getPullStatus(
                            login, repoId,
                            parameter.base.sha
                        ).blockingFirst(PullRequestStatusModel())
                    }
                ) { response, comments, status ->
                    lastPage = response.last
                    val models = convert(response.items, comments).toMutableList()
                    if (page == 1) {
                        status.isMergable = parameter.isMergeable
                        status.mergeableState = parameter.mergeableState
                        if (status.state != null) {
                            models.add(0, TimelineModel(status))
                        }
                    }
                    return@zip models
                }
            makeRestCall(
                observable
            ) { timeline ->
                sendToView { view ->
                    view.onNotifyAdapter(
                        timeline,
                        page
                    )
                }
            }
            return true
        }
        return false
    }
}