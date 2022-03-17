package com.fastaccess.ui.modules.pinned.gist

import com.fastaccess.data.dao.model.Gist
import com.fastaccess.ui.base.adapter.BaseViewHolder
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import java.util.ArrayList

/**
 * Created by Kosh on 25 Mar 2017, 7:57 PM
 */
interface PinnedGistMvp {
    interface View : FAView {
        fun onNotifyAdapter(items: List<Gist>?)
        fun onDeletePinnedGist(id: Long, position: Int)
    }

    interface Presenter : BaseViewHolder.OnItemClickListener<Gist> {
        val pinnedGists: ArrayList<Gist>
        fun onReload()
    }
}