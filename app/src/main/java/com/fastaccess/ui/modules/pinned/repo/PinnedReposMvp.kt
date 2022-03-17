package com.fastaccess.ui.modules.pinned.repo

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.data.dao.model.PinnedRepos
import com.fastaccess.ui.base.adapter.BaseViewHolder
import java.util.ArrayList

/**
 * Created by Kosh on 25 Mar 2017, 7:57 PM
 */
interface PinnedReposMvp {
    interface View : FAView {
        fun onNotifyAdapter(items: List<PinnedRepos>?)
        fun onDeletePinnedRepo(id: Long, position: Int)
    }

    interface Presenter : BaseViewHolder.OnItemClickListener<PinnedRepos> {
        val pinnedRepos: ArrayList<PinnedRepos>
        fun onReload()
    }
}