package com.fastaccess.ui.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.fastaccess.R
import com.fastaccess.data.dao.TimelineModel
import com.fastaccess.ui.adapter.callback.OnToggleView
import com.fastaccess.ui.adapter.callback.ReactionsCallback
import com.fastaccess.ui.adapter.viewholder.CommitThreadViewHolder.Companion.newInstance
import com.fastaccess.ui.adapter.viewholder.GroupedReviewsViewHolder.Companion.newInstance
import com.fastaccess.ui.adapter.viewholder.IssueDetailsViewHolder.Companion.newInstance
import com.fastaccess.ui.adapter.viewholder.IssueTimelineViewHolder.Companion.newInstance
import com.fastaccess.ui.adapter.viewholder.PullStatusViewHolder
import com.fastaccess.ui.adapter.viewholder.PullStatusViewHolder.Companion.newInstance
import com.fastaccess.ui.adapter.viewholder.ReviewsViewHolder.Companion.newInstance
import com.fastaccess.ui.adapter.viewholder.TimelineCommentsViewHolder.Companion.newInstance
import com.fastaccess.ui.adapter.viewholder.UnknownTypeViewHolder
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.adapter.BaseViewHolder.Companion.getView
import com.fastaccess.ui.modules.repos.pull_requests.pull_request.details.timeline.timeline.PullRequestTimelineMvp.ReviewCommentCallback

/**
 * Created by Kosh on 13 Dec 2016, 1:44 AM
 */
class IssuesTimelineAdapter(
    data: MutableList<TimelineModel>,
    private val onToggleView: OnToggleView?,
    private val showEmojies: Boolean,
    private val reactionsCallback: ReactionsCallback?,
    private val isMerged: Boolean,
    private val reviewCommentCallback: ReviewCommentCallback?,
    private val repoOwner: String?,
    private val poster: String?
) : BaseRecyclerAdapter<TimelineModel, BaseViewHolder<TimelineModel>, BaseViewHolder.OnItemClickListener<TimelineModel>>(
    data
) {
    constructor(
        data: MutableList<TimelineModel>,
        onToggleView: OnToggleView?,
        showEmojies: Boolean,
        reactionsCallback: ReactionsCallback?,
        repoOwner: String?,
        poster: String?
    ) : this(
        data,
        onToggleView,
        showEmojies,
        reactionsCallback,
        false,
        null,
        repoOwner,
        poster
    )

    override fun viewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<TimelineModel> {
        when (viewType) {
            0 -> {
                return UnknownTypeViewHolder(
                    getView(
                        parent,
                        R.layout.unknown_row_item
                    )
                ) as BaseViewHolder<TimelineModel>
            }
            TimelineModel.HEADER -> {
                return newInstance(
                    parent,
                    this,
                    onToggleView!!,
                    reactionsCallback!!,
                    repoOwner!!,
                    poster!!
                )
            }
            TimelineModel.EVENT -> {
                return newInstance(parent, this, isMerged)
            }
            TimelineModel.REVIEW -> {
                return newInstance(parent, this)
            }
            TimelineModel.GROUP -> {
                return newInstance(
                    parent, this, onToggleView!!, reactionsCallback!!,
                    reviewCommentCallback!!, repoOwner!!, poster!!
                )
            }
            TimelineModel.COMMIT_COMMENTS -> {
                return newInstance(parent, this, onToggleView!!)
            }
            TimelineModel.STATUS -> {
                return newInstance(parent) as BaseViewHolder<TimelineModel>
            }
            else -> return newInstance(
                parent, this, onToggleView!!, showEmojies,
                reactionsCallback!!, repoOwner!!, poster!!
            )
        }
    }

    override fun onBindView(holder: BaseViewHolder<TimelineModel>, position: Int) {
        val model = getItem(position)
        if (model!!.type == TimelineModel.HEADER) {
            holder.bind(model)
        } else if (model.type == TimelineModel.EVENT) {
            holder.bind(model)
        } else if (model.type == TimelineModel.COMMENT) {
            holder.bind(model)
        } else if (model.type == TimelineModel.GROUP) {
            holder.bind(model)
        } else if (model.type == TimelineModel.REVIEW) {
            holder.bind(model)
        } else if (model.type == TimelineModel.COMMIT_COMMENTS) {
            holder.bind(model)
        } else if (model.type == TimelineModel.STATUS && model.status != null) {
            (holder as PullStatusViewHolder).bind(model.status!!)
        }
        if (model.type != TimelineModel.COMMENT) {
            val layoutParams =
                holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
            layoutParams.isFullSpan = true
        }
    }

    //    @Override
    //    protected void onBindView(BaseViewHolder<PullRequestStatusModel> holder, int position) {
    //        TimelineModel model = getItem(position);
    //        if (model.getType() == TimelineModel.STATUS && model.getStatus() != null) {
    //            holder.bind(model.getStatus());
    //        }
    //        if (model.getType() != TimelineModel.COMMENT) {
    //            StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
    //            layoutParams.setFullSpan(true);
    //        }
    //    }
    override fun getItemViewType(position: Int): Int {
        val timelineModel = data[position]
        return timelineModel?.type ?: super.getItemViewType(position)
    }
}