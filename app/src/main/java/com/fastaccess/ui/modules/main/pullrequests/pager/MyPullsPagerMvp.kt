package com.fastaccess.ui.modules.main.pullrequests.pager

import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.modules.repos.RepoPagerMvp.TabsBadgeListener

/**
 * Created by Kosh on 26 Mar 2017, 12:15 AM
 */
interface MyPullsPagerMvp {
    interface View : FAView, TabsBadgeListener
    interface Presenter
}