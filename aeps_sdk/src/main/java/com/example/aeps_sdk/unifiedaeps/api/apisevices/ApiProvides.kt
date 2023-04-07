package com.example.aeps_sdk.unifiedaeps.api.apisevices

import com.example.aeps_sdk.unifiedaeps.api.RetrofitClient
import com.example.aeps_sdk.unifiedaeps.api.services.AddressApi
import com.example.aeps_sdk.unifiedaeps.api.services.BioAuthApi
import com.example.aeps_sdk.unifiedaeps.api.services.ITransactionApi
import com.example.aeps_sdk.application.AppController
import retrofit2.create

object ApiProvides {
    @Volatile
    private var bioAuthApi: BioAuthApi? = null
    private var any: AppController = AppController()
    fun getBioAuthApi(): BioAuthApi {
        var singletonInstant: BioAuthApi? = bioAuthApi
        if (singletonInstant == null) {
            synchronized(any) {
                singletonInstant = bioAuthApi
                if (singletonInstant == null) {
                    bioAuthApi =
                        RetrofitClient.getBioAuthClient().create(BioAuthApi::class.java)
                }
            }
        }
        return bioAuthApi!!
    }

    @Volatile
    private var addressApi: AddressApi? = null
    fun getAddressApi(): AddressApi {
        var singletonInstant: AddressApi? = addressApi
        if (singletonInstant == null) {
            synchronized(any) {
                singletonInstant = addressApi
                if (singletonInstant == null) {
                    addressApi =
                        RetrofitClient.getAddressClient().create(AddressApi::class.java)
                }
            }
        }
        return addressApi!!

    }
    @Volatile
    private var transactionApi: ITransactionApi? = null
    fun getTransactionApi(): ITransactionApi {
        var singletonInstant: ITransactionApi? = transactionApi
        if (singletonInstant == null) {
            synchronized(any) {
                singletonInstant = transactionApi
                if (singletonInstant == null) {
                    transactionApi =
                        RetrofitClient.getUnifiedAepsClient().create(ITransactionApi::class.java)
                }
            }
        }
        return transactionApi!!

    }

}