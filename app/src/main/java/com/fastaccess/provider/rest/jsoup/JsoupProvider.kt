package com.fastaccess.provider.rest.jsoup

import com.fastaccess.BuildConfig
import com.fastaccess.data.service.ScrapService
import com.fastaccess.provider.rest.interceptors.AuthenticationInterceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * Created by Kosh on 02 Jun 2017, 12:47 PM
 */
object JsoupProvider {
    private var okHttpClient: OkHttpClient? = null
    private fun provideOkHttpClient(): OkHttpClient {
        if (okHttpClient == null) {
            val client = OkHttpClient.Builder()
            if (BuildConfig.DEBUG) {
                client.addInterceptor(
                    HttpLoggingInterceptor()
                        .setLevel(HttpLoggingInterceptor.Level.BODY)
                )
            }
            client.addInterceptor(AuthenticationInterceptor(true))
            okHttpClient = client.build()
        }
        return okHttpClient!!
    }

    fun getTrendingService(url: String): ScrapService {
        return Retrofit.Builder()
            .baseUrl(url)
            .client(provideOkHttpClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ScrapService::class.java)
    }

    val wiki: ScrapService
        get() = Retrofit.Builder()
            .baseUrl("https://github.com/")
            .client(provideOkHttpClient())
            .addConverterFactory(ScalarsConverterFactory.create())
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(ScrapService::class.java)
}