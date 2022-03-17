package com.fastaccess.ui.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.view.ContextThemeWrapper
import butterknife.ButterKnife
import butterknife.Unbinder
import com.evernote.android.state.StateSaver
import com.fastaccess.data.dao.model.Login
import com.fastaccess.helper.RxHelper
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import net.grandcentrix.thirtyinch.TiFragment

/**
 * Created by Kosh on 27 May 2016, 7:54 PM
 */
abstract class BaseFragment<V : FAView, P : BasePresenter<V>> : TiFragment<P, V>(), FAView {
    protected open var callback: FAView? = null
    private var unbinder: Unbinder? = null
    private var compositeDisposable: CompositeDisposable? = CompositeDisposable()

    fun manageDisposable(vararg disposables: Disposable?) {
        compositeDisposable?.addAll(*disposables)
    }

    fun <T> manageObservable(observable: Observable<T>?) {
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

    fun disposable() {
        compositeDisposable?.let {
            it.dispose()
            it.clear()
        }
    }

    override fun onDestroy() {
        disposable()
        super.onDestroy()
        compositeDisposable = null
    }

    @LayoutRes
    protected abstract fun fragmentLayout(): Int
    protected abstract fun onFragmentCreated(view: View, savedInstanceState: Bundle?)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is FAView) {
            callback = context
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        StateSaver.saveInstanceState(this, outState)
        presenter!!.onSaveInstanceState(outState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null && !savedInstanceState.isEmpty) {
            StateSaver.restoreInstanceState(this, savedInstanceState)
            presenter!!.onRestoreInstanceState(savedInstanceState)
        }
        presenter!!.isEnterprise = isEnterprise
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (fragmentLayout() != 0) {
            val contextThemeWrapper: Context = ContextThemeWrapper(
                context, requireContext().theme
            )
            val themeAwareInflater = inflater.cloneInContext(contextThemeWrapper)
            val view = themeAwareInflater.inflate(fragmentLayout(), container, false)
            unbinder = ButterKnife.bind(this, view)
            return view
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Login.getUser() != null) {
            onFragmentCreated(view, savedInstanceState)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (unbinder != null) unbinder!!.unbind()
    }

    override fun showProgress(@StringRes resId: Int) {
        callback?.showProgress(resId)
    }

    override fun showBlockingProgress(resId: Int) {
        callback?.showBlockingProgress(resId)
    }

    override fun hideProgress() {
        callback?.hideProgress()
    }

    override fun showMessage(@StringRes titleRes: Int, @StringRes msgRes: Int) {
        callback?.showMessage(titleRes, msgRes)
    }

    override fun showMessage(titleRes: String, msgRes: String) {
        callback?.showMessage(titleRes, msgRes)
    }

    override fun showErrorMessage(msgRes: String) {
        callback?.showErrorMessage(msgRes)
    }

    override val isLoggedIn: Boolean
        get() = callback?.isLoggedIn ?: false

    override fun onRequireLogin() {
        callback?.onRequireLogin()
    }

    override fun onMessageDialogActionClicked(isOk: Boolean, bundle: Bundle?) {}
    override fun onDialogDismissed() {}
    override fun onLogoutPressed() {
        callback?.onLogoutPressed()
    }

    override fun onThemeChanged() {
        callback?.onThemeChanged()
    }

    override fun onOpenSettings() {
        callback?.onOpenSettings()
    }

    override fun onScrollTop(index: Int) {}
    override val isEnterprise: Boolean
        get() = callback?.isEnterprise ?: false

    override fun onOpenUrlInBrowser() {
        callback?.onOpenUrlInBrowser()
    }

    protected val isSafe: Boolean
        get() = view != null && activity != null && !requireActivity().isFinishing
}