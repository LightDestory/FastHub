package com.fastaccess.ui.modules.repos.code.files.activity

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import com.annimon.stream.Objects
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.NameParser
import com.fastaccess.data.dao.model.AbstractRepo
import com.fastaccess.helper.*
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.main.MainActivity.Companion.launchMain
import com.fastaccess.ui.modules.repos.RepoPagerActivity
import com.fastaccess.ui.modules.repos.code.files.paths.RepoFilePathFragment

/**
 * Created by Kosh on 08 Apr 2017, 4:24 PM
 */
class RepoFilesActivity : BaseActivity<BaseMvp.FAView, BasePresenter<BaseMvp.FAView>>() {
    @JvmField
    @State
    var login: String? = null

    @JvmField
    @State
    var repoId: String? = null
    override fun layout(): Int {
        return R.layout.toolbar_activity_layout
    }

    override val isTransparent: Boolean
        get() = true

    override fun canBack(): Boolean {
        return true
    }

    override val isSecured: Boolean
        get() = false

    override fun providePresenter(): BasePresenter<BaseMvp.FAView> {
        return BasePresenter()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            val bundle = intent.extras
            login = bundle!!.getString(BundleConstant.EXTRA)
            repoId = bundle.getString(BundleConstant.ID)
            val path = bundle.getString(BundleConstant.EXTRA_TWO)
            val defaultBranch =
                Objects.toString(bundle.getString(BundleConstant.EXTRA_THREE), "master")
            supportFragmentManager.beginTransaction()
                .add(
                    R.id.fragmentContainer,
                    RepoFilePathFragment.newInstance(login!!, repoId!!, path, defaultBranch, true),
                    "RepoFilePathFragment"
                )
                .commit()
        }
        title = String.format("%s/%s", login, repoId)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val intent = ActivityHelper.editBundle(
                RepoPagerActivity.createIntent(this, repoId!!, login!!),
                isEnterprise
            )
            startActivity(intent)
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        val filePathView = AppHelper.getFragmentByTag(
            supportFragmentManager,
            "RepoFilePathFragment"
        ) as RepoFilePathFragment?
        if (filePathView != null) {
            if (filePathView.canPressBack()) {
                super.onBackPressed()
            } else {
                filePathView.onBackPressed()
                return
            }
        }
        super.onBackPressed()
    }

    companion object {
        @JvmStatic
        fun startActivity(context: Context, url: String, isEnterprise: Boolean) {
            if (!isEmpty(url)) {
                val intent = ActivityHelper.editBundle(getIntent(context, url), isEnterprise)
                context.startActivity(intent)
            }
        }

        fun getIntent(context: Context, url: String): Intent {
            var url1 = url
            val isEnterprise = isEnterprise(url1)
            if (isEnterprise) {
                url1 = url1.replace("api/v3/", "")
                if (url1.contains("/raw")) {
                    url1 = url1.replace("/raw", "")
                }
            }
            val uri = Uri.parse(url1)
            if (uri.pathSegments != null && uri.pathSegments.size > 3) {
                var login: String? = null
                var repoId: String? = null
                var branch: String? = null
                val path = StringBuilder()
                var startWithRepo = false
                when {
                    uri.pathSegments[0].equals("repos", ignoreCase = true) -> {
                        login = uri.pathSegments[1]
                        repoId = uri.pathSegments[2]
                        branch = uri.getQueryParameter("ref")
                        startWithRepo = true
                    }
                    uri.pathSegments[0].equals("repositories", ignoreCase = true) -> {
                        val id = uri.pathSegments[1]
                        try {
                            val longRepoId = id.toLong()
                            if (longRepoId != 0L) {
                                val repo = AbstractRepo.getRepo(longRepoId)
                                if (repo != null) {
                                    val nameParser = NameParser(repo.htmlUrl)
                                    if (nameParser.username != null && nameParser.name != null) {
                                        login = nameParser.username
                                        repoId = nameParser.name
                                        branch = uri.getQueryParameter("ref")
                                    }
                                }
                            }
                        } catch (ignored: NumberFormatException) {
                            return launchMain(context, true)
                        }
                    }
                    else -> {
                        login = uri.pathSegments[0]
                        repoId = uri.pathSegments[1]
                        branch = uri.pathSegments[2]
                    }
                }
                for (i in (if (startWithRepo) 4 else 3) until uri.pathSegments.size) {
                    val appendedPath = uri.pathSegments[i]
                    path.append("/").append(appendedPath)
                }
                if (!isEmpty(repoId) && !isEmpty(login)) {
                    val intent = Intent(context, RepoFilesActivity::class.java)
                    intent.putExtras(
                        Bundler.start()
                            .put(BundleConstant.ID, repoId)
                            .put(BundleConstant.EXTRA, login)
                            .put(BundleConstant.EXTRA_TWO, path.toString())
                            .put(BundleConstant.EXTRA_THREE, branch)
                            .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                            .end()
                    )
                    return intent
                }
                return launchMain(context, true)
            }
            return launchMain(context, true)
        }
    }
}

