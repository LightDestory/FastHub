package com.fastaccess.ui.modules.repos.issues.issue.details.timeline

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.ui.adapter.callback.OnToggleView
import com.fastaccess.ui.adapter.callback.ReactionsCallback
import com.fastaccess.data.dao.TimelineModel
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.data.dao.types.ReactionTypes
import android.os.Bundle
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import com.fastaccess.ui.base.mvp.BaseMvp.PaginationListener
import androidx.annotation.IdRes
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.User
import com.fastaccess.provider.timeline.ReactionsProvider.ReactionType
import com.fastaccess.ui.base.adapter.BaseViewHolder
import java.util.ArrayList

/**
 * Created by Kosh on 31 Mar 2017, 7:15 PM
 */
interface IssueTimelineMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener, OnToggleView,
        ReactionsCallback {
        fun onNotifyAdapter(items: List<TimelineModel>?, page: Int)
        val loadMore: OnLoadMore<Issue>
        fun onEditComment(item: Comment)
        fun onRemove(timelineModel: TimelineModel)
        fun onStartNewComment(text: String?)
        fun onShowDeleteMsg(id: Long)
        fun onTagUser(user: User?)
        fun onReply(user: User?, message: String?)
        fun showReactionsPopup(
            type: ReactionTypes,
            login: String,
            repoId: String,
            idOrNumber: Long,
            isHeadre: Boolean
        )

        fun onSetHeader(timelineModel: TimelineModel)
        val issue: Issue?
        fun onUpdateHeader()
        fun onHandleComment(text: String, bundle: Bundle?)
        fun addNewComment(timelineModel: TimelineModel)
        val namesToTag: ArrayList<String>
        fun onHideBlockingProgress()
        val commentId: Long
        fun addComment(timelineModel: TimelineModel?, index: Int)
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<TimelineModel>,
        PaginationListener<Issue> {
        fun isPreviouslyReacted(commentId: Long, vId: Int): Boolean
        val events: ArrayList<TimelineModel>
        fun onWorkOffline()
        fun onHandleDeletion(bundle: Bundle?)
        fun onHandleReaction(@IdRes viewId: Int, id: Long, @ReactionType reactionType: Int)
        fun isCallingApi(id: Long, vId: Int): Boolean
        fun onHandleComment(text: String, bundle: Bundle?)
        fun setCommentId(commentId: Long)
    }
}