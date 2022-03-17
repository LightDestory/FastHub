package com.fastaccess.helper

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityOptionsCompat
import androidx.core.app.ShareCompat.IntentBuilder
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.fastaccess.App
import com.fastaccess.R
import com.fastaccess.helper.CustomTabsHelper.getPackageNameToUse
import com.fastaccess.helper.PrefGetter.isAppAnimationDisabled
import com.fastaccess.helper.ViewHelper.getPrimaryColor
import com.fastaccess.helper.ViewHelper.getTransitionName
import com.fastaccess.ui.modules.main.drawer.AccountDrawerFragment
import com.fastaccess.ui.modules.main.drawer.MainDrawerFragment
import com.fastaccess.ui.modules.parser.LinksParserActivity
import es.dmoral.toasty.Toasty
import java.lang.Deprecated

/**
 * Created by Kosh on 12/12/15 10:51 PM
 */
object ActivityHelper {
    @JvmStatic
    fun getActivity(content: Context?): Activity? {
        return when (content) {
            null -> null
            is Activity -> content
            is ContextWrapper -> getActivity(
                content.baseContext
            )
            else -> null
        }
    }

    fun startCustomTab(context: Activity?, url: Uri) {
        context ?: return
        val packageNameToUse = getPackageNameToUse(context)
        if (packageNameToUse != null) {
            val customTabsIntent = CustomTabsIntent.Builder()
                .setDefaultColorSchemeParams(
                    CustomTabColorSchemeParams.Builder()
                        .setToolbarColor(getPrimaryColor(context))
                        .build()
                ) //                    .setToolbarColor(ViewHelper.getPrimaryColor(context))
                .setShowTitle(true)
                .build()
            customTabsIntent.intent.setPackage(packageNameToUse)
            try {
                customTabsIntent.launchUrl(context, url)
            } catch (ignored: ActivityNotFoundException) {
                openChooser(context, url, true)
            }
        } else {
            openChooser(context, url, true)
        }
    }

    @JvmStatic
    fun startCustomTab(context: Activity, url: String) {
        startCustomTab(context, Uri.parse(url))
    }

    @JvmStatic
    fun openChooser(context: Context, url: Uri) {
        openChooser(context, url, false)
    }

    @JvmStatic
    fun safeOpenChooser(context: Context, intent: Intent) {
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    @JvmStatic
    fun safeOpenChooser(context: Activity, intent: Intent) {
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }

    private fun openChooser(context: Context, url: Uri, fromCustomTab: Boolean) {
        val i = Intent(Intent.ACTION_VIEW, url)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val finalIntent = chooserIntent(context, i, url)
        if (finalIntent != null) {
            try {
                safeOpenChooser(context, finalIntent)
            } catch (ignored: ActivityNotFoundException) {
            }
        } else {
            if (!fromCustomTab) {
                val activity = getActivity(context)
                activity?.let {
                    try {
                        safeOpenChooser(it, i)
                    } catch (ignored: ActivityNotFoundException) {
                    }
                    return
                }
                startCustomTab(activity, url)
            } else {
                try {
                    safeOpenChooser(context, i)
                } catch (ignored: ActivityNotFoundException) {
                }
            }
        }
    }

    fun openChooser(context: Context, url: String) {
        openChooser(context, Uri.parse(url))
    }

    @SafeVarargs
    fun start(activity: Activity, cl: Class<*>?, vararg sharedElements: Pair<View?, String?>?) {
        val intent = Intent(activity, cl)
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *sharedElements)
        activity.startActivity(intent, options.toBundle())
    }

    fun start(activity: Activity, intent: Intent?, sharedElement: View) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
            activity,
            sharedElement,
            getTransitionName(sharedElement)!!
        )
        activity.startActivity(intent, options.toBundle())
    }

    @SuppressWarnings("deprecation")
    @kotlin.Deprecated("use registerForActivityResult")
    @JvmStatic
    fun startReveal(activity: Activity, intent: Intent?, sharedElement: View, requestCode: Int) {
        if (!isAppAnimationDisabled) {
            val options = ActivityOptionsCompat.makeClipRevealAnimation(
                sharedElement, sharedElement.width / 2,
                sharedElement.height / 2,
                sharedElement.width, sharedElement.height
            )
            activity.startActivityForResult(intent, requestCode, options.toBundle())
        } else {
            activity.startActivityForResult(intent, requestCode)
        }
    }


    @JvmStatic
    fun startLauncher(
        launcher: ActivityResultLauncher<Intent>,
        intent: Intent?,
        sharedElement: View? = null
    ) {
        if (!isAppAnimationDisabled && sharedElement != null) {
            val options = ActivityOptionsCompat.makeClipRevealAnimation(
                sharedElement, sharedElement.width / 2,
                sharedElement.height / 2,
                sharedElement.width, sharedElement.height
            )
            launcher.launch(intent, options)
        } else {
            launcher.launch(intent)
        }
    }

    @SuppressWarnings("deprecation")
    @kotlin.Deprecated("use registerForActivityResult")
    @JvmStatic
    fun startReveal(fragment: Fragment, intent: Intent?, sharedElement: View, requestCode: Int) {
        if (!isAppAnimationDisabled) {
            val options = ActivityOptionsCompat.makeClipRevealAnimation(
                sharedElement, sharedElement.width / 2,
                sharedElement.height / 2,
                sharedElement.width, sharedElement.height
            )
            fragment.startActivityForResult(intent, requestCode, options.toBundle())
        } else {
            fragment.startActivityForResult(intent, requestCode)
        }
    }

    fun startReveal(activity: Activity, intent: Intent?, sharedElement: View) {
        if (!isAppAnimationDisabled) {
            val options = ActivityOptionsCompat.makeClipRevealAnimation(
                sharedElement, sharedElement.width / 2,
                sharedElement.height / 2,
                sharedElement.width, sharedElement.height
            )
            activity.startActivity(intent, options.toBundle())
        } else {
            activity.startActivity(intent)
        }
    }

    @SafeVarargs
    fun start(
        activity: Activity, intent: Intent,
        vararg sharedElements: Pair<View?, String?>
    ) {
        val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, *sharedElements)
        activity.startActivity(intent, options.toBundle())
    }

    @JvmStatic
    fun shareUrl(context: Context, url: String) {
        val activity = getActivity(context)
            ?: throw IllegalArgumentException("Context given is not an instance of activity " + context.javaClass.name)
        try {
            IntentBuilder(activity)
                .setChooserTitle(context.getString(R.string.share))
                .setType("text/plain")
                .setText(url)
                .startChooser()
        } catch (e: ActivityNotFoundException) {
            Toasty.error(App.getInstance(), e.message!!, Toast.LENGTH_LONG)
                .show()
        }
    }

    @JvmStatic
    fun getVisibleFragment(manager: FragmentManager): Fragment? {
        val fragments = manager.fragments
        if (fragments.isNotEmpty()) {
            for (fragment in fragments) {
                if (fragment != null && fragment.isVisible &&
                    !(fragment is MainDrawerFragment || fragment is AccountDrawerFragment)
                ) {
                    return fragment
                }
            }
        }
        return null
    }

    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isExplanationNeeded(context: Activity, permissionName: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(context, permissionName)
    }

    private fun isReadWritePermissionIsGranted(context: Context): Boolean {
        return (isPermissionGranted(context, Manifest.permission.READ_EXTERNAL_STORAGE)
                && isPermissionGranted(context, Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }

    private fun requestReadWritePermission(context: Activity) {
        ActivityCompat.requestPermissions(
            context, arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ), 1
        )
    }

    @JvmStatic
    fun checkAndRequestReadWritePermission(activity: Activity): Boolean {
        if (!isReadWritePermissionIsGranted(activity)) {
            requestReadWritePermission(activity)
            return false
        } else if (isExplanationNeeded(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
            || isExplanationNeeded(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        ) {
            Toasty.error(
                App.getInstance(),
                activity.getString(R.string.read_write_permission_explanation),
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    private fun chooserIntent(context: Context, intent: Intent, uri: Uri): Intent? {
        val pm = context.packageManager
        val activities = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val chooserIntents = ArrayList<Intent>()
        val ourPackageName = context.packageName
        for (resInfo in activities) {
            val info = resInfo.activityInfo
            if (!info.enabled || !info.exported) {
                continue
            }
            if (info.packageName == ourPackageName) {
                continue
            }
            val targetIntent = Intent(intent)
            targetIntent.setPackage(info.packageName)
            targetIntent.setDataAndType(uri, intent.type)
            chooserIntents.add(targetIntent)
        }
        if (chooserIntents.isEmpty()) {
            return null
        }
        val lastIntent = chooserIntents.removeAt(chooserIntents.size - 1)
        if (chooserIntents.isEmpty()) {
            return lastIntent
        }
        val chooserIntent = Intent.createChooser(lastIntent, null)
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, chooserIntents.toTypedArray())
        return chooserIntent
    }

    fun activateLinkInterceptorActivity(context: Context, activate: Boolean) {
        val pm = context.packageManager
        val flag =
            if (activate) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        pm.setComponentEnabledSetting(
            ComponentName(context, LinksParserActivity::class.java),
            flag,
            PackageManager.DONT_KILL_APP
        )
    }

    fun editBundle(intent: Intent, isEnterprise: Boolean): Intent {
        val bundle = intent.extras
        if (bundle != null) {
            bundle.putBoolean(BundleConstant.IS_ENTERPRISE, isEnterprise)
            intent.putExtras(bundle)
        }
        return intent
    }
}