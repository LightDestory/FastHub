package com.fastaccess.ui.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.fastaccess.R
import com.fastaccess.data.dao.BranchesModel
import com.fastaccess.ui.adapter.BranchesAdapter.BranchesViewHolder
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.adapter.BaseViewHolder.Companion.getView

/**
 * Created by Kosh on 09 Apr 2017, 10:55 AM
 */
class BranchesAdapter(
    data: MutableList<BranchesModel>,
    listener: BaseViewHolder.OnItemClickListener<BranchesModel>?
) : BaseRecyclerAdapter<BranchesModel, BranchesViewHolder, BaseViewHolder.OnItemClickListener<BranchesModel>>(
    data,
    listener
) {
    override fun viewHolder(parent: ViewGroup, viewType: Int): BranchesViewHolder {
        return BranchesViewHolder(getView(parent, R.layout.branches_row_item), this)
    }

    override fun onBindView(holder: BranchesViewHolder, position: Int) {
        holder.bind(getItem(position)!!)
    }

    class BranchesViewHolder(itemView: View, adapter: BranchesAdapter) :
        BaseViewHolder<BranchesModel>(itemView, adapter) {
        var image: AppCompatImageView? = itemView.findViewById(R.id.image)
        var title: TextView = itemView.findViewById(android.R.id.text1)
        override fun bind(t: BranchesModel) {
            if (image != null) {
                image!!.setImageResource(if (t.isTag) R.drawable.ic_label else R.drawable.ic_branch)
                image!!.contentDescription = t.name
            }
            title.text = t.name
        }

    }
}