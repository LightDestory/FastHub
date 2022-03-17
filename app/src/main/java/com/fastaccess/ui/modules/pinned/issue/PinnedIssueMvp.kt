package com.fastaccess.ui.modules.pinned.issue

import com.fastaccess.data.dao.model.Issue
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import java.util.ArrayList

/**
 * Created by Kosh on 25 Mar 2017, 7:57 PM
 */
interface PinnedIssueMvp {
    interface View : FAView {
        fun onNotifyAdapter(items: List<Issue>?)
        fun onDeletePinnedIssue(id: Long, position: Int)
    }

    interface Presenter : BaseViewHolder.OnItemClickListener<Issue> {
        val pinnedIssue: ArrayList<Issue>
        fun onReload()
    }
}