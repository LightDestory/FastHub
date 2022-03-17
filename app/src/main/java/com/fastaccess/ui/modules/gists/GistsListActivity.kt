package com.fastaccess.ui.modules.gists

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.fastaccess.R
import com.fastaccess.data.dao.FragmentPagerAdapterModel.Companion.buildForGists
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.ui.adapter.FragmentsPagerAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.gists.create.CreateGistActivity
import com.fastaccess.ui.modules.main.MainActivity.Companion.launchMainActivity
import com.fastaccess.ui.widgets.ViewPagerView
import com.fastaccess.utils.setOnThrottleClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

/**
 * Created by Kosh on 25 Mar 2017, 11:28 PM
 */
class GistsListActivity : BaseActivity<BaseMvp.FAView, BasePresenter<BaseMvp.FAView>>() {
    private val tabs: TabLayout? by lazy { window.decorView.findViewById(R.id.tabs) }
    private val pager: ViewPagerView? by lazy { window.decorView.findViewById(R.id.gistsContainer) }
    private val fab: FloatingActionButton? by lazy { window.decorView.findViewById(R.id.fab) }
    override fun layout(): Int {
        return R.layout.gists_activity_layout
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = false

    override fun providePresenter(): BasePresenter<BaseMvp.FAView> {
        return BasePresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fab!!.setOnThrottleClickListener {
            onViewClicked()
        }

        setTitle(R.string.gists)
        setTaskName(getString(R.string.gists))
        setupTabs()
        fab!!.show()
        tabs!!.addOnTabSelectedListener(object : TabLayout.ViewPagerOnTabSelectedListener(pager) {
            override fun onTabReselected(tab: TabLayout.Tab) {
                super.onTabReselected(tab)
                onScrollTop(tab.position)
            }
        })
    }

    override fun onScrollTop(index: Int) {
        if (pager == null || pager!!.adapter == null) return
        val fragment: Fragment = pager!!.adapter!!
            .instantiateItem(pager!!, index) as BaseFragment<*, *>
        if (fragment is BaseFragment<*, *>) {
            fragment.onScrollTop(index)
        }
    }

    private var createLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {}

    private fun onViewClicked() {
        ActivityHelper.startLauncher(
            createLauncher,
            Intent(this, CreateGistActivity::class.java),
            fab!!,
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            launchMainActivity(this, true)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupTabs() {
        pager!!.adapter =
            FragmentsPagerAdapter(supportFragmentManager, buildForGists(this))
        tabs!!.setupWithViewPager(pager)
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, GistsListActivity::class.java))
        }
    }
}