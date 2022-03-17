package com.fastaccess.ui.modules.gists.starred

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.data.dao.model.Gist
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.*

/**
 * Created by Kosh on 11 Nov 2016, 12:35 PM
 */
interface StarredGistsMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener {
        fun onNotifyAdapter(items: List<Gist>?, page: Int)
        val loadMore: OnLoadMore<String>
        fun onStartGistView(gistId: String)
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<Gist>,
        PaginationListener<String> {
        val gists: ArrayList<Gist>
        fun onWorkOffline(login: String)
    }
}