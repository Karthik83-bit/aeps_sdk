package com.example.aeps_sdk.unifiedaeps.api
import com.example.aeps_sdk.application.AppController
import com.example.aeps_sdk.utils.NetworkContants.ADDRESS_BASE_URL
import com.example.aeps_sdk.utils.NetworkContants.BIO_AUTH_BASE_URL
import com.example.aeps_sdk.utils.NetworkContants.UNIFIED_AEPS_BASE_URL
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object RetrofitClient {
    @Volatile
    private var retrofit : Retrofit ? = null
    private var app : AppController = AppController()
    fun getBioAuthClient() : Retrofit {
        var bioAuthInstance  : Retrofit? = retrofit
        if(bioAuthInstance == null){
            synchronized(app){
                bioAuthInstance = retrofit
                if(bioAuthInstance == null){
                    retrofit = Retrofit.Builder()
                        .baseUrl(BIO_AUTH_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(OkHttpProvider.getOkHttpClient())
                        .build()
                }
            }

        }

        return retrofit!!
    }

    @Volatile
    private var addressRetrofit : Retrofit ? = null
    fun getAddressClient():Retrofit{
        var addressInstant  : Retrofit? = addressRetrofit
        if(addressInstant==null){
            synchronized(app){
                addressInstant= addressRetrofit
                if(addressInstant==null){
                    addressRetrofit =Retrofit.Builder()
                        .baseUrl(ADDRESS_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(OkHttpProvider.getOkHttpClient())
                        .build()
                }
            }
        }
        return addressRetrofit!!
    }

    @Volatile
    private var unifiedAepsRetrofit : Retrofit ? = null
    fun getUnifiedAepsClient():Retrofit{
        var unifiedAepsInstant  : Retrofit? = unifiedAepsRetrofit
        if(unifiedAepsInstant==null){
            synchronized(app){
                unifiedAepsInstant= unifiedAepsRetrofit
                if(unifiedAepsInstant==null){
                    unifiedAepsRetrofit =Retrofit.Builder()
                        .baseUrl(UNIFIED_AEPS_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(OkHttpProvider.getOkHttpClient())
                        .build()
                }
            }
        }
        return unifiedAepsRetrofit!!
    }

}