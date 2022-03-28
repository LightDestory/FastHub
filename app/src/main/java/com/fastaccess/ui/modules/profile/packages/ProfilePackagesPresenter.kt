package com.fastaccess.ui.modules.profile.packages

import android.text.TextUtils
import android.view.View
import android.widget.Toast
import com.fastaccess.data.dao.model.GitHubPackage
import com.fastaccess.helper.RxHelper.getObservable
import com.fastaccess.provider.rest.RestProvider.getUserService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

class ProfilePackagesPresenter : BasePresenter<ProfilePackagesMvp.View>(),
    ProfilePackagesMvp.Presenter {
    override val packages = ArrayList<GitHubPackage>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE
    override var defaultType: String? = "npm"
    override fun onError(throwable: Throwable) {
        sendToView { view ->
            if (view.loadMore.parameter != null) {
                onWorkOffline(view.loadMore.parameter!!)
            }
        }
        super.onError(throwable)
    }

    override fun onCallApi(page: Int, parameter: String?): Boolean {
        if (parameter == null) {
            throw NullPointerException("Username is null")
        }
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        val observable = getUserService(isEnterprise).getPackages(parameter, defaultType!!, page)
        makeRestCall(
            observable
        ) { packageModelPageable ->
            lastPage = packageModelPageable.last
            if (currentPage == 1) {
                manageDisposable(GitHubPackage.save(packageModelPageable.items!!, -1))
            }
            sendToView { view ->
                view.onNotifyAdapter(packageModelPageable.items, page)
            }
        }
        return true
    }

    override fun onWorkOffline(login: String) {
        if (packages.isEmpty()) {
            manageDisposable(
                getObservable(
                    GitHubPackage.getPackagesOf(login, defaultType!!).toObservable()
                ).subscribe { packageModels ->
                    sendToView { view ->
                        view.onNotifyAdapter(packageModels, 1)
                    }
                })
        } else {
            sendToView { it.hideProgress() }
        }
    }

    override fun onTypeChanged(package_type: String, parameter: String?) {
        if (!TextUtils.equals(package_type, defaultType)) {
            defaultType = package_type
            onCallApi(1, parameter)
        }
    }

    override fun onItemClick(position: Int, v: View?, item: GitHubPackage) {
        //launchUri(v!!.context, item.htmlUrl)
        Toast.makeText(v?.context, "You found a package!", Toast.LENGTH_SHORT).show()
    }

    override fun onItemLongClick(position: Int, v: View?, item: GitHubPackage) {}
}