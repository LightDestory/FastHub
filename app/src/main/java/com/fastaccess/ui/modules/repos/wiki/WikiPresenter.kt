package com.fastaccess.ui.modules.repos.wiki

import android.content.Intent
import com.fastaccess.data.dao.wiki.FirebaseWikiConfigModel
import com.fastaccess.data.dao.wiki.WikiContentModel
import com.fastaccess.data.dao.wiki.WikiSideBarModel
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.jsoup.JsoupProvider
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import io.reactivex.Observable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import retrofit2.HttpException

/**
 * Created by Kosh on 13 Jun 2017, 8:14 PM
 */
class WikiPresenter : BasePresenter<WikiMvp.View>(), WikiMvp.Presenter {

    @com.evernote.android.state.State
    var repoId: String? = null

    @com.evernote.android.state.State
    var login: String? = null
    private var firebaseWikiConfigModel = FirebaseWikiConfigModel()

    override fun onActivityCreated(intent: Intent?) {
        if (intent != null) {
            val bundle = intent.extras!!
            repoId = bundle.getString(BundleConstant.ID)
            login = bundle.getString(BundleConstant.EXTRA)
            val page = bundle.getString(BundleConstant.EXTRA_TWO)
            if (!page.isNullOrEmpty()) {
                sendToView { it.onSetPage(page) }
            }
            if (!repoId.isNullOrEmpty() && !login.isNullOrEmpty()) {
                callApi(
                    WikiSideBarModel(
                        "Home", "$login/$repoId/wiki" +
                                if (!page.isNullOrEmpty()) "/$page" else ""
                    )
                )
            }
        }
    }

    override fun onSidebarClicked(sidebar: WikiSideBarModel) {
        callApi(sidebar)
    }

    private fun callApi(sidebar: WikiSideBarModel) {
        manageViewDisposable(RxHelper.getObservable(JsoupProvider.wiki.getWiki(sidebar.link))
            .flatMap { s -> RxHelper.getObservable(getWikiContent(s)) }
            .doOnSubscribe { sendToView { it.showProgress(0) } }
            .subscribe(
                { response -> sendToView { view -> view.onLoadContent(response) } },
                { throwable ->
                    if (throwable is HttpException) {
                        if (throwable.code() == 404) {
                            sendToView { it.showPrivateRepoError() }
                            return@subscribe
                        }
                    }
                    onError(throwable)
                },
                { sendToView { it.hideProgress() } }
            )
        )
    }

    private fun parserBarLevel(style: String): Int {
        var start = style.indexOf("padding-left:")
        if (start == -1) {
            return 1
        }
        start += "padding-left:".length
        val end = style.indexOf("px;", start)
        val px = style.substring(start, end).trim()
        val pxInt = px.toIntOrNull()
        pxInt ?: return 1
        return if (pxInt > 0) 2 else 1
    }

    private fun getWikiContent(body: String): Observable<WikiContentModel> {
        return Observable.fromPublisher { s ->
            val document: Document = Jsoup.parse(body, "")
            val wikiWrapper = document.select(firebaseWikiConfigModel.wikiWrapper)
            if (!wikiWrapper.isNullOrEmpty()) {
                val header = wikiWrapper.select(firebaseWikiConfigModel.wikiHeader).text()
                val subHeaderText = wikiWrapper.select(firebaseWikiConfigModel.wikiSubHeader).text()
                val wikiContent = wikiWrapper.select(firebaseWikiConfigModel.wikiContent)
                val wikiBody = wikiContent.select(firebaseWikiConfigModel.wikiBody).html()

                val barGroups = wikiContent.select(firebaseWikiConfigModel.sideBarGroup)
                val headerHtml =
                    "<div class='gh-header-meta'><h1>$header</h1><p>$subHeaderText</p></div>"
                val content = "$headerHtml $wikiBody"
                val barList = mutableListOf<WikiSideBarModel>()
                barGroups.forEach { barGroup ->
                    val title = barGroup.select(firebaseWikiConfigModel.sideBarGroupSummaryTitle)
                    if (title.isEmpty()) {
                        return@forEach
                    }
                    barList.add(
                        WikiSideBarModel(
                            title.text(),
                            title.attr(firebaseWikiConfigModel.sideBarHref),
                        )
                    )
                    barGroup.select(firebaseWikiConfigModel.sideBarGroupItem).forEach { item ->
                        val style = item.attr("style")
                        val level = parserBarLevel(style)
                        val barLink = item.select("a")
                        val href = barLink.attr(firebaseWikiConfigModel.sideBarHref).split("#")
                        var hash: String? = null
                        if (href.size > 1) {
                            hash = href.last()
                        }
                        barList.add(
                            WikiSideBarModel(
                                barLink.text(),
                                href.first(),
                                hash,
                                level,
                            )
                        )

                    }
                }
                s.onNext(WikiContentModel(content, null, barList))
            } else {
                s.onNext(WikiContentModel("<h2 align='center'>No Wiki</h4>", "", arrayListOf()))
            }
            s.onComplete()
        }
    }
}