package com.fastaccess.ui.adapter.viewholder

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.fastaccess.R
import com.fastaccess.data.dao.model.PinnedRepos
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.ParseDateFormat.Companion.getTimeAgo
import com.fastaccess.provider.colors.ColorsProvider.getColorAsColor
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.adapter.PinnedReposAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.AvatarLayout
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.LabelSpan
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import java.text.NumberFormat

/**
 * Created by Kosh on 11 Nov 2016, 2:08 PM
 */
class PinnedReposViewHolder private constructor(
    itemView: View,
    adapter: PinnedReposAdapter
) : BaseViewHolder<PinnedRepos>(itemView, adapter) {
    var title: FontTextView = itemView.findViewById(R.id.title)
    var avatarLayout: AvatarLayout? = itemView.findViewById(R.id.avatarLayout)
    var date: FontTextView? = itemView.findViewById(R.id.date)
    var stars: FontTextView? = itemView.findViewById(R.id.stars)
    var forks: FontTextView? = itemView.findViewById(R.id.forks)
    var language: FontTextView? = itemView.findViewById(R.id.language)
    var forked: String
    var privateRepo: String
    var forkColor: Int
    var privateColor: Int
    override fun bind(t: PinnedRepos) {
        val repo = t.pinnedRepo ?: return
        when {
            repo.isFork -> {
                title.text = builder()
                    .append(" $forked ", LabelSpan(forkColor))
                    .append(" ")
                    .append(repo.name, LabelSpan(Color.TRANSPARENT))
            }
            repo.isPrivateX -> {
                title.text = builder()
                    .append(" $privateRepo ", LabelSpan(privateColor))
                    .append(" ")
                    .append(repo.name, LabelSpan(Color.TRANSPARENT))
            }
            else -> {
                title.text = repo.fullName
            }
        }
        val avatar = if (repo.owner != null) repo.owner.avatarUrl else null
        val login = if (repo.owner != null) repo.owner.login else null
        val isOrg = repo.owner != null && repo.owner.isOrganizationType
        if (avatarLayout != null) {
            avatarLayout!!.visibility = View.VISIBLE
            avatarLayout!!.setUrl(avatar, login, isOrg, isEnterprise(repo.htmlUrl))
        }
        if (stars != null && forks != null && date != null && language != null) {
            val numberFormat = NumberFormat.getNumberInstance()
            stars!!.text = numberFormat.format(repo.stargazersCount)
            forks!!.text = numberFormat.format(repo.forks)
            date!!.text = getTimeAgo(repo.updatedAt)
            if (!isEmpty(repo.language)) {
                language!!.text = repo.language
                language!!.setTextColor(getColorAsColor(repo.language, language!!.context))
                language!!.visibility = View.VISIBLE
            }
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            viewGroup: ViewGroup?,
            adapter: PinnedReposAdapter,
            singleLine: Boolean
        ): PinnedReposViewHolder {
            return PinnedReposViewHolder(
                getView(
                    viewGroup!!,
                    if (singleLine) R.layout.repos_row_item_menu else R.layout.repos_row_item
                ), adapter
            )
        }
    }

    init {
        val `$$context` = itemView.context
        val `$$res` = `$$context`.resources
        forked = `$$res`.getString(R.string.forked)
        privateRepo = `$$res`.getString(R.string.private_repo)
        forkColor = ContextCompat.getColor(`$$context`, R.color.material_indigo_700)
        privateColor = ContextCompat.getColor(`$$context`, R.color.material_grey_700)
    }
}