package com.fastaccess.provider.tasks.slack

import android.app.IntentService
import android.content.Intent
import com.fastaccess.App
import com.fastaccess.R
import com.fastaccess.data.dao.SlackInvitePostModel
import com.fastaccess.data.dao.SlackResponseModel
import com.fastaccess.data.dao.model.Login
import com.fastaccess.helper.RxHelper
import com.fastaccess.provider.rest.RestProvider
import es.dmoral.toasty.Toasty

/**
 * Created by Kosh on 01 May 2017, 1:09 AM
 */
class SlackInvitationService(name: String?) : IntentService(name) {
    override fun onHandleIntent(intent: Intent?) {
        val login = Login.getUser()
        if (login != null) {
            val body = SlackInvitePostModel()
            body.email = login.email
            body.firstName = login.name
            body.lastName = login.login
            val task = RxHelper.getObservable(
                RestProvider.slackService
                    .invite(body)
            )
                .subscribe({ response: SlackResponseModel? ->
                    if (response != null) {
                        if (response.isOk) {
                            Toasty.success(
                                App.getInstance(),
                                getString(R.string.successfully_invited)
                            ).show()
                        } else {
                            response.error?.replace("_".toRegex(), " ")?.let {
                                Toasty.info(
                                    App.getInstance(),
                                    it
                                ).show()
                            }
                        }
                    }
                }) { obj: Throwable -> obj.printStackTrace() }
        }
    }

    fun handleIntent(p0: Intent) {
        onHandleIntent(p0)
    }
}