package com.fastaccess.provider.tasks.notification

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.annimon.stream.Stream
import com.fastaccess.R
import com.fastaccess.data.dao.Pageable
import com.fastaccess.data.dao.model.Comment
import com.fastaccess.data.dao.model.Login
import com.fastaccess.data.dao.model.Notification
import com.fastaccess.data.dao.model.NotificationQueue
import com.fastaccess.helper.AppHelper
import com.fastaccess.helper.InputHelper
import com.fastaccess.helper.ParseDateFormat
import com.fastaccess.helper.PrefGetter
import com.fastaccess.provider.markdown.MarkDownProvider
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.ui.modules.notification.NotificationActivity
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit


/**
 * Created by Kosh on 19 Feb 2017, 6:32 PM
 */
class NotificationSchedulerJobTask : JobService() {
    override fun onStartJob(job: JobParameters): Boolean {
        if (!(SINGLE_JOB_ID == job.jobId)) {
            if (PrefGetter.notificationTaskDuration == -1) {
                scheduleJob(this, -1, false)
                finishJob(job)
                return true
            }
        }
        var login: Login? = null
        try {
            login = Login.getUser()
        } catch (ignored: Exception) {
        }
        if (login != null) {
            val task = RestProvider.getNotificationService(PrefGetter.isEnterprise)
                .getNotifications(ParseDateFormat.lastWeekDate)
                .subscribeOn(Schedulers.io())
                .subscribe({ item: Pageable<Notification>? ->
                    AppHelper.cancelAllNotifications(
                        applicationContext
                    )
                    if (item != null) {
                        onSave(item.items, job)
                    } else {
                        finishJob(job)
                    }
                }) { jobFinished(job, true) }
        }
        return true
    }

    override fun onStopJob(jobParameters: JobParameters): Boolean {
        return false
    }

    private fun onSave(notificationThreadModels: List<Notification>?, job: JobParameters) {
        if (notificationThreadModels != null) {
            Notification.save(notificationThreadModels)
            onNotifyUser(notificationThreadModels, job)
        }
    }

    private fun onNotifyUser(notificationThreadModels: List<Notification>, job: JobParameters) {
        val count = Stream.of(notificationThreadModels)
            .filter { obj: Notification -> obj.isUnread }
            .count()
        if (count == 0L) {
            AppHelper.cancelAllNotifications(applicationContext)
            finishJob(job)
            return
        }
        val context = applicationContext
        val accentColor = ContextCompat.getColor(this, R.color.material_blue_700)
        val first = notificationThreadModels[0]
        val task = Observable.fromIterable(notificationThreadModels)
            .subscribeOn(Schedulers.io())
            .filter { notification: Notification ->
                notification.isUnread && first.id != notification.id && !NotificationQueue.exists(
                    notification.id
                )
            }
            .take(10)
            .flatMap({ notification: Notification ->
                if (notification.subject != null && notification.subject.latestCommentUrl != null) {
                    return@flatMap RestProvider.getNotificationService(PrefGetter.isEnterprise)
                        .getComment(notification.subject.latestCommentUrl!!)
                        .subscribeOn(Schedulers.io())
                } else {
                    return@flatMap Observable.empty<Comment>()
                }
            }) { thread: Notification, comment: Comment? ->
                val customNotificationModel = CustomNotificationModel()
                val url: String
                if (comment != null && comment.user != null) {
                    url = comment.user.avatarUrl
                    if (!InputHelper.isEmpty(thread.subject.latestCommentUrl)) {
                        customNotificationModel.comment = comment
                        customNotificationModel.url = url
                    }
                }
                customNotificationModel.notification = thread
                customNotificationModel
            }
            .subscribeOn(Schedulers.io())
            .subscribe({ custom: CustomNotificationModel ->
                if (custom.comment != null) {
                    getNotificationWithComment(
                        context,
                        accentColor,
                        custom.notification,
                        custom.comment,
                        custom.url
                    )
                } else {
                    showNotificationWithoutComment(
                        context,
                        accentColor,
                        custom.notification,
                        custom.url
                    )
                }
            }, { finishJob(job) }) {
                if (!NotificationQueue.exists(first.id)) {
                    val grouped = getSummaryGroupNotification(
                        first,
                        accentColor,
                        notificationThreadModels.size > 1
                    )
                    showNotification(first.id, grouped)
                }
                val task2 = NotificationQueue.put(notificationThreadModels)
                    .subscribe(
                        { },
                        { obj: Throwable -> obj.printStackTrace() }) { finishJob(job) }
            }
    }

    private fun finishJob(job: JobParameters) {
        jobFinished(job, false)
    }

    private fun showNotificationWithoutComment(
        context: Context,
        accentColor: Int,
        thread: Notification?,
        iconUrl: String?
    ) {
        withoutComments(thread, context, accentColor)
    }

    private fun withoutComments(thread: Notification?, context: Context, accentColor: Int) {
        @SuppressLint("LaunchActivityFromNotification") val toAdd = getNotification(
            thread!!.subject!!.title!!, thread.repository.fullName,
            if (thread.repository != null) thread.repository.fullName else "general"
        )
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setContentIntent(
                getPendingIntent(
                    thread.id, thread.subject!!.url!!
                )
            )
            .addAction(
                R.drawable.ic_github, context.getString(R.string.open), getPendingIntent(
                    thread.id, thread
                        .subject.url!!
                )
            )
            .addAction(
                R.drawable.ic_eye_off,
                context.getString(R.string.mark_as_read),
                getReadOnlyPendingIntent(
                    thread.id, thread
                        .subject.url!!
                )
            )
            .setWhen(if (thread.updatedAt != null) thread.updatedAt.time else System.currentTimeMillis())
            .setShowWhen(true)
            .setColor(accentColor)
            .setGroup(NOTIFICATION_GROUP_ID)
            .build()
        showNotification(thread.id, toAdd)
    }

    private fun getNotificationWithComment(
        context: Context,
        accentColor: Int,
        thread: Notification?,
        comment: Comment?,
        url: String?
    ) {
        withComments(comment, context, thread, accentColor)
    }

    private fun withComments(
        comment: Comment?,
        context: Context,
        thread: Notification?,
        accentColor: Int
    ) {
        @SuppressLint("LaunchActivityFromNotification") val toAdd = getNotification(
            if (comment?.user != null) comment.user.login else "",
            MarkDownProvider.stripMdText(comment?.body),
            if (thread?.repository != null) thread.repository.fullName else "general"
        )
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setSmallIcon(R.drawable.ic_notification)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .setBigContentTitle(if (comment?.user != null) comment.user.login else "")
                    .bigText(MarkDownProvider.stripMdText(comment?.body))
            )
            .setWhen(comment?.createdAt?.time!!)
            .setShowWhen(true)
            .addAction(
                R.drawable.ic_github, context.getString(R.string.open), getPendingIntent(
                    thread?.id!!,
                    thread.subject?.url!!
                )
            )
            .addAction(
                R.drawable.ic_eye_off,
                context.getString(R.string.mark_as_read),
                getReadOnlyPendingIntent(
                    thread.id,
                    thread.subject.url!!
                )
            )
            .setContentIntent(getPendingIntent(thread.id, thread.subject.url!!))
            .setColor(accentColor)
            .setGroup(NOTIFICATION_GROUP_ID)
            .build()
        showNotification(thread.id, toAdd)
    }

    private fun getSummaryGroupNotification(
        thread: Notification,
        accentColor: Int,
        toNotificationActivity: Boolean
    ): android.app.Notification {
        @SuppressLint("UnspecifiedImmutableFlag") val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            0,
            Intent(applicationContext, NotificationActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val builder = getNotification(
            thread.subject.title!!, thread.repository.fullName,
            if (thread.repository != null) thread.repository.fullName else "general"
        )
            .setContentIntent(
                if (toNotificationActivity) pendingIntent else getPendingIntent(
                    thread.id,
                    thread.subject.url!!
                )
            )
            .addAction(
                R.drawable.ic_github, getString(R.string.open), getPendingIntent(
                    thread.id, thread.subject.url!!
                )
            )
            .addAction(
                R.drawable.ic_eye_off, getString(R.string.mark_as_read), getReadOnlyPendingIntent(
                    thread.id, thread
                        .subject.url!!
                )
            )
            .setWhen(if (thread.updatedAt != null) thread.updatedAt.time else System.currentTimeMillis())
            .setShowWhen(true)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(accentColor)
            .setGroup(NOTIFICATION_GROUP_ID)
            .setGroupSummary(true)
        if (PrefGetter.isNotificationSoundEnabled) {
            builder.setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSound(PrefGetter.notificationSound, AudioManager.STREAM_NOTIFICATION)
        }
        return builder.build()
    }

    private fun getNotification(
        title: String,
        message: String,
        channelName: String
    ): NotificationCompat.Builder {
        return NotificationCompat.Builder(this, channelName)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
    }

    private fun showNotification(id: Long, notification: android.app.Notification) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notification.channelId,
                notification.channelId, NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(InputHelper.getSafeIntId(id), notification)
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getReadOnlyPendingIntent(id: Long, url: String): PendingIntent {
        val intent = ReadNotificationService.start(applicationContext, id, url, true)
        return PendingIntent.getService(
            applicationContext, InputHelper.getSafeIntId(id) / 2, intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getPendingIntent(id: Long, url: String): PendingIntent {
        val intent = ReadNotificationService.start(applicationContext, id, url)
        return PendingIntent.getService(
            applicationContext, InputHelper.getSafeIntId(id), intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    private class CustomNotificationModel {
        var url: String? = null
        var notification: Notification? = null
        var comment: Comment? = null
    }

    companion object {
        private const val JOB_ID = 1
        private const val SINGLE_JOB_ID = 2
        private const val THIRTY_MINUTES = 30 * 60
        private const val NOTIFICATION_GROUP_ID = "FastHub"
        @JvmStatic
        fun scheduleJob(context: Context) {
            val duration = PrefGetter.notificationTaskDuration
            scheduleJob(context, duration, false)
        }

        @JvmStatic
        fun scheduleJob(context: Context, duration: Int, cancel: Boolean) {
            val jobScheduler = context.getSystemService(JOB_SCHEDULER_SERVICE) as JobScheduler
            jobScheduler.cancel(SINGLE_JOB_ID)
            if (cancel) jobScheduler.cancel(JOB_ID)
            if (duration == -1) {
                jobScheduler.cancel(JOB_ID)
                return
            }
            val duration1 = if (duration <= 0) THIRTY_MINUTES else duration
            val builder = JobInfo.Builder(
                JOB_ID, ComponentName(
                    context.packageName,
                    NotificationSchedulerJobTask::class.java.name
                )
            )
                .setBackoffCriteria(
                    JobInfo.DEFAULT_INITIAL_BACKOFF_MILLIS,
                    JobInfo.BACKOFF_POLICY_LINEAR
                )
                .setPersisted(true)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(TimeUnit.SECONDS.toMillis(duration1.toLong()))

            jobScheduler.schedule(builder.build())
        }
    }
}