package com.fastaccess.ui.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import com.fastaccess.data.dao.FragmentPagerAdapterModel

/**
 * Created by Kosh on 03 Dec 2016, 9:25 AM
 */
class FragmentsPagerAdapter(
    fm: FragmentManager,
    fragments: List<FragmentPagerAdapterModel>?
) : FragmentStatePagerAdapter(
    fm
) {
    val fragments: List<FragmentPagerAdapterModel> = fragments
        ?: listOf()

    override fun getItem(position: Int): Fragment {
        return fragments[position].fragment!!
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence {
        return fragments[position].title
    }

//    fun remove(model: FragmentPagerAdapterModel) {
//        fragments.remove(model)
//        notifyDataSetChanged()
//    }
//
//    fun remove(position: Int) {
//        fragments.removeAt(position)
//        notifyDataSetChanged()
//    }
}