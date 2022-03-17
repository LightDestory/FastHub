package com.fastaccess.ui.modules.main.donation

import android.os.Bundle
import android.view.View
import butterknife.BindView
import butterknife.OnClick
import com.fastaccess.App
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.helper.AnimHelper.animateVisibility
import com.fastaccess.helper.AppHelper
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.PrefGetter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.main.premium.PremiumActivity.Companion.startActivity
import com.fastaccess.ui.modules.repos.RepoPagerActivity
import com.google.android.material.appbar.AppBarLayout

/**
 * Created by Kosh on 24 Mar 2017, 9:16 PM
 */
class DonationActivity : BaseActivity<BaseMvp.FAView, BasePresenter<BaseMvp.FAView>>() {
    @JvmField
    @BindView(R.id.cardsHolder)
    var cardsHolder: View? = null

    @JvmField
    @BindView(R.id.appbar)
    var appBarLayout: AppBarLayout? = null
    override fun layout(): Int {
        return R.layout.support_development_layout
    }

    override val isTransparent: Boolean
        get() = false

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        animateVisibility(cardsHolder, true)
    }

    @OnClick(R.id.two)
    fun onTwoClicked() {
        PrefGetter.setProItems();
        PrefGetter.setEnterpriseItem();
        showMessage(getString(R.string.success), "\"Pro\" features unlocked, but don't forget to support development!")
    }

    @OnClick(R.id.five)
    fun onFiveClicked() {
        startActivity(RepoPagerActivity.createIntent(this, "FastHub", "k0shk0sh"))
    }

    override fun providePresenter(): BasePresenter<BaseMvp.FAView> {
        return BasePresenter()
    }
}