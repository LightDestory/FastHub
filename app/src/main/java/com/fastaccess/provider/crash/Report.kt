package com.fastaccess.provider.crash

import android.content.Context
import timber.log.Timber

class Report {
    companion object {
        fun init(context: Context) {
        }

        fun reportCatchException(e: Exception) {
            Timber.e(e, "FastHub-Re Crash Report")
        }
    }
}