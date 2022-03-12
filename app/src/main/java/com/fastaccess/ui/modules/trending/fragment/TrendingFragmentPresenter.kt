package com.fastaccess.ui.modules.trending.fragment

import android.view.View
import com.fastaccess.data.dao.TrendingModel
import com.fastaccess.helper.InputHelper
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.jsoup.JsoupProvider
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.repos.RepoPagerActivity
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Kosh on 30 May 2017, 11:04 PM
 */

class TrendingFragmentPresenter : BasePresenter<TrendingFragmentMvp.View>(), TrendingFragmentMvp.Presenter {

    private var disposel: Disposable? = null

    private val trendingList: ArrayList<TrendingModel> = ArrayList()

    override fun getTendingList(): ArrayList<TrendingModel> {
        return trendingList
    }

    override fun onItemLongClick(position: Int, v: View?, item: TrendingModel) {}

    override fun onItemClick(position: Int, v: View?, item: TrendingModel) {
        val split = item.title?.trim()?.split("/")!!
        v?.context!!.startActivity(RepoPagerActivity.createIntent(v.context!!, split[1].trim(), split[0].trim()))
    }

    override fun onCallApi(lang: String, since: String) {
        val l = when (lang) {
            TrendingModel.DEFAULT_LANG -> ""
            else -> lang
        }
        disposel?.let { if (!it.isDisposed) it.dispose() }
        disposel = RxHelper.getObservable(JsoupProvider.getTrendingService().getTrending(
            (if (!InputHelper.isEmpty(l)) l.replace(" ".toRegex(), "-") else "").lowercase(Locale.getDefault()), since))
                .flatMap { s -> RxHelper.getObservable(getTrendingObservable(s)) }
                .doOnSubscribe {
                    sendToView {
                        it.showProgress(0)
                        it.clearAdapter()
                    }
                }
                .subscribe({ response -> sendToView { view -> view.onNotifyAdapter(response) } },
                        { throwable -> onError(throwable) }) { sendToView { it.hideProgress() } }
        manageDisposable(disposel)
    }


    private fun getTrendingObservable(response: String): Observable<TrendingModel> {
        return Observable.fromPublisher { s ->
            val document: Document = Jsoup.parse(response, "")
            val repoList = document.select(".Box")
            if (repoList.isNotEmpty()) {
                val list: Elements? = repoList.select(".Box-row")
                list?.let { it ->
                    if (list.isNotEmpty()) {
                        it.onEach { element ->
                            val title = element.select("h1 > a").text()
                            val description = element.select("p").text()
                            val stars = element.select(".f6 > a[href*=/stargazers]").text()
                            val forks = element.select(".f6 > a[href*=/network]").text()
                            var todayStars = element.select(".f6 > span.float-right").text()
                            if (todayStars.isNullOrBlank()) {
                                todayStars = element.select(".f6 > span.float-sm-right").text()
                            }
                            var language = element.select(".f6 .mr-3 > span[itemprop=programmingLanguage]").text()
                            if (language.isNullOrBlank()) {
                                language = element.select(".f6 span[itemprop=programmingLanguage]").text()
                            }
                            s.onNext(TrendingModel(title, description, language, stars, forks, todayStars))
                        }
                    }
                }
            }
            s.onComplete()
        }
    }
}