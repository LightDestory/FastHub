package com.fastaccess.provider.rest

import com.fastaccess.BuildConfig
import com.fastaccess.data.service.ImgurService
import com.fastaccess.provider.rest.converters.GithubResponseConverter
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.lang.reflect.Modifier

/**
 * Created by Kosh on 15 Apr 2017, 7:59 PM
 */
object ImgurProvider {
    val gson: Gson = GsonBuilder()
        .excludeFieldsWithModifiers(Modifier.FINAL, Modifier.TRANSIENT, Modifier.STATIC)
        .setPrettyPrinting()
        .create()

    private fun provideOkHttpClient(): OkHttpClient {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            client.addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }
        client.addInterceptor(Interceptor { chain: Interceptor.Chain ->
            val original = chain.request()
            val requestBuilder: Request.Builder = original.newBuilder()
            requestBuilder.header("Authorization", "Client-ID " + BuildConfig.IMGUR_CLIENT_ID)
            requestBuilder.method(original.method, original.body)
            val request: Request = requestBuilder.build()
            chain.proceed(request)
        })
        return client.build()
    }

    private fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.IMGUR_URL)
            .client(provideOkHttpClient())
            .addConverterFactory(GithubResponseConverter(gson))
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
    }

    val imgurService: ImgurService
        get() = provideRetrofit().create(ImgurService::class.java)
}