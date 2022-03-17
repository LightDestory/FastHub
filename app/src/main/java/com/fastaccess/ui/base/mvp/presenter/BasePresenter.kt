package com.fastaccess.ui.base.mvp.presenter

import android.os.Bundle
import androidx.annotation.StringRes
import com.evernote.android.state.StateSaver
import com.fastaccess.R
import com.fastaccess.exception.AuthNullOrBlankException
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import net.grandcentrix.thirtyinch.TiPresenter
import net.grandcentrix.thirtyinch.rx2.RxTiPresenterDisposableHandler
import java.io.IOException
import java.util.concurrent.TimeoutException

/**
 * Created by Kosh on 25 May 2016, 9:12 PM
 */
open class BasePresenter<V : FAView> : TiPresenter<V>(), FAPresenter {
    @com.evernote.android.state.State
    var isEnterprise = false
    private var apiCalled = false
    private val subscriptionHandler = RxTiPresenterDisposableHandler(this)
    override fun manageDisposable(vararg disposables: Disposable?) {
        subscriptionHandler.manageDisposables(*disposables.filterNotNull().toTypedArray())
    }
    override fun <T> manageObservable(observable: Observable<T>?) {
        manageObservable(observable) {}
    }
    fun <T> manageObservable(observable: Observable<T>?, onNext: (T) -> Unit) {
        if (observable != null) {
            manageDisposable(
                RxHelper.getObservable(observable)
                    .subscribe(onNext) { obj: Throwable ->
                        obj.printStackTrace()
                        // Todo bugly
                    }
            )
        }
    }
    override fun manageViewDisposable(vararg disposables: Disposable?) {
        if (isViewAttached) {
            subscriptionHandler.manageViewDisposables(*disposables)
        } else {
            sendToView { manageViewDisposable(*disposables) }
        }
    }


    override fun onSaveInstanceState(outState: Bundle?) {
        StateSaver.saveInstanceState(this, outState!!)
    }

    override fun onRestoreInstanceState(outState: Bundle?) {
        StateSaver.restoreInstanceState(this, outState)
    }


    override val isApiCalled: Boolean
        get() = apiCalled

    override fun onSubscribed(cancelable: Boolean) {
        sendToView { v: V ->
            if (cancelable) {
                v.showProgress(R.string.in_progress)
            } else {
                v.showBlockingProgress(R.string.in_progress)
            }
        }
    }

    override fun onError(throwable: Throwable) {
        apiCalled = true
        throwable.printStackTrace()
        val code = RestProvider.getErrorCode(throwable)
        if (code == 401) {
            sendToView { it?.onRequireLogin() }
            return
        }
        val errorResponse = RestProvider.getErrorResponse(throwable)
        if (errorResponse?.message != null) {
            sendToView { v ->
                v.showErrorMessage(
                    errorResponse.message!!
                )
            }
        } else {
            sendToView { v ->
                v.showMessage(
                    R.string.error,
                    getPrettifiedErrorMessage(throwable)
                )
            }
        }
    }

    override fun <T> makeRestCall(observable: Observable<T>, onNext: Consumer<T>) {
        makeRestCall(observable, onNext, true)
    }

    override fun <T> makeRestCall(
        observable: Observable<T>,
        onNext: Consumer<T>,
        cancelable: Boolean
    ) {
        manageDisposable(
            RxHelper.getObservable(observable)
                .doOnSubscribe { onSubscribed(cancelable) }
                .subscribe(onNext, { throwable: Throwable -> onError(throwable) }) {
                    apiCalled = true
                }
        )
    }

    @StringRes
    private fun getPrettifiedErrorMessage(throwable: Throwable?): Int {
        var resId = R.string.network_error
        when (throwable) {
            is IOException -> {
                resId = R.string.request_error
            }
            is TimeoutException -> {
                resId = R.string.unexpected_error
            }
            is AuthNullOrBlankException -> {
                resId = R.string.auth_null_or_blank
            }
        }
        return resId
    }

    override fun onCheckGitHubStatus() {
        manageObservable(RestProvider.gitHubStatus()
            .doOnNext { status ->
                val components = status.components!!
                components.asSequence()
                    .filter { it.name == "API Requests" || it.name == "Git Operations" }
                    .forEach {
                        if (!it.status.equals("operational", true)) {
                            sendToView { v ->
                                v.showErrorMessage(
                                    """Github Status ${it.name}: ${it.status}""".trimIndent()
                                )
                            }
                        }
                    }
            })
    }
}