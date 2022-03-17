package com.fastaccess.ui.modules.repos.issues.issue.details.timeline

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import com.fastaccess.R
import com.fastaccess.data.dao.CommentRequestModel
import com.fastaccess.data.dao.TimelineModel
import com.fastaccess.data.dao.TimelineModel.Companion.constructComment
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.types.ReactionTypes
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.RestProvider.getIssueService
import com.fastaccess.provider.rest.RestProvider.getRepoService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.provider.timeline.CommentsHelper.isOwner
import com.fastaccess.provider.timeline.ReactionsProvider
import com.fastaccess.provider.timeline.ReactionsProvider.ReactionType
import com.fastaccess.provider.timeline.TimelineConverter.convert
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.filter.issues.FilterIssuesActivity
import com.fastaccess.ui.modules.repos.issues.create.CreateIssueActivity
import io.reactivex.Observable
import io.reactivex.ObservableEmitter

/**
 * Created by Kosh on 31 Mar 2017, 7:17 PM
 */
class IssueTimelinePresenter : BasePresenter<IssueTimelineMvp.View>(), IssueTimelineMvp.Presenter {
    val timeline = ArrayList<TimelineModel>()
    private var reactionsProvider: ReactionsProvider? = null
        get() {
            if (field == null) {
                field = ReactionsProvider()
            }
            return field
        }
    override var currentPage = 0

    override var previousTotal = 0
    var lastPage = Int.MAX_VALUE
        private set

    @com.evernote.android.state.State
    var isCollaborator = false
    private var commentId: Long = 0
    fun getCommentId(): Long {
        return commentId
    }

    override fun isPreviouslyReacted(commentId: Long, vId: Int): Boolean {
        return reactionsProvider!!.isPreviouslyReacted(commentId, vId)
    }

    override fun onItemClick(position: Int, v: View?, item: TimelineModel) {
        if (view != null) {
            val issue = view!!.issue ?: return
            if (item.type == TimelineModel.COMMENT) {
                if (v!!.id == R.id.commentMenu) {
                    val popupMenu = PopupMenu(v.context, v)
                    popupMenu.inflate(R.menu.comments_menu)
                    val username = Login.getUser().login
                    val isOwner =
                        isOwner(username, issue.login, item.comment!!.user.login) || isCollaborator
                    popupMenu.menu.findItem(R.id.delete).isVisible = isOwner
                    popupMenu.menu.findItem(R.id.edit).isVisible = isOwner
                    popupMenu.setOnMenuItemClickListener { item1: MenuItem ->
                        if (view == null) return@setOnMenuItemClickListener false
                        when (item1.itemId) {
                            R.id.delete -> {
                                view!!.onShowDeleteMsg(item.comment!!.id)
                            }
                            R.id.reply -> {
                                view!!.onReply(item.comment!!.user, item.comment!!.body)
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
                        v!!, issue.login, issue.repoId, true,
                        true, isEnterprise, "label:\"" + issueEventModel.label!!.name + "\""
                    )
                } else if (issueEventModel.milestone != null) {
                    FilterIssuesActivity.startActivity(
                        v!!,
                        issue.login,
                        issue.repoId,
                        true,
                        true,
                        isEnterprise,
                        "milestone:\"" + issueEventModel.milestone!!.title + "\""
                    )
                } else if (issueEventModel.assignee != null) {
                    FilterIssuesActivity.startActivity(
                        v!!, issue.login, issue.repoId, true,
                        true, isEnterprise, "assignee:\"" + issueEventModel.assignee!!.login + "\""
                    )
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
                        username, item.issue!!.login,
                        item.issue!!.user.login
                    ) || isCollaborator
                    popupMenu.menu.findItem(R.id.edit).isVisible = isOwner
                    popupMenu.setOnMenuItemClickListener { item1: MenuItem ->
                        if (view == null) return@setOnMenuItemClickListener false
                        when (item1.itemId) {
                            R.id.reply -> {
                                view!!.onReply(item.issue!!.user, item.issue!!.body)
                            }
                            R.id.edit -> {
                                val activity = ActivityHelper.getActivity(v.context)
                                    ?: return@setOnMenuItemClickListener false
                                CreateIssueActivity.startForResult(
                                    activity,
                                    item.issue!!.login, item.issue!!.repoId, item.issue, isEnterprise
                                )
                            }
                            R.id.share -> {
                                ActivityHelper.shareUrl(v.context, item.issue!!.htmlUrl)
                            }
                        }
                        true
                    }
                    popupMenu.show()
                } else {
                    onHandleReaction(v.id, item.issue!!.number.toLong(), ReactionsProvider.HEADER)
                }
            }
        }
    }

    override fun onItemLongClick(position: Int, v: View?, item: TimelineModel) {
        if (view == null) return
        if (item.type == TimelineModel.COMMENT || item.type == TimelineModel.HEADER) {
            if (v!!.id == R.id.commentMenu && item.type == TimelineModel.COMMENT) {
                val comment = item.comment
                if (view != null) view!!.onReply(comment!!.user, comment.body)
            } else {
                if (view!!.issue == null) return
                val issue = view!!.issue
                val login = issue!!.login
                val repoId = issue.repoId
                if (!isEmpty(login) && !isEmpty(repoId)) {
                    val type = ReactionTypes[v.id]
                    if (type != null) {
                        if (item.type == TimelineModel.HEADER) {
                            view!!.showReactionsPopup(
                                type,
                                login,
                                repoId,
                                item.issue!!.number.toLong(),
                                true
                            )
                        } else {
                            view!!.showReactionsPopup(type, login, repoId, item.comment!!.id, false)
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
        if (bundle != null) {
            val commId = bundle.getLong(BundleConstant.EXTRA, 0)
            if (commId != 0L) {
                if (view == null || view!!.issue == null) return
                val issue = view!!.issue
                makeRestCall(getIssueService(isEnterprise).deleteIssueComment(
                    issue!!.login, issue.repoId, commId
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
            }
        }
    }

    override fun onHandleReaction(viewId: Int, id: Long, @ReactionType reactionType: Int) {
        if (view == null || view!!.issue == null) return
        val issue = view!!.issue
        val login = issue!!.login
        val repoId = issue.repoId
        val observable = reactionsProvider!!.onHandleReaction(
            viewId,
            id,
            login,
            repoId,
            reactionType,
            isEnterprise
        )
        observable?.let { manageObservable(it) }
    }

    override fun isCallingApi(id: Long, vId: Int): Boolean {
        return reactionsProvider!!.isCallingApi(id, vId)
    }

    override fun onHandleComment(text: String, bundle: Bundle?) {
        if (view == null) return
        val issue = view!!.issue
        if (issue != null) {
            if (bundle == null) {
                val commentRequestModel = CommentRequestModel()
                commentRequestModel.body = text
                manageDisposable(RxHelper.getObservable(
                    getIssueService(isEnterprise).createIssueComment(
                        issue.login, issue
                            .repoId,
                        issue.number, commentRequestModel
                    )
                )
                    .doOnSubscribe {
                        sendToView { view: IssueTimelineMvp.View ->
                            view.showBlockingProgress(
                                0
                            )
                        }
                    }
                    .subscribe(
                        { comment: Comment? ->
                            sendToView { view: IssueTimelineMvp.View ->
                                view.addNewComment(
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

    override fun setCommentId(commentId: Long) {
        this.commentId = commentId
    }

    override fun onCallApi(page: Int, parameter: Issue?): Boolean {
        if (parameter == null) {
            sendToView { it.hideProgress() }
            return false
        }
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view: IssueTimelineMvp.View -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        val login = parameter.login
        val repoId = parameter.repoId
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
        val number = parameter.number
        val observable = getIssueService(isEnterprise)
            .getTimeline(login, repoId, number, page)
            .flatMap { response ->
                lastPage = response.last
                convert(response.items)
            }
            .toList()
            .toObservable()
        makeRestCall(observable) { timeline: List<TimelineModel>? ->
            sendToView { view: IssueTimelineMvp.View ->
                view.onNotifyAdapter(
                    timeline,
                    page
                )
            }
            loadComment(page, commentId, login, repoId, timeline)
        }
        return true
    }

    private fun loadComment(
        page: Int,
        commentId: Long,
        login: String,
        repoId: String,
        timeline: List<TimelineModel>?
    ) {
        if (page == 1 && commentId > 0) {
            val observable = Observable.create { source: ObservableEmitter<TimelineModel> ->
                var index = -1
                if (timeline != null) {
                    for (i in timeline.indices) {
                        val timelineModel = timeline[i]
                        if (timelineModel.comment != null) {
                            if (timelineModel.comment!!.id == commentId) {
                                index = i
                                break
                            }
                        }
                    }
                }
                val timelineModel = TimelineModel()
                timelineModel.position = index
                source.onNext(timelineModel)
                source.onComplete()
            }
            manageObservable(observable.doOnNext { timelineModel: TimelineModel ->
                sendToView { view: IssueTimelineMvp.View ->
                    if (timelineModel.comment != null) {
                        view.addComment(timelineModel, -1)
                    } else {
                        view.addComment(null, timelineModel.position)
                    }
                }
            })
        }
    }
}