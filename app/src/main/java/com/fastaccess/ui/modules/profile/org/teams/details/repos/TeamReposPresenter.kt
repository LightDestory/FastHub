package com.fastaccess.ui.modules.profile.org.teams.details.repos

import android.view.View
import com.fastaccess.data.dao.model.Repo
import com.fastaccess.provider.rest.RestProvider.getOrgService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 03 Dec 2016, 3:48 PM
 */
class TeamReposPresenter : BasePresenter<TeamReposMvp.View>(), TeamReposMvp.Presenter {
    override val repos = ArrayList<Repo>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE

    override fun onCallApi(page: Int, parameter: Long?): Boolean {
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
        makeRestCall(getOrgService(isEnterprise).getTeamRepos(parameter, page)
        ) { repoModelPageable ->
            lastPage = repoModelPageable.last
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
        //TODO
    }

    override fun onItemClick(position: Int, v: View?, item: Repo) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Repo) {}
}