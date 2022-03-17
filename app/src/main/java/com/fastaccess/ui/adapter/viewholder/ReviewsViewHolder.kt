package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import com.fastaccess.R
import com.fastaccess.data.dao.TimelineModel
import com.fastaccess.helper.ParseDateFormat
import com.fastaccess.provider.timeline.HtmlHelper
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.SpannableBuilder
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder

/**
 * Created by Kosh on 13 Dec 2016, 1:42 AM
 */

class ReviewsViewHolder private constructor(
    itemView: View,
    adapter: BaseRecyclerAdapter<TimelineModel, *, OnItemClickListener<TimelineModel>>,
    val viewGroup: ViewGroup
) : BaseViewHolder<TimelineModel>(itemView, adapter) {
    val stateImage: ForegroundImageView = itemView.findViewById(R.id.stateImage)
    val avatarLayout: AvatarLayout = itemView.findViewById(R.id.avatarLayout)
    val stateText: FontTextView = itemView.findViewById(R.id.stateText)
    val body: FontTextView = itemView.findViewById(R.id.body)

    init {
        itemView.setOnLongClickListener(null)
        itemView.setOnClickListener(null)
    }

    override fun bind(t: TimelineModel) {
        val review = t.review
        review?.let {
            stateImage.setImageResource(R.drawable.ic_eye)
            avatarLayout.setUrl(it.user?.avatarUrl, it.user?.login, false, false)
            stateText.text = SpannableBuilder.builder().bold(
                if (it.user != null) {
                    it.user?.login!!
                } else {
                    ""
                }
            ).append(" ${review.state?.replace("_", " ")} ")
                .append(ParseDateFormat.getTimeAgo(it.submittedAt))
            if (!it.bodyHtml.isNullOrBlank()) {
                HtmlHelper.htmlIntoTextView(body, it.bodyHtml!!, viewGroup.width)
                body.visibility = View.VISIBLE
            } else {
                body.text = ""
                body.visibility = View.GONE
            }
        }
    }

    companion object {
        fun newInstance(
            viewGroup: ViewGroup,
            adapter: BaseRecyclerAdapter<TimelineModel, *, OnItemClickListener<TimelineModel>>
        ): ReviewsViewHolder {
            return ReviewsViewHolder(
                getView(viewGroup, R.layout.review_timeline_row_item),
                adapter,
                viewGroup
            )
        }
    }

}