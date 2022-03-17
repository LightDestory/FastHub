package com.fastaccess.ui.adapter.viewholder

import android.view.View
import android.view.ViewGroup
import butterknife.BindString
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.model.Release
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.ui.adapter.ReleasesAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class ReleasesViewHolder private constructor(
    itemView: View,
    adapter: ReleasesAdapter
) : BaseViewHolder<Release>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.title)
    var title: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.details)
    var details: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.download)
    var download: ForegroundImageView? = null

    @kotlin.jvm.JvmField
    @BindString(R.string.released)
    var released: String? = null

    @kotlin.jvm.JvmField
    @BindString(R.string.drafted)
    var drafted: String? = null
    override fun bind(t: Release) {
        title!!.text = builder()
            .bold(if (!isEmpty(t.name)) t.name else t.tagName)
        if (t.author != null) {
            details!!.text = builder()
                .append(t.author.login)
                .append(" ")
                .append(if (t.isDraft) drafted else released)
                .append(" ")
                .append(getTimeAgo(t.createdAt))
        } else {
            details!!.visibility = View.GONE
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: ReleasesAdapter
        ): ReleasesViewHolder {
            return ReleasesViewHolder(getView(viewGroup!!, R.layout.releases_row_item), adapter)
        }
    }

    init {
        download!!.setOnClickListener(this)
        download!!.setOnLongClickListener(this)
    }
}