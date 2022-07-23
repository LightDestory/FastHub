package com.fastaccess.ui.modules.profile.org.repos

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.data.entity.Repo
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.*

/**
 * Created by Kosh on 03 Dec 2016, 3:45 PM
 */
interface OrgReposMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener {
        fun onNotifyAdapter(items: List<Repo>?, page: Int)
        val loadMore: OnLoadMore<String>
        fun onRepoFilterClicked()
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<Repo>,
        PaginationListener<String> {
        val repos: ArrayList<Repo>
        fun onWorkOffline(login: String)
        fun onFilterApply(org: String?)
        fun onTypeSelected(selectedType: String?)
    }
}