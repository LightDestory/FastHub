package com.fastaccess.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.fastaccess.R
import com.fastaccess.data.dao.SettingsModel
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.ForegroundImageView

/**
 * Created by JediB on 5/12/2017.
 */
class SettingsAdapter(context: Context, private val settings: ArrayList<SettingsModel>) :
    BaseAdapter() {
    override fun getCount(): Int {
        return settings.size
    }

    override fun getItem(position: Int): SettingsModel {
        return settings[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val viewHolder: ViewHolder = convertView.tag as ViewHolder
        val model = getItem(position)
        viewHolder.title.text = model.title
        viewHolder.image.setImageResource(model.image)
        viewHolder.summary.visibility = View.GONE
        return convertView
    }

    internal class ViewHolder(view: View) {
        var image: ForegroundImageView = view.findViewById(R.id.iconItemImage)
        var title: FontTextView = view.findViewById(R.id.iconItemTitle)
        var summary: FontTextView = view.findViewById(R.id.iconItemSummary)
    }
}