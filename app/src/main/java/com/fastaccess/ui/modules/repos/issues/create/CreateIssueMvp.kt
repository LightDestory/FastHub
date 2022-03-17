package com.fastaccess.ui.modules.repos.issues.create

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.modules.repos.extras.labels.LabelsMvp.SelectedLabelsListener
import com.fastaccess.ui.modules.repos.extras.assignees.AssigneesMvp.SelectedAssigneesListener
import com.fastaccess.ui.modules.repos.extras.milestone.MilestoneMvp.OnMilestoneSelected
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import android.content.Intent
import com.fastaccess.data.dao.LabelModel
import com.fastaccess.data.dao.MilestoneModel
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.data.dao.model.User
import java.util.ArrayList

/**
 * Created by Kosh on 19 Feb 2017, 12:12 PM
 */
interface CreateIssueMvp {
    interface View : FAView, SelectedLabelsListener, SelectedAssigneesListener,
        OnMilestoneSelected {
        fun onSetCode(charSequence: CharSequence)
        fun onTitleError(isEmptyTitle: Boolean)
        fun onDescriptionError(isEmptyDesc: Boolean)
        fun onSuccessSubmission(issueModel: Issue)
        fun onSuccessSubmission(issueModel: PullRequest)
        fun onShowUpdate()
        fun onShowIssueMisc()
    }

    interface Presenter : FAPresenter {
        fun checkAuthority(login: String, repoId: String)
        fun onActivityForResult(resultCode: Int, requestCode: Int, intent: Intent?)
        fun onSubmit(
            title: String, description: CharSequence, login: String,
            repo: String, issueModel: Issue?, pullRequestModel: PullRequest?,
            labels: ArrayList<LabelModel>?, milestoneModel: MilestoneModel?,
            users: ArrayList<User>?
        )

        fun onCheckAppVersion()
        val isCollaborator: Boolean
    }
}