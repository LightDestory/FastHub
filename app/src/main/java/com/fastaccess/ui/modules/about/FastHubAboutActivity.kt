package com.fastaccess.ui.modules.about

import com.fastaccess.provider.theme.ThemeEngine.applyForAbout
import com.fastaccess.ui.modules.user.UserPagerActivity.Companion.startActivity
import com.danielstone.materialaboutlibrary.MaterialAboutActivity
import android.os.Bundle
import com.fastaccess.provider.theme.ThemeEngine
import com.fastaccess.R
import com.danielstone.materialaboutlibrary.model.MaterialAboutList
import com.danielstone.materialaboutlibrary.model.MaterialAboutCard
import android.content.Intent
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.fastaccess.helper.BundleConstant
import es.dmoral.toasty.Toasty
import com.fastaccess.App
import android.widget.Toast
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import com.danielstone.materialaboutlibrary.items.MaterialAboutActionItem
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.danielstone.materialaboutlibrary.items.MaterialAboutItemOnClickAction
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.ui.modules.user.UserPagerActivity
import com.fastaccess.ui.modules.repos.RepoPagerActivity
import com.danielstone.materialaboutlibrary.ConvenienceBuilder
import com.fastaccess.BuildConfig
import com.fastaccess.ui.modules.main.donation.DonationActivity
import com.fastaccess.ui.modules.changelog.ChangelogBottomSheetDialog
import com.fastaccess.provider.tasks.version.CheckVersionService
import com.fastaccess.ui.modules.repos.issues.create.CreateIssueActivity
import com.mikepenz.aboutlibraries.LibsBuilder
import com.mikepenz.aboutlibraries.ui.LibsActivity
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer

/**
 * Created by danielstone on 12 Mar 2017, 1:57 AM
 */
class FastHubAboutActivity : MaterialAboutActivity() {
    private var malRecyclerview: View? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        applyForAbout(this)
        super.onCreate(savedInstanceState)
        malRecyclerview = findViewById(R.id.mal_recyclerview)
    }

    override fun getMaterialAboutList(context: Context): MaterialAboutList {
        val appCardBuilder = MaterialAboutCard.Builder()
        buildApp(context, appCardBuilder)
        val miscCardBuilder = MaterialAboutCard.Builder()
        buildMisc(context, miscCardBuilder)
        val authorCardBuilder = MaterialAboutCard.Builder()
        buildAuthor(context, authorCardBuilder)
        val newLogoAuthor = MaterialAboutCard.Builder()
        val logoAuthor = MaterialAboutCard.Builder()
        buildLogo(context, newLogoAuthor, logoAuthor)
        return MaterialAboutList(
            appCardBuilder.build(), miscCardBuilder.build(), authorCardBuilder.build(),
            newLogoAuthor.build(), logoAuthor.build()
        )
    }

    override fun getActivityTitle(): CharSequence? {
        return getString(R.string.app_name)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == BundleConstant.REQUEST_CODE) {
            Toasty.success(
                App.getInstance(),
                getString(R.string.thank_you_for_feedback),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return false //override
    }

    private fun buildLogo(
        context: Context,
        newLogoAuthor: MaterialAboutCard.Builder,
        logoAuthor: MaterialAboutCard.Builder
    ) {
        newLogoAuthor.title(getString(R.string.logo_designer, "Cookicons"))
        newLogoAuthor.addItem(MaterialAboutActionItem.Builder()
            .text(R.string.google_plus)
            .icon(ContextCompat.getDrawable(context, R.drawable.ic_profile))
            .setOnClickAction {
                ActivityHelper.startCustomTab(
                    this,
                    "https://plus.google.com/+CookiconsDesign"
                )
            }
            .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.twitter)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_profile))
                .setOnClickAction {
                    ActivityHelper.startCustomTab(
                        this,
                        "https://twitter.com/mcookie"
                    )
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.website)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_brower))
                .setOnClickAction {
                    ActivityHelper.startCustomTab(
                        this,
                        "https://cookicons.co/"
                    )
                }
                .build())
        logoAuthor.title(
            String.format(
                "Old %s",
                getString(R.string.logo_designer, "Kevin Aguilar")
            )
        )
        logoAuthor.addItem(MaterialAboutActionItem.Builder()
            .text(R.string.google_plus)
            .icon(ContextCompat.getDrawable(context, R.drawable.ic_profile))
            .setOnClickAction {
                ActivityHelper.startCustomTab(
                    this,
                    "https://plus.google.com/+KevinAguilarC"
                )
            }
            .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.twitter)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_profile))
                .setOnClickAction {
                    ActivityHelper.startCustomTab(
                        this,
                        "https://twitter.com/kevttob"
                    )
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.website)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_brower))
                .setOnClickAction {
                    ActivityHelper.startCustomTab(
                        this,
                        "http://kevaguilar.com/"
                    )
                }
                .build())
    }

    private fun buildAuthor(context: Context, authorCardBuilder: MaterialAboutCard.Builder) {
        authorCardBuilder.title(R.string.author)
        authorCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text("Kosh Sergani")
            .subText("k0shk0sh")
            .icon(ContextCompat.getDrawable(context, R.drawable.ic_profile))
            .setOnClickAction { startActivity(context, "k0shk0sh", false, false, 0) }
            .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.fork_github)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_github))
                .setOnClickAction {
                    startActivity(
                        RepoPagerActivity.createIntent(
                            this,
                            "FastHub",
                            "k0shk0sh"
                        )
                    )
                }
                .build())
            .addItem(
                ConvenienceBuilder.createEmailItem(
                    context,
                    ContextCompat.getDrawable(context, R.drawable.ic_email),
                    getString(R.string.send_email),
                    true,
                    getString(R.string.email_address),
                    getString(R.string.question_concerning_fasthub)
                )
            )
    }

    private fun buildMisc(context: Context, miscCardBuilder: MaterialAboutCard.Builder) {
        miscCardBuilder.title(R.string.about)
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.support_development)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_heart))
                .setOnClickAction {
                    startActivity(
                        Intent(
                            context,
                            DonationActivity::class.java
                        )
                    )
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.changelog)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_track_changes))
                .setOnClickAction {
                    ChangelogBottomSheetDialog().show(
                        supportFragmentManager,
                        "ChangelogBottomSheetDialog"
                    )
                }
                .build())
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.open_source_libs)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_github))
                .setOnClickAction {
                    val builder = LibsBuilder()
                        .withSearchEnabled(true)
                        .withActivityTitle(this.resources.getString(R.string.open_source_libs))
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withAboutAppName(this.resources.getString(R.string.app_name))
                        .withAboutVersionString(BuildConfig.VERSION_NAME)
                    startLibsActivity(this, builder)
                }
                .build())
    }

    private fun startLibsActivity(ctx: Context, builder: LibsBuilder) {
        val i = Intent(ctx, CommonLibsActivity::class.java)
        i.putExtra("data", builder)
        if (builder.activityTitle != null) {
            i.putExtra(LibsBuilder.BUNDLE_TITLE, builder.activityTitle)
        }
        i.putExtra(LibsBuilder.BUNDLE_EDGE_TO_EDGE, builder.edgeToEdge)
        i.putExtra(LibsBuilder.BUNDLE_SEARCH_ENABLED, builder.searchEnabled)
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(i)
    }

    private fun buildApp(context: Context, appCardBuilder: MaterialAboutCard.Builder) {
        appCardBuilder.addItem(MaterialAboutActionItem.Builder()
            .text(getString(R.string.version))
            .icon(ContextCompat.getDrawable(context, R.drawable.ic_update))
            .subText(BuildConfig.VERSION_NAME)
            .setOnClickAction { startService(Intent(this, CheckVersionService::class.java)) }
            .build())
            .addItem(
                ConvenienceBuilder.createRateActionItem(
                    context, ContextCompat.getDrawable(context, R.drawable.ic_star_filled),
                    getString(R.string.rate_app), null
                )
            )
            .addItem(MaterialAboutActionItem.Builder()
                .text(R.string.report_issue)
                .subText(R.string.report_issue_here)
                .icon(ContextCompat.getDrawable(context, R.drawable.ic_bug))
                .setOnClickAction {
                    CreateIssueActivity.startForResult(
                        this,
                        CreateIssueActivity.startForResult(this),
                        malRecyclerview!!
                    )
                }
                .build())
    }
}