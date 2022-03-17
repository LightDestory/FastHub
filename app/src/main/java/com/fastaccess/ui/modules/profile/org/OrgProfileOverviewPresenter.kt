package com.fastaccess.ui.modules.profile.org

import android.os.Bundle
import com.fastaccess.data.dao.model.User
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.rest.RestProvider.getOrgService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 04 Apr 2017, 10:36 AM
 */
class OrgProfileOverviewPresenter : BasePresenter<OrgProfileOverviewMvp.View>(),
    OrgProfileOverviewMvp.Presenter {
    @com.evernote.android.state.State
    override var login: String? = null
    override fun onError(throwable: Throwable) {
        if (!isEmpty(login)) {
            onWorkOffline(login!!)
        }
        super.onError(throwable)
    }

    override fun onFragmentCreated(bundle: Bundle?) {
        if (bundle?.getString(BundleConstant.EXTRA) == null) {
            throw NullPointerException("Either bundle or User is null")
        }
        login = bundle.getString(BundleConstant.EXTRA)
        if (login != null) {
            makeRestCall(
                getOrgService(isEnterprise).getOrganization(
                    login!!
                )
            ) { userModel: User? -> onSendUserToView(userModel) }
        }
    }

    override fun onWorkOffline(login: String) {
        onSendUserToView(User.getUser(login))
    }


    private fun onSendUserToView(userModel: User?) {
        sendToView { view -> view.onInitViews(userModel) }
    }
}