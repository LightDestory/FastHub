package com.fastaccess.ui.modules.search.issues

import android.view.View
import com.fastaccess.R
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.provider.rest.RestProvider.getSearchService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import net.grandcentrix.thirtyinch.ViewAction

/**
 * Created by Kosh on 03 Dec 2016, 3:48 PM
 */
class SearchIssuesPresenter : BasePresenter<SearchIssuesMvp.View>(),
    SearchIssuesMvp.Presenter {
    override val issues = ArrayList<Issue>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE
    override fun onCallApi(page: Int, parameter: String?): Boolean {
        if (page == 1) {
            lastPage = Int.MAX_VALUE
            sendToView(ViewAction { view: SearchIssuesMvp.View -> view.loadMore.reset() })
        }
        if (page > lastPage || lastPage == 0 || parameter == null) {
            sendToView(ViewAction { it.hideProgress() })
            return false
        }
        makeRestCall(getSearchService(isEnterprise).searchIssues(parameter, page.toLong())
        ) { response ->
            lastPage = response.last
            sendToView { view ->
                view.onNotifyAdapter(
                    if (response.incompleteResults) null else response.items,
                    page
                )
                if (!response.incompleteResults) {
                    view.onSetTabCount(response.totalCount)
                } else {
                    view.onSetTabCount(0)
                    view.showMessage(R.string.error, R.string.search_results_warning)
                }
            }
        }
        return true
    }

    override fun onItemClick(position: Int, v: View?, item: Issue) {
        launchUri(v!!.context, item.htmlUrl)
    }

    override fun onItemLongClick(position: Int, v: View?, item: Issue) {
        if (view != null) view!!.onShowPopupDetails(item)
    }
}