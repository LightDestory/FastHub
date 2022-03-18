package com.fastaccess.ui.modules.login

import android.content.Intent
import android.net.Uri
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.data.dao.AccessTokenModel
import com.fastaccess.data.dao.AuthModel
import com.fastaccess.data.dao.model.Login
import com.fastaccess.helper.GithubConfigHelper.clientId
import com.fastaccess.helper.GithubConfigHelper.redirectUrl
import com.fastaccess.helper.GithubConfigHelper.secret
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.PrefGetter
import com.fastaccess.helper.PrefGetter.enterpriseOtpCode
import com.fastaccess.helper.PrefGetter.enterpriseUrl
import com.fastaccess.helper.PrefGetter.otpCode
import com.fastaccess.helper.PrefGetter.setTokenEnterprise
import com.fastaccess.helper.PrefGetter.token
import com.fastaccess.provider.rest.LoginProvider.getLoginRestService
import com.fastaccess.provider.rest.LoginProvider.loginRestService
import com.fastaccess.provider.rest.RestProvider.clearHttpClient
import com.fastaccess.provider.rest.RestProvider.getErrorCode
import com.fastaccess.provider.rest.RestProvider.getUserService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import okhttp3.Credentials.basic
import retrofit2.HttpException

/**
 * Created by Kosh on 09 Nov 2016, 9:43 PM
 */
class LoginPresenter internal constructor() : BasePresenter<LoginMvp.View>(), LoginMvp.Presenter {

    override fun onError(throwable: Throwable) {
        if (getErrorCode(throwable) == 401 && throwable is HttpException) {
            val response = throwable.response()
            if (response != null) {
                val twoFaToken = response.headers()["X-GitHub-OTP"]
                if (twoFaToken != null) {
                    sendToView { it.onRequire2Fa() }
                } else {
                    sendToView { view ->
                        view.showMessage(
                            R.string.error,
                            R.string.failed_login
                        )
                    }
                }
                return
            }
        }
        sendToView { view ->
            view.showErrorMessage(
                throwable.message!!
            )
        }
    }

    override fun onTokenResponse(response: AccessTokenModel?) {
        if (response != null) {
            val token =
                if (response.token != null) response.token else response.accessToken
            if (!isEmpty(token)) {
                PrefGetter.token = token
                makeRestCall(getUserService(false).user) { userModel: Login? ->
                    onUserResponse(
                        userModel
                    )
                }
                return
            }
        }
        sendToView { view ->
            view.showMessage(
                R.string.error,
                R.string.failed_login
            )
        }
    }

    override val authorizationUrl: Uri
        get() = Uri.Builder().scheme("https")
            .authority("github.com")
            .appendPath("login")
            .appendPath("oauth")
            .appendPath("authorize")
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUrl)
            .appendQueryParameter("scope", "user,repo,gist,notifications,read:org,workflow")
            .appendQueryParameter("state", BuildConfig.GITHUB_APP_ID)
            .build()

    override fun onHandleAuthIntent(intent: Intent?) {
        if (intent != null && intent.data != null) {
            val uri = intent.data
            if (uri.toString().startsWith(redirectUrl)) {
                val tokenCode = uri!!.getQueryParameter("code")
                if (!isEmpty(tokenCode)) {
                    makeRestCall(
                        loginRestService.getAccessToken(
                            tokenCode!!,
                            clientId, secret,
                            BuildConfig.GITHUB_APP_ID, redirectUrl
                        )
                    ) { modelResponse: AccessTokenModel? -> onTokenResponse(modelResponse) }
                } else {
                    sendToView { view ->
                        view.showMessage(
                            R.string.error,
                            R.string.error
                        )
                    }
                }
            }
        }
    }

    override fun onUserResponse(response: Login?) {
        if (response != null) {
            manageObservable(Login.onMultipleLogin(response, isEnterprise, true)
                .doOnComplete {
                    sendToView { view ->
                        view.onSuccessfullyLoggedIn(
                            isEnterprise
                        )
                    }
                })
            return
        }
        sendToView { view: LoginMvp.View ->
            view.showMessage(
                R.string.error,
                R.string.failed_login
            )
        }
    }

    override fun login(
        username: String, password: String, twoFactorCode: String?,
        isBasicAuth: Boolean, endpoint: String?
    ) {
        val usernameIsEmpty = isEmpty(username)
        val passwordIsEmpty = isEmpty(password)
        val endpointIsEmpty = isEmpty(endpoint) && isEnterprise
        if (view == null) return
        view!!.onEmptyUserName(usernameIsEmpty)
        view!!.onEmptyPassword(passwordIsEmpty)
        view!!.onEmptyEndpoint(endpointIsEmpty)
        if (!usernameIsEmpty && !passwordIsEmpty) {
            try {
                val authToken = basic(username, password)
                if (isBasicAuth && !isEnterprise) {
                    val authModel = AuthModel()
                    authModel.scopes =
                        listOf("user", "repo", "gist", "notifications", "read:org")
                    authModel.note = BuildConfig.GITHUB_APP_ID
                    authModel.clientSecret = secret
                    authModel.clientId = clientId
                    authModel.noteUrl = redirectUrl
                    if (!isEmpty(twoFactorCode)) {
                        authModel.otpCode = twoFactorCode
                    }
                    makeRestCall(
                        getLoginRestService(
                            authToken,
                            twoFactorCode,
                            null
                        ).login(authModel)
                    ) { accessTokenModel: AccessTokenModel? ->
                        if (!isEmpty(twoFactorCode)) {
                            otpCode = twoFactorCode
                        }
                        onTokenResponse(accessTokenModel)
                    }
                } else {
                    accessTokenLogin(password, endpoint, twoFactorCode, authToken)
                }
            } catch (e: Exception) {
                sendToView { view: LoginMvp.View ->
                    view.showMessage(
                        "Error",
                        "The app was about to crash!!(" + e.message + ")"
                    )
                }
            }
        }
    }

    private fun accessTokenLogin(
        password: String, endpoint: String?, otp: String?,
        authToken: String
    ) {
        makeRestCall(
            getLoginRestService(authToken, otp, endpoint).loginAccessToken()
        ) { login: Login? ->
            if (!isEnterprise) {
                token = password
            } else {
                enterpriseOtpCode = otp
                setTokenEnterprise(authToken)
                enterpriseUrl = endpoint
            }
            onUserResponse(login)
        }
    }

    init {
        clearHttpClient()
    }
}