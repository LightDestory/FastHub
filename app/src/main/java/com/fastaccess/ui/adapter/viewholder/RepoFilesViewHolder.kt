package com.fastaccess.ui.adapter.viewholder

import android.text.format.Formatter
import android.view.View
import android.view.ViewGroup
import com.fastaccess.R
import com.fastaccess.data.dao.model.RepoFile
import com.fastaccess.data.dao.types.FilesType
import com.fastaccess.ui.adapter.RepoFilesAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView

/**
 * Created by Kosh on 15 Feb 2017, 10:29 PM
 */
class RepoFilesViewHolder private constructor(
    itemView: View,
    adapter: RepoFilesAdapter
) : BaseViewHolder<RepoFile>(itemView, adapter) {
    private var contentTypeImage: ForegroundImageView = itemView.findViewById(R.id.contentTypeImage)
    var title: FontTextView = itemView.findViewById(R.id.title)
    var size: FontTextView = itemView.findViewById(R.id.size)
    var menu: ForegroundImageView = itemView.findViewById(R.id.menu)
    var file: String = itemView.context.resources.getString(R.string.file)
    override fun onClick(v: View) {
        if (v.id == R.id.contentTypeImage) {
            itemView.callOnClick()
        } else {
            super.onClick(v)
        }
    }

    override fun bind(t: RepoFile) {
        contentTypeImage.contentDescription = String.format("%s %s", t.name, file)
        title.text = t.name
        if (t.type != null && t.type.icon != 0) {
            contentTypeImage.setImageResource(t.type.icon)
            if (t.type === FilesType.file) {
                size.text = Formatter.formatFileSize(size.context, t.size)
                size.visibility = View.VISIBLE
            } else {
                size.visibility = View.GONE
            }
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: RepoFilesAdapter
        ): RepoFilesViewHolder {
            return RepoFilesViewHolder(getView(viewGroup!!, R.layout.repo_files_row_item), adapter)
        }
    }

    init {
        menu.setOnClickListener(this)
        contentTypeImage.setOnClickListener(this)
    }
}