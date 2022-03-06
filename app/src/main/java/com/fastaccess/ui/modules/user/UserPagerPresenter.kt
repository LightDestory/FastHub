package com.fastaccess.ui.modules.user

import com.fastaccess.data.dao.model.Login
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import retrofit2.Response

/**
 * Created by Kosh on 03 Dec 2016, 8:00 AM
 */
class UserPagerPresenter : BasePresenter<UserPagerMvp.View?>(), UserPagerMvp.Presenter {
    @com.evernote.android.state.State
    var isMember = -1

    @com.evernote.android.state.State
    var isUserBlocked = false

    @com.evernote.android.state.State
    var isUserBlockedRequested = false
    override fun onCheckBlocking(login: String) {
        makeRestCall(RestProvider.getUserService(isEnterprise).isUserBlocked(login)
        ) { booleanResponse: Response<Boolean?> ->
            sendToView { view: UserPagerMvp.View? ->
                isUserBlockedRequested = true
                isUserBlocked = booleanResponse.code() == 204
                view?.onInvalidateMenu()
            }
        }
    }

    override fun checkOrgMembership(org: String) {
        makeRestCall(RestProvider.getOrgService(isEnterprise).isMember(org, Login.getUser().login)
        ) { booleanResponse: Response<Boolean?> ->
            sendToView { view: UserPagerMvp.View? ->
                isMember = if (booleanResponse.code() == 204) 1 else 0
                view?.onInitOrg(isMember == 1)
            }
        }
    }

    override fun onBlockUser(login: String) {
        if (isUserBlocked) {
            onUnblockUser(login)
        } else {
            makeRestCall(RestProvider.getUserService(isEnterprise).blockUser(login)
            ) {
                sendToView { view: UserPagerMvp.View? ->
                    isUserBlocked = true
                    view?.onUserBlocked()
                }
            }
        }
    }

    override fun onUnblockUser(login: String) {
        makeRestCall(RestProvider.getUserService(isEnterprise).unBlockUser(login)
        ) {
            sendToView { view: UserPagerMvp.View? ->
                isUserBlocked = false
                view?.onUserUnBlocked()
            }
        }
    }
}