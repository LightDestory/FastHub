package com.fastaccess.ui.modules.profile

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.base.mvp.BaseMvp.FAPresenter

/**
 * Created by Kosh on 03 Dec 2016, 7:59 AM
 */
interface ProfilePagerMvp {
    interface View : FAView {
        fun onNavigateToFollowers()
        fun onNavigateToFollowing()
        fun onCheckType(isOrg: Boolean)
    }

    interface Presenter : FAPresenter
}