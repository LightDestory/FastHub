package com.fastaccess.ui.modules.pinned.gist

import android.view.View
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.data.dao.model.PinnedGists
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 25 Mar 2017, 8:00 PM
 */
class PinnedGistPresenter : BasePresenter<PinnedGistMvp.View>(), PinnedGistMvp.Presenter {
    override val pinnedGists = ArrayList<Gist>()
    override fun onAttachView(view: PinnedGistMvp.View) {
        super.onAttachView(view)
        if (pinnedGists.isEmpty()) {
            onReload()
        }
    }

    override fun onReload() {
        manageDisposable(PinnedGists.getMyPinnedGists()
            .subscribe({ repos ->
                sendToView { view-> view.onNotifyAdapter(repos) }
            }) {
                sendToView { view -> view.onNotifyAdapter(null) }
            })
    }

    override fun onItemClick(position: Int, v: View?, item: Gist) {
        launchUri(v!!.context, if (!isEmpty(item.htmlUrl)) item.htmlUrl else item.url)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Gist) {
        if (view != null) {
            view!!.onDeletePinnedGist(item.id, position)
        }
    }
}