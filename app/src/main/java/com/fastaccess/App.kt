package com.fastaccess

import android.app.Application
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.fastaccess.data.dao.model.Models
import com.fastaccess.helper.DeviceNameGetter
import com.fastaccess.helper.PrefHelper.init
import com.fastaccess.helper.SettingsDataStore
import com.fastaccess.helper.TypeFaceHelper.generateTypeface
import com.fastaccess.provider.colors.ColorsProvider
import com.fastaccess.provider.emoji.EmojiManager
import com.fastaccess.provider.tasks.notification.NotificationSchedulerJobTask
import es.dmoral.toasty.Toasty
import io.requery.Persistable
import io.requery.android.sqlite.DatabaseSource
import io.requery.reactivex.ReactiveEntityStore
import io.requery.reactivex.ReactiveSupport
import io.requery.sql.EntityDataStore
import io.requery.sql.TableCreationMode

/**
 * Created by Kosh on 03 Feb 2017, 12:07 AM
 */
class App : Application() {
    var dataStore: ReactiveEntityStore<Persistable>? = null
        get() {
            if (field == null) {
                val model = Models.DEFAULT
                val source = DatabaseSource(this, model, "FastHub-DB", 18)
                val configuration = source.configuration
                if (BuildConfig.DEBUG) {
                    source.setTableCreationMode(TableCreationMode.CREATE_NOT_EXISTS)
                }
                field = ReactiveSupport.toReactiveStore(EntityDataStore(configuration))
            }
            return field
        }
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        init()
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private fun initShortcut() {
        val shortcutManager = this.applicationContext.getSystemService(
            ShortcutManager::class.java
        )
        shortcutManager.removeAllDynamicShortcuts()
    }

    private fun init() {
        deleteDatabase("database.db")
        dataStore
        setupPreference()
        generateTypeface(this)
        NotificationSchedulerJobTask.scheduleJob(this);
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
        private var instance: App? = null
        @JvmStatic
        fun getInstance(): App {
            return instance!!
        }
    }
}