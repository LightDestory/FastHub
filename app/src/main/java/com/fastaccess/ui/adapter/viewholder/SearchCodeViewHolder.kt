package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import com.fastaccess.R
import com.fastaccess.data.dao.SearchCodeModel
import com.fastaccess.ui.adapter.SearchCodeAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class SearchCodeViewHolder private constructor(
    itemView: View,
    adapter: SearchCodeAdapter
) : BaseViewHolder<SearchCodeModel>(itemView, adapter) {
    var title: FontTextView = itemView.findViewById(R.id.title)
    var details: FontTextView = itemView.findViewById(R.id.details)
    var commentsNo: View = itemView.findViewById(R.id.commentsNo)
    fun bind(codeModel: SearchCodeModel, showRepoName: Boolean) {
        if (showRepoName) {
            title.text =
                if (codeModel.repository != null) codeModel.repository!!.fullName else "N/A"
            details.text = codeModel.name
        } else {
            title.text = codeModel.name
            details.text = codeModel.path
        }
        commentsNo.visibility = View.GONE
    }

    override fun bind(t: SearchCodeModel) {}

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: SearchCodeAdapter
        ): SearchCodeViewHolder {
            return SearchCodeViewHolder(
                getView(viewGroup!!, R.layout.issue_no_image_row_item),
                adapter
            )
        }
    }

}