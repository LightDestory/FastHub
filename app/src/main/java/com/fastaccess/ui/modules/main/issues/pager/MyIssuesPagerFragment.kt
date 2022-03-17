package com.fastaccess.ui.modules.main.issues.pager

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import android.widget.TextView
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener
import butterknife.BindView
import com.annimon.stream.Stream
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.FragmentPagerAdapterModel.Companion.buildForMyIssues
import com.fastaccess.data.dao.TabsCountStateModel
import com.fastaccess.data.dao.types.IssueState
import com.fastaccess.helper.ViewHelper
import com.fastaccess.ui.adapter.FragmentsPagerAdapter
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.modules.main.issues.MyIssuesFragment
import com.fastaccess.ui.widgets.SpannableBuilder.Companion.builder
import com.fastaccess.ui.widgets.ViewPagerView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import kotlin.Boolean
import kotlin.Int

/**
 * Created by Kosh on 26 Mar 2017, 12:14 AM
 */
class MyIssuesPagerFragment : BaseFragment<MyIssuesPagerMvp.View, MyIssuesPagerPresenter>(),
    MyIssuesPagerMvp.View {
    @JvmField
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null

    @JvmField
    @BindView(R.id.pager)
    var pager: ViewPagerView? = null

    @JvmField
    @State
    var counts = HashSet<TabsCountStateModel>()
    override fun fragmentLayout(): Int {
        return R.layout.tabbed_viewpager
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = FragmentsPagerAdapter(
            childFragmentManager,
            buildForMyIssues(requireContext())
        )
        pager!!.adapter = adapter
        tabs!!.setTabsFromPagerAdapter(adapter)
        tabs!!.tabGravity = TabLayout.GRAVITY_FILL
        tabs!!.tabMode = TabLayout.MODE_SCROLLABLE
        if (savedInstanceState == null) {
            tabs!!.getTabAt(0)!!.select()
        }
        pager!!.addOnPageChangeListener(object : SimpleOnPageChangeListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                selectTab(position, true)
            }
        })
        tabs!!.addOnTabSelectedListener(object : OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.tag == null) {
                    val position = tab.position
                    selectTab(position, false)
                }
                tab.tag = null
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                selectTab(tab.position, false)
            }
        })
        if (savedInstanceState != null && counts.isNotEmpty()) {
            Stream.of(counts).forEach { model: TabsCountStateModel -> updateCount(model) }
        }
    }

    override fun providePresenter(): MyIssuesPagerPresenter {
        return MyIssuesPagerPresenter()
    }

    override fun onSetBadge(tabIndex: Int, count: Int) {
        var model = getModelAtIndex(tabIndex)
        if (model == null) {
            model = TabsCountStateModel()
        }
        model.tabIndex = tabIndex
        model.count = count
        counts.remove(model)
        counts.add(model)
        if (tabs != null) {
            updateCount(model)
        }
    }

    private fun getModelAtIndex(index: Int): TabsCountStateModel? {
        return Stream.of(counts)
            .filter { model: TabsCountStateModel -> model.tabIndex == index }
            .findFirst()
            .orElse(null)
    }

    override fun onScrollTop(index: Int) {
        super.onScrollTop(index)
        if (pager != null && pager!!.adapter != null) {
            val myIssuesFragment = pager!!.adapter!!
                .instantiateItem(pager!!, pager!!.currentItem) as MyIssuesFragment
            myIssuesFragment.onScrollTop(0)
        }
    }

    private fun selectTab(position: Int, fromViewPager: Boolean) {
        if (!fromViewPager) {
            onShowFilterMenu(getModelAtIndex(position), ViewHelper.getTabTextView(tabs!!, position))
            pager!!.currentItem = position
        } else {
            val tab = tabs!!.getTabAt(position)
            if (tab != null) {
                tab.tag = "hello"
                if (!tab.isSelected) tab.select()
            }
        }
    }

    private fun updateCount(model: TabsCountStateModel) {
        val tv = ViewHelper.getTabTextView(tabs!!, model.tabIndex)
        var title = getString(R.string.created)
        when (model.tabIndex) {
            0 -> title = getString(R.string.created)
            1 -> title = getString(R.string.assigned)
            2 -> title = getString(R.string.mentioned)
            3 -> title = getString(R.string.participated)
        }
        updateDrawable(model, tv)
        tv.text = builder()
            .append(title)
            .append("   ")
            .append("(")
            .bold(model.count.toString())
            .append(")")
    }

    private fun onShowFilterMenu(model: TabsCountStateModel?, tv: TextView) {
        if (model == null) return
        val popup = PopupMenu(context, tv)
        val inflater = popup.menuInflater
        inflater.inflate(R.menu.filter_issue_state_menu, popup.menu)
        popup.setOnMenuItemClickListener { item: MenuItem ->
            if (pager == null || pager!!.adapter == null) return@setOnMenuItemClickListener false
            val myIssuesFragment = pager!!.adapter!!
                .instantiateItem(pager!!, model.tabIndex) as MyIssuesFragment
            when (item.itemId) {
                R.id.opened -> {
                    counts.remove(model)
                    model.drawableId = R.drawable.ic_issue_opened_small
                    counts.add(model)
                    updateDrawable(model, tv)
                    myIssuesFragment.onFilterIssue(IssueState.open)
                    return@setOnMenuItemClickListener true
                }
                R.id.closed -> {
                    counts.remove(model)
                    model.drawableId = R.drawable.ic_issue_closed_small
                    counts.add(model)
                    updateDrawable(model, tv)
                    myIssuesFragment.onFilterIssue(IssueState.closed)
                    return@setOnMenuItemClickListener true
                }
            }
            false
        }
        popup.show()
    }

    private fun updateDrawable(model: TabsCountStateModel, tv: TextView) {
        model.drawableId =
            if (model.drawableId == 0) R.drawable.ic_issue_opened_small else model.drawableId
        tv.compoundDrawablePadding = 16
        tv.setCompoundDrawablesWithIntrinsicBounds(
            model.drawableId,
            0,
            R.drawable.ic_arrow_drop_down,
            0
        )
    }

    companion object {
        @JvmField
        val TAG: String = MyIssuesPagerFragment::class.java.simpleName
        @JvmStatic
        fun newInstance(): MyIssuesPagerFragment {
            return MyIssuesPagerFragment()
        }
    }
}