package com.example.aeps_sdk.unifiedaeps.api.services

import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankListResponse
import com.example.aeps_sdk.unifiedaeps.models.response.AddressResponse
import com.example.aeps_sdk.unifiedaeps.models.req.PinRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AddressApi {
    @POST("pincodeFetch/api/v1/getCitystateAndroid")
    suspend fun getAddressFromPin(@Body pinRequest: PinRequest): Response<AddressResponse>

    @GET("iin/api/v1/getIIN")
    suspend fun getBankList():Response<BankListResponse>
}