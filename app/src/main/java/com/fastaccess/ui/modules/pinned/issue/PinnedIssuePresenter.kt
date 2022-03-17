package com.fastaccess.ui.modules.pinned.issue

import android.view.View
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.data.dao.model.PinnedIssues
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 25 Mar 2017, 8:00 PM
 */
class PinnedIssuePresenter : BasePresenter<PinnedIssueMvp.View>(), PinnedIssueMvp.Presenter {
    override val pinnedIssue = ArrayList<Issue>()
    override fun onAttachView(view: PinnedIssueMvp.View) {
        super.onAttachView(view)
        if (pinnedIssue.isEmpty()) {
            onReload()
        }
    }

    override fun onReload() {
        manageDisposable(PinnedIssues.getMyPinnedIssues()
            .subscribe({ repos ->
                sendToView { view: PinnedIssueMvp.View -> view.onNotifyAdapter(repos) }
            }) {
                sendToView { view: PinnedIssueMvp.View -> view.onNotifyAdapter(null) }
            })
    }

    override fun onItemClick(position: Int, v: View?, item: Issue) {
        launchUri(v!!.context, if (!isEmpty(item.htmlUrl)) item.htmlUrl else item.url)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Issue) {
        if (view != null) {
            view!!.onDeletePinnedIssue(item.id, position)
        }
    }
}