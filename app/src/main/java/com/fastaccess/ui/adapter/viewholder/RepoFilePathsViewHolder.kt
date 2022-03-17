package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import com.fastaccess.R
import com.fastaccess.data.dao.model.RepoFile
import com.fastaccess.ui.adapter.RepoFilePathsAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 18 Feb 2017, 2:53 AM
 */
class RepoFilePathsViewHolder private constructor(
    itemView: View,
    baseAdapter: RepoFilePathsAdapter
) : BaseViewHolder<RepoFile>(itemView, baseAdapter) {
    var pathName: FontTextView = itemView.findViewById(R.id.pathName)
    override fun bind(t: RepoFile) {
        pathName.text = t.name
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: RepoFilePathsAdapter
        ): RepoFilePathsViewHolder {
            return RepoFilePathsViewHolder(
                getView(viewGroup!!, R.layout.file_path_row_item),
                adapter
            )
        }
    }

}