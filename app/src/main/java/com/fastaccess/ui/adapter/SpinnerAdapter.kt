package com.fastaccess.ui.adapter

import android.R
import android.content.Context
import android.widget.BaseAdapter
import android.view.ViewGroup
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView

/**
 * Created by Kosh on 09 Apr 2017, 10:55 AM
 */
class SpinnerAdapter<O>(private val context: Context, private val data: List<O>) : BaseAdapter() {
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): O {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        return getRowView(position, convertView, parent, false)
    }

    override fun getDropDownView(position: Int, convertView: View, parent: ViewGroup): View {
        return getRowView(position, convertView, parent, true)
    }

    private fun getRowView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        isDropDown: Boolean
    ): View {
        var convertView1 = convertView
        val viewHolder: ViewHolder
        if (convertView1 == null) {
            convertView1 = if (!isDropDown) {
                LayoutInflater.from(context).inflate(R.layout.simple_list_item_1, parent, false)
            } else {
                LayoutInflater.from(context)
                    .inflate(R.layout.simple_dropdown_item_1line, parent, false)
            }
            viewHolder = ViewHolder(convertView1)
            convertView1.tag = viewHolder
        } else {
            viewHolder = convertView1.tag as ViewHolder
        }
        viewHolder.title.text = getItem(position).toString()
        return convertView1!!
    }

    internal class ViewHolder(view: View) {
        var title: TextView = view.findViewById(R.id.text1)
    }
}