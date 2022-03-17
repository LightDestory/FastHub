package com.fastaccess.ui.modules.gists

import android.view.View
import com.fastaccess.data.dao.Pageable
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.provider.rest.RestProvider.getGistService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.gists.gist.GistActivity.Companion.createIntent
import io.reactivex.functions.Consumer
import net.grandcentrix.thirtyinch.ViewAction

/**
 * Created by Kosh on 11 Nov 2016, 12:36 PM
 */
class GistsPresenter : BasePresenter<GistsMvp.View>(), GistsMvp.Presenter {
    override val gists = ArrayList<Gist>()
    override var currentPage = 0
    override var previousTotal = 0

    private var lastPage = Int.MAX_VALUE
    override fun onError(throwable: Throwable) {
        onWorkOffline()
        super.onError(throwable)
    }

    override fun onCallApi(page: Int, parameter: Gist?): Boolean {
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        makeRestCall(getGistService(isEnterprise).getPublicGists(RestProvider.PAGE_SIZE, page)
        ) { listResponse ->
            lastPage = listResponse.last
            sendToView { view ->
                view.onNotifyAdapter(
                    listResponse.items,
                    page
                )
            }
        }
        return true
    }

    override fun onWorkOffline() {
        if (gists.isEmpty()) {
            manageDisposable(RxHelper.getObservable(Gist.getGists().toObservable())
                .subscribe { gists ->
                    sendToView { view: GistsMvp.View -> view.onNotifyAdapter(gists, 1) }
                })
        } else {
            sendToView { it.hideProgress() }
        }
    }

    override fun onItemClick(position: Int, v: View?, item: Gist) {
        v!!.context.startActivity(createIntent(v.context, item.gistId, isEnterprise))
    }

    override fun onItemLongClick(position: Int, v: View?, item: Gist) {}
}