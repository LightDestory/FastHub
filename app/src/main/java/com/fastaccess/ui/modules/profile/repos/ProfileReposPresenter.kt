package com.fastaccess.ui.modules.profile.repos

import android.text.TextUtils
import android.view.View
import com.fastaccess.data.dao.FilterOptionsModel
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.Repo
import com.fastaccess.helper.RxHelper.getObservable
import com.fastaccess.provider.rest.RestProvider.getUserService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 03 Dec 2016, 3:48 PM
 */
class ProfileReposPresenter : BasePresenter<ProfileReposMvp.View>(),
    ProfileReposMvp.Presenter {
    override val repos = ArrayList<Repo>()
    override var currentPage = 0
    override var previousTotal = 0
    private var username: String? = null
    private var lastPage = Int.MAX_VALUE
    private var currentLoggedIn: String? = null
    val filterOptions = FilterOptionsModel()
    override fun onError(throwable: Throwable) {
        sendToView { view ->
            if (view.loadMore.parameter != null) {
                onWorkOffline(view.loadMore.parameter!!)
            }
        }
        super.onError(throwable)
    }

    override fun onCallApi(page: Int, parameter: String?): Boolean {
        if (currentLoggedIn == null) {
            currentLoggedIn = Login.getUser().login
        }
        if (parameter == null) {
            throw NullPointerException("Username is null")
        }
        username = parameter
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view: ProfileReposMvp.View -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        val isProfile = TextUtils.equals(currentLoggedIn, username)
        filterOptions.setIsPersonalProfile(isProfile)
        makeRestCall(if (isProfile) getUserService(isEnterprise).getRepos(
            filterOptions.getQueryMap(),
            page
        ) else getUserService(isEnterprise).getRepos(
            parameter,
            filterOptions.getQueryMap(),
            page
        )
        ) { repoModelPageable ->
            lastPage = repoModelPageable.last
            if (currentPage == 1) {
                manageDisposable(Repo.saveMyRepos(repoModelPageable.items!!, parameter))
            }
            sendToView { view ->
                view.onNotifyAdapter(
                    repoModelPageable.items,
                    page
                )
            }
        }
        return true
    }

    override fun onWorkOffline(login: String) {
        if (repos.isEmpty()) {
            manageDisposable(
                getObservable(
                    Repo.getMyRepos(login).toObservable()
                ).subscribe { repoModels ->
                    sendToView { view ->
                        view.onNotifyAdapter(
                            repoModels,
                            1
                        )
                    }
                })
        } else {
            sendToView { it.hideProgress() }
        }
    }

    override fun onItemClick(position: Int, v: View?, item: Repo) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Repo) {}
    override fun onFilterApply() {
        onCallApi(1, username)
    }

    override fun onTypeSelected(selectedType: String?) {
        filterOptions.setType(selectedType)
    }

    override fun onSortOptionSelected(selectedSortOption: String?) {
        filterOptions.setSort(selectedSortOption)
    }

    override fun onSortDirectionSelected(selectedSortDirection: String?) {
        filterOptions.setSortDirection(selectedSortDirection)
    }
}