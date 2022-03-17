package com.fastaccess.ui.modules.repos.wiki

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources.Theme
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.core.view.iterator
import androidx.drawerlayout.widget.DrawerLayout
import com.evernote.android.state.State
import com.fastaccess.R
import com.fastaccess.data.dao.NameParser
import com.fastaccess.data.dao.wiki.WikiContentModel
import com.fastaccess.data.dao.wiki.WikiSideBarModel
import com.fastaccess.databinding.WikiActivityLayoutBinding
import com.fastaccess.helper.ActivityHelper
import com.fastaccess.helper.BundleConstant
import com.fastaccess.helper.Bundler
import com.fastaccess.provider.scheme.LinkParserHelper
import com.fastaccess.ui.base.BaseActivity
import com.fastaccess.ui.delegate.viewBinding
import com.fastaccess.ui.modules.repos.RepoPagerActivity
import com.fastaccess.ui.widgets.StateLayout
import com.google.android.material.navigation.NavigationView
import com.prettifier.pretty.PrettifyWebView


/**
 * Created by Kosh on 13 Jun 2017, 8:35 PM
 */
class WikiActivity : BaseActivity<WikiMvp.View, WikiPresenter>(), WikiMvp.View {
    val binding: WikiActivityLayoutBinding by viewBinding()

    private val navMenu: NavigationView by lazy { window.decorView.findViewById(R.id.wikiSidebar) }
    val drawerLayout: DrawerLayout by lazy { window.decorView.findViewById(R.id.drawer) }
    val progressbar: ProgressBar by lazy { window.decorView.findViewById(R.id.progress) }
    val stateLayout: StateLayout by lazy { window.decorView.findViewById(R.id.stateLayout) }
    val webView: PrettifyWebView by lazy { window.decorView.findViewById(R.id.webView) }

    //    @BindView(R.id.wikiSidebar)
//    lateinit var navMenu: NavigationView
//    val webView: PrettifyWebView by lazy { binding.webView }
//    @BindView(R.id.drawer)
//    lateinit var drawerLayout: DrawerLayout

//    @BindView(R.id.progress)
//    lateinit var progressbar: ProgressBar

//    @BindView(R.id.stateLayout)
//    lateinit var stateLayout: StateLayout
//
//    @BindView(R.id.webView)
//    lateinit var webView: PrettifyWebView

    @State
    var wiki = WikiContentModel(null, null, arrayListOf())

    @State
    var selectedTitle: String = "Home"

    @State
    var selectedId: Int = selectedTitle.hashCode()

    override fun layout(): Int = R.layout.wiki_activity_layout

    override val isTransparent: Boolean = true

    override fun providePresenter(): WikiPresenter = WikiPresenter()

    override fun onLoadContent(wiki: WikiContentModel) {
        hideProgress()
        this.wiki = wiki
        if (wiki.sidebar.isNotEmpty()) {
            loadMenu()
        }
        if (wiki.content != null) {
            val baseUrl = Uri.Builder().scheme(LinkParserHelper.PROTOCOL_HTTPS)
                .authority(LinkParserHelper.HOST_DEFAULT)
                .appendPath(presenter.login)
                .appendPath(presenter.repoId)
                .appendPath("wiki")
                .build()
                .toString()
            webView.setWikiContent(wiki.content, baseUrl)
        }
    }

    override fun onSetPage(page: String) {
        selectedTitle = page
        selectedId = page.hashCode()
    }

    @SuppressLint("ResourceType")
    private fun toSequence(sideBar: WikiSideBarModel): CharSequence {
        val sb = SpannableStringBuilder()
        var title = sideBar.title
        if (sideBar.level == 2) {
            title = "  $title"
        }
        val len = title.length
        sb.append(title)
        when (sideBar.level) {
            0 -> {
                val styleSpan = StyleSpan(Typeface.BOLD)
                val absoluteSizeSpan = AbsoluteSizeSpan(14, true)
                sb.setSpan(styleSpan, 0, len, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                sb.setSpan(absoluteSizeSpan, 0, len, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
            1 -> {
                val absoluteSizeSpan = AbsoluteSizeSpan(12, true)
                sb.setSpan(absoluteSizeSpan, 0, len, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
            2 -> {
                val typedValue = TypedValue()
                val theme: Theme = this.theme
                theme.resolveAttribute(android.R.attr.textColorSecondary, typedValue, true)
                val color = typedValue.data
                val colorSpan = ForegroundColorSpan(
                    color
                )
                sb.setSpan(colorSpan, 0, len, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
                val absoluteSizeSpan = AbsoluteSizeSpan(12, true)
                sb.setSpan(absoluteSizeSpan, 0, len, Spannable.SPAN_EXCLUSIVE_INCLUSIVE)
            }
            else -> {}
        }
        return sb
    }

    private fun loadMenu() {
        navMenu.menu.clear()
        wiki.sidebar.onEach {
            navMenu.menu.add(
                R.id.languageGroup,
                it.title.hashCode(),
                Menu.NONE,
                // Todo 不同的样式
                toSequence(it)
            )
                .setCheckable(true)
                .isChecked = it.id == selectedId
        }
    }

    override fun canBack(): Boolean = true

    override val isSecured: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        this.navMenu = window.decorView.findViewById(R.id.wikiSidebar)
//        this.stateLayout = window.decorView.findViewById(R.id.stateLayout)
        if (savedInstanceState != null) {
            onLoadContent(wiki)
        } else {
            presenter.onActivityCreated(intent)
        }
        navMenu.setNavigationItemSelectedListener {
            onSidebarClicked(it)
            return@setNavigationItemSelectedListener true
        }

        toolbar?.subtitle = presenter.login + "/" + presenter.repoId
        setTaskName("${presenter.login}/${presenter.repoId} - Wiki - $selectedTitle")
    }

    override fun showPrivateRepoError() {
        onLoadContent(
            WikiContentModel(
                "<h3>${getString(R.string.private_wiki_error_msg)}</h3>",
                null,
                listOf()
            )
        )
    }

    private fun onSidebarClicked(item: MenuItem) {
        closeDrawerLayout()
        val selectItem =
            wiki.sidebar.firstOrNull { it.id == this.selectedId }
        val nextSelectItem =
            wiki.sidebar.firstOrNull { it.id == item.itemId }
        if (selectItem == nextSelectItem) {
            return
        }
        nextSelectItem?.let {
            if (selectItem != null) {
                if (selectItem.link == nextSelectItem.link) {
                    if (nextSelectItem.hash != null && nextSelectItem.hash != selectItem.hash) {
                        webView.scrollToHash(nextSelectItem.hash!!)
                        for (menu in navMenu.menu.iterator()) {
                            menu.isChecked = menu.itemId == nextSelectItem.id
                        }
                    }
                } else {
                    presenter.onSidebarClicked(it)
                }
            } else {
                presenter.onSidebarClicked(it)
            }
            this.selectedTitle = item.title.toString()
            this.selectedId = item.itemId
            setTaskName("${presenter.login}/${presenter.repoId} - Wiki - ${this.selectedTitle}")
        }
    }

    @SuppressLint("RtlHardcoded")
    private fun closeDrawerLayout() {
        drawerLayout.closeDrawer(Gravity.RIGHT)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.trending_menu, menu)
        menu.findItem(R.id.menu)?.setIcon(R.drawable.ic_menu)
        return super.onCreateOptionsMenu(menu)
    }

    @SuppressLint("RtlHardcoded")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu -> {
                drawerLayout.openDrawer(Gravity.RIGHT)
                return true
            }
            R.id.share -> {
                ActivityHelper.shareUrl(
                    this, "${LinkParserHelper.PROTOCOL_HTTPS}://${LinkParserHelper.HOST_DEFAULT}/" +
                            "${presenter.login}/${presenter.repoId}/wiki/$selectedTitle"
                )
                return true
            }
            android.R.id.home -> {
                if (!presenter.login.isNullOrEmpty() && !presenter.repoId.isNullOrEmpty()) {
                    val nameParse = NameParser("")
                    nameParse.name = presenter.repoId!!
                    nameParse.username = presenter.login!!
                    nameParse.isEnterprise = isEnterprise
                    RepoPagerActivity.startRepoPager(this, nameParse)
                }
                finish()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun showMessage(titleRes: Int, msgRes: Int) {
        hideProgress()
        super.showMessage(titleRes, msgRes)
    }

    override fun showMessage(titleRes: String, msgRes: String) {
        hideProgress()
        super.showMessage(titleRes, msgRes)
    }

    override fun showErrorMessage(msgRes: String) {
        hideProgress()
        super.showErrorMessage(msgRes)
    }

    override fun showProgress(resId: Int) {
        progressbar.visibility = View.VISIBLE
        stateLayout.showProgress()
    }

    override fun hideProgress() {
        progressbar.visibility = View.GONE
        stateLayout.hideProgress()
    }

    companion object {
        fun getWiki(context: Context, repoId: String?, username: String?): Intent {
            return getWiki(context, repoId, username, null)
        }

        fun getWiki(context: Context, repoId: String?, username: String?, page: String?): Intent {
            val intent = Intent(context, WikiActivity::class.java)
            intent.putExtras(
                Bundler.start()
                    .put(BundleConstant.ID, repoId)
                    .put(BundleConstant.EXTRA, username)
                    .put(BundleConstant.EXTRA_TWO, page)
                    .end()
            )
            return intent
        }
    }
}