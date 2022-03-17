package com.fastaccess.ui.modules.profile.org.members

import android.view.View
import com.fastaccess.data.dao.model.User
import com.fastaccess.provider.rest.RestProvider.getOrgService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 03 Dec 2016, 3:48 PM
 */
class OrgMembersPresenter : BasePresenter<OrgMembersMvp.View>(), OrgMembersMvp.Presenter {
    override val followers = ArrayList<User>()
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
        makeRestCall(getOrgService(isEnterprise).getOrgMembers(parameter, page)
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

    override fun onItemClick(position: Int, v: View?, item: User) {}
    override fun onItemLongClick(position: Int, v: View?, item: User) {}
}