package com.fastaccess.ui.adapter.viewholder

import android.text.format.Formatter
import android.view.View
import android.view.ViewGroup
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.FilesListModel
import com.fastaccess.ui.adapter.GistFilesAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder

/**
 * Created by Kosh on 12 Nov 2016, 3:44 PM
 */
class GistFilesViewHolder private constructor(
    itemView: View,
    adapter: GistFilesAdapter,
    private val isOwner: Boolean
) : BaseViewHolder<FilesListModel>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.fileName)
    var fileName: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.language)
    var language: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.size)
    var size: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.delete)
    var delete: ForegroundImageView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.edit)
    var edit: ForegroundImageView? = null
    override fun bind(t: FilesListModel) {
        fileName!!.text = t.filename
        language!!.text = builder().bold(t.type!!)
        size!!.text =
            Formatter.formatFileSize(size!!.context, t.size!!)
        delete!!.visibility = if (isOwner) View.VISIBLE else View.GONE
        edit!!.visibility = if (isOwner) View.VISIBLE else View.GONE
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            parent: ViewGroup,
            adapter: GistFilesAdapter,
            isOwner: Boolean
        ): GistFilesViewHolder {
            return GistFilesViewHolder(
                getView(parent, R.layout.gist_files_row_item),
                adapter,
                isOwner
            )
        }
    }

    init {
        if (isOwner) {
            delete!!.setOnClickListener(this)
            edit!!.setOnClickListener(this)
        }
    }
}