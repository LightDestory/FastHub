package com.fastaccess.ui.modules.profile.starred

import android.view.View
import com.fastaccess.data.dao.Pageable
import com.fastaccess.data.dao.model.Repo
import com.fastaccess.helper.RxHelper.getObservable
import com.fastaccess.provider.rest.RestProvider.getUserService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import io.reactivex.Observable

/**
 * Created by Kosh on 03 Dec 2016, 3:48 PM
 */
class ProfileStarredPresenter : BasePresenter<ProfileStarredMvp.View>(),
    ProfileStarredMvp.Presenter {
    @JvmField
    @com.evernote.android.state.State
    var starredCount = -1
    override val repos = ArrayList<Repo>()
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
        val observable: Observable<Pageable<Repo>>
        if (starredCount == -1) {
            observable = Observable.zip(
                getUserService(isEnterprise).getStarred(parameter, page),
                getUserService(isEnterprise).getStarredCount(parameter)
            ) { repoPageable, count ->
                starredCount = count.last
                repoPageable
            }
        } else {
            observable = getUserService(isEnterprise).getStarred(parameter, page)
        }
        makeRestCall(
            observable
        ) { repoModelPageable ->
            lastPage = repoModelPageable.last
            if (currentPage == 1) {
                manageDisposable(Repo.saveStarred(repoModelPageable.items!!, parameter))
            }
            sendToView { view ->
                view.onUpdateCount(starredCount)
                view.onNotifyAdapter(repoModelPageable.items, page)
            }
        }
        return true
    }

    override fun onWorkOffline(login: String) {
        if (repos.isEmpty()) {
            manageDisposable(
                getObservable(
                    Repo.getStarred(login).toObservable()
                ).subscribe { repoModels ->
                    sendToView { view ->
                        starredCount = -1
                        view.onUpdateCount(repoModels?.size ?: 0)
                        view.onNotifyAdapter(repoModels, 1)
                    }
                })
        } else {
            sendToView { it.hideProgress() }
        }
    }

    override fun onItemClick(position: Int, v: View?, item: Repo) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Repo) {}
}