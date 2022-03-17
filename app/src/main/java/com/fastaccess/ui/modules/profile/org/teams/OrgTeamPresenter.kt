package com.fastaccess.ui.modules.profile.org.teams

import android.view.View
import com.fastaccess.data.dao.TeamsModel
import com.fastaccess.provider.rest.RestProvider.getOrgService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.profile.org.teams.details.TeamPagerActivity

/**
 * Created by Kosh on 03 Dec 2016, 3:48 PM
 */
class OrgTeamPresenter : BasePresenter<OrgTeamMvp.View>(), OrgTeamMvp.Presenter {
    override val teams = ArrayList<TeamsModel>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE

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
        makeRestCall(getOrgService(isEnterprise).getOrgTeams(parameter, page)
        ) { response ->
            lastPage = response.last
            sendToView { view ->
                view.onNotifyAdapter(
                    response.items,
                    page
                )
            }
        }
        return true
    }

    override fun onWorkOffline(login: String) {
        //TODO
    }

    override fun onItemClick(position: Int, v: View?, item: TeamsModel) {
        TeamPagerActivity.startActivity(v!!.context, item.id, item.name!!)
    }

    override fun onItemLongClick(position: Int, v: View?, item: TeamsModel) {}
}