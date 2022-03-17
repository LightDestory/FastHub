package com.fastaccess.ui.modules.gists.gist

import android.content.Intent
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.PinnedGists
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.RestProvider.getGistService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import net.grandcentrix.thirtyinch.ViewAction
import retrofit2.Response

/**
 * Created by Kosh on 12 Nov 2016, 12:17 PM
 */
class GistPresenter : BasePresenter<GistMvp.View>(), GistMvp.Presenter {
    @com.evernote.android.state.State
    override var isStarred = false

    @com.evernote.android.state.State
    override var isForked = false

    @com.evernote.android.state.State
    override var gist: Gist? = null

    @com.evernote.android.state.State
    var gistId: String? = null
    override fun gistId(): String {
        return gistId!!
    }

    override fun onActivityCreated(intent: Intent?) {
        if (intent == null || intent.extras == null) {
            return
        }
        val bundle = intent.extras
        gistId = bundle!!.getString(BundleConstant.EXTRA)
        when {
            gist != null -> {
                checkStarring(gist!!.gistId)
                sendToView { it.onSetupDetails() }
            }
            gistId != null -> {
                callApi()
            }
            else -> {
                sendToView { it.onSetupDetails() }
            }
        }
    }

    override fun onDeleteGist() {
        if (gist == null) return
        manageDisposable(RxHelper.getObservable(
            getGistService(isEnterprise).deleteGist(
                gist!!.gistId
            )
        )
            .doOnSubscribe { onSubscribed(false) }
            .doOnNext { booleanResponse: Response<Boolean> ->
                if (booleanResponse.code() == 204) {
                    sendToView { it.onSuccessDeleted() }
                } else {
                    sendToView { it.onErrorDeleting() }
                }
            }
            .subscribe({ }) { throwable: Throwable ->
                sendToView { view: GistMvp.View ->
                    view.showErrorMessage(
                        throwable.message!!
                    )
                }
            })
    }

    override val isOwner: Boolean
        get() = gist != null && gist!!.owner != null && gist!!.owner.login == Login.getUser().login

    override fun onStarGist() {
        isStarred = !isStarred
        sendToView { view: GistMvp.View ->
            view.onGistStarred(
                isStarred
            )
        }
    }

    override fun onForkGist() {
        isForked = !isForked
        sendToView { view ->
            view.onGistForked(
                isForked
            )
        }
    }

    override fun checkStarring(gistId: String) {
        makeRestCall(getGistService(isEnterprise).checkGistStar(gistId)
        ) { booleanResponse ->
            isStarred = booleanResponse.code() == 204
            sendToView { view: GistMvp.View ->
                view.onGistStarred(
                    isStarred
                )
            }
        }
    }

    override fun onWorkOffline(gistId: String) {
        if (gist == null) {
            manageDisposable(RxHelper.getObservable(Gist.getGist(gistId))
                .subscribe { gistsModel ->
                    gist = gistsModel
                    sendToView { it.onSetupDetails() }
                })
        }
    }

    override fun onPinUnpinGist() {
        if (gist == null) return
        PinnedGists.pinUpin(gist!!)
        sendToView { view: GistMvp.View ->
            view.onUpdatePinIcon(
                gist!!
            )
        }
    }

    override fun callApi() {
        if (!isEmpty(gistId)) {
            checkStarring(gistId!!)
            makeRestCall(
                getGistService(isEnterprise).getGist(
                    gistId!!
                )
            ) { gistsModel: Gist? ->
                gist = gistsModel
                sendToView { it.onSetupDetails() }
            }
        }
    }
}