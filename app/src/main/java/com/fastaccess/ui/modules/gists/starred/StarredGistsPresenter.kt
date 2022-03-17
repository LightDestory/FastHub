package com.fastaccess.ui.modules.gists.starred

import android.view.View
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.provider.rest.RestProvider.getGistService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 11 Nov 2016, 12:36 PM
 */
class StarredGistsPresenter : BasePresenter<StarredGistsMvp.View>(),
    StarredGistsMvp.Presenter {
    override val gists = ArrayList<Gist>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE
    override fun onError(throwable: Throwable) {
        sendToView { view ->
            if (view.loadMore.parameter != null) {
                onWorkOffline(view.loadMore.parameter!!)
            }
        }
        super.onError(throwable)
    }

    override fun onCallApi(page: Int, parameter: String?): Boolean {
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view: StarredGistsMvp.View -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        makeRestCall(getGistService(isEnterprise).getStarredGists(page)
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

    override fun onWorkOffline(login: String) {} // do nothing for now.
    override fun onItemClick(position: Int, v: View?, item: Gist) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Gist) {}
}