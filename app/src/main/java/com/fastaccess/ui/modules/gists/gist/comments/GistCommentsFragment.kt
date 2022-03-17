package com.fastaccess.ui.modules.gists.gist.comments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.SparseBooleanArrayParcelable
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.data.dao.model.User
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.BundleConstant.ExtraType
import com.fastaccess.helper.Bundler
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.provider.timeline.CommentsHelper.getUsers
import com.fastaccess.ui.adapter.CommentsAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.editor.EditorActivity
import com.fastaccess.ui.modules.editor.comment.CommentEditorFragment.CommentListener
import com.fastaccess.ui.widgets.StateLayout
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.dialog.MessageDialogView.Companion.newInstance
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.ui.widgets.recyclerview.scroll.RecyclerViewFastScroller

/**
 * Created by Kosh on 11 Nov 2016, 12:36 PM
 */
class GistCommentsFragment : BaseFragment<GistCommentsMvp.View, GistCommentsPresenter>(),
    GistCommentsMvp.View {
    @JvmField
    @BindView(R.id.recycler)
    var recycler: DynamicRecyclerView? = null

    @JvmField
    @BindView(R.id.refresh)
    var refresh: SwipeRefreshLayout? = null

    @JvmField
    @BindView(R.id.stateLayout)
    var stateLayout: StateLayout? = null

    @JvmField
    @BindView(R.id.fastScroller)
    var fastScroller: RecyclerViewFastScroller? = null

    @JvmField
    @State
    var sparseBooleanArray: SparseBooleanArrayParcelable? = null
    private var commentsCallback: CommentListener? = null
    private var gistId: String? = null
    private var adapter: CommentsAdapter? = null
    private var onLoadMore: OnLoadMore<String>? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        commentsCallback = if (parentFragment is CommentListener) {
            parentFragment as CommentListener?
        } else if (context is CommentListener) {
            context
        } else {
            throw IllegalArgumentException(
                String.format(
                    "%s or parent fragment must implement CommentEditorFragment.CommentListener",
                    context.javaClass.simpleName
                )
            )
        }
    }

    override fun onDetach() {
        commentsCallback = null
        super.onDetach()
    }

    override fun fragmentLayout(): Int {
        return R.layout.fab_micro_grid_refresh_list
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        gistId = requireArguments().getString("gistId")
        recycler!!.setEmptyView(stateLayout!!, refresh)
        if (gistId == null) return
        stateLayout!!.setEmptyText(R.string.no_comments)
        recycler!!.setItemViewCacheSize(30)
        refresh!!.setOnRefreshListener(this)
        stateLayout!!.setOnReloadListener(this)
        adapter = CommentsAdapter(presenter!!.comments)
        adapter!!.listener = presenter
        loadMore.initialize(presenter!!.currentPage, presenter!!.previousTotal)
        recycler!!.adapter = adapter
        recycler!!.addKeyLineDivider()
        loadMore.let { recycler!!.addOnScrollListener(it) }
        recycler!!.addNormalSpacingDivider()
        if (presenter!!.comments.isEmpty() && !presenter!!.isApiCalled) {
            sparseBooleanArray = SparseBooleanArrayParcelable()
            onRefresh()
        }
        fastScroller!!.attachRecyclerView(recycler!!)
    }

    override fun onRefresh() {
        presenter!!.onCallApi(1, gistId)
    }

    override fun onNotifyAdapter(items: List<Comment>, page: Int) {
        hideProgress()
        if (items.isEmpty()) {
            adapter!!.clear()
            return
        }
        if (page <= 1) {
            adapter!!.insertItems(items)
        } else {
            adapter!!.addItems(items)
        }
    }

    override fun onRemove(comment: Comment) {
        hideProgress()
        adapter!!.removeItem(comment)
    }

    override fun hideProgress() {
        super.hideProgress()
        refresh!!.isRefreshing = false
        stateLayout!!.hideProgress()
    }

    override fun showProgress(@StringRes resId: Int) {
        refresh!!.isRefreshing = true
        stateLayout!!.showProgress()
    }

    override fun showErrorMessage(msgRes: String) {
        showReload()
        super.showErrorMessage(msgRes)
    }

    override fun showMessage(titleRes: Int, msgRes: Int) {
        showReload()
        super.showMessage(titleRes, msgRes)
    }

    override fun providePresenter(): GistCommentsPresenter {
        return GistCommentsPresenter()
    }

    override val loadMore: OnLoadMore<String>
        get() {
            if (onLoadMore == null) {
                onLoadMore = OnLoadMore(presenter, gistId!!)
            }
            return onLoadMore!!
        }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        val data = it.data
        if (data == null) {
            onRefresh()
            return@registerForActivityResult
        }
        val bundle = data.extras
        if (bundle != null) {
            val isNew = bundle.getBoolean(BundleConstant.EXTRA)
            val commentsModel: Comment = bundle.getParcelable(BundleConstant.ITEM)
                ?: return@registerForActivityResult
            if (isNew) {
                adapter!!.addItem(commentsModel)
                recycler!!.smoothScrollToPosition(adapter!!.itemCount)
            } else {
                val position = adapter!!.getItem(commentsModel)
                if (position != -1) {
                    adapter!!.swapItem(commentsModel, position)
                    recycler!!.smoothScrollToPosition(position)
                } else {
                    adapter!!.addItem(commentsModel)
                    recycler!!.smoothScrollToPosition(adapter!!.itemCount)
                }
            }
        }
    }

    override fun onEditComment(item: Comment) {
        val intent = Intent(context, EditorActivity::class.java)
        intent.putExtras(
            Bundler
                .start()
                .put(BundleConstant.ID, gistId)
                .put(BundleConstant.EXTRA, item.body)
                .put(BundleConstant.EXTRA_FOUR, item.id)
                .put(BundleConstant.EXTRA_TYPE, ExtraType.EDIT_GIST_COMMENT_EXTRA)
                .putStringArrayList("participants", getUsers(adapter!!.data.filterNotNull()))
                .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                .end()
        )
        val view =
            if (activity != null && requireActivity().findViewById<View?>(R.id.fab) != null) requireActivity().findViewById<View>(
                R.id.fab
            ) else recycler!!
        ActivityHelper.startLauncher(launcher, intent, view)
    }

    override fun onShowDeleteMsg(id: Long) {
        newInstance(
            getString(R.string.delete), getString(R.string.confirm_message),
            Bundler.start()
                .put(BundleConstant.EXTRA, id)
                .put(BundleConstant.ID, gistId)
                .put(BundleConstant.YES_NO_EXTRA, true)
                .putStringArrayList("participants", getUsers(adapter!!.data.filterNotNull()))
                .end()
        )
            .show(childFragmentManager, MessageDialogView.TAG)
    }

    override fun onTagUser(user: User) {
        if (commentsCallback != null) {
            commentsCallback!!.onTagUser(user.login)
        }
    }

    override fun onReply(user: User, message: String) {
        onTagUser(user)
    }

    override fun onHandleComment(text: String, bundle: Bundle) {
        presenter!!.onHandleComment(text, bundle, gistId!!)
    }

    override fun onAddNewComment(comment: Comment) {
        hideBlockingProgress()
        adapter!!.addItem(comment)
        if (commentsCallback != null) commentsCallback!!.onClearEditText()
    }

    override val namesToTag: ArrayList<String>
        get() = getUsers(adapter!!.data.filterNotNull())

    override fun hideBlockingProgress() {
        hideProgress()
        super.hideProgress()
    }

    override fun onDestroyView() {
        loadMore.let { recycler!!.removeOnScrollListener(it) }
        super.onDestroyView()
    }

    override fun onClick(view: View) {
        onRefresh()
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {
        super.onMessageDialogActionClicked(isOk, bundle)
        if (isOk) {
            presenter!!.onHandleDeletion(bundle)
        }
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
        if (recycler != null) recycler!!.scrollToPosition(0)
    }

    private fun showReload() {
        hideProgress()
        stateLayout!!.showReload(adapter!!.itemCount)
    }

    companion object {
        fun newInstance(gistId: String): GistCommentsFragment {
            val view = GistCommentsFragment()
            view.arguments = Bundler.start().put("gistId", gistId).end()
            return view
        }
    }
}