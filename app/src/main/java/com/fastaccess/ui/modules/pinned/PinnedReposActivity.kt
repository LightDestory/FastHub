package com.fastaccess.ui.modules.pinned

import android.content.Context
import android.content.Intent
import android.os.Bundle
import butterknife.BindView
import com.fastaccess.R
import com.fastaccess.data.dao.FragmentPagerAdapterModel.Companion.buildForPinned
import com.fastaccess.ui.adapter.FragmentsPagerAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.main.MainActivity.Companion.launchMainActivity
import com.fastaccess.ui.widgets.ViewPagerView
import com.google.android.material.tabs.TabLayout

/**
 * Created by Kosh on 25 Mar 2017, 11:14 PM
 */
class PinnedReposActivity : BaseActivity<BaseMvp.FAView, BasePresenter<BaseMvp.FAView>>() {
    @JvmField
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null

    @JvmField
    @BindView(R.id.tabbedPager)
    var tabbedPager: ViewPagerView? = null
    override fun layout(): Int {
        return R.layout.tabbed_pager_layout
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
        selectPinned()
        tabbedPager!!.adapter = FragmentsPagerAdapter(
            supportFragmentManager,
            buildForPinned(this)
        )
        tabs!!.setupWithViewPager(tabbedPager)
        tabs!!.setPadding(0, 0, 0, 0)
        tabs!!.tabMode = TabLayout.MODE_SCROLLABLE
    }

    override fun onBackPressed() {
        if (isTaskRoot) {
            launchMainActivity(this, true)
            finish()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNavBack() {
        if (isTaskRoot) {
            launchMainActivity(this, true)
        }
        finish()
    }

    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, PinnedReposActivity::class.java))
        }
    }
}