package com.fastaccess.ui.modules.repos.issues.issue.details.timeline

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.StringRes
import butterknife.BindView
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.TimelineModel
import com.fastaccess.data.dao.TimelineModel.Companion.constructComment
import com.fastaccess.data.dao.TimelineModel.Companion.constructHeader
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.User
import com.fastaccess.data.dao.types.ReactionTypes
import com.fastaccess.helper.ActivityHelper.startReveal
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler.Companion.start
import com.fastaccess.helper.Logger.e
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.provider.timeline.CommentsHelper.getUsersByTimeline
import com.fastaccess.provider.timeline.ReactionsProvider
import com.fastaccess.ui.adapter.IssuesTimelineAdapter
import com.fastaccess.ui.adapter.viewholder.TimelineCommentsViewHolder
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.editor.EditorActivity
import com.fastaccess.ui.modules.editor.comment.CommentEditorFragment.CommentListener
import com.fastaccess.ui.modules.repos.issues.issue.details.IssuePagerMvp.IssuePrCallback
import com.fastaccess.ui.modules.repos.reactions.ReactionsDialogFragment
import com.fastaccess.ui.widgets.AppbarRefreshLayout
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 31 Mar 2017, 7:35 PM
 */
class IssueTimelineFragment : BaseFragment<IssueTimelineMvp.View, IssueTimelinePresenter>(),
    IssueTimelineMvp.View {
    @JvmField
    @BindView(R.id.recycler)
    var recycler: DynamicRecyclerView? = null

    @JvmField
    @BindView(R.id.refresh)
    var refresh: AppbarRefreshLayout? = null

    @JvmField
    @BindView(R.id.fastScroller)
    var fastScroller: RecyclerViewFastScroller? = null

    @JvmField
    @BindView(R.id.stateLayout)
    var stateLayout: StateLayout? = null

    @JvmField
    @State
    var toggleMap: HashMap<Long, Boolean> = LinkedHashMap()
    private var adapter: IssuesTimelineAdapter? = null
    private var onLoadMore: OnLoadMore<Issue>? = null
    private var issueCallback: IssuePrCallback<Issue>? = null
    private var commentsCallback: CommentListener? = null

    @Suppress("UNCHECKED_CAST")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        issueCallback = when {
            parentFragment is IssuePrCallback<*> -> {
                parentFragment as IssuePrCallback<Issue>
            }
            context is IssuePrCallback<*> -> {
                context as IssuePrCallback<Issue>
            }
            else -> {
                throw IllegalArgumentException(
                    String.format(
                        "%s or parent fragment must implement IssuePagerMvp.IssuePrCallback",
                        context.javaClass.simpleName
                    )
                )
            }
        }
        commentsCallback = when {
            parentFragment is CommentListener -> {
                parentFragment as CommentListener?
            }
            context is CommentListener -> {
                context
            }
            else -> {
                throw IllegalArgumentException(
                    String.format(
                        "%s or parent fragment must implement CommentEditorFragment.CommentListener",
                        context.javaClass.simpleName
                    )
                )
            }
        }
    }

    override fun onDetach() {
        issueCallback = null
        commentsCallback = null
        super.onDetach()
    }

    override fun onRefresh() {
        presenter!!.onCallApi(1, issue)
    }

    override fun onNotifyAdapter(items: List<TimelineModel>?, page: Int) {
        hideProgress()
        if (items == null) {
            adapter!!.subList(1, adapter!!.itemCount)
            return
        }
        if (page == 1) {
            adapter!!.subList(1, adapter!!.itemCount)
        }
        adapter!!.addItems(items)
        e(adapter!!.itemCount)
    }

    override val loadMore: OnLoadMore<Issue>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter)
            }
            onLoadMore!!.parameter = issue
            return onLoadMore!!
        }

    override fun fragmentLayout(): Int {
        return R.layout.micro_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        if (issue == null) {
            throw NullPointerException("Issue went missing!!!")
        }
        presenter!!.setCommentId(commentId)
        adapter = if (issueCallback != null && issueCallback!!.data != null) {
            IssuesTimelineAdapter(
                presenter!!.events, this, true,
                this, issueCallback!!.data!!.login, issueCallback!!.data!!.user.login
            )
        } else {
            IssuesTimelineAdapter(
                presenter!!.events, this, true,
                this, "", ""
            )
        }
        recycler!!.isVerticalScrollBarEnabled = false
        stateLayout!!.setEmptyText(R.string.no_events)
        recycler!!.setEmptyView(stateLayout!!, refresh)
        refresh!!.setOnRefreshListener(this)
        stateLayout!!.setOnReloadListener(this)
        adapter!!.listener = presenter
        recycler!!.adapter = adapter
        fastScroller!!.visibility = View.VISIBLE
        fastScroller!!.attachRecyclerView(recycler!!)
        recycler!!.addDivider(TimelineCommentsViewHolder::class.java)
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        recycler!!.addOnScrollListener(loadMore)
        if (savedInstanceState == null) {
            onSetHeader(constructHeader(issue))
            onRefresh()
        } else if (presenter!!.events.isEmpty() || presenter!!.events.size == 1) {
            onRefresh()
        }
    }

    override fun providePresenter(): IssueTimelinePresenter {
        return IssueTimelinePresenter()
    }

    override fun showProgress(@StringRes resId: Int) {
        refresh!!.isRefreshing = true
        stateLayout!!.showProgress()
    }

    override fun hideProgress() {
        refresh!!.isRefreshing = false
        stateLayout!!.hideProgress()
    }

    override fun showErrorMessage(msgRes: String) {
        showReload()
        super.showErrorMessage(msgRes)
    }

    override fun showMessage(titleRes: Int, msgRes: Int) {
        showReload()
        super.showMessage(titleRes, msgRes)
    }

    override fun onEditComment(item: Comment) {
        if (issue == null) return
        val intent = Intent(context, EditorActivity::class.java)
        intent.putExtras(
            start()
                .put(BundleConstant.ID, issue!!.repoId)
                .put(BundleConstant.EXTRA_TWO, issue!!.login)
                .put(BundleConstant.EXTRA_THREE, issue!!.number)
                .put(BundleConstant.EXTRA_FOUR, item.id)
                .put(BundleConstant.EXTRA, item.body)
                .put(BundleConstant.EXTRA_TYPE, BundleConstant.ExtraType.EDIT_ISSUE_COMMENT_EXTRA)
                .putStringArrayList(
                    "participants",
                    getUsersByTimeline(adapter!!.data.filterNotNull())
                )
                .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                .end()
        )
        val view =
            if (activity != null && requireActivity().findViewById<View?>(R.id.fab) != null)
                requireActivity().findViewById<View>(
                    R.id.fab
                ) else recycler!!
        startReveal(this, intent, view, BundleConstant.REQUEST_CODE)
    }

    override fun onRemove(timelineModel: TimelineModel) {
        hideProgress()
        adapter!!.removeItem(timelineModel)
    }

    override fun onStartNewComment(text: String?) {
        onTagUser(null)
    }

    override fun onShowDeleteMsg(id: Long) {
        newInstance(
            getString(R.string.delete), getString(R.string.confirm_message),
            start()
                .put(BundleConstant.EXTRA, id)
                .put(BundleConstant.YES_NO_EXTRA, true)
                .end()
        )
            .show(childFragmentManager, MessageDialogView.TAG)
    }

    override fun onTagUser(user: User?) {
        if (commentsCallback != null) if (user != null) {
            commentsCallback!!.onTagUser(user.login)
        }
    }

    override fun onReply(user: User?, message: String?) {
        if (issue == null) return
        val intent = Intent(context, EditorActivity::class.java)
        intent.putExtras(
            start()
                .put(BundleConstant.ID, issue!!.repoId)
                .put(BundleConstant.EXTRA_TWO, issue!!.login)
                .put(BundleConstant.EXTRA_THREE, issue!!.number)
                .put(BundleConstant.EXTRA, "@" + user!!.login)
                .put(BundleConstant.EXTRA_TYPE, BundleConstant.ExtraType.NEW_ISSUE_COMMENT_EXTRA)
                .putStringArrayList("participants", getUsersByTimeline(adapter!!.data.filterNotNull()))
                .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                .put("message", message)
                .end()
        )
        val view =
            if (activity != null && requireActivity().findViewById<View?>(R.id.fab) != null)
                requireActivity().findViewById<View>(
                R.id.fab
            ) else recycler!!
        startReveal(this, intent, view, BundleConstant.REQUEST_CODE)
    }

    override fun showReactionsPopup(
        type: ReactionTypes, login: String,
        repoId: String, idOrNumber: Long, isHeadre: Boolean
    ) {
        ReactionsDialogFragment.newInstance(
            login,
            repoId,
            type,
            idOrNumber,
            if (isHeadre) ReactionsProvider.HEADER else ReactionsProvider.COMMENT
        )
            .show(childFragmentManager, "ReactionsDialogFragment")
    }

    override fun onSetHeader(timelineModel: TimelineModel) {
        if (adapter != null) {
            if (adapter!!.isEmpty) {
                adapter!!.addItem(timelineModel, 0)
            } else {
                adapter!!.swapItem(timelineModel, 0)
            }
        }
    }

    override val issue: Issue?
        get() = issueCallback!!.data

    override fun onUpdateHeader() {
        if (issue == null) return
        onSetHeader(constructHeader(issue))
    }

    override fun onHandleComment(text: String, bundle: Bundle?) {
        presenter!!.onHandleComment(text, bundle)
    }

    override fun addNewComment(timelineModel: TimelineModel) {
        onHideBlockingProgress()
        adapter!!.addItem(timelineModel)
        if (commentsCallback != null) commentsCallback!!.onClearEditText()
    }

    override val namesToTag: ArrayList<String>
        get() = getUsersByTimeline(adapter!!.data.filterNotNull())

    override fun onHideBlockingProgress() {
        hideProgress()
        super.hideProgress()
    }

    override val commentId: Long
        get() = if (arguments != null) requireArguments().getLong(BundleConstant.ID) else 0

    override fun addComment(timelineModel: TimelineModel?, index: Int) {
        if (timelineModel != null) {
            adapter!!.addItem(timelineModel, 1)
            recycler!!.smoothScrollToPosition(1)
        } else if (index != -1) {
            recycler!!.smoothScrollToPosition(index + 1)
            if (index + 1 > adapter!!.itemCount) {
                showMessage(R.string.error, R.string.comment_is_too_far_to_paginate)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == BundleConstant.REQUEST_CODE) {
                if (data == null) {
                    onRefresh()
                    return
                }
                val bundle = data.extras
                if (bundle != null) {
                    val isNew = bundle.getBoolean(BundleConstant.EXTRA)
                    val commentsModel: Comment? = bundle.getParcelable(BundleConstant.ITEM)
                    if (commentsModel == null) {
                        onRefresh() // shit happens, refresh()?
                        return
                    }
                    adapter!!.notifyDataSetChanged()
                    if (isNew) {
                        adapter!!.addItem(constructComment(commentsModel))
                        recycler!!.smoothScrollToPosition(adapter!!.itemCount)
                    } else {
                        val position = adapter!!.getItem(constructComment(commentsModel))
                        if (position != -1) {
                            adapter!!.swapItem(constructComment(commentsModel), position)
                            recycler!!.smoothScrollToPosition(position)
                        } else {
                            adapter!!.addItem(constructComment(commentsModel))
                            recycler!!.smoothScrollToPosition(adapter!!.itemCount)
                        }
                    }
                } else {
                    onRefresh() // bundle size is too large? refresh the api
                }
            }
        }
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (isOk) {
            presenter!!.onHandleDeletion(bundle)
        }
    }

    override fun onClick(view: View) {
        onRefresh()
    }

    override fun onToggle(position: Long, isCollapsed: Boolean) {
        toggleMap[position] = isCollapsed
    }

    override fun isCollapsed(position: Long): Boolean {
        val toggle = toggleMap[position]
        return toggle != null && toggle
    }

    override fun isPreviouslyReacted(id: Long, vId: Int): Boolean {
        return presenter!!.isPreviouslyReacted(id, vId)
    }

    override fun isCallingApi(id: Long, vId: Int): Boolean {
        return presenter!!.isCallingApi(id, vId)
    }

    private fun showReload() {
        hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }

    companion object {
        fun newInstance(commentId: Long): IssueTimelineFragment {
            val fragment = IssueTimelineFragment()
            fragment.arguments =
                start().put(BundleConstant.ID, commentId).end()
            return fragment
        }
    }
}