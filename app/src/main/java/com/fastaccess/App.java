package com.fastaccess;

import android.app.Application;
import android.content.pm.ShortcutManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.fastaccess.data.dao.model.Models;
import com.fastaccess.helper.DeviceNameGetter;
import com.fastaccess.helper.PrefHelper;
import com.fastaccess.helper.TypeFaceHelper;
import com.fastaccess.provider.colors.ColorsProvider;
import com.fastaccess.provider.emoji.EmojiManager;
import com.fastaccess.provider.tasks.notification.NotificationSchedulerJobTask;
import com.tencent.bugly.crashreport.CrashReport;
import es.dmoral.toasty.Toasty;
import io.requery.Persistable;
import io.requery.android.sqlite.DatabaseSource;
import io.requery.meta.EntityModel;
import io.requery.reactivex.ReactiveEntityStore;
import io.requery.reactivex.ReactiveSupport;
import io.requery.sql.Configuration;
import io.requery.sql.EntityDataStore;
import io.requery.sql.TableCreationMode;


/**
 * Created by Kosh on 03 Feb 2017, 12:07 AM
 */

public class App extends Application {
    private static App instance;
    private ReactiveEntityStore<Persistable> dataStore;

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "ae44f59ed7", BuildConfig.DEBUG);
        instance = this;
        init();
    }

    @NonNull
    public static App getInstance() {
        return instance;
    }

    @RequiresApi(api = Build.VERSION_CODES.N_MR1)
    private void initShortcut() {
        ShortcutManager shortcutManager = this.getApplicationContext().getSystemService(ShortcutManager.class);
        shortcutManager.removeAllDynamicShortcuts();
    }

    private void init() {
        deleteDatabase("database.db");
        getDataStore();
        setupPreference();
        TypeFaceHelper.generateTypeface(this);
        NotificationSchedulerJobTask.scheduleJob(this);
        if (BuildConfig.DEBUG) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                initShortcut();
            }
        }

        EmojiManager.load();
        ColorsProvider.load();
        DeviceNameGetter.instance.loadDevice();
        Toasty.Config.getInstance().allowQueue(true).apply();
//        ThemeEngine.applyApplication(this);
    }

    private void setupPreference() {
        PrefHelper.init(this.getApplicationContext());
    }

    public ReactiveEntityStore<Persistable> getDataStore() {
        if (dataStore == null) {
            EntityModel model = Models.DEFAULT;
            DatabaseSource source = new DatabaseSource(this, model, "FastHub-DB", 18);
            Configuration configuration = source.getConfiguration();
            if (BuildConfig.DEBUG) {
                source.setTableCreationMode(TableCreationMode.CREATE_NOT_EXISTS);
            }
            dataStore = ReactiveSupport.toReactiveStore(new EntityDataStore<>(configuration));
        }
        return dataStore;
    }
}