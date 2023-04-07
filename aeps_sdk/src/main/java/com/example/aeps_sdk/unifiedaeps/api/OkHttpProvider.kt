package com.example.aeps_sdk.unifiedaeps.api

import com.example.aeps_sdk.application.AppController
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpProvider {
    @Volatile
    private var okHttpClient: OkHttpClient? = null
    private var app: AppController = AppController()
    fun getOkHttpClient(): OkHttpClient {
        var singletonInstant: OkHttpClient? = okHttpClient
        if (singletonInstant == null) {
            synchronized(app) {
                singletonInstant = okHttpClient
                if (singletonInstant == null) {
                    val interceptor = HttpLoggingInterceptor()
                    interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
                    okHttpClient = OkHttpClient.Builder()
                        .connectTimeout(60, TimeUnit.SECONDS)
                        .readTimeout(60, TimeUnit.SECONDS)
                        .writeTimeout(60, TimeUnit.SECONDS)
                        .addInterceptor(interceptor)
                        .build()
                }
            }
        }
        return okHttpClient!!
    }
}