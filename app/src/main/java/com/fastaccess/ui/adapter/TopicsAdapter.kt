package com.fastaccess.ui.adapter

import android.content.Intent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import com.fastaccess.App.Companion.getInstance
import com.fastaccess.R
import com.fastaccess.helper.AppHelper.isNightMode
import com.fastaccess.helper.ViewHelper.getCardBackground
import com.fastaccess.ui.adapter.viewholder.SimpleViewHolder
import com.fastaccess.ui.base.adapter.BaseRecyclerAdapter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.adapter.BaseViewHolder.Companion.getView
import com.fastaccess.ui.modules.search.SearchActivity

/**
 * Created by Kosh on 11 May 2017, 6:58 PM
 */
class TopicsAdapter(data: MutableList<String>) :
    BaseRecyclerAdapter<String, SimpleViewHolder<String>, BaseViewHolder.OnItemClickListener<String>>(
        data
    ) {
    private var isLightTheme = true

    @ColorInt
    private var cardBackground = 0
    override fun viewHolder(parent: ViewGroup, viewType: Int): SimpleViewHolder<String> {
        isLightTheme = !isNightMode(parent.resources)
        cardBackground = getCardBackground(parent.context)
        return SimpleViewHolder(getView(parent, R.layout.topics_row_item), null)
    }

    override fun onBindView(holder: SimpleViewHolder<String>, position: Int) {
        if (isLightTheme) {
            holder.itemView.setBackgroundColor(cardBackground)
        }
        val item = getItem(position)
        holder.itemView.setOnClickListener { view: View ->
            val intent =
                Intent(Intent(getInstance().applicationContext, SearchActivity::class.java))
            intent.putExtra("search", "topic:\"$item\"")
            view.context.startActivity(intent)
        }
        holder.bind(getItem(position)!!)
    }
}