package com.fastaccess.ui.modules.profile.org.repos

import android.view.View
import com.fastaccess.data.dao.FilterOptionsModel
import com.fastaccess.data.dao.model.Repo
import com.fastaccess.helper.RxHelper.getObservable
import com.fastaccess.provider.rest.RestProvider.getOrgService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 03 Dec 2016, 3:48 PM
 */
class OrgReposPresenter : BasePresenter<OrgReposMvp.View>(), OrgReposMvp.Presenter {
    override val repos = ArrayList<Repo>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE

    @JvmField
    @com.evernote.android.state.State
    var filterOptions = FilterOptionsModel()
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
        filterOptions.isOrg = true
        makeRestCall(getOrgService(isEnterprise).getOrgRepos(
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
                    sendToView { view: OrgReposMvp.View ->
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

    override fun onFilterApply(org: String?) {
        onCallApi(1, org)
    }

    override fun onTypeSelected(selectedType: String?) {
        filterOptions.setType(selectedType)
    }

    override fun onItemClick(position: Int, v: View?, item: Repo) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Repo) {}
}