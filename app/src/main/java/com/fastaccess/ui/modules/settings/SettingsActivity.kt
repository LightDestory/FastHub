package com.fastaccess.ui.modules.settings

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import com.fastaccess.R
import com.fastaccess.data.dao.SettingsModel
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.ui.adapter.SettingsAdapter
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.mvp.BaseMvp.FAView
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.settings.LanguageBottomSheetDialog.LanguageDialogListener
import com.fastaccess.ui.modules.settings.category.SettingsCategoryActivity
import com.fastaccess.ui.modules.theme.ThemeActivity
import com.fastaccess.ui.modules.theme.code.ThemeCodeActivity
import io.reactivex.functions.Action

class SettingsActivity : BaseActivity<FAView, BasePresenter<FAView>>(), LanguageDialogListener {
    private lateinit var settingsList: ListView
    private val settings = ArrayList<SettingsModel>()
    override fun layout(): Int {
        return R.layout.activity_settings
    }

    override val isTransparent: Boolean = false

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val root = window.decorView
        settingsList = root.findViewById(R.id.settingsList)
        setToolbarIcon(R.drawable.ic_back)
        title = getString(R.string.settings)
        if (savedInstanceState == null) {
            setResult(RESULT_CANCELED)
        }
        settings.add(
            SettingsModel(
                R.drawable.ic_color_lens,
                getString(R.string.theme_title),
                SettingsModel.THEME
            )
        )
        settings.add(
            SettingsModel(
                R.drawable.ic_color_lens,
                getString(R.string.choose_code_theme),
                SettingsModel.CODE_THEME
            )
        )
        settings.add(
            SettingsModel(
                R.drawable.ic_edit,
                getString(R.string.customization),
                SettingsModel.CUSTOMIZATION
            )
        )
        settings.add(
            SettingsModel(
                R.drawable.ic_ring,
                getString(R.string.notifications),
                SettingsModel.NOTIFICATION
            )
        )
        settings.add(
            SettingsModel(
                R.drawable.ic_settings,
                getString(R.string.behavior),
                SettingsModel.BEHAVIOR
            )
        )
        settings.add(
            SettingsModel(
                R.drawable.ic_backup,
                getString(R.string.backup),
                SettingsModel.BACKUP
            )
        )
        settings.add(
            SettingsModel(
                R.drawable.ic_language,
                getString(R.string.app_language),
                SettingsModel.LANGUAGE
            )
        )
        settingsList.adapter = SettingsAdapter(this, settings)
        settingsList.setOnItemClickListener { parent, view, position, _ ->
            val settingsModel = parent.getItemAtPosition(position) as SettingsModel
            val intent = Intent(this, SettingsCategoryActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.ITEM, settingsModel.settingsType)
                    .put(BundleConstant.EXTRA, settingsModel.title)
                    .end()
            )
            when (settingsModel.settingsType) {
                SettingsModel.LANGUAGE -> {
                    showLanguageList()
                }
                SettingsModel.THEME -> {
                    ActivityHelper.startLauncher(
                        launcher,
                        Intent(this, ThemeActivity::class.java),
                        view,
                    )
                }
                SettingsModel.CODE_THEME -> {
                    ActivityHelper.startLauncher(
                        launcher,
                        Intent(this, ThemeCodeActivity::class.java),
                        view
                    )
                }
                else -> {
                    ActivityHelper.startReveal(this, intent, view!!)
                }
            }
        }
    }

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        it.data?.let {
            setResult(THEME_CHANGE)
            finish()
        }
    }

    override fun providePresenter(): BasePresenter<FAView> {
        return BasePresenter()
    }

    private fun showLanguageList() {
        val languageBottomSheetDialog = LanguageBottomSheetDialog()
        languageBottomSheetDialog.onAttach((this as Context))
        languageBottomSheetDialog.show(supportFragmentManager, "LanguageBottomSheetDialog")
    }

    override fun onLanguageChanged(action: Action?) {
        try {
            action!!.run() //dismiss dialog avoid leakage
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setResult(RESULT_OK)
        finish()
    }

    companion object {
        private const val THEME_CHANGE = 32
    }
}