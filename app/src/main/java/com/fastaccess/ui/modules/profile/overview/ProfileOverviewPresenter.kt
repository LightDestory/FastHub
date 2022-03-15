package com.fastaccess.ui.modules.profile.overview

import android.graphics.Bitmap
import android.os.Bundle
import android.text.TextUtils
import com.apollographql.apollo3.rx2.Rx2Apollo
import com.fastaccess.data.dao.Pageable
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.User
import com.fastaccess.github.GetPinnedReposQuery
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.InputHelper
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.ApolloProdivder.getApollo
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.provider.rest.RestProvider.getRepoService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.widgets.contributions.ContributionsDay
import com.fastaccess.ui.widgets.contributions.ContributionsHtmlProvider
import com.fastaccess.ui.widgets.contributions.GitHubContributionsView
import io.reactivex.Observable
import retrofit2.Response


/**
 * Created by Kosh on 03 Dec 2016, 9:16 AM
 */
class ProfileOverviewPresenter : BasePresenter<ProfileOverviewMvp.View>(),
    ProfileOverviewMvp.Presenter {
    @com.evernote.android.state.State
    override var isSuccessResponse = false

    @com.evernote.android.state.State
    override var isFollowing = false

    @com.evernote.android.state.State
    override var login: String? = null
    override val orgs: MutableList<User> = mutableListOf()
    override val nodes: MutableList<GetPinnedReposQuery.Node> = mutableListOf()
    override val contributions: MutableList<ContributionsDay> = mutableListOf()
    override var readmeFetch: Boolean = false
    override fun onCheckFollowStatus(login: String) {
        if (!TextUtils.equals(login, Login.getUser().login)) {
            manageDisposable(RxHelper.getObservable(
                RestProvider.getUserService(
                    isEnterprise
                ).getFollowStatus(login)
            )
                .subscribe({ booleanResponse: Response<Boolean> ->
                    isSuccessResponse = true
                    isFollowing = booleanResponse.code() == 204
                    sendToView {
                        it?.invalidateFollowBtn()
                    }
                }) { obj: Throwable -> obj.printStackTrace() })
        }
    }

    override fun onFollowButtonClicked(login: String) {
        manageDisposable(RxHelper.getObservable(
            if (!isFollowing) RestProvider.getUserService(
                isEnterprise
            ).followUser(login) else RestProvider.getUserService(isEnterprise).unfollowUser(login)
        )
            .subscribe({ booleanResponse: Response<Boolean> ->
                if (booleanResponse.code() == 204) {
                    isFollowing = !isFollowing
                    sendToView { it?.invalidateFollowBtn() }
                }
            }) { throwable: Throwable -> onError(throwable) })
    }

    override fun onError(throwable: Throwable) {
        val statusCode = RestProvider.getErrorCode(throwable)
        if (statusCode == 404) {
            if (readmeFetch){
                readmeFetch = false
                return
            }
            sendToView { it?.onUserNotFound() }
            return
        }
        if (!InputHelper.isEmpty(login)) {
            onWorkOffline(login!!)
        }
        sendToView { it?.invalidateFollowBtn() }
        super.onError(throwable)
    }

    override fun onFragmentCreated(bundle: Bundle?) {
        if (bundle?.getString(BundleConstant.EXTRA) == null) {
            throw NullPointerException("Either bundle or User is null")
        }
        login = bundle.getString(BundleConstant.EXTRA)
        if (login != null) {
            makeRestCall(RestProvider.getUserService(
                isEnterprise
            )
                .getUser(login!!)
                .doOnComplete {
                    loadPinnedRepos(login!!)
                    loadOrgs()
                    loadProfileREADME()
                }) { userModel: User ->
                onSendUserToView(userModel)
                userModel.save(userModel)
                if (userModel.type != null && userModel.type.equals(
                        "user",
                        ignoreCase = true
                    )
                ) {
                    onCheckFollowStatus(login!!)
                }
            }
        }
    }

    private fun loadPinnedRepos(login: String) {
        val apolloCall = getApollo(isEnterprise)
            .query(GetPinnedReposQuery(login))
        val disposable =
            RxHelper.getObservable(Rx2Apollo.flowable(apolloCall).toObservable()).filter {
                !it.hasErrors()
            }.flatMap {
                if (it.data != null && it.data?.user != null) {
                    return@flatMap Observable.fromIterable(it.data?.user?.pinnedItems?.edges!!)
                }
                return@flatMap Observable.empty<GetPinnedReposQuery.Edge>()
            }.map { it.node!! }.toList()
                .toObservable()
                .subscribe(
                    { nodes1 ->
                        nodes.clear()
                        nodes.addAll(nodes1)
                        sendToView {
                            it?.onInitPinnedRepos(nodes1)
                        }
                    },
                    {
                        it.printStackTrace()
                    }
                )
        manageDisposable(disposable)
    }

    override fun onWorkOffline(login: String) {
        val userModel = User.getUser(login) ?: return
        onSendUserToView(userModel)
    }

    override fun onSendUserToView(userModel: User) {
        sendToView { it!!.onInitViews(userModel) }
    }

    override fun onLoadContributionWidget(view: GitHubContributionsView) {
        if (!isEnterprise) {
            if (contributions.isEmpty()) {
                val url = String.format(URL, login)
                manageDisposable(RxHelper.getObservable(
                    RestProvider.contribution.getContributions(url)
                )
                    .flatMap { s: String? ->
                        Observable.just(
                            ContributionsHtmlProvider.getContributions(
                                s
                            )
                        )
                    }
                    .subscribe({ lists: List<ContributionsDay> ->
                        contributions.clear()
                        contributions.addAll(lists)
                        loadContributions(contributions, view)
                    }) { obj: Throwable -> obj.printStackTrace() })
            } else {
                loadContributions(contributions, view)
            }
        }
    }

    private fun loadContributions(
        contributions: MutableList<ContributionsDay>,
        gitHubContributionsView: GitHubContributionsView
    ) {
        val filter = gitHubContributionsView.getLastContributions(contributions)
        if (contributions.isNotEmpty()) {
            val bitmapObservable =
                Observable.just(gitHubContributionsView.drawOnCanvas(filter, contributions))
            manageObservable(bitmapObservable
                .doOnNext { bitmap: Bitmap? ->
                    sendToView { view ->
                        view?.onInitContributions(
                            bitmap != null
                        )
                    }
                })
        }
    }

    private fun loadOrgs() {
        val isMe = login.equals(
            if (Login.getUser() != null) Login.getUser().login else "",
            ignoreCase = true
        )
        manageDisposable(RxHelper.getObservable(
            if (isMe) RestProvider.getOrgService(
                isEnterprise
            ).myOrganizations else RestProvider.getOrgService(isEnterprise).getMyOrganizations(
                login!!
            )
        )
            .subscribe({ response: Pageable<User>? ->
                if (response?.items != null) {
                    orgs.addAll(response.items!!)
                }
                sendToView { view ->
                    view?.onInitOrgs(
                        orgs
                    )
                }
            }) { obj: Throwable -> obj.printStackTrace() })
    }

    private fun loadProfileREADME() {
        readmeFetch = true
        val isMe = login.equals(
            if (Login.getUser() != null) Login.getUser().login else "",
            ignoreCase = true
        )
        var user = login
        if (isMe) {
            user = Login.getUser().login
        }
        val url = String.format("https://api.github.com/repos/%s/%s/readme", user, user)
        val htmlUrl = String.format("https://github.com/%s%s", user, user)
        val observable = getRepoService(isEnterprise).getReadmeHtml(url)
        makeRestCall(
            observable
        ) { content: String? ->
            sendToView { view: ProfileOverviewMvp.View ->
                view.onSetMdText(
                    content!!, htmlUrl, false
                )
            }
        }
    }

    companion object {
        private const val URL = "https://github.com/users/%s/contributions"
    }
}