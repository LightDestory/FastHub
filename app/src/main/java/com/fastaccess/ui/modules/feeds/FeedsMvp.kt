package com.fastaccess.ui.modules.feeds

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.fastaccess.ui.widgets.dialog.ListDialogView.OnSimpleItemSelection
import android.os.Parcelable
import com.fastaccess.data.dao.SimpleUrlsModel
import com.fastaccess.provider.rest.loadmore.OnLoadMore
import com.fastaccess.data.dao.GitCommitModel
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.PaginationListener
import android.os.Bundle
import com.fastaccess.data.dao.model.Event

/**
 * Created by Kosh on 11 Nov 2016, 12:35 PM
 */
interface FeedsMvp {
    interface View : FAView, OnRefreshListener, android.view.View.OnClickListener,
        OnSimpleItemSelection<Parcelable> {
        fun onNotifyAdapter(events: List<Event>, page: Int)
        fun onOpenRepoChooser(models: List<SimpleUrlsModel>)
        val loadMore: OnLoadMore<String>
        fun onOpenCommitChooser(commits: List<GitCommitModel>)
    }

    interface Presenter : FAPresenter, BaseViewHolder.OnItemClickListener<Event>,
        PaginationListener<String> {
        fun onFragmentCreated(argument: Bundle)
        fun onCallApi(page: Int): Boolean
        val events: MutableList<Event>
        fun onWorkOffline()
    }
}