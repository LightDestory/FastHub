package com.fastaccess.ui.modules.filter.issues

import com.fastaccess.ui.base.mvp.BaseMvp.FAView

/**
 * Created by Kosh on 09 Apr 2017, 6:19 PM
 */
interface FilterIssuesActivityMvp {
    interface View : FAView {
        fun onSetCount(count: Int, isOpen: Boolean)
    }

    interface Presenter {
        fun onStart(login: String, repoId: String)
    }
}