package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import butterknife.BindString
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.CommitFileChanges
import com.fastaccess.data.dao.CommitFileChanges.Companion.canAttachToBundle
import com.fastaccess.data.dao.CommitLinesModel
import com.fastaccess.helper.AppHelper.copyToClipboard
import com.fastaccess.ui.adapter.CommitFilesAdapter
import com.fastaccess.ui.adapter.CommitLinesAdapter
import com.fastaccess.ui.adapter.callback.OnToggleView
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.modules.repos.pull_requests.pull_request.details.files.PullRequestFilesMvp.OnPatchClickListener
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import es.dmoral.toasty.Toasty

/**
 * Created by Kosh on 15 Feb 2017, 10:29 PM
 */
class PullRequestFilesViewHolder private constructor(
    itemView: View,
    private val viewGroup: ViewGroup,
    adapter: CommitFilesAdapter,
    private val onToggleView: OnToggleView, private val onPatchClickListener: OnPatchClickListener?
) : BaseViewHolder<CommitFileChanges>(itemView, adapter),
    BaseViewHolder.OnItemClickListener<CommitLinesModel> {
    @kotlin.jvm.JvmField
    @BindView(R.id.name)
    var name: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.patchList)
    var patch: DynamicRecyclerView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.changes)
    var changes: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.addition)
    var addition: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.deletion)
    var deletion: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.status)
    var status: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.toggle)
    var toggle: View? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.open)
    var open: View? = null

    @kotlin.jvm.JvmField
    @BindString(R.string.changes)
    var changesText: String? = null

    @kotlin.jvm.JvmField
    @BindString(R.string.addition)
    var additionText: String? = null

    @kotlin.jvm.JvmField
    @BindString(R.string.deletion)
    var deletionText: String? = null

    @kotlin.jvm.JvmField
    @BindString(R.string.status)
    var statusText: String? = null
    override fun onClick(v: View) {
        if (v.id != R.id.open) {
            val position = absoluteAdapterPosition
            onToggleView.onToggle(position.toLong(), !onToggleView.isCollapsed(position.toLong()))
            onToggle(onToggleView.isCollapsed(position.toLong()), true, position)
        } else {
            super.onClick(v)
        }
    }

    override fun bind(t: CommitFileChanges) {
        val commit = t.commitFileModel
        toggle!!.visibility =
            if (commit!!.patch == null) View.GONE else View.VISIBLE
        name!!.text = commit.filename
        changes!!.text = builder()
            .append(changesText)
            .append("\n")
            .bold(commit.changes.toString())
        addition!!.text = builder()
            .append(additionText)
            .append("\n")
            .bold(commit.additions.toString())
        deletion!!.text = builder()
            .append(deletionText)
            .append("\n")
            .bold(commit.deletions.toString())
        status!!.text = builder()
            .append(statusText)
            .append("\n")
            .bold(commit.status.toString())
        val position = absoluteAdapterPosition
        onToggle(onToggleView.isCollapsed(position.toLong()), false, position)
    }

    private fun onToggle(expanded: Boolean, animate: Boolean, position: Int) {
        if (!expanded) {
            patch!!.swapAdapter(null, true)
            patch!!.visibility = View.GONE
            name!!.maxLines = 2
            toggle!!.rotation = 0.0f
        } else {
            if (adapter != null) {
                val model = adapter.getItem(position)
                if (model!!.linesModel != null && model.linesModel!!.isNotEmpty()) {
                    if (model.linesModel!!.size <= 100) {
                        patch!!.adapter =
                            CommitLinesAdapter(model.linesModel!!.toMutableList(), this)
                        patch!!.visibility = View.VISIBLE
                    } else if (canAttachToBundle(model)) {
                        if (adapter.listener != null) {
                            adapter.listener!!.onItemClick(position, patch, model)
                        }
                    } else {
                        Toasty.warning(
                            itemView.context,
                            itemView.resources.getString(R.string.too_large_changes)
                        ).show()
                        return
                    }
                } else {
                    patch!!.swapAdapter(null, true)
                    patch!!.visibility = View.GONE
                }
            }
            name!!.maxLines = 5
            toggle!!.rotation = 180f
        }
    }

    override fun onItemClick(position: Int, v: View?, item: CommitLinesModel) {
        if (onPatchClickListener != null && adapter != null) {
            val groupPosition = absoluteAdapterPosition
            onPatchClickListener.onPatchClicked(
                groupPosition,
                position,
                v,
                adapter.getItem(groupPosition)!!.commitFileModel!!,
                item
            )
        }
    }

    override fun onItemLongClick(position: Int, v: View?, item: CommitLinesModel) {
        if (adapter == null) return
        val groupPosition = absoluteAdapterPosition
        val lineNo = if (item.leftLineNo > 0) item.leftLineNo else item.rightLineNo
        val url = adapter.getItem(groupPosition)!!.commitFileModel!!.blobUrl + "#L" + lineNo
        copyToClipboard(v!!.context, url)
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: CommitFilesAdapter,
            onToggleView: OnToggleView,
            onPatchClickListener: OnPatchClickListener?
        ): PullRequestFilesViewHolder {
            return PullRequestFilesViewHolder(
                getView(viewGroup, R.layout.pullrequest_file_row_item), viewGroup, adapter,
                onToggleView, onPatchClickListener
            )
        }
    }

    init {
        open!!.setOnClickListener(this)
        patch!!.isNestedScrollingEnabled = false
    }
}