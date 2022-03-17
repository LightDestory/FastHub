package com.fastaccess.ui.adapter.viewholder

import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import butterknife.BindColor
import butterknife.BindView
import com.annimon.stream.Stream
import com.fastaccess.R
import com.fastaccess.data.dao.PullRequestStatusModel
import com.fastaccess.data.dao.StatusesModel
import com.fastaccess.data.dao.types.StatusStateType
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder

/**
 * Created by Kosh on 10 Apr 2017, 3:40 AM
 */
class PullStatusViewHolder private constructor(itemView: View) :
    BaseViewHolder<PullRequestStatusModel>(itemView) {
    @kotlin.jvm.JvmField
    @BindView(R.id.stateImage)
    var stateImage: ForegroundImageView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.status)
    var status: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.statuses)
    var statuses: FontTextView? = null

    @kotlin.jvm.JvmField
    @BindColor(R.color.material_green_700)
    var green = 0

    @kotlin.jvm.JvmField
    @BindColor(R.color.material_red_700)
    var red = 0

    @kotlin.jvm.JvmField
    @BindColor(R.color.material_indigo_700)
    var indigo = 0
    override fun bind(t: PullRequestStatusModel) {
        if (t.state != null) {
            val stateType = t.state
            stateImage!!.setImageResource(stateType!!.drawableRes)
            val mergeableState = t.mergeableState
            val isBlocked = "blocked".equals(mergeableState, ignoreCase = true)
            if (stateType === StatusStateType.failure) {
                stateImage!!.tintDrawableColor(red)
                if (t.isMergable) {
                    status!!.setText(R.string.checks_failed)
                } else {
                    status!!.text = builder()
                        .append(status!!.resources.getString(R.string.checks_failed))
                        .append("\n")
                        .append(status!!.resources.getString(R.string.can_not_merge_pr))
                }
            } else if (stateType === StatusStateType.pending) {
                if (t.isMergable) {
                    stateImage!!.setImageResource(R.drawable.ic_check_small)
                    stateImage!!.tintDrawableColor(green)
                    status!!.setText(if (!isBlocked) R.string.commit_can_be_merged else R.string.can_not_merge_pr)
                } else {
                    stateImage!!.setImageResource(stateType.drawableRes)
                    stateImage!!.tintDrawableColor(indigo)
                    status!!.setText(R.string.checks_pending)
                }
            } else {
                stateImage!!.tintDrawableColor(green)
                if (t.isMergable) {
                    status!!.setText(if (!isBlocked) R.string.commit_can_be_merged else R.string.can_not_merge_pr)
                } else {
                    status!!.setText(R.string.checks_passed)
                }
            }
        }
        if (t.statuses != null && t.statuses!!.isNotEmpty()) {
            val builder = builder()
            Stream.of(t.statuses!!)
                .filter { statusesModel: StatusesModel? -> statusesModel?.state != null && statusesModel.targetUrl != null }
                .forEach { statusesModel: StatusesModel ->
                    if (!isEmpty(statusesModel.targetUrl)) {
                        builder.append(
                            ContextCompat.getDrawable(
                                statuses!!.context,
                                statusesModel.state?.drawableRes!!
                            )
                        )
                        builder.append(" ")
                            .append(if (statusesModel.context != null) statusesModel.context + " " else "")
                            .url(statusesModel.description!!) { v: View ->
                                launchUri(
                                    v.context,
                                    statusesModel.targetUrl!!
                                )
                            }
                            .append("\n")
                    }
                }
            if (!isEmpty(builder)) {
                statuses!!.movementMethod = LinkMovementMethod.getInstance()
                statuses!!.text = builder
                statuses!!.visibility = View.VISIBLE
            } else {
                statuses!!.visibility = View.GONE
            }
        } else {
            statuses!!.visibility = View.GONE
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(parent: ViewGroup): PullStatusViewHolder {
            return PullStatusViewHolder(getView(parent, R.layout.pull_status_row_item))
        }
    }

    init {
        itemView.setOnClickListener(null)
        itemView.setOnLongClickListener(null)
    }
}