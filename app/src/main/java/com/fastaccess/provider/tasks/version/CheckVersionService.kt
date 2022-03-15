package com.fastaccess.provider.tasks.version

import android.content.Intent
import android.widget.Toast
import com.fastaccess.App
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.RestProvider
import com.google.firebase.messaging.EnhancedIntentService

/**
 * Created by Kosh on 09 Jun 2017, 9:02 PM
 */
class CheckVersionService : EnhancedIntentService() {
    override fun handleIntent(p0: Intent) {
        val d = RxHelper.getObservable(RestProvider.getRepoService(false)
                .getLatestRelease("LightDestory", "FastHub-RE"))
                .subscribe({ t ->
                    t?.let {
                        Toast.makeText(App.getInstance(), if (BuildConfig.VERSION_NAME.contains(it.tagName))
                            R.string.up_to_date else R.string.new_version, Toast.LENGTH_LONG).show()
                    }
                }, { throwable -> throwable.printStackTrace() })
    }
}