package com.fastaccess.ui.adapter.viewholder

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.LabelModel
import com.fastaccess.helper.ViewHelper.generateTextColor
import com.fastaccess.helper.ViewHelper.getPrimaryTextColor
import com.fastaccess.ui.adapter.LabelsAdapter
import com.fastaccess.ui.adapter.LabelsAdapter.OnSelectLabel
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.widgets.FontTextView

/**
 * Created by Kosh on 22 Feb 2017, 7:36 PM
 */
class LabelsViewHolder private constructor(
    itemView: View,
    private val onSelectLabel: OnSelectLabel?,
    adapter: LabelsAdapter
) : BaseViewHolder<LabelModel>(itemView, adapter) {
    @kotlin.jvm.JvmField
    @BindView(R.id.colorImage)
    var colorImage: AppCompatImageView? = null

    @kotlin.jvm.JvmField
    @BindView(R.id.name)
    var name: FontTextView? = null
    override fun onClick(v: View) {
        if (onSelectLabel != null) {
            val position = adapterPosition
            if (adapter != null) {
                val labelModel = adapter.getItem(position)
                onSelectLabel.onToggleSelection(
                    labelModel,
                    !onSelectLabel.isLabelSelected(labelModel)
                )
            }
        } else {
            super.onClick(v)
        }
    }

    override fun bind(t: LabelModel) {
        name!!.text = t.name
        if (t.color != null) {
            val color =
                Color.parseColor(if (t.color!!.startsWith("#")) t.color else "#" + t.color)
            colorImage!!.setBackgroundColor(color)
            if (onSelectLabel != null) {
                if (onSelectLabel.isLabelSelected(t)) {
                    name!!.setTextColor(generateTextColor(color))
                } else {
                    name!!.setTextColor(getPrimaryTextColor(itemView.context))
                }
                itemView.setBackgroundColor(if (onSelectLabel.isLabelSelected(t)) color else 0)
            }
        } else {
            colorImage!!.setBackgroundColor(0)
        }
    }

    companion object {
        @kotlin.jvm.JvmStatic
        fun newInstance(
            parent: ViewGroup,
            onSelectLabel: OnSelectLabel?,
            adapter: LabelsAdapter
        ): LabelsViewHolder {
            return LabelsViewHolder(
                getView(parent, R.layout.label_row_item),
                onSelectLabel,
                adapter
            )
        }
    }
}