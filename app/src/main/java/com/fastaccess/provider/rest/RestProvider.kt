package com.fastaccess.provider.rest

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.text.TextUtils
import com.fastaccess.BuildConfig
import com.fastaccess.R
import com.fastaccess.data.dao.GitHubErrorResponse
import com.fastaccess.data.dao.GithubStatusComponentsModel
import com.fastaccess.data.service.*
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.helper.PrefGetter.enterpriseToken
import com.fastaccess.helper.PrefGetter.enterpriseUrl
import com.fastaccess.helper.PrefGetter.isEnterprise
import com.fastaccess.helper.PrefGetter.token
import com.fastaccess.provider.rest.converters.GithubResponseConverter
import com.fastaccess.provider.rest.interceptors.AuthenticationInterceptor
import com.fastaccess.provider.rest.interceptors.ContentTypeInterceptor
import com.fastaccess.provider.rest.interceptors.PaginationInterceptor
import com.fastaccess.provider.scheme.LinkParserHelper.getEndpoint
import com.fastaccess.provider.scheme.LinkParserHelper.isEnterprise
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.File
import java.lang.reflect.Modifier

/**
 * Created by Kosh on 08 Feb 2017, 8:37 PM
 */
object RestProvider {
    const val PAGE_SIZE = 10
    private var okHttpClient: OkHttpClient? = null

    @JvmField
    val gson: Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .disableHtmlEscaping()
        .setPrettyPrinting()
        .create()

    fun provideOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            val client = OkHttpClient.Builder()
            if (BuildConfig.DEBUG) {
                client.addInterceptor(
                    HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
            }
            client.addInterceptor(AuthenticationInterceptor())
            client.addInterceptor(PaginationInterceptor())
            client.addInterceptor(ContentTypeInterceptor())
            okHttpClient = client.build()
        }
        return okHttpClient!!
    }

    private fun provideRetrofit(enterprise: Boolean): Retrofit {
        return Retrofit.Builder()
            .baseUrl(
                if (enterprise && isEnterprise) getEndpoint(enterpriseUrl!!) else BuildConfig.REST_URL
            )
            .client(provideOkHttpClient())
            .addConverterFactory(GithubResponseConverter(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @JvmStatic
    fun downloadFile(context: Context, url: String) {
        downloadFile(context, url, null)
    }

    @JvmStatic
    fun downloadFile(context: Context, url: String, extension: String? = null) {
        if (isEmpty(url)) return
        val isEnterprise = isEnterprise(url)
        val uri = Uri.parse(url)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(uri)
        val authToken = if (isEnterprise) enterpriseToken else token
        if (!TextUtils.isEmpty(authToken)) {
            assert(authToken != null)
            request.addRequestHeader(
                "Authorization",
                if (authToken!!.startsWith("Basic")) authToken else "token $authToken"
            )
        }
        var fileName = File(url).name
        if (!isEmpty(extension)) {
            fileName += if (extension!!.startsWith(".")) {
                extension
            } else {
                ".$extension"
            }
        }
        request.setDestinationInExternalPublicDir(
            Environment.DIRECTORY_DOWNLOADS,
            context.getString(R.string.app_name) + "/" + fileName
        )
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE or DownloadManager.Request.NETWORK_WIFI)
        request.setTitle(fileName)
        request.setDescription(context.getString(R.string.downloading_file))
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        downloadManager.enqueue(request)
    }

    @JvmStatic
    fun getErrorCode(throwable: Throwable?): Int {
        return if (throwable is HttpException) {
            throwable.code()
        } else -1
    }

    @JvmStatic
    fun getUserService(enterprise: Boolean): UserRestService {
        return provideRetrofit(enterprise).create(UserRestService::class.java)
    }

    @JvmStatic
    fun getGistService(enterprise: Boolean): GistService {
        return provideRetrofit(enterprise).create(GistService::class.java)
    }

    @JvmStatic
    fun getRepoService(enterprise: Boolean): RepoService {
        return provideRetrofit(enterprise).create(RepoService::class.java)
    }

    @JvmStatic
    fun getIssueService(enterprise: Boolean): IssueService {
        return provideRetrofit(enterprise).create(IssueService::class.java)
    }

    @JvmStatic
    fun getPullRequestService(enterprise: Boolean): PullRequestService {
        return provideRetrofit(enterprise).create(
            PullRequestService::class.java
        )
    }

    @JvmStatic
    fun getNotificationService(enterprise: Boolean): NotificationService {
        return provideRetrofit(enterprise).create(
            NotificationService::class.java
        )
    }

    @JvmStatic
    fun getReactionsService(enterprise: Boolean): ReactionsService {
        return provideRetrofit(enterprise).create(ReactionsService::class.java)
    }

    @JvmStatic
    fun getOrgService(enterprise: Boolean): OrganizationService {
        return provideRetrofit(enterprise).create(
            OrganizationService::class.java
        )
    }

    @JvmStatic
    fun getReviewService(enterprise: Boolean): ReviewService {
        return provideRetrofit(enterprise).create(ReviewService::class.java)
    }

    val contribution: UserRestService
        get() = Retrofit.Builder()
            .baseUrl(BuildConfig.REST_URL)
            .addConverterFactory(GithubResponseConverter(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(UserRestService::class.java)

    @JvmStatic
    fun getSearchService(enterprise: Boolean): SearchService {
        return provideRetrofit(enterprise).create(SearchService::class.java)
    }

    val slackService: SlackService
        get() = Retrofit.Builder()
            .baseUrl("https://ok13pknpj4.execute-api.eu-central-1.amazonaws.com/prod/")
            .addConverterFactory(GithubResponseConverter(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(SlackService::class.java)

    @JvmStatic
    fun getContentService(enterprise: Boolean): ContentService {
        return provideRetrofit(enterprise).create(ContentService::class.java)
    }

    fun getProjectsService(enterprise: Boolean): ProjectsService {
        return provideRetrofit(enterprise).create(ProjectsService::class.java)
    }

    fun getErrorResponse(throwable: Throwable): GitHubErrorResponse? {
        var body: ResponseBody? = null
        if (throwable is HttpException) {
            body = throwable.response()!!.errorBody()
        }
        if (body != null) {
            try {
                return gson.fromJson(body.string(), GitHubErrorResponse::class.java)
            } catch (ignored: Exception) {
            }
        }
        return null
    }

    fun gitHubStatus(): Observable<GithubStatusComponentsModel> {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.GITHUB_STATUS_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(GithubResponseConverter(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ContentService::class.java)
            .checkStatus()
    }

    @JvmStatic
    fun clearHttpClient() {
        okHttpClient = null
    }
}
