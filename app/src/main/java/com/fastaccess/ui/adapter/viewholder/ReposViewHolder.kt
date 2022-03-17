package com.fastaccess.ui.adapter.viewholder

import android.graphics.Color
import android.text.format.Formatter
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fastaccess.R
import com.fastaccess.data.dao.model.Repo
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.provider.colors.ColorsProvider.getColorAsColor
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.adapter.ReposAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.LabelSpan
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import java.text.NumberFormat

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class ReposViewHolder private constructor(
    itemView: View,
    adapter: ReposAdapter,
    isStarred: Boolean,
    withImage: Boolean
) : BaseViewHolder<Repo>(itemView, adapter) {
    var title: FontTextView
    var date: FontTextView
    var stars: FontTextView
    var forks: FontTextView
    var language: FontTextView
    var size: FontTextView
    var avatarLayout: AvatarLayout?
    var forked: String
    var privateRepo: String
    private var forkColor: Int
    var privateColor: Int
    private val isStarred: Boolean
    private val withImage: Boolean
    override fun bind(t: Repo) {
        if (t.isFork && !isStarred) {
            title.text = builder()
                .append(" $forked ", LabelSpan(forkColor))
                .append(" ")
                .append(t.name, LabelSpan(Color.TRANSPARENT))
        } else if (t.isPrivateX) {
            title.text = builder()
                .append(" $privateRepo ", LabelSpan(privateColor))
                .append(" ")
                .append(t.name, LabelSpan(Color.TRANSPARENT))
        } else {
            title.text = if (!isStarred) t.name else t.fullName
        }
        if (withImage) {
            val avatar = if (t.owner != null) t.owner.avatarUrl else null
            val login = if (t.owner != null) t.owner.login else null
            val isOrg = t.owner != null && t.owner.isOrganizationType
            if (avatarLayout != null) {
                avatarLayout!!.visibility = View.VISIBLE
                avatarLayout!!.setUrl(avatar, login, isOrg, isEnterprise(t.htmlUrl))
            }
        }
        val repoSize = if (t.size > 0) t.size * 1000 else t.size
        size.text = Formatter.formatFileSize(size.context, repoSize)
        val numberFormat = NumberFormat.getNumberInstance()
        stars.text = numberFormat.format(t.stargazersCount)
        forks.text = numberFormat.format(t.forks)
        date.text = getTimeAgo(t.updatedAt)
        if (!isEmpty(t.language)) {
            language.text = t.language
            language.setTextColor(getColorAsColor(t.language, language.context))
            language.visibility = View.VISIBLE
        } else {
            language.setTextColor(Color.BLACK)
            language.visibility = View.GONE
            language.text = ""
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: ReposAdapter,
            isStarred: Boolean,
            withImage: Boolean
        ): ReposViewHolder {
            return if (withImage) {
                ReposViewHolder(
                    getView(viewGroup!!, R.layout.repos_row_item),
                    adapter,
                    isStarred,
                    true
                )
            } else {
                ReposViewHolder(
                    getView(
                        viewGroup!!,
                        R.layout.repos_row_no_image_item
                    ), adapter, isStarred, false
                )
            }
        }
    }

    init {
        val context = itemView.context
        val res = context.resources
        title = itemView.findViewById(R.id.title)
        date = itemView.findViewById(R.id.date)
        stars = itemView.findViewById(R.id.stars)
        forks = itemView.findViewById(R.id.forks)
        language = itemView.findViewById(R.id.language)
        size = itemView.findViewById(R.id.size)
        avatarLayout = itemView.findViewById(R.id.avatarLayout)
        forkColor = ContextCompat.getColor(context, R.color.material_indigo_700)
        privateColor = ContextCompat.getColor(context, R.color.material_grey_700)
        forked = res.getString(R.string.forked)
        privateRepo = res.getString(R.string.private_repo)
        this.isStarred = isStarred
        this.withImage = withImage
    }
}