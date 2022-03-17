package com.fastaccess.ui.adapter.callback

/**
 * Created by Kosh on 03 Apr 2017, 2:52 PM
 */
interface ReactionsCallback {
    fun isPreviouslyReacted(id: Long, vId: Int): Boolean
    fun isCallingApi(id: Long, vId: Int): Boolean
}