package com.fastaccess.provider.rest

import com.fastaccess.BuildConfig
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object HttpProvider {
    fun provideOkHttpClient(vararg interceptor: Interceptor): OkHttpClient {
        val client = OkHttpClient.Builder()
        if (BuildConfig.DEBUG) {
            client.addInterceptor(
                HttpLoggingInterceptor()
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
        }
        for (inter in interceptor) {
            client.addInterceptor(inter)
        }
        return client.build()
    }
}