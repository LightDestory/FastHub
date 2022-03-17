package com.fastaccess.ui.modules.filter.issues

import com.fastaccess.data.dao.LabelModel
import com.fastaccess.data.dao.MilestoneModel
import com.fastaccess.data.dao.model.User
import com.fastaccess.provider.rest.RestProvider.getRepoService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 09 Apr 2017, 6:22 PM
 */
class FilterIssuesActivityPresenter : BasePresenter<FilterIssuesActivityMvp.View>(),
    FilterIssuesActivityMvp.Presenter {
    @JvmField
    @com.evernote.android.state.State
    var labels = ArrayList<LabelModel>()

    @JvmField
    @com.evernote.android.state.State
    var milestones = ArrayList<MilestoneModel>()

    @JvmField
    @com.evernote.android.state.State
    var assignees = ArrayList<User>()
    override fun onStart(login: String, repoId: String) {
        val observable = getRepoService(isEnterprise).getLabels(login, repoId)
            .flatMap { labelModelPageable ->
                if (labelModelPageable.items != null) {
                    labels.clear()
                    labels.addAll(labelModelPageable.items!!)
                }
                getRepoService(isEnterprise).getAssignees(login, repoId)
            }
            .flatMap { userPageable ->
                if (userPageable.items != null) {
                    assignees.clear()
                    assignees.addAll(userPageable.items!!)
                }
                getRepoService(isEnterprise).getMilestones(login, repoId)
            }
        makeRestCall(observable) { response ->
            if (response?.items != null) {
                milestones.clear()
                milestones.addAll(response.items!!)
            }
            sendToView { it.hideProgress() }
        }
    }
}