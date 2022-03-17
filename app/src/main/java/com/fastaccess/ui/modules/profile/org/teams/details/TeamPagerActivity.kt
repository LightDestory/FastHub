package com.fastaccess.ui.modules.profile.org.teams.details

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import butterknife.BindView
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.FragmentPagerAdapterModel.Companion.buildForTeam
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler.Companion.start
import com.fastaccess.ui.adapter.FragmentsPagerAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.BaseFragment
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.EmptyPresenter
import com.fastaccess.ui.widgets.ViewPagerView
import com.google.android.material.tabs.TabLayout

/**
 * Created by Kosh on 03 Apr 2017, 10:08 PM
 */
class TeamPagerActivity : BaseActivity<BaseMvp.FAView, EmptyPresenter>() {
    @JvmField
    @State
    var id: Long = 0

    @JvmField
    @State
    var name: String? = null

    @JvmField
    @BindView(R.id.tabs)
    var tabs: TabLayout? = null

    @JvmField
    @BindView(R.id.tabbedPager)
    var pager: ViewPagerView? = null
    override fun layout(): Int {
        return R.layout.tabbed_pager_layout
    }

    override val isTransparent: Boolean
        get() = false

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = false

    override fun providePresenter(): EmptyPresenter {
        return EmptyPresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            id = intent.extras!!.getLong(BundleConstant.ID)
            name = intent.extras!!.getString(BundleConstant.EXTRA)
        }
        title = name
        if (id <= 0) {
            finish()
            return
        }
        val adapter = FragmentsPagerAdapter(
            supportFragmentManager,
            buildForTeam(this, id)
        )
        pager!!.adapter = adapter
        tabs!!.tabGravity = TabLayout.GRAVITY_FILL
        tabs!!.tabMode = TabLayout.MODE_FIXED
        tabs!!.setupWithViewPager(pager)
        tabs!!.setPaddingRelative(0, 0, 0, 0)
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

    companion object {
        fun startActivity(context: Context, id: Long, name: String) {
            val intent = Intent(context, TeamPagerActivity::class.java)
            intent.putExtras(
                start()
                    .put(BundleConstant.ID, id)
                    .put(BundleConstant.EXTRA, name)
                    .end()
            )
            context.startActivity(intent)
        }
    }
}