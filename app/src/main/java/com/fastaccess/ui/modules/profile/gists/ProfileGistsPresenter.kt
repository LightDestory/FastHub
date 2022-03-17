package com.fastaccess.ui.modules.profile.gists

import android.view.View
import com.fastaccess.data.dao.Pageable
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.helper.RxHelper.getObservable
import com.fastaccess.provider.rest.RestProvider.getGistService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 11 Nov 2016, 12:36 PM
 */
class ProfileGistsPresenter : BasePresenter<ProfileGistsMvp.View>(),
    ProfileGistsMvp.Presenter {
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
        if (parameter == null) {
            throw NullPointerException("Username is null")
        }
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView { view -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0) {
            sendToView { it.hideProgress() }
            return false
        }
        makeRestCall(
            getGistService(isEnterprise).getUserGists(parameter, page)
        ) { listResponse: Pageable<Gist> ->
            lastPage = listResponse.last
            sendToView { view ->
                view.onNotifyAdapter(
                    listResponse.items,
                    page
                )
            }
            manageDisposable(Gist.save(listResponse.items!!, parameter))
        }
        return true
    }

    override fun onWorkOffline(login: String) {
        if (gists.isEmpty()) {
            manageDisposable(
                getObservable(
                    Gist.getMyGists(login).toObservable()
                ).subscribe { gistsModels1 ->
                    sendToView { view ->
                        view.onNotifyAdapter(
                            gistsModels1,
                            1
                        )
                    }
                })
        } else {
            sendToView { it.hideProgress() }
        }
    }

    override fun onItemClick(position: Int, v: View?, item: Gist) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Gist) {}
}