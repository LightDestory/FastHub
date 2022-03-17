package com.fastaccess.ui.modules.repos.issues.issue.details

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.modules.repos.extras.labels.LabelsMvp.SelectedLabelsListener
import com.fastaccess.ui.modules.repos.extras.assignees.AssigneesMvp.SelectedAssigneesListener
import com.fastaccess.ui.modules.editor.comment.CommentEditorFragment.CommentListener
import com.fastaccess.ui.modules.repos.extras.locking.LockIssuePrCallback
import com.fastaccess.data.dao.MilestoneModel
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import android.content.Intent
import android.os.Bundle
import com.fastaccess.data.dao.LabelModel
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.User
import java.util.ArrayList

/**
 * Created by Kosh on 10 Dec 2016, 9:21 AM
 */
interface IssuePagerMvp {
    interface View : FAView, SelectedLabelsListener, SelectedAssigneesListener,
        IssuePrCallback<Issue>, CommentListener, LockIssuePrCallback {
        fun onSetupIssue(isUpdate: Boolean)
        fun showSuccessIssueActionMsg(isClose: Boolean)
        fun showErrorIssueActionMsg(isClose: Boolean)
        fun onUpdateTimeline()
        fun onUpdateMenu()
        fun onMileStoneSelected(milestoneModel: MilestoneModel)
        fun onFinishActivity()
    }

    interface Presenter : FAPresenter {
        val issue: Issue?
        fun onActivityCreated(intent: Intent?)
        fun onWorkOffline(issueNumber: Long, repoId: String, login: String)
        val isOwner: Boolean
        val isRepoOwner: Boolean
        val isLocked: Boolean
        val isCollaborator: Boolean
        fun showToRepoBtn(): Boolean
        fun onHandleConfirmDialog(bundle: Bundle?)
        fun onOpenCloseIssue()
        fun onLockUnlockIssue(reason: String?)
        fun onPutMilestones(milestone: MilestoneModel)
        fun onPutLabels(labels: ArrayList<LabelModel>)
        fun onPutAssignees(users: ArrayList<User>)
        val login: String?
        val repoId: String?
        fun onUpdateIssue(issueModel: Issue)
        fun onSubscribeOrMute(mute: Boolean)
        fun onPinUnpinIssue()
    }

    interface IssuePrCallback<T> {
        val data: T?
    }
}