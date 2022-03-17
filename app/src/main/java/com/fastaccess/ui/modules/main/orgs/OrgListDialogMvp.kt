package com.fastaccess.ui.modules.main.orgs

import com.fastaccess.data.dao.model.User
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import java.util.ArrayList

/**
 * Created by Kosh on 15 Apr 2017, 1:53 PM
 */
interface OrgListDialogMvp {
    interface View : FAView {
        fun onNotifyAdapter(items: List<User>?)
    }

    interface Presenter {
        fun onLoadOrgs()
        val orgs: ArrayList<User>
    }
}