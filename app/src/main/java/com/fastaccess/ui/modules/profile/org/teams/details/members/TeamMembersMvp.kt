package com.fastaccess.ui.modules.profile.org.teams.details.members

import android.view.View
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.data.dao.model.User
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import com.fastaccess.ui.base.mvp.BaseMvp.PaginationListener
import java.util.ArrayList

/**
 * Created by Kosh on 03 Dec 2016, 3:45 PM
 */
interface TeamMembersMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener {
        fun onNotifyAdapter(items: List<User>?, page: Int)
        val loadMore: OnLoadMore<Long>
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<User>,
        PaginationListener<Long> {
        val followers: ArrayList<User>
        fun onWorkOffline(login: String)
    }
}