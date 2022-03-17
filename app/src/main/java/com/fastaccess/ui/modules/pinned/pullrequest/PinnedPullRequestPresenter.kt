package com.fastaccess.ui.modules.pinned.pullrequest

import android.view.View
import com.fastaccess.data.dao.model.PinnedPullRequests
import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 25 Mar 2017, 8:00 PM
 */
class PinnedPullRequestPresenter : BasePresenter<PinnedPullRequestMvp.View>(),
    PinnedPullRequestMvp.Presenter {
    override val pinnedPullRequest = ArrayList<PullRequest>()
    override fun onAttachView(view: PinnedPullRequestMvp.View) {
        super.onAttachView(view)
        if (pinnedPullRequest.isEmpty()) {
            onReload()
        }
    }

    override fun onReload() {
        manageDisposable(PinnedPullRequests.getMyPinnedPullRequests()
            .subscribe({ repos ->
                sendToView { view -> view.onNotifyAdapter(repos) }
            }) {
                sendToView { view -> view.onNotifyAdapter(null) }
            })
    }

    override fun onItemClick(position: Int, v: View?, item: PullRequest) {
        launchUri(v!!.context, if (!isEmpty(item.htmlUrl)) item.htmlUrl else item.url)
    }

    override fun onItemLongClick(position: Int, v: View?, item: PullRequest) {
        if (view != null) {
            view!!.onDeletePinnedPullRequest(item.id, position)
        }
    }
}