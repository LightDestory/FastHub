package com.fastaccess.ui.modules.pinned.pullrequest

import com.fastaccess.data.dao.model.PullRequest
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import java.util.ArrayList

/**
 * Created by Kosh on 25 Mar 2017, 7:57 PM
 */
interface PinnedPullRequestMvp {
    interface View : FAView {
        fun onNotifyAdapter(items: List<PullRequest>?)
        fun onDeletePinnedPullRequest(id: Long, position: Int)
    }

    interface Presenter : BaseViewHolder.OnItemClickListener<PullRequest> {
        val pinnedPullRequest: ArrayList<PullRequest>
        fun onReload()
    }
}