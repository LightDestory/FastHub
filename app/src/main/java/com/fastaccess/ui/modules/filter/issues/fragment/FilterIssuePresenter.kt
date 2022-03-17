package com.fastaccess.ui.modules.filter.issues.fragment

import android.view.View
import com.fastaccess.data.dao.model.Issue
import com.fastaccess.provider.rest.RestProvider.getSearchService
import com.fastaccess.ui.base.mvp.presenter.BasePresenter

/**
 * Created by Kosh on 09 Apr 2017, 7:10 PM
 */
class FilterIssuePresenter : BasePresenter<FilterIssuesMvp.View>(), FilterIssuesMvp.Presenter {
    override val issues = ArrayList<Issue>()
    override var currentPage = 0
    override var previousTotal = 0
    private var lastPage = Int.MAX_VALUE
    override fun onItemClick(position: Int, v: View?, item: Issue) {
        if (view != null) {
            view!!.onItemClicked(item)
        }
    }

    override fun onItemLongClick(position: Int, v: View?, item: Issue) {
        if (view != null) view!!.onShowPopupDetails(item)
    }

    override fun onCallApi(page: Int, parameter: String?): Boolean {
        if (page == 1 || parameter == null) {
            lastPage = Int.MAX_VALUE
            sendToView { view -> view.loadMore.reset() }
        }
        if (page > lastPage || lastPage == 0 || parameter == null) {
            sendToView { it.hideProgress() }
            return false
        }
        makeRestCall(getSearchService(isEnterprise).searchIssues(parameter, page.toLong())
        ) { issues ->
            lastPage = issues.last
            if (currentPage == 1) {
                sendToView { view -> view.onSetCount(issues.totalCount) }
            }
            sendToView { view ->
                view.onNotifyAdapter(
                    issues.items,
                    page
                )
            }
        }
        return true
    }
}