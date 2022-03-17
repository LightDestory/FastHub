package com.fastaccess.ui.modules.pinned.repo

import android.view.View
import com.fastaccess.data.dao.model.AbstractPinnedRepos
import com.fastaccess.data.dao.model.PinnedRepos
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 25 Mar 2017, 8:00 PM
 */
class PinnedReposPresenter : BasePresenter<PinnedReposMvp.View>(), PinnedReposMvp.Presenter {
    override val pinnedRepos = ArrayList<PinnedRepos>()
    override fun onAttachView(view: PinnedReposMvp.View) {
        super.onAttachView(view)
        if (pinnedRepos.isEmpty()) {
            onReload()
        }
    }

    override fun onReload() {
        manageDisposable(AbstractPinnedRepos.getMyPinnedRepos()
            .subscribe({ repos ->
                sendToView { view -> view.onNotifyAdapter(repos) }
            }) {
                sendToView { view -> view.onNotifyAdapter(null) }
            })
    }

    override fun onItemClick(position: Int, v: View?, item: PinnedRepos) {
        launchUri(v!!.context, item.pinnedRepo.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: PinnedRepos) {
        if (view != null) {
            if (item.repoFullName.equals("k0shk0sh/FastHub", ignoreCase = true)) {
                return
            }
            view!!.onDeletePinnedRepo(item.id, position)
        }
    }
}