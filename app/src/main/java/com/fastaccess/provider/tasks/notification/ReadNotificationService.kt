package com.fastaccess.provider.tasks.notification

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.app.NotificationCompat
import com.annimon.stream.LongStream
import com.fastaccess.R
import com.fastaccess.helper.AppHelper.cancelNotification
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler.Companion.start
import com.fastaccess.helper.InputHelper.getSafeIntId
import com.fastaccess.helper.PrefGetter.isEnterprise
import com.fastaccess.helper.PrefGetter.isMarkAsReadEnabled
import com.fastaccess.provider.rest.RestProvider.getNotificationService
import com.fastaccess.provider.scheme.SchemeParser.launchUri
import com.google.firebase.messaging.EnhancedIntentService
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Kosh on 11 Mar 2017, 12:13 AM
 */
class ReadNotificationService : EnhancedIntentService() {
    private var notification: NotificationCompat.Builder? = null
        get() {
            if (field == null) {
                field = NotificationCompat.Builder(this, "read-notification")
                    .setContentTitle(getString(R.string.marking_as_read))
                    .setSmallIcon(R.drawable.ic_sync)
                    .setProgress(0, 100, true)
            }
            return field
        }
    private var notificationManager: NotificationManager? = null
    override fun onCreate() {
        super.onCreate()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun handleIntent(p0: Intent) {
        onHandleIntent(p0)
    }

    fun onHandleIntent(intent: Intent?) {
        if (intent != null && intent.extras != null) {
            val bundle = intent.extras
            when (bundle!!.getInt(BundleConstant.EXTRA_TYPE)) {
                READ_SINGLE -> {
                    markSingleAsRead(bundle.getLong(BundleConstant.ID))
                }
                READ_ALL -> {
                    markMultiAsRead(bundle.getLongArray(BundleConstant.ID))
                }
                OPEN_NOTIFICATION -> {
                    openNotification(
                        bundle.getLong(BundleConstant.ID), bundle.getString(BundleConstant.EXTRA),
                        bundle.getBoolean(BundleConstant.YES_NO_EXTRA)
                    )
                }
                UN_SUBSCRIBE -> {
                    unSubscribeFromThread(bundle.getLong(BundleConstant.ID))
                }
            }
        }
    }

    private fun unSubscribeFromThread(id: Long) {
        val task = getNotificationService(isEnterprise)
            .unSubscribe(id)
            .doOnSubscribe { notify(id, notification!!.build()) }
            .subscribeOn(Schedulers.io())
            .flatMap {
                Observable.create<Unit> {
                    markSingleAsRead(
                        id
                    )
                }
            }
            .subscribe({ cancel(id) }) { cancel(id) }
    }

    private fun openNotification(id: Long, url: String?, readOnly: Boolean) {
        if (id > 0 && url != null) {
            cancelNotification(this, getSafeIntId(id))
            if (readOnly) {
                markSingleAsRead(id)
            } else if (!isMarkAsReadEnabled) {
                markSingleAsRead(id)
            }
            if (!readOnly) {
                launchUri(applicationContext, Uri.parse(url),
                    showRepoBtn = true,
                    newDocument = true
                )
            }
        }
    }

    private fun markMultiAsRead(ids: LongArray?) {
        if (ids != null && ids.isNotEmpty()) {
            LongStream.of(*ids).forEach { id: Long -> markSingleAsRead(id) }
        }
    }

    private fun markSingleAsRead(id: Long) {
        com.fastaccess.data.dao.model.Notification.markAsRead(id)
        val task = getNotificationService(isEnterprise)
            .markAsRead(id.toString())
            .doOnSubscribe { notify(id, notification!!.build()) }
            .subscribeOn(Schedulers.io())
            .subscribe({
                cancel(id)
            }) { cancel(id) }
    }

    private fun notify(id: Long, notification: Notification) {
        notificationManager!!.notify(getSafeIntId(id), notification)
    }

    private fun cancel(id: Long) {
        notificationManager!!.cancel(getSafeIntId(id))
    }

    companion object {
        const val READ_SINGLE = 1
        const val READ_ALL = 2
        const val OPEN_NOTIFICATION = 3
        const val UN_SUBSCRIBE = 4
        fun start(context: Context, id: Long) {
            val intent = Intent(context.applicationContext, ReadNotificationService::class.java)
            intent.putExtras(
                start()
                    .put(BundleConstant.EXTRA_TYPE, READ_SINGLE)
                    .put(BundleConstant.ID, id)
                    .end()
            )
            context.startService(intent)
        }

        @JvmOverloads
        fun start(context: Context, id: Long, url: String, onlyRead: Boolean = false): Intent {
            val intent = Intent(context.applicationContext, ReadNotificationService::class.java)
            intent.putExtras(
                start()
                    .put(BundleConstant.EXTRA_TYPE, OPEN_NOTIFICATION)
                    .put(BundleConstant.EXTRA, url)
                    .put(BundleConstant.ID, id)
                    .put(BundleConstant.YES_NO_EXTRA, onlyRead)
                    .end()
            )
            return intent
        }

        fun unSubscribe(context: Context, id: Long) {
            val intent = Intent(context.applicationContext, ReadNotificationService::class.java)
            intent.putExtras(
                start()
                    .put(BundleConstant.EXTRA_TYPE, UN_SUBSCRIBE)
                    .put(BundleConstant.ID, id)
                    .end()
            )
            context.startService(intent)
        }

        fun start(context: Context, ids: LongArray) {
            val intent = Intent(context.applicationContext, ReadNotificationService::class.java)
            intent.putExtras(
                start()
                    .put(BundleConstant.EXTRA_TYPE, READ_ALL)
                    .put(BundleConstant.ID, ids)
                    .end()
            )
            context.startService(intent)
        }
    }
}