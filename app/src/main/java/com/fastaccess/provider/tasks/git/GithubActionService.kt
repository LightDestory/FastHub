package com.fastaccess.provider.tasks.git

import android.app.IntentService
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import androidx.annotation.IntDef
import androidx.core.app.NotificationCompat
import com.fastaccess.R
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.provider.rest.RestProvider
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

/**
 * Created by Kosh on 12 Mar 2017, 2:25 PM
 */
class GithubActionService(name: String?) : IntentService(name) {
    private var notification: NotificationCompat.Builder? = null
    private var notificationManager: NotificationManager? = null
        get() {
            if (field == null) {
                field = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            }
            return field
        }

    override fun onDestroy() {
        super.onDestroy()

    }

    @IntDef(
        STAR_REPO,
        UNSTAR_REPO,
        FORK_REPO,
        WATCH_REPO,
        UNWATCH_REPO,
        STAR_GIST,
        UNSTAR_GIST,
        FORK_GIST
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class GitActionType

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null && intent.extras != null) {
            val bundle = intent.extras
            @GitActionType val type = bundle!!.getInt(BundleConstant.EXTRA_TYPE)
            val id = bundle.getString(BundleConstant.ID)
            val login = bundle.getString(BundleConstant.EXTRA)
            val isEnterprise = bundle.getBoolean(BundleConstant.IS_ENTERPRISE)
            when (type) {
                FORK_GIST -> forkGist(id, isEnterprise)
                FORK_REPO -> forkRepo(id, login, isEnterprise)
                STAR_GIST -> starGist(id, isEnterprise)
                STAR_REPO -> starRepo(id, login, isEnterprise)
                UNSTAR_GIST -> unStarGist(id, isEnterprise)
                UNSTAR_REPO -> unStarRepo(id, login, isEnterprise)
                UNWATCH_REPO -> unWatchRepo(id, login, isEnterprise)
                WATCH_REPO -> watchRepo(id, login, isEnterprise)
            }
        }
    }

    private fun forkGist(id: String?, isEnterprise: Boolean): Disposable? {
        if (id != null) {
            val msg = getString(R.string.forking, getString(R.string.gist))
            return RestProvider.getGistService(isEnterprise)
                .forkGist(id)
                .doOnSubscribe { showNotification(msg) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { },
                    { hideNotification(msg) }) { hideNotification(msg) }
        }
        return null
    }

    private fun forkRepo(id: String?, login: String?, isEnterprise: Boolean): Disposable? {
        if (id != null && login != null) {
            val msg = getString(R.string.forking, id)
            return RestProvider.getRepoService(isEnterprise)
                .forkRepo(login, id)
                .doOnSubscribe { showNotification(msg) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { },
                    { hideNotification(msg) }) { hideNotification(msg) }
        }
        return null
    }

    private fun starGist(id: String?, isEnterprise: Boolean): Disposable? {
        if (id != null) {
            val msg = getString(R.string.starring, getString(R.string.gist))
            return RestProvider.getGistService(isEnterprise)
                .starGist(id)
                .doOnSubscribe { showNotification(msg) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { },
                    { hideNotification(msg) }) { hideNotification(msg) }
        }
        return null
    }

    private fun starRepo(id: String?, login: String?, isEnterprise: Boolean): Disposable? {
        if (id != null && login != null) {
            val msg = getString(R.string.starring, id)
            return RestProvider.getRepoService(isEnterprise)
                .starRepo(login, id)
                .doOnSubscribe { showNotification(msg) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { },
                    { hideNotification(msg) }) { hideNotification(msg) }
        }
        return null
    }

    private fun unStarGist(id: String?, isEnterprise: Boolean): Disposable? {
        if (id != null) {
            val msg = getString(R.string.un_starring, getString(R.string.gist))
            return RestProvider.getGistService(isEnterprise)
                .unStarGist(id)
                .doOnSubscribe { showNotification(msg) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { },
                    { hideNotification(msg) }) { hideNotification(msg) }
        }
        return null
    }

    private fun unStarRepo(id: String?, login: String?, isEnterprise: Boolean): Disposable? {
        if (id != null && login != null) {
            val msg = getString(R.string.un_starring, id)
            return RestProvider.getRepoService(isEnterprise)
                .unstarRepo(login, id)
                .doOnSubscribe { showNotification(msg) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { },
                    { hideNotification(msg) }) { hideNotification(msg) }
        }
        return null
    }

    private fun unWatchRepo(id: String?, login: String?, isEnterprise: Boolean): Disposable? {
        if (id != null && login != null) {
            val msg = getString(R.string.un_watching, id)
            return RestProvider.getRepoService(isEnterprise)
                .unwatchRepo(login, id)
                .doOnSubscribe { showNotification(msg) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { },
                    { hideNotification(msg) }) { hideNotification(msg) }
        }
        return null
    }

    private fun watchRepo(id: String?, login: String?, isEnterprise: Boolean): Disposable? {
        if (id != null && login != null) {
            val msg = getString(R.string.watching, id)
            return RestProvider.getRepoService(isEnterprise)
                .watchRepo(login, id)
                .doOnSubscribe { showNotification(msg) }
                .subscribeOn(Schedulers.io())
                .subscribe(
                    { },
                    { hideNotification(msg) }) { hideNotification(msg) }
        }
        return null
    }

    private fun getNotification(title: String): NotificationCompat.Builder {
        if (notification == null) {
            notification = NotificationCompat.Builder(this, title)
                .setSmallIcon(R.drawable.ic_sync)
                .setProgress(0, 100, true)
        }
        notification!!.setContentTitle(title)
        return notification!!
    }

    private fun showNotification(msg: String) {
        notificationManager!!.notify(msg.hashCode(), getNotification(msg).build())
    }

    private fun hideNotification(msg: String) {
        notificationManager!!.cancel(msg.hashCode())
    }

    companion object {
        const val STAR_REPO = 1
        const val UNSTAR_REPO = 2
        const val FORK_REPO = 3
        const val WATCH_REPO = 4
        const val UNWATCH_REPO = 5
        const val STAR_GIST = 6
        const val UNSTAR_GIST = 7
        const val FORK_GIST = 8

        @JvmStatic
        fun startForRepo(
            context: Context, login: String, repo: String,
            @GitActionType type: Int, isEnterprise: Boolean
        ) {
            val intent = Intent(context.applicationContext, GithubActionService::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.ID, repo)
                    .put(BundleConstant.EXTRA, login)
                    .put(BundleConstant.EXTRA_TYPE, type)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .end()
            )
            context.startService(intent)
        }

        @JvmStatic
        fun startForGist(
            context: Context,
            id: String,
            @GitActionType type: Int,
            isEnterprise: Boolean
        ) {
            val intent = Intent(context.applicationContext, GithubActionService::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.ID, id)
                    .put(BundleConstant.EXTRA_TYPE, type)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .end()
            )
            context.startService(intent)
        }
    }

    fun handleIntent(p0: Intent) {
        onHandleIntent(p0)
    }
}