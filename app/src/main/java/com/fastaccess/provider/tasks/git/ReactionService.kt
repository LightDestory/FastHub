package com.fastaccess.provider.tasks.git

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.fastaccess.R
import com.fastaccess.data.dao.PostReactionModel
import com.fastaccess.data.dao.types.ReactionTypes
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler.Companion.start
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.RxHelper.safeObservable
import com.fastaccess.provider.rest.RestProvider.getReactionsService
import com.google.firebase.messaging.EnhancedIntentService

/**
 * Created by Kosh on 29 Mar 2017, 9:59 PM
 */
class ReactionService : EnhancedIntentService() {
    private var notification: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null
        get() {
            if (field == null) {
                field = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }

    fun onHandleIntent(intent: Intent?) {
        if (intent != null && intent.extras != null) {
            val bundle = intent.extras
            val reactionType = bundle!!.getSerializable(BundleConstant.EXTRA_TYPE) as ReactionTypes?
            val isCommit = bundle.getBoolean(BundleConstant.EXTRA)
            val login = bundle.getString(BundleConstant.EXTRA_TWO)
            val repo = bundle.getString(BundleConstant.EXTRA_THREE)
            val commentId = bundle.getLong(BundleConstant.ID)
            val isEnterprise = bundle.getBoolean(BundleConstant.IS_ENTERPRISE)
            if (isEmpty(login) || isEmpty(repo) || reactionType == null) {
                stopSelf()
                return
            }
            if (isCommit) {
                postCommit(reactionType, login!!, repo!!, commentId, isEnterprise)
            } else {
                post(reactionType, login!!, repo!!, commentId, isEnterprise)
            }
        }
    }

    private fun post(
        reactionType: ReactionTypes,
        login: String,
        repo: String,
        commentId: Long,
        isEnterprise: Boolean
    ) {
        val task = safeObservable(
            getReactionsService(isEnterprise)
                .postIssueCommentReaction(
                    PostReactionModel(reactionType.content),
                    login,
                    repo,
                    commentId
                )
        )
            .doOnSubscribe {
                showNotification(
                    getNotification(
                        reactionType
                    ), commentId.toInt()
                )
            }
            .subscribe({
                hideNotification(
                    commentId.toInt()
                )
            }) {
                hideNotification(
                    commentId.toInt()
                )
            }
    }

    private fun postCommit(
        reactionType: ReactionTypes,
        login: String,
        repo: String,
        commentId: Long,
        isEnterprise: Boolean
    ) {
        val task = safeObservable(
            getReactionsService(isEnterprise)
                .postCommitReaction(PostReactionModel(reactionType.content), login, repo, commentId)
        )
            .doOnSubscribe {
                showNotification(
                    getNotification(
                        reactionType
                    ), commentId.toInt()
                )
            }
            .subscribe({
                hideNotification(
                    commentId.toInt()
                )
            }) {
                hideNotification(
                    commentId.toInt()
                )
            }
    }

    fun getNotification(reactionTypes: ReactionTypes): NotificationCompat.Builder {
        if (notification == null) {
            notification = NotificationCompat.Builder(this, "reaction")
                .setSmallIcon(R.drawable.ic_sync)
                .setProgress(0, 100, true)
        }
        notification!!.setContentTitle(getString(R.string.posting_reaction, reactionTypes.content))
        return notification!!
    }

    private fun showNotification(builder: NotificationCompat.Builder, id: Int) {
        notificationManager!!.notify(id, builder.build())
    }

    private fun hideNotification(id: Int) {
        notificationManager!!.cancel(id)
    }

    companion object {
        fun start(
            context: Context, login: String, repo: String,
            commentId: Long, reactionType: ReactionTypes?, isCommit: Boolean, isDelete: Boolean,
            isEnterprise: Boolean
        ) {
            val intent = Intent(context, ReactionService::class.java)
            intent.putExtras(
                start()
                    .put(BundleConstant.EXTRA, isCommit)
                    .put(BundleConstant.EXTRA_TWO, login)
                    .put(BundleConstant.EXTRA_THREE, repo)
                    .put(BundleConstant.EXTRA_FOUR, isDelete)
                    .put(BundleConstant.ID, commentId)
                    .put(BundleConstant.EXTRA_TYPE, reactionType)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .end()
            )
            context.startService(intent)
        }
    }

    override fun handleIntent(p0: Intent) {
        onHandleIntent(p0)
    }
}