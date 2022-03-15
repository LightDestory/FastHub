package com.fastaccess.provider.scheme

import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.fastaccess.provider.scheme.LinkParserHelper.returnNonNull
import com.fastaccess.ui.modules.repos.projects.details.ProjectPagerActivity.Companion.getIntent
import com.fastaccess.ui.modules.repos.wiki.WikiActivity.Companion.getWiki
import com.fastaccess.provider.scheme.LinkParserHelper.getEndpoint
import com.fastaccess.ui.modules.user.UserPagerActivity.Companion.createIntent
import com.fastaccess.provider.scheme.LinkParserHelper.getBlobBuilder
import com.fastaccess.ui.modules.trending.TrendingActivity.Companion.getTrendingIntent
import kotlin.jvm.JvmOverloads
import android.content.Intent
import android.app.Application
import android.app.Service
import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import com.fastaccess.provider.markdown.MarkDownProvider
import com.fastaccess.ui.modules.code.CodeViewerActivity
import com.fastaccess.ui.modules.gists.gist.GistActivity
import android.text.TextUtils
import com.annimon.stream.Optional
import com.annimon.stream.Stream
import com.fastaccess.helper.*
import com.fastaccess.provider.scheme.LinkParserHelper.API_AUTHORITY
import com.fastaccess.provider.scheme.LinkParserHelper.HOST_DEFAULT
import com.fastaccess.provider.scheme.LinkParserHelper.HOST_GISTS
import com.fastaccess.provider.scheme.LinkParserHelper.HOST_GISTS_RAW
import com.fastaccess.provider.scheme.LinkParserHelper.IGNORED_LIST
import com.fastaccess.provider.scheme.LinkParserHelper.PROTOCOL_HTTPS
import com.fastaccess.provider.scheme.LinkParserHelper.RAW_AUTHORITY
import com.fastaccess.ui.modules.repos.pull_requests.pull_request.details.PullRequestPagerActivity
import com.fastaccess.ui.modules.repos.issues.issue.details.IssuePagerActivity
import com.fastaccess.ui.modules.filter.issues.FilterIssuesActivity
import com.fastaccess.ui.modules.repos.RepoPagerActivity
import com.fastaccess.ui.modules.repos.RepoPagerMvp
import com.fastaccess.ui.modules.repos.code.commit.details.CommitPagerActivity
import com.fastaccess.ui.modules.search.SearchActivity
import com.fastaccess.ui.modules.repos.code.files.activity.RepoFilesActivity
import com.fastaccess.ui.modules.repos.code.releases.ReleasesListActivity
import com.fastaccess.ui.modules.repos.issues.create.CreateIssueActivity
import java.lang.Exception
import java.lang.NumberFormatException

/**
 * Created by Kosh on 09 Dec 2016, 4:44 PM
 */
object SchemeParser {
    @JvmStatic
    fun launchUri(context: Context, url: String) {
        launchUri(context, Uri.parse(url), false)
    }

    @JvmStatic
    @JvmOverloads
    fun launchUri(
        context: Context,
        data: Uri,
        showRepoBtn: Boolean = false,
        newDocument: Boolean = false
    ) {
        Logger.e(data)
        val intent = convert(context, data, showRepoBtn)
        if (intent != null) {
            intent.putExtra(BundleConstant.SCHEME_URL, data.toString())
            if (newDocument) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
            }
            if (context is Service || context is Application) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } else {
            val activity = ActivityHelper.getActivity(context)
            if (activity != null) {
                ActivityHelper.startCustomTab(activity, data)
            } else {
                ActivityHelper.openChooser(context, data)
            }
        }
    }

    private fun convert(context: Context, data: Uri?, showRepoBtn: Boolean): Intent? {
        var data1 = data ?: return null
        if (InputHelper.isEmpty(data1.host) || InputHelper.isEmpty(data1.scheme)) {
            var host = data1.host
            if (InputHelper.isEmpty(host)) host = HOST_DEFAULT
            var scheme = data1.scheme
            if (InputHelper.isEmpty(scheme)) scheme = PROTOCOL_HTTPS
            val prefix = "$scheme://$host"
            val path = data1.path
            data1 = if (!InputHelper.isEmpty(path)) {
                if (path!![0] == '/') {
                    Uri.parse(prefix + path)
                } else {
                    Uri.parse("$prefix/$path")
                }
            } else {
                Uri.parse(prefix)
            }
        }
        return if (data1.pathSegments != null && data1.pathSegments.isNotEmpty()) {
            if (IGNORED_LIST.contains(data1.pathSegments[0])) null else getIntentForURI(
                context,
                data1,
                showRepoBtn
            )
        } else null
    }

    private fun getIntentForURI(context: Context, data: Uri, showRepoBtn: Boolean): Intent? {
        val authority = data.authority
        val isEnterprise = PrefGetter.isEnterprise && isEnterprise(authority ?: data.toString())
        if (HOST_GISTS == data.host || "gist".equals(data.pathSegments[0], ignoreCase = true)) {
            val extension = MimeTypeMap.getFileExtensionFromUrl(data.toString())
            if (!InputHelper.isEmpty(extension) && !MarkDownProvider.isArchive(data.lastPathSegment)) {
                val url = data.toString()
                return CodeViewerActivity.createIntent(context, url, url)
            }
            val gist = getGistId(data)
            if (gist != null) {
                return GistActivity.createIntent(context, gist, isEnterprise)
            }
        } else if (HOST_GISTS_RAW.equals(data.host, ignoreCase = true)) {
            return getGistFile(context, data)
        } else {
            if (MarkDownProvider.isArchive(data.toString())) return null
            if (TextUtils.equals(authority, HOST_DEFAULT) || TextUtils.equals(
                    authority,
                    RAW_AUTHORITY
                ) ||
                TextUtils.equals(authority, API_AUTHORITY) || isEnterprise
            ) {
                val trending = getTrending(context, data)
                val projects = getRepoProject(context, data)
                val userIntent = getUser(context, data)
                val repoIssues = getRepoIssueIntent(context, data)
                val repoPulls = getRepoPullRequestIntent(context, data)
                val createIssueIntent = getCreateIssueIntent(context, data)
                val pullRequestIntent = getPullRequestIntent(context, data, showRepoBtn)
                val issueIntent = getIssueIntent(context, data, showRepoBtn)
                val releasesIntent = getReleases(context, data, isEnterprise)
                val repoIntent = getRepo(context, data)
                val repoWikiIntent = getWiki(context, data)
                val commit = getCommit(context, data, showRepoBtn)
                val commits = getCommits(context, data, showRepoBtn)
                val blob = getBlob(context, data)
                val label = getLabel(context, data)
                val search = getSearchIntent(context, data)
                val intentOptional = returnNonNull(
                    trending,
                    projects,
                    search,
                    userIntent,
                    repoIssues,
                    repoPulls,
                    pullRequestIntent,
                    label,
                    commit,
                    commits,
                    createIssueIntent,
                    issueIntent,
                    releasesIntent,
                    repoIntent,
                    repoWikiIntent,
                    blob
                )
                val empty = Optional.empty<Intent>()
                return if (intentOptional.isPresent && intentOptional !== empty) {
                    val intent = intentOptional.get()
                    if (isEnterprise) {
                        if (intent.extras != null) {
                            val bundle = intent.extras
                            bundle!!.putBoolean(BundleConstant.IS_ENTERPRISE, true)
                            intent.putExtras(bundle)
                        } else {
                            intent.putExtra(BundleConstant.IS_ENTERPRISE, true)
                        }
                    }
                    intent
                } else {
                    val intent = getGeneralRepo(context, data)
                    if (isEnterprise) {
                        if (intent != null && intent.extras != null) {
                            val bundle = intent.extras
                            bundle!!.putBoolean(BundleConstant.IS_ENTERPRISE, true)
                            intent.putExtras(bundle)
                        } else intent?.putExtra(BundleConstant.IS_ENTERPRISE, true)
                    }
                    intent
                }
            }
        }
        return null
    }

    private fun getInvitationIntent(uri: Uri): Boolean {
        val segments = uri.pathSegments
        return segments != null && segments.size == 3 && "invitations".equals(
            uri.lastPathSegment,
            ignoreCase = true
        )
    }

    private fun getPullRequestIntent(context: Context, uri: Uri, showRepoBtn: Boolean): Intent? {
        val segments = uri.pathSegments
        if (segments == null || segments.size < 3) return null
        var owner: String? = null
        var repo: String? = null
        var number: String? = null
        var fragment = uri.encodedFragment //#issuecomment-332236665
        var commentId: Long? = null
        if (!InputHelper.isEmpty(fragment) && fragment!!.split("-").toTypedArray().size > 1) {
            fragment = fragment.split("-").toTypedArray()[1]
            if (!InputHelper.isEmpty(fragment)) {
                try {
                    commentId = fragment.toLong()
                } catch (ignored: Exception) {
                }
            }
        }
        if (segments.size > 3) {
            if ("pull" == segments[2] || "pulls" == segments[2]) {
                owner = segments[0]
                repo = segments[1]
                number = segments[3]
            } else if (("pull" == segments[3] || "pulls" == segments[3]) && segments.size > 4) {
                owner = segments[1]
                repo = segments[2]
                number = segments[4]
            } else {
                return null
            }
        }
        if (InputHelper.isEmpty(number)) return null
        val issueNumber: Int = try {
            number!!.toInt()
        } catch (nfe: NumberFormatException) {
            return null
        }
        return if (issueNumber < 1) null else PullRequestPagerActivity.createIntent(
            context, repo!!, owner!!, issueNumber, showRepoBtn,
            isEnterprise(uri.toString()), commentId ?: 0
        )
    }

    private fun getIssueIntent(context: Context, uri: Uri, showRepoBtn: Boolean): Intent? {
        val segments = uri.pathSegments
        if (segments == null || segments.size < 3) return null
        var owner: String? = null
        var repo: String? = null
        var number: String? = null
        var fragment = uri.encodedFragment //#issuecomment-332236665
        var commentId: Long? = null
        if (!InputHelper.isEmpty(fragment) && fragment!!.split("-").toTypedArray().size > 1) {
            fragment = fragment.split("-").toTypedArray()[1]
            if (!InputHelper.isEmpty(fragment)) {
                try {
                    commentId = fragment.toLong()
                } catch (ignored: Exception) {
                }
            }
        }
        if (segments.size > 3) {
            if (segments[2].equals("issues", ignoreCase = true)) {
                owner = segments[0]
                repo = segments[1]
                number = segments[3]
            } else if (segments[3].equals("issues", ignoreCase = true) && segments.size > 4) {
                owner = segments[1]
                repo = segments[2]
                number = segments[4]
            } else {
                return null
            }
        }
        if (InputHelper.isEmpty(number)) return null
        val issueNumber: Int = try {
            number!!.toInt()
        } catch (nfe: NumberFormatException) {
            return null
        }
        return if (issueNumber < 1) null else IssuePagerActivity.createIntent(
            context, repo!!, owner!!, issueNumber, showRepoBtn,
            isEnterprise(uri.toString()), commentId ?: 0
        )
    }

    private fun getLabel(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments == null || segments.size < 3) return null
        val owner = segments[0]
        val repoName = segments[1]
        val lastPath = segments[2]
        return if ("labels".equals(lastPath, ignoreCase = true)) {
            FilterIssuesActivity.getIntent(
                context,
                owner,
                repoName,
                "label:\"" + segments[3] + "\""
            )
        } else null
    }

    private fun getRepo(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments == null || segments.size < 2 || segments.size > 3) return null
        val owner = segments[0]
        var repoName = segments[1]
        if (!InputHelper.isEmpty(repoName)) {
            if (repoName.endsWith(".git")) repoName = repoName.replace(".git", "")
        }
        return if (segments.size == 3) {
            val lastPath = uri.lastPathSegment
            when {
                "milestones".equals(lastPath, ignoreCase = true) -> {
                    RepoPagerActivity.createIntent(context, repoName, owner, RepoPagerMvp.CODE, 4)
                }
                "network".equals(lastPath, ignoreCase = true) -> {
                    RepoPagerActivity.createIntent(context, repoName, owner, RepoPagerMvp.CODE, 3)
                }
                "stargazers".equals(lastPath, ignoreCase = true) -> {
                    RepoPagerActivity.createIntent(context, repoName, owner, RepoPagerMvp.CODE, 2)
                }
                "watchers".equals(lastPath, ignoreCase = true) -> {
                    RepoPagerActivity.createIntent(context, repoName, owner, RepoPagerMvp.CODE, 1)
                }
                "labels".equals(lastPath, ignoreCase = true) -> {
                    RepoPagerActivity.createIntent(context, repoName, owner, RepoPagerMvp.CODE, 5)
                }
                else -> {
                    null
                }
            }
        } else {
            RepoPagerActivity.createIntent(context, repoName, owner)
        }
    }

    private fun getRepoProject(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments == null || segments.size < 3) return null
        val owner = segments[0]
        val repoName = segments[1]
        if (segments.size == 3 && "projects".equals(segments[2], ignoreCase = true)) {
            return RepoPagerActivity.createIntent(context, repoName, owner, RepoPagerMvp.PROJECTS)
        } else if (segments.size == 4 && "projects".equals(segments[2], ignoreCase = true)) {
            try {
                val projectId = segments[segments.size - 1].toInt()
                if (projectId > 0) {
                    return getIntent(
                        context, owner, repoName, projectId.toLong(),
                        isEnterprise(uri.toString())
                    )
                }
            } catch (ignored: Exception) {
            }
        }
        return null
    }

    private fun getWiki(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments == null || segments.size < 3) return null
        if ("wiki".equals(segments[2], ignoreCase = true)) {
            val owner = segments[0]
            val repoName = segments[1]
            return getWiki(
                context, repoName, owner,
                if ("wiki".equals(
                        uri.lastPathSegment,
                        ignoreCase = true
                    )
                ) null else uri.lastPathSegment
            )
        }
        return null
    }

    /**
     * [[k0shk0sh, FastHub, issues], k0shk0sh/fastHub/(issues,pulls,commits, etc)]
     */
    private fun getGeneralRepo(context: Context, uri: Uri): Intent? {
        //TODO parse deeper links to their associate views. meantime fallback to repoPage
        if (getInvitationIntent(uri)) {
            return null
        }
        val isEnterprise =
            PrefGetter.isEnterprise && Uri.parse(getEndpoint(PrefGetter.enterpriseUrl!!)).authority
                .equals(uri.authority, ignoreCase = true)
        if (uri.authority == HOST_DEFAULT || uri.authority == API_AUTHORITY || isEnterprise) {
            val segments = uri.pathSegments
            if (segments == null || segments.isEmpty()) return null
            if (segments.size == 1) {
                return getUser(context, uri)
            } else if (segments.size > 1) {
                return if (segments[0].equals("repos", ignoreCase = true) && segments.size >= 2) {
                    val owner = segments[1]
                    val repoName = segments[2]
                    RepoPagerActivity.createIntent(context, repoName, owner)
                } else if ("orgs".equals(segments[0], ignoreCase = true)) {
                    null
                } else {
                    val owner = segments[0]
                    val repoName = segments[1]
                    RepoPagerActivity.createIntent(context, repoName, owner)
                }
            }
        }
        return null
    }

    private fun getCommits(context: Context, uri: Uri, showRepoBtn: Boolean): Intent? {
        val segments = Stream.of(uri.pathSegments)
            .filter { value: String ->
                !value.equals(
                    "api",
                    ignoreCase = true
                ) || !value.equals("v3", ignoreCase = true)
            }
            .toList()
        if (segments.isEmpty() || segments.size < 3) return null
        var login: String? = null
        var repoId: String? = null
        var sha: String? = null
        if (segments.size > 3 && segments[3] == "commits") {
            login = segments[1]
            repoId = segments[2]
            sha = segments[4]
        } else if (segments.size > 2 && segments[2] == "commits") {
            login = segments[0]
            repoId = segments[1]
            sha = uri.lastPathSegment
        }
        return if (login != null && sha != null && repoId != null) {
            CommitPagerActivity.createIntent(context, repoId, login, sha, showRepoBtn)
        } else null
    }

    private fun getCommit(context: Context, uri: Uri, showRepoBtn: Boolean): Intent? {
        val segments = Stream.of(uri.pathSegments)
            .filter { value: String ->
                !value.equals(
                    "api",
                    ignoreCase = true
                ) || !value.equals("v3", ignoreCase = true)
            }
            .toList()
        if (segments.size < 3 || "commit" != segments[2]) return null
        val login = segments[0]
        val repoId = segments[1]
        val sha = segments[3]
        return CommitPagerActivity.createIntent(context, repoId, login, sha, showRepoBtn)
    }

    @JvmStatic
    private val wordRegex = "[a-fA-F0-9]+".toRegex()

    private fun getGistId(uri: Uri): String? {
        val segments = uri.pathSegments
        if (segments.size != 1 && segments.size != 2) return null
        val gistId = segments[segments.size - 1]
        if (InputHelper.isEmpty(gistId)) return null
        return if (TextUtils.isDigitsOnly(gistId)) gistId else if (gistId.matches(wordRegex)) gistId else null
    }

    private fun getUser(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments != null && segments.size == 1) {
            return createIntent(context, segments[0])
        } else {
            if (segments != null && segments.isNotEmpty()) {
                segments.size
                if (segments[0].equals("orgs", ignoreCase = true)) {
                    return when {
                        "invitation".equals(uri.lastPathSegment, ignoreCase = true) -> {
                            null
                        }
                        "search".equals(uri.lastPathSegment, ignoreCase = true) -> {
                            val query = uri.getQueryParameter("q")
                            SearchActivity.getIntent(context, query)
                        }
                        else -> {
                            createIntent(context, segments[1], true)
                        }
                    }
                }
            }
        }
        return null
    }

    private fun getBlob(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments == null || segments.size < 4) return null
        val segmentTwo = segments[2]
        val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        if (InputHelper.isEmpty(extension) || TextUtils.isDigitsOnly(extension)) {
            val urlBuilder = getBlobBuilder(uri)
            return RepoFilesActivity.getIntent(context, urlBuilder.toString())
        }
        if (segmentTwo == "blob" || segmentTwo == "tree") {
            val urlBuilder = getBlobBuilder(uri)
            Logger.e(urlBuilder)
            return CodeViewerActivity.createIntent(context, urlBuilder.toString(), uri.toString())
        } else {
            val authority = uri.authority
            if (TextUtils.equals(authority, RAW_AUTHORITY)) {
                return CodeViewerActivity.createIntent(context, uri.toString(), uri.toString())
            }
        }
        return null
    }

    private fun getRepoIssueIntent(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments != null && segments.size == 3 && uri.lastPathSegment.equals(
                "issues",
                ignoreCase = true
            )
        ) {
            val owner = segments[0]
            val repo = segments[1]
            val encoded = Uri.parse(uri.toString().replace("utf8=%E2%9C%93&amp;", ""))
            if (encoded.getQueryParameter("q") != null) {
                val query = encoded.getQueryParameter("q")
                return FilterIssuesActivity.getIntent(context, owner, repo, query!!)
            }
            return RepoPagerActivity.createIntent(context, repo, owner, RepoPagerMvp.ISSUES)
        }
        return null
    }

    private fun getRepoPullRequestIntent(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments != null && segments.size == 3 && uri.lastPathSegment.equals(
                "pulls",
                ignoreCase = true
            )
        ) {
            val owner = segments[0]
            val repo = segments[1]
            val encoded = Uri.parse(uri.toString().replace("utf8=%E2%9C%93&amp;", ""))
            if (encoded.getQueryParameter("q") != null) {
                val query = encoded.getQueryParameter("q")
                return FilterIssuesActivity.getIntent(context, owner, repo, query!!)
            }
            return RepoPagerActivity.createIntent(context, repo, owner, RepoPagerMvp.PULL_REQUEST)
        }
        return null
    }

    private fun getReleases(context: Context, uri: Uri, isEnterprise: Boolean): Intent? {
        val segments = uri.pathSegments
        if (segments != null && segments.size > 2) {
            if (uri.pathSegments[2] == "releases") {
                val owner = segments[0]
                val repo = segments[1]
                val tag = uri.lastPathSegment
                return if (tag != null && !repo.equals(tag, ignoreCase = true)) {
                    if (TextUtils.isDigitsOnly(tag)) {
                        ReleasesListActivity.getIntent(
                            context,
                            owner,
                            repo,
                            InputHelper.toLong(tag),
                            isEnterprise
                        )
                    } else {
                        ReleasesListActivity.getIntent(context, owner, repo, tag, isEnterprise)
                    }
                } else ReleasesListActivity.getIntent(context, owner, repo)
            } else if (segments.size > 3 && segments[3].equals("releases", ignoreCase = true)) {
                val owner = segments[1]
                val repo = segments[2]
                val tag = uri.lastPathSegment
                return if (tag != null && !repo.equals(tag, ignoreCase = true)) {
                    if (TextUtils.isDigitsOnly(tag)) {
                        ReleasesListActivity.getIntent(
                            context,
                            owner,
                            repo,
                            InputHelper.toLong(tag),
                            isEnterprise
                        )
                    } else {
                        ReleasesListActivity.getIntent(context, owner, repo, tag, isEnterprise)
                    }
                } else ReleasesListActivity.getIntent(context, owner, repo)
            }
            return null
        }
        return null
    }

    private fun getTrending(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments != null && segments.isNotEmpty()) {
            if (uri.pathSegments[0] == "trending") {
                var query: String? = ""
                var lang: String? = ""
                if (uri.pathSegments.size > 1) {
                    lang = uri.pathSegments[1]
                }
                if (uri.queryParameterNames != null && uri.queryParameterNames.isNotEmpty()) {
                    query = uri.getQueryParameter("since")
                }
                return getTrendingIntent(context, lang, query)
            }
            return null
        }
        return null
    }

    /**
     * https://github.com/owner/repo/issues/new
     */
    private fun getCreateIssueIntent(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (uri.lastPathSegment == null) return null
        if (segments == null || segments.size < 3 || !uri.lastPathSegment.equals(
                "new",
                ignoreCase = true
            )
        ) return null
        if ("issues" == segments[2]) {
            val owner = segments[0]
            val repo = segments[1]
            val isFeedback = "LightDestory/FastHub-RE".equals("$owner/$repo", ignoreCase = true)
            return CreateIssueActivity.getIntent(context, owner, repo, isFeedback)
        }
        return null
    }

    private fun getGistFile(context: Context, uri: Uri): Intent? {
        return if (HOST_GISTS_RAW.equals(uri.host, ignoreCase = true)) {
            CodeViewerActivity.createIntent(context, uri.toString(), uri.toString())
        } else null
    }

    private fun getSearchIntent(context: Context, uri: Uri): Intent? {
        val segments = uri.pathSegments
        if (segments == null || segments.size > 1) return null
        val search = segments[0]
        if ("search".equals(search, ignoreCase = true)) {
            val encoded = Uri.parse(uri.toString().replace("utf8=%E2%9C%93&amp;", ""))
            val query = encoded.getQueryParameter("q")
            Logger.e(encoded, query)
            return SearchActivity.getIntent(context, query)
        }
        return null
    }
}