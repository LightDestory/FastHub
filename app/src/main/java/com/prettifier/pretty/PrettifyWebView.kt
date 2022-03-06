package com.prettifier.pretty

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.webkit.*
import com.fastaccess.R
import com.fastaccess.helper.AppHelper
import com.fastaccess.helper.InputHelper
import com.fastaccess.helper.Logger
import com.fastaccess.helper.ViewHelper
import com.fastaccess.provider.markdown.MarkDownProvider
import com.fastaccess.provider.scheme.SchemeParser
import com.fastaccess.ui.modules.code.CodeViewerActivity
import com.prettifier.pretty.callback.MarkDownInterceptorInterface
import com.prettifier.pretty.helper.GithubHelper
import com.prettifier.pretty.helper.PrettifyHelper
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.disposables.Disposable
import java.io.File
import java.util.concurrent.TimeUnit


class PrettifyWebView : NestedWebView {
    private var onContentChangedListener: OnContentChangedListener? = null
    private var interceptTouch = false
    private var enableNestedScrolling = false
    private var onReadyListener: OnReadyListener? = null
    private lateinit var disposable: Disposable

    interface OnContentChangedListener {
        fun onContentChanged(progress: Int)
        fun onScrollChanged(reachedTop: Boolean, scroll: Int)
    }

    interface OnReadyListener {
        fun onReady(view: WebView)
    }

    constructor(context: Context?) : super(context) {
        if (isInEditMode) return
        initView(null)
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        initView(attrs)
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initView(attrs)
    }

    override fun onInterceptTouchEvent(p: MotionEvent): Boolean {
        return true
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(interceptTouch)
        }
        return super.onTouchEvent(ev)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initView(attrs: AttributeSet?) {
        if (isInEditMode) return
        if (attrs != null) {
            val tp = context.obtainStyledAttributes(attrs, R.styleable.PrettifyWebView)
            try {
                val color = tp.getColor(
                    R.styleable.PrettifyWebView_webview_background, ViewHelper.getWindowBackground(
                        context
                    )
                )
                setBackgroundColor(color)
            } finally {
                tp.recycle()
            }
        }
        if (!File(context.cacheDir.path, "WebView").exists()) {
            listOf(
                File(context.cacheDir.path, "WebView/Default/HTTP Cache/Code Cache/js"),
                File(context.cacheDir.path, "WebView/Default/HTTP Cache/Code Cache/wasm"),
                File(context.cacheDir.path, "WebView/Default/HTTP Cache/Code Cache/html"),
                File(context.cacheDir.path, "WebView/Default/HTTP Cache/Code Cache/css"),
            ).filter {
                !it.exists()
            }.map {
                it.mkdirs()
            }
        }

        webChromeClient = ChromeClient()
        webViewClient = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            WebClient()
        } else {
            WebClientCompat()
        }
        val settings = settings
        settings.javaScriptEnabled = true
        settings.setAppCachePath(context.cacheDir.path)
        settings.setAppCacheEnabled(true)
        settings.cacheMode = WebSettings.LOAD_DEFAULT
//        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.defaultTextEncodingName = "utf-8"
        settings.loadsImagesAutomatically = true
        settings.blockNetworkImage = false
        setOnLongClickListener {
            val result = hitTestResult
            if (hitLinkResult(result) && !InputHelper.isEmpty(result.extra)) {
                AppHelper.copyToClipboard(context, result.extra!!)
                return@setOnLongClickListener true
            }
            false
        }
        disposable = Observable.create(ObservableOnSubscribe<WebView> { emitter ->
            this.onReadyListener = object : OnReadyListener {
                override fun onReady(view: WebView) {
                    if (!emitter.isDisposed) {
                        emitter.onNext(view)
                    }
                }
            }
        }).debounce(1000, TimeUnit.MILLISECONDS).subscribe {
            this.resize(it)
        }
        // webview height
//        this.addJavascriptInterface(this, "android")
    }

    //    @JavascriptInterface
    fun resize(view: WebView) {
        val w: Int = MeasureSpec.makeMeasureSpec(
            0,
            MeasureSpec.UNSPECIFIED
        )
        val h: Int = MeasureSpec.makeMeasureSpec(
            0,
            MeasureSpec.UNSPECIFIED
        )
        view.measure(w, h)
        this.disposable.dispose()
        this.onReadyListener = null
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)
        if (onContentChangedListener != null) {
            onContentChangedListener!!.onScrollChanged(t == 0, t)
        }
    }

    override fun onDetachedFromWindow() {
        onContentChangedListener = null
        super.onDetachedFromWindow()
    }

    private fun hitLinkResult(result: HitTestResult): Boolean {
        return result.type == HitTestResult.SRC_ANCHOR_TYPE || result.type == HitTestResult.IMAGE_TYPE || result.type == HitTestResult.SRC_IMAGE_ANCHOR_TYPE
    }

    fun setOnContentChangedListener(onContentChangedListener: OnContentChangedListener) {
        this.onContentChangedListener = onContentChangedListener
    }

    fun setThemeSource(source: String, theme: String?) {
        if (!InputHelper.isEmpty(source)) {
            val settings = settings
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
            settings.setSupportZoom(true)
            settings.builtInZoomControls = true
            settings.displayZoomControls = false
            val page = PrettifyHelper.generateContent(source, theme!!)
            loadCode(page)
        }
    }

    fun setSource(source: String, wrap: Boolean) {
        if (!InputHelper.isEmpty(source)) {
            val settings = settings
            settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.TEXT_AUTOSIZING
            scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
            settings.setSupportZoom(!wrap)
            settings.builtInZoomControls = !wrap
            if (!wrap) settings.displayZoomControls = false
            val page =
                PrettifyHelper.generateContent(source, AppHelper.isNightMode(resources), wrap)
            loadCode(page)
        }
    }

    private fun loadCode(page: String) {
        post {
            loadDataWithBaseURL(
                "file:///android_asset/highlight/",
                page,
                "text/html",
                "utf-8",
                null
            )
        }
    }

    fun scrollToLine(url: String) {
        val lineNo = getLineNo(url)
        if (lineNo != null && lineNo.size > 1) {
            loadUrl("javascript:scrollToLineNumber('" + lineNo[0] + "', '" + lineNo[1] + "')")
        } else if (lineNo != null) {
            loadUrl("javascript:scrollToLineNumber('" + lineNo[0] + "', '0')")
        }
    }

    fun setGithubContentWithReplace(source: String, baseUrl: String?, replace: Boolean) {
        setGithubContent(source, baseUrl, false)
        addJavascriptInterface(MarkDownInterceptorInterface(this, false), "Android")
        val page = GithubHelper.generateContent(
            context, source, baseUrl, AppHelper.isNightMode(
                resources
            ), false, replace
        )
        post { loadDataWithBaseURL("file:///android_asset/md/", page, "text/html", "utf-8", null) }
    }

    fun setGithubContent(source: String, baseUrl: String?, toggleNestScrolling: Boolean) {
        setGithubContent(source, baseUrl, toggleNestScrolling, true)
    }

    fun setWikiContent(source: String, baseUrl: String?) {
        addJavascriptInterface(MarkDownInterceptorInterface(this, true), "Android")
        val page = GithubHelper.generateContent(
            context, source, baseUrl, AppHelper.isNightMode(
                resources
            ), AppHelper.isNightMode(resources), true
        )
        post { loadDataWithBaseURL("file:///android_asset/md/", page, "text/html", "utf-8", null) }
    }

    fun setGithubContent(
        source: String,
        baseUrl: String?,
        toggleNestScrolling: Boolean,
        enableBridge: Boolean
    ) {
        if (enableBridge) addJavascriptInterface(
            MarkDownInterceptorInterface(
                this,
                toggleNestScrolling
            ), "Android"
        )
        val page = GithubHelper.generateContent(
            context, source, baseUrl, AppHelper.isNightMode(resources),
            AppHelper.isNightMode(resources), false
        )
        Log.e("githubContent", page)
        post { loadDataWithBaseURL("file:///android_asset/md/", page, "text/html", "utf-8", null) }
    }

    fun loadImage(url: String, isSvg: Boolean) {
        val settings = settings
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
        settings.setSupportZoom(true)
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        val html: String = if (isSvg) {
            url
        } else {
            "<html><head><style>img{display: inline; height: auto; max-width: 100%;}</style></head><body>" +
                    "<img src=\"" + url + "\"/></body></html>"
        }
        Logger.e(html)
        loadData(html, "text/html", null)
    }

    fun setInterceptTouch(interceptTouch: Boolean) {
        this.interceptTouch = interceptTouch
    }

    fun setEnableNestedScrolling(enableNestedScrolling: Boolean) {
        if (this.enableNestedScrolling != enableNestedScrolling) {
            isNestedScrollingEnabled = enableNestedScrolling
            this.enableNestedScrolling = enableNestedScrolling
        }
    }

    private fun startActivity(url: Uri?) {
        if (url == null) return
        if (MarkDownProvider.isImage(url.toString())) {
            CodeViewerActivity.startActivity(context, url.toString(), url.toString())
        } else {
            val lastSegment = url.encodedFragment
            if (lastSegment != null || url.toString().startsWith("#") || url.toString()
                    .indexOf('#') != -1
            ) {
                return
            }
            SchemeParser.launchUri(context, url, true)
        }
    }


    private inner class ChromeClient : WebChromeClient() {
        override fun onProgressChanged(view: WebView, progress: Int) {
            super.onProgressChanged(view, progress)
            if (onContentChangedListener != null) {
                onContentChangedListener!!.onContentChanged(progress)
            }
            if (progress == 100) {
                onReadyListener?.onReady(view)
            }
        }
    }

    private inner class WebClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            startActivity(request.url)
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onReadyListener!!.onReady(view!!)
        }
    }

    private inner class WebClientCompat : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            startActivity(Uri.parse(url))
            return true
        }

        override fun onPageFinished(view: WebView?, url: String?) {
            super.onPageFinished(view, url)
            onReadyListener!!.onReady(view!!)
        }
    }

    companion object {
        fun getLineNo(url: String?): Array<String>? {
            var lineNo: Array<String>? = null
            if (url != null) {
                try {
                    val uri = Uri.parse(url)
                    val lineNumber = uri.encodedFragment
                    if (lineNumber != null) {
                        lineNo = lineNumber.replace("L".toRegex(), "").split("-").toTypedArray()
                    }
                } catch (ignored: Exception) {
                }
            }
            return lineNo
        }
    }
}