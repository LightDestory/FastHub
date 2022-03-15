package com.fastaccess.ui.modules.settings.category

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.bumptech.glide.Glide
import com.fastaccess.App
import com.fastaccess.R
import com.fastaccess.data.dao.SettingsModel
import com.fastaccess.data.dao.SettingsModel.SettingsType
import com.fastaccess.data.dao.model.SearchHistory
import com.fastaccess.helper.*
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.modules.settings.sound.NotificationSoundBottomSheet.Companion.newInstance
import com.fastaccess.ui.modules.settings.sound.NotificationSoundMvp.NotificationSoundListener
import com.fastaccess.ui.widgets.SpannableBuilder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import es.dmoral.toasty.Toasty
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class SettingsCategoryFragment : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener,
    NotificationSoundListener {
    interface SettingsCallback {
        @get:SettingsType
        val settingsType: Int
    }

    private var callback: FAView? = null
    private var appColor: String? = null
    private var appLanguage: String? = null
    private var notificationTime: Preference? = null
    private var notificationRead: Preference? = null
    private var notificationSound: Preference? = null
    private var notificationSoundPath: Preference? = null
    private var settingsCallback: SettingsCallback? = null
    private val disposable = CompositeDisposable()
    private var preferenceDataStore: SettingsDataStore = SettingsDataStore.instance!!
    override fun onAttach(context: Context) {
        super.onAttach(context)
        callback = context as FAView
        settingsCallback = context as SettingsCallback
        appColor = preferenceDataStore.getString("appColor", getString(R.string.blue_theme_mode))
        appLanguage = preferenceDataStore.getString("app_language", "en")
    }

    override fun onDetach() {
        callback = null
        settingsCallback = null
        super.onDetach()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore = preferenceDataStore
        when (settingsCallback!!.settingsType) {
            SettingsModel.BACKUP -> addBackup()
            SettingsModel.BEHAVIOR -> addBehaviour()
            SettingsModel.CUSTOMIZATION -> addCustomization()
            SettingsModel.NOTIFICATION -> addNotifications()
            else -> Toast.makeText(
                context,
                "You reached the impossible :'(",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        when {
            preference.key.equals("notificationEnabled", ignoreCase = true) -> {
                if (newValue as Boolean) {
                    preferenceScreen.addPreference(notificationTime)
                    preferenceScreen.addPreference(notificationRead)
                    preferenceScreen.addPreference(notificationSound)
                    preferenceScreen.addPreference(notificationSoundPath)
                } else {
                    preferenceScreen.removePreference(notificationTime)
                    preferenceScreen.removePreference(notificationRead)
                    preferenceScreen.removePreference(notificationSound)
                    preferenceScreen.removePreference(notificationSoundPath)
                }
                return true
            }
            preference.key.equals("recylerViewAnimation", ignoreCase = true) -> {
                callback!!.onThemeChanged()
                return true
            }
            preference.key.equals("rect_avatar", ignoreCase = true) -> {
                callback!!.onThemeChanged()
                return true
            }
            preference.key.equals("appColor", ignoreCase = true) -> {
                if (newValue.toString().equals(appColor, ignoreCase = true)) return true
                Toasty.warning(
                    App.getInstance(),
                    getString(R.string.change_theme_warning),
                    Toast.LENGTH_LONG
                ).show()
                callback!!.onThemeChanged()
                return true
            }
            preference.key.equals("app_language", ignoreCase = true) -> {
                if (newValue.toString().equals(appLanguage, ignoreCase = true)) return true
                callback!!.onThemeChanged()
                return true
            }
            else -> return false
        }
    }

    override fun onSoundSelected(uri: Uri?) {
        PrefGetter.setNotificationSound(uri!!)
        if (notificationSoundPath != null && notificationSoundPath!!.isVisible) notificationSoundPath!!.summary =
            FileHelper.getRingtoneName(requireContext(), uri)
    }


    override fun onDestroy() {
        disposable.dispose()
        disposable.clear()
        super.onDestroy()
    }

    private val fileChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it?.data ?: return@registerForActivityResult
        restoreData(it.data)
    }

    private fun getBackupUri(): Uri {
        return Uri.parse(
            "content://com.android.externalstorage.documents/document/primary:Download:${
                getString(R.string.app_name)
            }:backup.json"
        )
    }

    private fun showFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/json"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getBackupUri())
        }
        fileChooserLauncher.launch(intent)
    }

    private val fileBackupChooserLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        it?.data ?: return@registerForActivityResult
        backupData(it.data!!)
    }

    private fun showFileBackupChooser() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/json"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, getBackupUri())
        }
        fileBackupChooserLauncher.launch(intent)
    }

    private fun backupData(intent: Intent) {
        try {
            requireContext().contentResolver.openOutputStream(intent.data!!).use { outputStream ->
                val preferences = preferenceDataStore.getAll()
                preferences.remove("token")
                val json = Gson().toJson(preferences)
                OutputStreamWriter(
                    outputStream
                ).use { myOutWriter -> myOutWriter.append(json) }
            }
        } catch (e: IOException) {
            Toasty.error(App.getInstance(), getString(R.string.backed_failed)).show()
            e.printStackTrace()
        }
        preferenceDataStore.putAny(
            "backed_up",
            SimpleDateFormat("MM/dd", Locale.ENGLISH).format(Date())
        )
        Toasty.success(App.getInstance(), getString(R.string.backed_up)).show()
    }

    private fun addBackup() {
        addPreferencesFromResource(R.xml.backup_settings)
        findPreference("backup").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                showFileBackupChooser()
                true
            }
        if (preferenceDataStore.getString("backed_up", null) != null) {
            findPreference("backup").summary = SpannableBuilder.builder()
                .append(
                    getString(
                        R.string.backup_summary,
                        preferenceDataStore.getString("backed_up", null)
                    )
                )
        } else {
            findPreference("backup").summary = ""
        }
        findPreference("restore").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                showFileChooser()
                true
            }
    }

    private fun addCustomization() {
        addPreferencesFromResource(R.xml.customization_settings)
        findPreference("enable_ads").isVisible = false
        findPreference("recylerViewAnimation").onPreferenceChangeListener = this
        findPreference("rect_avatar").onPreferenceChangeListener = this
        findPreference("appColor").onPreferenceChangeListener = this
    }

    private fun addBehaviour() {
        addPreferencesFromResource(R.xml.behaviour_settings)
        findPreference("clear_search").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                callback!!.showMessage(R.string.success, R.string.deleted)
                SearchHistory.deleteAll()
                true
            }
        findPreference("clear_image_cache").onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val glide = Glide.get(App.getInstance())
                disposable.add(RxHelper.getObservable(Observable.fromCallable {
                    glide.clearDiskCache()
                    true
                }).subscribe {
                    glide.clearMemory()
                    Toasty.info(
                        App.getInstance(),
                        getString(R.string.restart_app_message),
                        Toast.LENGTH_LONG
                    ).show()
                })
                true
            }
    }

    private fun addNotifications() {
        addPreferencesFromResource(R.xml.notification_settings)
        notificationTime = findPreference("notificationTime")
        notificationRead = findPreference("markNotificationAsRead")
        notificationSound = findPreference("notificationSound")
        notificationTime?.onPreferenceChangeListener = this
        findPreference("notificationEnabled").onPreferenceChangeListener = this
        notificationSoundPath = findPreference("notification_sound_path")
        notificationSoundPath?.summary = FileHelper.getRingtoneName(
            requireContext(),
            PrefGetter.notificationSound
        )
        notificationSoundPath?.setOnPreferenceClickListener {
            newInstance(
                FileHelper.getRingtoneName(
                    requireContext(),
                    PrefGetter.notificationSound
                )
            )
                .show(childFragmentManager, "NotificationSoundBottomSheet")
            true
        }
        if (!preferenceDataStore.getBoolean("notificationEnabled", false)) {
            preferenceScreen.removePreference(notificationTime)
            preferenceScreen.removePreference(notificationRead)
            preferenceScreen.removePreference(notificationSound)
            preferenceScreen.removePreference(notificationSoundPath)
        }
    }

    private fun restoreData(data: Intent?) {
        val json = StringBuilder()
        try {
            requireContext().contentResolver.openInputStream(data!!.data!!).use { inputStream ->
                if (inputStream != null) {
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            json.append(line)
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Toasty.error(App.getInstance(), getString(R.string.error)).show()
        }
        if (!InputHelper.isEmpty(json)) {
            try {
                val gson = Gson()
                val typeOfHashMap = object : TypeToken<Map<String?, *>?>() {}.type
                val savedPref = gson.fromJson<Map<String, *>>(json.toString(), typeOfHashMap)
                if (savedPref != null && savedPref.isNotEmpty()) {
                    for ((key, value) in savedPref) {
                        preferenceDataStore.putAny(key, value)
                    }
                }
                callback!!.onThemeChanged()
                Toasty.success(App.getInstance(), getString(R.string.restore_up)).show()
            } catch (ignored: Exception) {
                Toasty.error(App.getInstance(), getString(R.string.error), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    companion object {
        val TAG: String = SettingsCategoryFragment::class.java.simpleName
    }
}