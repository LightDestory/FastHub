package com.fastaccess.ui.modules.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.StringRes
import com.evernote.android.state.State
import com.fastaccess.App
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.helper.*
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.modules.login.chooser.LoginChooserActivity
import com.fastaccess.ui.widgets.FontCheckbox
import com.fastaccess.ui.widgets.dialog.MessageDialogView
import com.fastaccess.utils.setOnThrottleClickListener
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import es.dmoral.toasty.Toasty
import io.reactivex.functions.Action

/**
 * Created by Kosh on 08 Feb 2017, 9:10 PM
 */
open class LoginActivity : BaseActivity<LoginMvp.View, LoginPresenter>(), LoginMvp.View {
    var usernameEditText: TextInputEditText? = null

    var username: TextInputLayout? = null

    var passwordEditText: TextInputEditText? = null

    var password: TextInputLayout? = null

    var twoFactor: TextInputLayout? = null

    var twoFactorEditText: TextInputEditText? = null

    var login: FloatingActionButton? = null

    var progress: ProgressBar? = null

    var accessTokenCheckbox: FontCheckbox? = null

    var endpoint: TextInputLayout? = null

    @JvmField
    @State
    var isBasicAuth = false


    fun onOpenBrowser() {
        if (isEnterprise) {
            MessageDialogView.newInstance(
                getString(R.string.warning), getString(R.string.github_enterprise_reply),
                true, Bundler.start().put("hide_buttons", true).end()
            )
                .show(supportFragmentManager, MessageDialogView.TAG)
            return
        }
        ActivityHelper.startCustomTab(this, presenter!!.authorizationUrl)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.LoginTheme)
        super.onCreate(savedInstanceState)

        val root = window.decorView
        this.usernameEditText = root.findViewById(R.id.usernameEditText)
        this.username = root.findViewById(R.id.username)
        this.passwordEditText = root.findViewById(R.id.passwordEditText)
        this.password = root.findViewById(R.id.password)
        this.twoFactor = root.findViewById(R.id.twoFactor)
        this.twoFactorEditText = root.findViewById(R.id.twoFactorEditText)
        this.login = root.findViewById(R.id.login)
        this.progress = root.findViewById(R.id.progress)
        this.accessTokenCheckbox = root.findViewById(R.id.accessTokenCheckbox)
        this.endpoint = root.findViewById(R.id.endpoint)

        this.login!!.setOnThrottleClickListener {
            this.doLogin()
        }

        root.findViewById<View>(R.id.browserLogin).setOnThrottleClickListener {
            this.onOpenBrowser()
        }

        this.accessTokenCheckbox!!.setOnCheckedChangeListener { _, isChecked ->
            this.onCheckChanged(isChecked)
        }

        this.passwordEditText!!.setOnEditorActionListener { _, _, _ ->
            this.onSendPassword()
        }

        this.twoFactorEditText!!.setOnEditorActionListener { _, _, _ ->
            this.doLogin()
            true
        }

        root.findViewById<TextInputEditText>(R.id.endpointEditText).setOnEditorActionListener { _, _, _ ->
            this.doLogin()
            true
        }

        if (savedInstanceState == null) {
            if (intent != null && intent.extras != null) {
                isBasicAuth = intent.extras!!.getBoolean(BundleConstant.YES_NO_EXTRA)
                password!!.hint =
                    if (isBasicAuth) getString(R.string.password) else getString(R.string.access_token)
                if (intent.extras!!.getBoolean(BundleConstant.EXTRA_TWO)) {
                    onOpenBrowser()
                }
            }
        }
        accessTokenCheckbox!!.visibility =
            if (isEnterprise) View.VISIBLE else View.GONE
        endpoint!!.visibility = if (isEnterprise) View.VISIBLE else View.GONE
    }

    private fun onCheckChanged(checked: Boolean) {
        isBasicAuth = !checked
        password!!.hint =
            if (checked) getString(R.string.access_token) else getString(R.string.password)
    }

    fun onSendPassword(): Boolean {
        when {
            twoFactor!!.visibility == View.VISIBLE -> {
                twoFactorEditText!!.requestFocus()
            }
            endpoint!!.visibility == View.VISIBLE -> {
                endpoint!!.requestFocus()
            }
            else -> {
                doLogin()
            }
        }
        return true
    }

//    fun onSend2FA(): Boolean {
//        doLogin()
//        return true
//    }
//
//    fun onSendEndpoint(): Boolean {
//        doLogin()
//        return true
//    }

    override fun layout(): Int {
        return R.layout.login_form_layout
    }

    override val isTransparent = true

    override fun canBack(): Boolean {
        return false
    }

    override val isSecured = true

    override fun providePresenter(): LoginPresenter {
        return LoginPresenter()
    }

    override fun onEmptyUserName(isEmpty: Boolean) {
        username!!.error = if (isEmpty) getString(R.string.required_field) else null
    }

    override fun onRequire2Fa() {
        Toasty.warning(App.getInstance(), getString(R.string.two_factors_otp_error)).show()
        twoFactor!!.visibility = View.VISIBLE
        hideProgress()
    }

    override fun onEmptyPassword(isEmpty: Boolean) {
        password!!.error = if (isEmpty) getString(R.string.required_field) else null
    }

    override fun onEmptyEndpoint(isEmpty: Boolean) {
        endpoint!!.error = if (isEmpty) getString(R.string.required_field) else null
    }

    override fun onSuccessfullyLoggedIn(extraLogin: Boolean) {
        hideProgress()
        onRestartApp()
    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        presenter!!.onHandleAuthIntent(intent)
        setIntent(null)
    }

    override fun onResume() {
        super.onResume()
        presenter!!.onHandleAuthIntent(intent)
        intent = null
    }

    override fun showErrorMessage(msgRes: String) {
        hideProgress()
        super.showErrorMessage(msgRes)
    }

    override fun showMessage(@StringRes titleRes: Int, @StringRes msgRes: Int) {
        hideProgress()
        super.showMessage(titleRes, msgRes)
    }

    override fun showMessage(titleRes: String, msgRes: String) {
        hideProgress()
        super.showMessage(titleRes, msgRes)
    }

    override fun showProgress(@StringRes resId: Int) {
        login!!.hide()
        AppHelper.hideKeyboard(login!!)
        AnimHelper.animateVisibility(progress, true)
    }

    override fun onBackPressed() {
        startActivity(Intent(this, LoginChooserActivity::class.java))
        finish()
    }

    override fun hideProgress() {
        progress!!.visibility = View.GONE
        login!!.show()
    }

    private fun doLogin() {
        if (progress!!.visibility == View.GONE) {
            presenter!!.login(
                InputHelper.toString(username),
                InputHelper.toString(password),
                InputHelper.toString(twoFactor),
                isBasicAuth, InputHelper.toString(endpoint)
            )
        }
    }

    companion object {
        fun startOAuth(activity: Activity) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.YES_NO_EXTRA, false)
                    .put(BundleConstant.EXTRA_TWO, true)
                    .end()
            )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(intent)
            activity.finish()
        }

        @JvmOverloads
        fun start(activity: Activity, isBasicAuth: Boolean, isEnterprise: Boolean = false) {
            val intent = Intent(activity, LoginActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.YES_NO_EXTRA, isBasicAuth)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .end()
            )
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }
}