package com.fastaccess.ui.modules.profile.overview

import android.os.Bundle
import com.fastaccess.data.dao.model.User
import com.fastaccess.github.GetPinnedReposQuery
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.widgets.contributions.ContributionsDay
import com.fastaccess.ui.widgets.contributions.GitHubContributionsView

/**
 * Created by Kosh on 03 Dec 2016, 9:15 AM
 */
interface ProfileOverviewMvp {
    interface View : FAView {
        fun onInitViews(userModel: User?)
        fun invalidateFollowBtn()
        fun onInitContributions(show: Boolean)
        fun onInitOrgs(orgs: List<User>)
        fun onUserNotFound()
        fun onInitPinnedRepos(nodes: List<GetPinnedReposQuery.Node>)
        fun onSetMdText(text: String, baseUrl: String, replace: Boolean)
    }

    interface Presenter : FAPresenter {
        fun onFragmentCreated(bundle: Bundle?)
        fun onWorkOffline(login: String)
        fun onCheckFollowStatus(login: String)
        val isSuccessResponse: Boolean
        val isFollowing: Boolean
        fun onFollowButtonClicked(login: String)
        fun onSendUserToView(userModel: User)
        fun onLoadContributionWidget(view: GitHubContributionsView)
        val orgs: MutableList<User>
        val contributions: MutableList<ContributionsDay>
        val nodes: List<GetPinnedReposQuery.Node>
        val login: String?
        var readmeFetch: Boolean
    }
}