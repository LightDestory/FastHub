package com.fastaccess.provider.rest

import com.fastaccess.BuildConfig
import com.fastaccess.data.service.LoginRestService
import com.fastaccess.helper.InputHelper.isEmpty
import com.fastaccess.provider.rest.converters.GithubResponseConverter
import com.fastaccess.provider.rest.interceptors.AuthenticationInterceptor
import com.fastaccess.provider.scheme.LinkParserHelper.getEndpoint
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.reflect.Modifier

/**
 * Created by Kosh on 08 Feb 2017, 8:37 PM
 */
object LoginProvider {
    private val gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
        .setDateFormat("yyyy-MM-dd HH:mm:ss")
        .setPrettyPrinting()
        .create()

    private fun provideOkHttpClient(authToken: String?, otp: String?): OkHttpClient {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            client.addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }
        client.addInterceptor(AuthenticationInterceptor(authToken, otp))
        return client.build()
    }

    private fun provideRetrofit(
        authToken: String?,
        otp: String?,
        enterpriseUrl: String?
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(
                if (isEmpty(enterpriseUrl)) BuildConfig.REST_URL else getEndpoint(
                    enterpriseUrl!!
                )
            )
            .client(provideOkHttpClient(authToken, otp))
            .addConverterFactory(GithubResponseConverter(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    @JvmStatic
    val loginRestService: LoginRestService
        get() = Retrofit.Builder()
            .baseUrl("https://github.com/login/oauth/")
            .client(provideOkHttpClient(null, null))
            .addConverterFactory(GithubResponseConverter(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(LoginRestService::class.java)

    fun getLoginRestService(
        authToken: String, otp: String?,
        endpoint: String?
    ): LoginRestService {
        return provideRetrofit(authToken, otp, endpoint).create(
            LoginRestService::class.java
        )
    }
}