package com.fastaccess

import android.app.Application
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.fastaccess.data.db.ObjectBox
import com.fastaccess.helper.DeviceNameGetter
import com.fastaccess.helper.PrefHelper.init
import com.fastaccess.helper.SettingsDataStore
import com.fastaccess.helper.TypeFaceHelper.generateTypeface
import com.fastaccess.provider.colors.ColorsProvider
import com.fastaccess.provider.crash.Report
import com.fastaccess.provider.emoji.EmojiManager
import com.fastaccess.provider.tasks.notification.NotificationSchedulerJobTask
import es.dmoral.toasty.Toasty

/**
 * Created by Kosh on 03 Feb 2017, 12:07 AM
 */
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        init()
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun initShortcut() {
        val shortcutManager = this.applicationContext.getSystemService(
            ShortcutManager::class.java
        )
        shortcutManager.removeAllDynamicShortcuts()
    }

    private fun init() {
        Report.init(applicationContext)
        ObjectBox.init(applicationContext)
        deleteDatabase("database.db")
        setupPreference()
        generateTypeface(this)
        NotificationSchedulerJobTask.scheduleJob(this)
        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                initShortcut()
            }
        }
        EmojiManager.load()
        ColorsProvider.load()
        DeviceNameGetter.instance.loadDevice()
        Toasty.Config.getInstance().allowQueue(true).apply()
    }

    private fun setupPreference() {
        init(this.applicationContext)
        SettingsDataStore.init()
    }

    companion object {
        private lateinit var instance: App

        @JvmStatic
        fun getInstance(): App {
            return instance
        }
    }
}
