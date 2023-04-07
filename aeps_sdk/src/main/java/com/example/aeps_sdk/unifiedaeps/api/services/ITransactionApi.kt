package com.example.aeps_sdk.unifiedaeps.api.services

import com.example.aeps_sdk.unifiedaeps.models.response.EncodedUrlResponse
import com.example.aeps_sdk.unifiedaeps.models.req.TransactionRequest
import com.example.aeps_sdk.unifiedaeps.models.response.TransactionStatusResponse
import retrofit2.Response
import retrofit2.http.*

interface ITransactionApi {
    @GET("generate/v102")
    suspend fun getBalanceEnqEncodedUrl():Response<EncodedUrlResponse>

    @GET("generate/v103")
    suspend fun getCashWithdrawalEncodedUrl():Response<EncodedUrlResponse>

    @GET("generate/v104")
    suspend fun getMiniStatementEncodedUrl():Response<EncodedUrlResponse>

    @POST()
    suspend fun getTransactionStatus(@Header("Authorization") token:String, @Url url:String,@Body transactionRequest: TransactionRequest):Response<TransactionStatusResponse>


}