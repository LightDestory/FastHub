package com.fastaccess.helper

import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.ConnectivityManager
import android.net.NetworkRequest
import android.os.Build
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fastaccess.App
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.PrefGetter.ThemeType
import com.fastaccess.helper.PrefGetter.getAppLanguage
import com.fastaccess.helper.PrefGetter.getThemeType
import es.dmoral.toasty.Toasty
import java.util.*

/**
 * Created by kosh20111 on 18 Oct 2016, 9:29 PM
 */
object AppHelper {
    @JvmStatic
    fun hideKeyboard(view: View) {
        val inputManager =
            view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @JvmStatic
    fun getFragmentByTag(fragmentManager: FragmentManager, tag: String): Fragment? {
        return fragmentManager.findFragmentByTag(tag)
    }

    @JvmStatic
    @JvmOverloads
    fun cancelNotification(context: Context, id: Int = BundleConstant.REQUEST_CODE) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
    }

    fun cancelAllNotifications(context: Context) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancelAll()
    }

    @JvmStatic
    fun copyToClipboard(context: Context, uri: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(context.getString(R.string.app_name), uri)
        clipboard.setPrimaryClip(clip)
        Toasty.success(App.getInstance(), context.getString(R.string.success_copied)).show()
    }

    @JvmStatic
    fun isNightMode(resources: Resources): Boolean {
        @ThemeType val themeType = getThemeType(resources)
        return themeType != PrefGetter.LIGHT
    }

    fun getFastHubIssueTemplate(enterprise: Boolean): String {
        val brand = if (!isEmulator) Build.BRAND else "Android Emulator"
        val model = if (!isEmulator) DeviceNameGetter.instance.deviceName else "Android Emulator"
        val builder = StringBuilder()
            .append("**FastHub-RE Version: ").append(BuildConfig.VERSION_NAME)
            .append(if (enterprise) " Enterprise**" else "**").append("  \n")
            .append(if (!isInstalledFromPlaySore(App.getInstance())) "**APK Source: Unknown**  \n" else "")
            .append("**Android Version: ").append(Build.VERSION.RELEASE.toString())
            .append(" (SDK: ")
            .append(Build.VERSION.SDK_INT.toString()).append(")**").append("  \n")
            .append("**Device Information:**").append("  \n")
            .append("- **")
            .append(
                if (!model.equals(
                        brand,
                        ignoreCase = true
                    )
                ) "Manufacturer" else "Manufacturer&Brand"
            )
            .append(":** ")
            .append(Build.MANUFACTURER)
            .append("  \n")
        if (!(model.equals(brand, ignoreCase = true) || "google" == Build.BRAND)) {
            builder.append("- **Brand:** ").append(brand).append("  \n")
        }
        builder.append("- **Model:** ").append(model).append("  \n")
            .append("---").append("\n\n")
        if (Locale.getDefault().language != Locale("en").language) {
            builder.append("<!--")
                .append(App.getInstance().getString(R.string.english_please))
                .append("-->")
                .append("\n")
        }
        return builder.toString()
    }

    fun updateAppLanguage(context: Context) {
        val lang = getAppLanguage(context.resources)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, lang)
        }
        updateResourcesLegacy(context, lang)
    }

    private fun updateResources(context: Context, language: String) {
        val locale = getLocale(language)
        Locale.setDefault(locale)
        val configuration = context.resources.configuration
        configuration.setLocale(locale)
        context.createConfigurationContext(configuration)
    }

    private fun updateResourcesLegacy(context: Context, language: String) {
        val locale = getLocale(language)
        Locale.setDefault(locale)
        val resources = context.resources
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    private fun getLocale(language: String): Locale {
        var locale: Locale? = null
        if (language.equals("zh-rCN", ignoreCase = true)) {
            locale = Locale.SIMPLIFIED_CHINESE
        } else if (language.equals("zh-rTW", ignoreCase = true)) {
            locale = Locale.TRADITIONAL_CHINESE
        }
        if (locale != null) return locale
        val split = language.split("-").toTypedArray()
        locale = if (split.size > 1) {
            Locale(split[0], split[1])
        } else {
            Locale(language)
        }
        return locale
    }

    val deviceName: String
        get() = if (isEmulator) {
            "Android Emulator"
        } else DeviceNameGetter.instance.deviceName
    val isEmulator: Boolean
        get() = (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
                || "google_sdk" == Build.PRODUCT)

    private fun isInstalledFromPlaySore(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val sourceInfo = context.packageManager.getInstallSourceInfo(BuildConfig.GITHUB_APP_ID)
            return !isEmpty(sourceInfo.installingPackageName)
        } else {
            val ipn = context.packageManager.getInstallerPackageName(BuildConfig.GITHUB_APP_ID)
            !isEmpty(ipn)
        }
    }

    fun isGoogleAvailable(context: Context): Boolean {
        /*
        var applicationInfo: ApplicationInfo? = null
        try {
            applicationInfo = context.packageManager.getApplicationInfo("com.google.android.gms", 0)
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return applicationInfo != null && applicationInfo.enabled && GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
         */
        return true
    }

    @JvmStatic
    fun isDeviceAnimationEnabled(context: Context): Boolean {
        val duration = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        )
        val transition = Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.TRANSITION_ANIMATION_SCALE,
            1f
        )
        return duration != 0f && transition != 0f
    }

    @JvmStatic
    val isDataPlan: Boolean
        @RequiresApi(Build.VERSION_CODES.S)
        get() {
            val connectivityManager = App.getInstance()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            return mobile!!.isConnectedOrConnecting
        }
}