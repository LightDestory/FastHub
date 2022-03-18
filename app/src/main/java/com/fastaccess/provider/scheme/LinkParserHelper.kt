package com.fastaccess.provider.scheme

import android.net.Uri
import android.webkit.MimeTypeMap
import com.fastaccess.helper.InputHelper
import com.fastaccess.helper.PrefGetter
import android.text.TextUtils
import com.annimon.stream.Optional
import com.annimon.stream.Stream
import com.fastaccess.helper.ObjectsCompat
import java.lang.Exception
import java.util.*

/**
 * Created by Kosh on 11 Apr 2017, 10:02 PM
 */
object LinkParserHelper {
    const val HOST_DEFAULT = "github.com"
    const val PROTOCOL_HTTPS = "https"
    const val HOST_GISTS = "gist.github.com"
    const val HOST_GISTS_RAW = "gist.githubusercontent.com"
    const val RAW_AUTHORITY = "raw.githubusercontent.com"
    const val API_AUTHORITY = "api.github.com"

    @JvmField
    val IGNORED_LIST: List<String> = listOf(
        "notifications", "settings", "blog",
        "explore", "dashboard", "repositories",
        "logout", "sessions", "site", "security",
        "contact", "about", "logos", "login", "pricing", ""
    )

    private val escapeSymbols: Map<String, String> = mapOf(
        "&quot;" to "\"", "&apos;" to "'",
        "&lt;" to "<", "&gt;" to ">", "&amp;" to "&")

    @JvmStatic
    @SafeVarargs
    fun <T> returnNonNull(vararg t: T): Optional<T> {
        return Stream.of(*t).filter { obj: T -> ObjectsCompat.nonNull(obj) }
            .findFirst()
    }

    @JvmStatic
    fun getBlobBuilder(uri: Uri): Uri {
        val isSvg =
            "svg".equals(MimeTypeMap.getFileExtensionFromUrl(uri.toString()), ignoreCase = true)
        val segments = uri.pathSegments
        if (isSvg) {
            val svgBlob = Uri.parse(uri.toString().replace("blob/", ""))
            return svgBlob.buildUpon().authority(RAW_AUTHORITY).build()
        }
        val urlBuilder = Uri.Builder()
        val owner = segments[0]
        val repo = segments[1]
        val branch = segments[3]
        urlBuilder.scheme("https")
            .authority(API_AUTHORITY)
            .appendPath("repos")
            .appendPath(owner)
            .appendPath(repo)
            .appendPath("contents")
        for (i in 4 until segments.size) {
            urlBuilder.appendPath(segments[i])
        }
        if (uri.queryParameterNames != null) {
            for (query in uri.queryParameterNames) {
                urlBuilder.appendQueryParameter(query, uri.getQueryParameter(query))
            }
        }
        if (uri.encodedFragment != null) {
            urlBuilder.encodedFragment(uri.encodedFragment)
        }
        urlBuilder.appendQueryParameter("ref", branch)
        return urlBuilder.build()
    }

    @JvmStatic
    fun isEnterprise(url: String?): Boolean {
        var url1 = url
        if (InputHelper.isEmpty(url1) || !PrefGetter.isEnterprise) return false
        val enterpriseUrl = PrefGetter.enterpriseUrl?.lowercase(Locale.getDefault())
        enterpriseUrl ?: return false
        url1 = url1!!.lowercase(Locale.getDefault())
        return (url1.equals(
            enterpriseUrl,
            ignoreCase = true
        ) || url1.startsWith(enterpriseUrl) || url1.startsWith(
            getEndpoint(enterpriseUrl)
        )
                || url1.contains(enterpriseUrl) || enterpriseUrl.contains(url1))
    }

    fun stripScheme(url: String): String? {
        try {
            val uri = Uri.parse(url)
            return if (!InputHelper.isEmpty(uri.authority)) uri.authority else url
        } catch (ignored: Exception) {
        }
        return url
    }

    @JvmStatic
    fun getEndpoint(url: String): String {
        var url1 = url
        if (url1.startsWith("http://")) {
            url1 = url1.replace("http://", "https://")
        }
        if (!url1.startsWith("https://")) {
            url1 = "https://$url1"
        }
        return getEnterpriseUrl(url1)
    }

    private fun getEnterpriseUrl(url: String): String {
        if (url.endsWith("/api/v3/")) {
            return url
        } else if (url.endsWith("/api/")) {
            return url + "v3/"
        } else if (url.endsWith("/api")) {
            return "$url/v3/"
        } else if (url.endsWith("/api/v3")) {
            return "$url/"
        } else if (!url.endsWith("/")) {
            return "$url/api/v3/"
        } else if (url.endsWith("/")) {
            return url + "api/v3/"
        }
        return url
    }

    @JvmStatic
    fun getEnterpriseGistUrl(url: String, isEnterprise: Boolean): String {
        var url1 = url
        if (isEnterprise) {
            val uri = Uri.parse(url1)
            val isGist =
                if (uri == null || uri.pathSegments == null) url1.contains("gist/") else uri.pathSegments[0] == "gist"
            if (isGist) {
                val enterpriseUrl = PrefGetter.enterpriseUrl!!
                if (!url1.contains("$enterpriseUrl/raw/")) {
                    url1 = url1.replace(enterpriseUrl, "$enterpriseUrl/raw")
                }
            }
        }
        return url1
    }

    @JvmStatic
    fun getGistId(uri: Uri): String? {
        var gistId: String? = null
        if (uri.toString().contains("raw/gist")) {
            if (uri.pathSegments.size > 5) {
                gistId = uri.pathSegments[5]
            }
        } else if (uri.pathSegments != null) {
            if (TextUtils.equals(HOST_GISTS_RAW, uri.authority)) {
                if (uri.pathSegments.size > 1) {
                    gistId = uri.pathSegments[1]
                }
            }
        }
        return gistId
    }

    @JvmStatic
    fun isGitHubBlobImage(url: String): Boolean {
        return Uri.parse(url).authority == HOST_DEFAULT && url.contains("/blob/")
    }

    @JvmStatic
    fun minifyGitHubImageUri(url: String): String {
        return url.replace(HOST_DEFAULT, RAW_AUTHORITY).replace("blob/", "")
    }

    @JvmStatic
    fun parseReferenceSymbols(uri: Uri): Uri {
        var formattedUrl = uri.toString()
        escapeSymbols.entries.forEach {
            formattedUrl = formattedUrl.replace(it.key, it.value)
        }
        return Uri.parse(formattedUrl)
    }
}