package com.fastaccess.ui.modules.profile.org.teams.details.repos

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.data.dao.model.Repo
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import com.fastaccess.ui.base.mvp.BaseMvp.PaginationListener
import java.util.ArrayList

/**
 * Created by Kosh on 03 Dec 2016, 3:45 PM
 */
interface TeamReposMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener {
        fun onNotifyAdapter(items: List<Repo>?, page: Int)
        val loadMore: OnLoadMore<Long>
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<Repo>,
        PaginationListener<Long> {
        val repos: ArrayList<Repo>
        fun onWorkOffline(login: String)
    }
}