package com.fastaccess.ui.modules.main.orgs

import com.fastaccess.data.dao.Pageable
import com.fastaccess.data.dao.model.User
import com.fastaccess.helper.PrefGetter
import com.fastaccess.provider.rest.RestProvider.getOrgService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import io.reactivex.Observable

/**
 * Created by Kosh on 15 Apr 2017, 1:54 PM
 */
class OrgListDialogPresenter : BasePresenter<OrgListDialogMvp.View>(), OrgListDialogMvp.Presenter {
    override val orgs = ArrayList<User>()
    override fun onLoadOrgs() {
        makeRestCall(getOrgService(PrefGetter.isEnterprise).myOrganizations
            .flatMap { userPageable: Pageable<User>? ->
                if (userPageable?.items != null) {
                    return@flatMap Observable.fromIterable(userPageable.items)
                }
                Observable.empty()
            }
            .map<User?> { user: User? ->
                if (user != null) user.type = "Organization"
                user
            }
            .toList()
            .toObservable()) { list: List<User>? ->
            val myOrgs: MutableList<User> = ArrayList()
            if (list != null && list.isNotEmpty()) {
                myOrgs.addAll(list)
            }
            sendToView { view: OrgListDialogMvp.View -> view.onNotifyAdapter(myOrgs) }
        }
    }
}