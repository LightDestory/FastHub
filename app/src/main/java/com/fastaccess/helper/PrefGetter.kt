package com.fastaccess.helper

import android.content.Context
import android.content.res.Resources
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.annotation.IntDef
import com.fastaccess.App
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.helper.InputHelper.isEmpty
import java.util.*

/**
 * Created by Kosh on 10 Nov 2016, 3:43 PM
 */
object PrefGetter {
    const val LIGHT = 1
    const val DARK = 2
    const val AMLOD = 3
    const val BLUISH = 4
    const val MID_NIGHT_BLUE = 5
    const val RED = 1
    const val PINK = 2
    const val PURPLE = 3
    const val DEEP_PURPLE = 4
    const val INDIGO = 5
    const val BLUE = 6
    const val LIGHT_BLUE = 7
    const val CYAN = 8
    const val TEAL = 9
    const val GREEN = 10
    const val LIGHT_GREEN = 11
    const val LIME = 12
    const val YELLOW = 13
    const val AMBER = 14
    const val ORANGE = 15
    const val DEEP_ORANGE = 16
    private const val WHATS_NEW_VERSION = "whats_new"
    private const val ADS = "enable_ads"
    private const val TOKEN = "token"
    private const val ENTERPRISE_TOKEN = "enterprise_token"
    private const val USER_ICON_GUIDE = "user_icon_guide"
    private const val RELEASE_GUIDE = "release_guide"
    private const val FILE_OPTION_GUIDE = "file_option_guide"
    private const val COMMENTS_GUIDE = "comments_guide"
    private const val REPO_GUIDE = "repo_guide"
    private const val MARKDOWNDOWN_GUIDE = "markdowndown_guide"
    private const val HOME_BUTTON_GUIDE = "home_button_guide"
    private const val NAV_DRAWER_GUIDE = "nav_drawer_guide"
    private const val ACC_NAV_DRAWER_GUIDE = "acc_nav_drawer_guide"
    private const val FAB_LONG_PRESS_REPO_GUIDE = "fab_long_press_repo_guide"
    private const val WRAP_CODE = "wrap_code"
    private const val OTP_CODE = "otp_code"
    private const val ENTERPRISE_OTP_CODE = "enterprise_otp_code"
    private const val APP_LANGUAGE = "app_language"
    private const val SENT_VIA = "fasthub_signature"
    private const val SENT_VIA_BOX = "sent_via_enabled"
    private const val PROFILE_BACKGROUND_URL = "profile_background_url"
    private const val AMLOD_THEME_ENABLED = "amlod_theme_enabled"
    private const val MIDNIGHTBLUE_THEME_ENABLED = "midnightblue_theme_enabled"
    private const val BLUISH_THEME_ENABLED = "bluish_theme_enabled"
    private const val PRO_ITEMS = "fasth_pro_items"
    private const val ENTERPRISE_ITEM = "enterprise_item"
    private const val CODE_THEME = "code_theme"
    private const val ENTERPRISE_URL = "enterprise_url"
    private const val NOTIFICATION_SOUND_PATH = "notification_sound_path"
    private const val DISABLE_AUTO_LOAD_IMAGE = "disable_auto_loading_image"
    private const val PLAY_STORE_REVIEW_ACTIVITY = "play_store_review_activity"

    @JvmStatic
    fun setTokenEnterprise(token: String?) {
        PrefHelper.putAny(ENTERPRISE_TOKEN, token)
    }

    @JvmStatic
    var token: String?
        get() = PrefHelper.getString(TOKEN)
        set(token) {
            PrefHelper.putAny(TOKEN, token)
        }

    @JvmStatic
    val enterpriseToken: String?
        get() = PrefHelper.getString(ENTERPRISE_TOKEN)

    @JvmStatic
    var enterpriseOtpCode: String?
        get() = PrefHelper.getString(ENTERPRISE_OTP_CODE)
        set(otp) {
            PrefHelper.putAny(ENTERPRISE_OTP_CODE, otp)
        }

    @JvmStatic
    var otpCode: String?
        get() = PrefHelper.getString(OTP_CODE)
        set(otp) {
            PrefHelper.putAny(OTP_CODE, otp)
        }
    var isAdsEnabled: Boolean
        get() = PrefHelper.getBoolean(ADS)
        set(isEnabled) {
            PrefHelper.putAny(ADS, isEnabled)
        }

    @JvmStatic
    fun clear() {
        PrefHelper.clearPrefs()
    }

    @JvmStatic
    val isUserIconGuideShowed: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(USER_ICON_GUIDE)
            PrefHelper.putAny(USER_ICON_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isReleaseHintShow: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(RELEASE_GUIDE)
            PrefHelper.putAny(RELEASE_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isFileOptionHintShow: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(FILE_OPTION_GUIDE)
            PrefHelper.putAny(FILE_OPTION_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isCommentHintShowed: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(COMMENTS_GUIDE)
            PrefHelper.putAny(COMMENTS_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isHomeButoonHintShowed: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(HOME_BUTTON_GUIDE)
            PrefHelper.putAny(HOME_BUTTON_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isRepoGuideShowed: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(REPO_GUIDE)
            PrefHelper.putAny(REPO_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isEditorHintShowed: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(MARKDOWNDOWN_GUIDE)
            PrefHelper.putAny(MARKDOWNDOWN_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isNavDrawerHintShowed: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(NAV_DRAWER_GUIDE)
            PrefHelper.putAny(NAV_DRAWER_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isAccountNavDrawerHintShowed: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(ACC_NAV_DRAWER_GUIDE)
            PrefHelper.putAny(ACC_NAV_DRAWER_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isRepoFabHintShowed: Boolean
        get() {
            val isShowed = PrefHelper.getBoolean(FAB_LONG_PRESS_REPO_GUIDE)
            PrefHelper.putAny(FAB_LONG_PRESS_REPO_GUIDE, true)
            return isShowed
        }

    @JvmStatic
    val isRVAnimationEnabled: Boolean
        get() = PrefHelper.getBoolean("recylerViewAnimation")

    @JvmStatic
    val notificationTaskDuration: Int
        get() {
            if (
                PrefHelper.isExist<Boolean>("notificationEnabled") &&
                PrefHelper.getBoolean("notificationEnabled")
            ) {
                val prefValue = PrefHelper.getString("notificationTime")
                if (prefValue != null) {
                    return notificationDurationMillis(prefValue)
                }
            }
            return -1
        }

    @JvmStatic
    fun notificationDurationMillis(prefValue: String): Int {
        if (!isEmpty(prefValue)) {
            when (prefValue) {
                "1" -> return 60
                "5" -> return 5 * 60
                "10" -> return 10 * 60
                "20" -> return 20 * 60
                "30" -> return 30 * 60
                "60" -> return 60 * 60 // 1 hour
                "120" -> return 60 * 2 * 60 // 2 hours
                "180" -> return 60 * 3 * 60 // 3 hours
            }
        }
        return -1
    }

    @JvmStatic
    val isTwiceBackButtonDisabled: Boolean
        get() = PrefHelper.getBoolean("back_button")

    @JvmStatic
    val isRectAvatar: Boolean
        get() = PrefHelper.getBoolean("rect_avatar")

    @JvmStatic
    val isMarkAsReadEnabled: Boolean
        get() = PrefHelper.getBoolean("markNotificationAsRead")

    @JvmStatic
    val isWrapCode: Boolean
        get() = PrefHelper.getBoolean(WRAP_CODE)

    @JvmStatic
    val isSentViaEnabled: Boolean
        get() = PrefHelper.getBoolean(SENT_VIA)

    @JvmStatic
    val isSentViaBoxEnabled: Boolean
        get() = PrefHelper.getBoolean(SENT_VIA_BOX)

    @JvmStatic
    @ThemeType
    fun getThemeType(context: Context): Int {
        return getThemeType(context.resources)
    }

    @JvmStatic
    @get:ThemeType
    val themeType: Int
        get() = getThemeType(App.getInstance().resources)

    @JvmStatic
    @ThemeColor
    fun getThemeColor(context: Context): Int {
        return getThemeColor(context.resources)
    }

    @JvmStatic
    @ThemeType
    fun getThemeType(resources: Resources): Int {
        val appTheme = PrefHelper.getString("appTheme")
        if (!isEmpty(appTheme)) {
            when {
                appTheme.equals(
                    resources.getString(R.string.dark_theme_mode),
                    ignoreCase = true
                ) -> {
                    return DARK
                }
                appTheme.equals(
                    resources.getString(R.string.light_theme_mode),
                    ignoreCase = true
                ) -> {
                    return LIGHT
                }
                appTheme.equals(
                    resources.getString(R.string.amlod_theme_mode),
                    ignoreCase = true
                ) -> {
                    return AMLOD
                }
                appTheme.equals(
                    resources.getString(R.string.mid_night_blue_theme_mode),
                    ignoreCase = true
                ) -> {
                    return MID_NIGHT_BLUE
                }
                appTheme.equals(
                    resources.getString(R.string.bluish_theme),
                    ignoreCase = true
                ) -> {
                    return BLUISH
                }
            }
        }
        return LIGHT
    }

    @JvmStatic
    @ThemeColor
    private fun getThemeColor(resources: Resources): Int {
        val appColor = PrefHelper.getString("appColor")
        return getThemeColor(resources, appColor)
    }

    @JvmStatic
    @ThemeColor
    // used for color picker to get the index of the color (enum) from the name of the color
    fun getThemeColor(resources: Resources, appColor: String?): Int {
        if (!isEmpty(appColor)) {
            if (appColor.equals(
                    resources.getString(R.string.red_theme_mode),
                    ignoreCase = true
                )
            ) return RED
            if (appColor.equals(
                    resources.getString(R.string.pink_theme_mode),
                    ignoreCase = true
                )
            ) return PINK
            if (appColor.equals(
                    resources.getString(R.string.purple_theme_mode),
                    ignoreCase = true
                )
            ) return PURPLE
            if (appColor.equals(
                    resources.getString(R.string.deep_purple_theme_mode),
                    ignoreCase = true
                )
            ) return DEEP_PURPLE
            if (appColor.equals(
                    resources.getString(R.string.indigo_theme_mode),
                    ignoreCase = true
                )
            ) return INDIGO
            if (appColor.equals(
                    resources.getString(R.string.blue_theme_mode),
                    ignoreCase = true
                )
            ) return BLUE
            if (appColor.equals(
                    resources.getString(R.string.light_blue_theme_mode),
                    ignoreCase = true
                )
            ) return LIGHT_BLUE
            if (appColor.equals(
                    resources.getString(R.string.cyan_theme_mode),
                    ignoreCase = true
                )
            ) return CYAN
            if (appColor.equals(
                    resources.getString(R.string.teal_theme_mode),
                    ignoreCase = true
                )
            ) return TEAL
            if (appColor.equals(
                    resources.getString(R.string.green_theme_mode),
                    ignoreCase = true
                )
            ) return GREEN
            if (appColor.equals(
                    resources.getString(R.string.light_green_theme_mode),
                    ignoreCase = true
                )
            ) return LIGHT_GREEN
            if (appColor.equals(
                    resources.getString(R.string.lime_theme_mode),
                    ignoreCase = true
                )
            ) return LIME
            if (appColor.equals(
                    resources.getString(R.string.yellow_theme_mode),
                    ignoreCase = true
                )
            ) return YELLOW
            if (appColor.equals(
                    resources.getString(R.string.amber_theme_mode),
                    ignoreCase = true
                )
            ) return AMBER
            if (appColor.equals(
                    resources.getString(R.string.orange_theme_mode),
                    ignoreCase = true
                )
            ) return ORANGE
            if (appColor.equals(
                    resources.getString(R.string.deep_orange_theme_mode),
                    ignoreCase = true
                )
            ) return DEEP_ORANGE
        }
        return BLUE
    }

    @JvmStatic
    private fun getSystemLanguage(res: Resources): String {
        val locale: Locale = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            res.configuration.locales[0]
        } else {
            res.configuration.locale
        }
        return locale.language
    }

    @JvmStatic
    fun getAppLanguage(res: Resources): String {
        val appLanguage = PrefHelper.getString(APP_LANGUAGE)
        return appLanguage ?: getSystemLanguage(res)
    }

    @JvmStatic
    fun setAppLangauge(language: String?) {
        PrefHelper.putAny(APP_LANGUAGE, language ?: "en")
    }

    @JvmStatic
    var profileBackgroundUrl: String?
        get() = PrefHelper.getString(PROFILE_BACKGROUND_URL)
        set(url) {
            if (url == null) {
                PrefHelper.clearKey<String>(PROFILE_BACKGROUND_URL)
            } else {
                PrefHelper.putAny(PROFILE_BACKGROUND_URL, url)
            }
        }

    @JvmStatic
    fun setWhatsNewVersion() {
        PrefHelper.putAny(WHATS_NEW_VERSION, BuildConfig.VERSION_CODE)
    }

    @JvmStatic
    fun showWhatsNew(): Boolean {
        return PrefHelper.getInt(WHATS_NEW_VERSION) != BuildConfig.VERSION_CODE
    }

    @JvmStatic
    val isNotificationSoundEnabled: Boolean
        get() = PrefHelper.getBoolean("notificationSound")

    @JvmStatic
    fun enableAmlodTheme() {
        PrefHelper.putAny(AMLOD_THEME_ENABLED, true)
    }

    @JvmStatic
    val isAmlodEnabled: Boolean
        get() = PrefHelper.getBoolean(AMLOD_THEME_ENABLED)

    @JvmStatic
    fun enableMidNightBlueTheme() {
        PrefHelper.putAny(MIDNIGHTBLUE_THEME_ENABLED, true)
    }

    @JvmStatic
    val isMidNightBlueThemeEnabled: Boolean
        get() = PrefHelper.getBoolean(MIDNIGHTBLUE_THEME_ENABLED)

    @JvmStatic
    val isBluishEnabled: Boolean
        get() = PrefHelper.getBoolean(BLUISH_THEME_ENABLED)

    @JvmStatic
    fun enableBluishTheme() {
        PrefHelper.putAny(BLUISH_THEME_ENABLED, true)
    }

    @JvmStatic
    fun setProItems() {
        PrefHelper.putAny(PRO_ITEMS, true)
        enableAmlodTheme()
        enableBluishTheme()
        enableMidNightBlueTheme()
    }

    @JvmStatic
    fun setEnterpriseItem() {
        PrefHelper.putAny(ENTERPRISE_ITEM, true)
    }

    @JvmStatic
    val isEnterpriseEnabled: Boolean
        get() = PrefHelper.getBoolean(ENTERPRISE_ITEM)

    @JvmStatic
    val isAllFeaturesUnlocked: Boolean
        get() = isProEnabled && isEnterprise

    @JvmStatic
    val isProEnabled: Boolean
        get() = PrefHelper.getBoolean(PRO_ITEMS)

    @JvmStatic
    fun hasSupported(): Boolean {
        return isProEnabled || isAmlodEnabled || isBluishEnabled
    }

    @JvmStatic
    val codeTheme: String?
        get() = PrefHelper.getString(CODE_THEME)

    @JvmStatic
    fun setCodeTheme(theme: String) {
        PrefHelper.putAny(CODE_THEME, theme)
    }

    @JvmStatic
    var enterpriseUrl: String?
        get() = PrefHelper.getString(ENTERPRISE_URL)
        set(value) {
            PrefHelper.putAny(ENTERPRISE_URL, value)
        }

    @JvmStatic
    val isEnterprise: Boolean
        get() = !isEmpty(enterpriseUrl)

    @JvmStatic
    val isNavBarTintingDisabled: Boolean
        get() = PrefHelper.getBoolean("navigation_color")

    @JvmStatic
    fun resetEnterprise() {
        setTokenEnterprise(null)
        enterpriseOtpCode = null
        enterpriseUrl = null
    }

    @JvmStatic
    val notificationSound: Uri?
        get() {
            val nsp = PrefHelper.getString(NOTIFICATION_SOUND_PATH)
            return if (!isEmpty(nsp)) Uri.parse(nsp) else RingtoneManager.getDefaultUri(
                RingtoneManager.TYPE_NOTIFICATION
            )
        }

    @JvmStatic
    fun setNotificationSound(uri: Uri) {
        PrefHelper.putAny(NOTIFICATION_SOUND_PATH, uri.toString())
    }

    @JvmStatic
    val isAutoImageDisabled: Boolean
        get() = PrefHelper.getBoolean(DISABLE_AUTO_LOAD_IMAGE) && AppHelper.isDataPlan

    @JvmStatic
    val isAppAnimationDisabled: Boolean
        get() = PrefHelper.getBoolean("app_animation")

    @JvmStatic
    val isPlayStoreWarningShowed: Boolean
        get() = PrefHelper.getBoolean(PLAY_STORE_REVIEW_ACTIVITY)

    @JvmStatic
    fun setPlayStoreWarningShowed() {
        PrefHelper.putAny(PLAY_STORE_REVIEW_ACTIVITY, true)
    }

    @JvmStatic
    fun clearPurchases() {
        PrefHelper.putAny(PRO_ITEMS, false)
        PrefHelper.putAny(BLUISH_THEME_ENABLED, false)
        PrefHelper.putAny(AMLOD_THEME_ENABLED, false)
        enterpriseUrl = null
    }

    @JvmStatic
    val isFeedsHintShowed: Boolean
        get() {
            val isFeedsHitShowed = PrefHelper.getBoolean("feeds_hint")
            if (!isFeedsHitShowed) {
                PrefHelper.putAny("feeds_hint", true)
            }
            return isFeedsHitShowed
        }

    @JvmStatic
    val isIssuesLongPressHintShowed: Boolean
        get() {
            val isIssuesLongPressHintShowed = PrefHelper.getBoolean("issues_long_press_hint")
            if (!isIssuesLongPressHintShowed) {
                PrefHelper.putAny("issues_long_press_hint", true)
            }
            return isIssuesLongPressHintShowed
        }

    @JvmStatic
    val isPRLongPressHintShowed: Boolean
        get() {
            val isPRLongPressHintShowed = PrefHelper.getBoolean("pr_long_press_hint")
            if (!isPRLongPressHintShowed) {
                PrefHelper.putAny("pr_long_press_hint", true)
            }
            return isPRLongPressHintShowed
        }

    @IntDef(LIGHT, DARK, AMLOD, MID_NIGHT_BLUE, BLUISH)
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    annotation class ThemeType

    @IntDef(
        RED,
        PINK,
        PURPLE,
        DEEP_PURPLE,
        INDIGO,
        BLUE,
        LIGHT_BLUE,
        CYAN,
        TEAL,
        GREEN,
        LIGHT_GREEN,
        LIME,
        YELLOW,
        AMBER,
        ORANGE,
        DEEP_ORANGE
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class ThemeColor
}