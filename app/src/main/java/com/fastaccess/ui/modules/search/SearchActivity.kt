package com.fastaccess.ui.modules.search

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import butterknife.BindView
import butterknife.OnClick
import butterknife.OnEditorAction
import butterknife.OnTextChanged
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.FragmentPagerAdapterModel.Companion.buildForSearch
import com.fastaccess.data.dao.TabsCountStateModel
import com.fastaccess.data.dao.model.SearchHistory
import com.fastaccess.helper.AnimHelper.animateVisibility
import com.fastaccess.helper.ViewHelper.getTabTextView
import com.fastaccess.ui.adapter.FragmentsPagerAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.widgets.FontAutoCompleteEditText
import com.fastaccess.ui.widgets.ForegroundImageView
import com.fastaccess.ui.widgets.ViewPagerView
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat

/**
 * Created by Kosh on 08 Dec 2016, 8:22 PM
 */
class SearchActivity : BaseActivity<SearchMvp.View, SearchPresenter>(), SearchMvp.View {
    @JvmField
    @BindView(R.id.searchEditText)
    var searchEditText: FontAutoCompleteEditText? = null

    @JvmField
    @BindView(R.id.clear)
    var clear: ForegroundImageView? = null

    @JvmField
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null

    @JvmField
    @BindView(R.id.pager)
    var pager: ViewPagerView? = null

    @JvmField
    @State
    var tabsCountSet: HashSet<TabsCountStateModel> = LinkedHashSet()
    private val numberFormat = NumberFormat.getNumberInstance()
    private var adapter: ArrayAdapter<SearchHistory>? = null
        get() {
            if (field == null) field =
                ArrayAdapter(this, android.R.layout.simple_list_item_1, presenter!!.hints)
            return field
        }

    @OnTextChanged(
        value = [R.id.searchEditText],
        callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED
    )
    fun onTextChange(s: Editable) {
        val text = s.toString()
        if (text.isEmpty()) {
            animateVisibility(clear, false)
        } else {
            animateVisibility(clear, true)
        }
    }

    @OnClick(R.id.search)
    fun onSearchClicked() {
        presenter!!.onSearchClicked(pager!!, searchEditText!!)
    }

    @OnEditorAction(R.id.searchEditText)
    fun onEditor(): Boolean {
        onSearchClicked()
        return true
    }

    @OnClick(value = [R.id.clear])
    fun onClear(view: View) {
        if (view.id == R.id.clear) {
            searchEditText!!.setText("")
        }
    }

    override fun layout(): Int {
        return R.layout.search_layout
    }

    override val isTransparent: Boolean = false

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean = false

    override fun providePresenter(): SearchPresenter {
        return SearchPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = ""
        pager!!.adapter = FragmentsPagerAdapter(supportFragmentManager, buildForSearch(this))
        tabs!!.setupWithViewPager(pager)
        searchEditText!!.setAdapter(adapter)
        searchEditText!!.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, _, _ ->
                presenter!!.onSearchClicked(
                    pager!!, searchEditText!!
                )
            }
        if (tabsCountSet.isNotEmpty()) {
            setupTab()
        }
        if (savedInstanceState == null && intent != null) {
            if (intent.hasExtra("search")) {
                searchEditText!!.setText(intent.getStringExtra("search"))
                presenter!!.onSearchClicked(pager!!, searchEditText!!)
            }
        }
        tabs!!.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(pager) {
            override fun onTabReselected(tab: TabLayout.Tab) {
                super.onTabReselected(tab)
                onScrollTop(tab.position)
            }
        })
    }

    override fun onNotifyAdapter(query: SearchHistory?) {
        if (query == null) adapter!!.notifyDataSetChanged() else adapter!!.add(query)
    }

    override fun onSetCount(count: Int, index: Int) {
        val model = TabsCountStateModel()
        model.count = count
        model.tabIndex = index
        tabsCountSet.add(model)
        val textView = getTabTextView(tabs!!, index)
        when (index) {
            0 -> {
                textView.text = String.format(
                    "%s(%s)",
                    getString(R.string.repos),
                    numberFormat.format(count.toLong())
                )
            }
            1 -> {
                textView.text = String.format(
                    "%s(%s)",
                    getString(R.string.users),
                    numberFormat.format(count.toLong())
                )
            }
            2 -> {
                textView.text = String.format(
                    "%s(%s)",
                    getString(R.string.issues),
                    numberFormat.format(count.toLong())
                )
            }
            3 -> {
                textView.text = String.format(
                    "%s(%s)",
                    getString(R.string.code),
                    numberFormat.format(count.toLong())
                )
            }
        }
    }

    override fun onScrollTop(index: Int) {
        if (pager == null || pager!!.adapter == null) return
        val fragment: Fragment = pager!!.adapter!!
            .instantiateItem(pager!!, index) as BaseFragment<*, *>
        if (fragment is BaseFragment<*, *>) {
            fragment.onScrollTop(index)
        }
    }

    private fun setupTab() {
        for (model in tabsCountSet) {
            val index = model.tabIndex
            val count = model.count
            val textView = getTabTextView(tabs!!, index)
            when (index) {
                0 -> {
                    textView.text = String.format(
                        "%s(%s)",
                        getString(R.string.repos),
                        numberFormat.format(count.toLong())
                    )
                }
                1 -> {
                    textView.text = String.format(
                        "%s(%s)",
                        getString(R.string.users),
                        numberFormat.format(count.toLong())
                    )
                }
                2 -> {
                    textView.text = String.format(
                        "%s(%s)",
                        getString(R.string.issues),
                        numberFormat.format(count.toLong())
                    )
                }
                3 -> {
                    textView.text = String.format(
                        "%s(%s)",
                        getString(R.string.code),
                        numberFormat.format(count.toLong())
                    )
                }
            }
        }
    }

    companion object {
        fun getIntent(context: Context, query: String?): Intent {
            val intent = Intent(context, SearchActivity::class.java)
            intent.putExtra("search", query)
            return intent
        }
    }
}