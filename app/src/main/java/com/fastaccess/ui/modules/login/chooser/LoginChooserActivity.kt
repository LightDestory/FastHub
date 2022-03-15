package com.fastaccess.ui.modules.login.chooser

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.transition.TransitionManager
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.data.dao.model.Login
import com.fastaccess.helper.Bundler
import com.fastaccess.helper.PrefGetter
import com.fastaccess.ui.adapter.LoginAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.modules.login.LoginActivity
import com.fastaccess.ui.modules.main.premium.PremiumActivity
import com.fastaccess.ui.modules.settings.LanguageBottomSheetDialog
import com.fastaccess.ui.widgets.FontTextView
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.ui.widgets.recyclerview.DynamicRecyclerView
import com.fastaccess.utils.setOnThrottleClickListener
import io.reactivex.functions.Action
import java.util.*

/**
 * Created by Kosh on 28 Apr 2017, 9:03 PM
 */

class LoginChooserActivity : BaseActivity<LoginChooserMvp.View, LoginChooserPresenter>(),
    LoginChooserMvp.View {

    private lateinit var languageSelector: RelativeLayout
    lateinit var recycler: DynamicRecyclerView
    private lateinit var multiAccLayout: View
    lateinit var viewGroup: CoordinatorLayout
    private lateinit var toggleImage: View

    private val adapter = LoginAdapter()

    override fun layout(): Int = R.layout.login_chooser_layout

    override val isTransparent: Boolean = true

    override fun canBack(): Boolean = false

    override val isSecured: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = window.decorView
        languageSelector = root.findViewById(R.id.language_selector)
        recycler = root.findViewById(R.id.recycler)
        multiAccLayout = root.findViewById(R.id.multiAccLayout)
        viewGroup = root.findViewById(R.id.viewGroup)
        toggleImage = root.findViewById(R.id.toggleImage)
        root.findViewById<View>(R.id.accessToken).setOnThrottleClickListener {
            this.onAccessTokenClicked()
        }
        root.findViewById<View>(R.id.enterprise).setOnThrottleClickListener {
            this.onEnterpriseClicked()
        }
        root.findViewById<View>(R.id.browserLogin).setOnThrottleClickListener {
            this.onOpenBrowser()
        }
        root.findViewById<FontTextView>(R.id.language_selector_clicker).setOnThrottleClickListener {
            this.onChangeLanguage()
        }
        root.findViewById<View>(R.id.toggle).setOnThrottleClickListener {
            this.onToggle()
        }
        adapter.listener = this
        recycler.adapter = adapter
        val languages = resources.getStringArray(R.array.languages_array_values)
        if (Locale.getDefault().language in languages) {
            val language = PrefGetter.getAppLanguage(resources)
            PrefGetter.setAppLangauge(Locale.getDefault().language)
            if (!BuildConfig.DEBUG) languageSelector.visibility = View.GONE
            if (Locale.getDefault().language != language) recreate()
        }
    }

    internal fun onAccessTokenClicked() {
        LoginActivity.start(this, false)
    }

    internal fun onEnterpriseClicked() {
        if (Login.hasNormalLogin()) {
            if (PrefGetter.isAllFeaturesUnlocked || PrefGetter.isEnterpriseEnabled) {
                LoginActivity.start(this, isBasicAuth = true, isEnterprise = true)
            } else {
                startActivity(Intent(this, PremiumActivity::class.java))
            }
        } else {
            MessageDialogView.newInstance(
                getString(R.string.warning), getString(R.string.enterprise_login_warning),
                false, Bundler.start().put("hide_buttons", true).end()
            )
                .show(supportFragmentManager, MessageDialogView.TAG)
        }
    }

    private fun onOpenBrowser() {
        LoginActivity.startOAuth(this)
    }

    internal fun onChangeLanguage() {
        showLanguage()
    }

    internal fun onToggle() {
        TransitionManager.beginDelayedTransition(viewGroup)
        val isVisible = recycler.visibility == View.VISIBLE
        recycler.visibility = if (isVisible) View.GONE else View.VISIBLE
        toggleImage.rotation = if (!isVisible) 180f else 0f
    }

    override fun onLanguageChanged(action: Action?) {
        try {
            action?.run()
            recreate()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun providePresenter(): LoginChooserPresenter {
        return LoginChooserPresenter()
    }

    override fun onAccountsLoaded(accounts: List<Login>?) {
        if (accounts == null || accounts.isEmpty()) {
            multiAccLayout.visibility = View.GONE
        } else {
            TransitionManager.beginDelayedTransition(viewGroup)
            adapter.insertItems(accounts)
            multiAccLayout.visibility = View.VISIBLE
        }
    }

    override fun onItemClick(position: Int, v: View?, item: Login) {
        presenter.manageViewDisposable(Login.onMultipleLogin(item, item.isIsEnterprise, false)
            .doOnSubscribe { showProgress(0) }
            .doOnComplete { this.hideProgress() }
            .subscribe({ onRestartApp() }, ::println)
        )
    }

    override fun onItemLongClick(position: Int, v: View?, item: Login) {}

    private fun showLanguage() {
        val languageBottomSheetDialog = LanguageBottomSheetDialog()
        languageBottomSheetDialog.onAttach(this as Context)
        languageBottomSheetDialog.show(supportFragmentManager, "LanguageBottomSheetDialog")
    }

}
