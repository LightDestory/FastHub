package com.fastaccess.ui.modules.code

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.webkit.MimeTypeMap
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.AppHelper
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.rest.RestProvider
import com.fastaccess.provider.scheme.LinkParserHelper
import com.fastaccess.provider.scheme.LinkParserHelper.getEnterpriseGistUrl
import com.fastaccess.provider.scheme.LinkParserHelper.getGistId
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.base.mvp.BaseMvp
import com.fastaccess.ui.base.mvp.presenter.BasePresenter
import com.fastaccess.ui.modules.gists.gist.GistActivity
import com.fastaccess.ui.modules.repos.code.files.activity.RepoFilesActivity
import com.fastaccess.ui.modules.repos.code.prettifier.ViewerFragment

/**
 * Created by Kosh on 27 Nov 2016, 3:43 PM
 */
class CodeViewerActivity : BaseActivity<BaseMvp.FAView, BasePresenter<BaseMvp.FAView>>() {
    @JvmField
    @State
    var url: String? = null

    @JvmField
    @State
    var htmlUrl: String? = null
    override fun layout(): Int {
        return R.layout.activity_fragment_layout
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
            val intent = intent ?: error("Intent is null")
            val bundle = intent.extras!!
            url = bundle.getString(BundleConstant.EXTRA) ?: error("Url is null")
            htmlUrl = bundle.getString(BundleConstant.EXTRA_TWO)
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.container,
                    ViewerFragment.newInstance(url!!, htmlUrl),
                    ViewerFragment.TAG
                )
                .commit()
        }
        val title = Uri.parse(url).lastPathSegment
        setTitle(title)
        if (toolbar != null) toolbar!!.subtitle = MimeTypeMap.getFileExtensionFromUrl(url)
        setTaskName(title)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.download_browser_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (url.isNullOrEmpty()) return super.onOptionsItemSelected(item)
        when (item.itemId) {
            R.id.viewAsCode -> {
                val viewerFragment = AppHelper.getFragmentByTag(
                    supportFragmentManager,
                    ViewerFragment.TAG
                ) as ViewerFragment?
                viewerFragment?.onViewAsCode()
                return true
            }
            R.id.download -> {
                if (ActivityHelper.checkAndRequestReadWritePermission(this)) {
                    RestProvider.downloadFile(this, url!!)
                }
                return true
            }
            R.id.browser -> {
                ActivityHelper.openChooser(this, (if (htmlUrl != null) htmlUrl else url)!!)
                return true
            }
            R.id.copy -> {
                AppHelper.copyToClipboard(this, (if (htmlUrl != null) htmlUrl else url)!!)
                return true
            }
            R.id.share -> {
                ActivityHelper.shareUrl(this, (if (htmlUrl != null) htmlUrl else url)!!)
                return true
            }
            android.R.id.home -> {
                val uri = Uri.parse(url)
                if (uri == null) {
                    finish()
                    return true
                }
                val gistId = getGistId(uri)
                if (!isEmpty(gistId)) {
                    startActivity(GistActivity.createIntent(this, gistId!!, isEnterprise))
                } else {
                    RepoFilesActivity.startActivity(this, url!!, isEnterprise)
                }
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    companion object {
        @JvmStatic
        fun startActivity(context: Context, url: String, htmlUrl: String) {
            if (!isEmpty(url)) {
                val intent = ActivityHelper.editBundle(
                    createIntent(context, url, htmlUrl),
                    isEnterprise(htmlUrl)
                )
                context.startActivity(intent)
            }
        }

        @JvmStatic
        fun createIntent(context: Context, url: String, htmlUrl: String): Intent {
            var url1 = url
            val intent = Intent(context, CodeViewerActivity::class.java)
            if (LinkParserHelper.isGitHubBlobImage(url1)){
                url1 = LinkParserHelper.minifyGitHubImageUri(url1)
            }
            val isEnterprise = isEnterprise(htmlUrl)
            url1 = getEnterpriseGistUrl(url1, isEnterprise)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.EXTRA_TWO, htmlUrl)
                    .put(BundleConstant.EXTRA, url1)
                    .put(BundleConstant.IS_ENTERPRISE, isEnterprise)
                    .end()
            )
            return intent
        }
    }
}