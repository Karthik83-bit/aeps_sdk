package com.example.aeps_sdk.unifiedaeps.api.services

import com.example.aeps_sdk.unifiedaeps.models.response.EncodedUrlResponse
import com.example.aeps_sdk.unifiedaeps.models.response.PropAddressResponse
import com.example.aeps_sdk.unifiedaeps.models.req.BioAuthSubmitRequest
import com.example.aeps_sdk.unifiedaeps.models.req.EncodedUrlRequest
import com.example.aeps_sdk.unifiedaeps.models.req.SetAddressRequest
import com.example.aeps_sdk.unifiedaeps.models.response.SubmitBioAuthResponse
import com.example.aeps_sdk.unifiedaeps.models.response.UpdateUserPropAddress
import retrofit2.Response
import retrofit2.http.*

interface BioAuthApi {

    @GET("generate/v118")
    suspend fun getEncodedUrlForAddressStatus():Response<EncodedUrlResponse>

    @GET()
    suspend fun viewUserPropAddress(@Header("Authorization") token:String,@Url url:String):Response<PropAddressResponse>

    @POST("generate/v82")
    suspend fun getEncodedUrlForSetAddress(@Body encodedUrlRequest: EncodedUrlRequest):Response<EncodedUrlResponse>

    @POST()
    suspend fun updateUserPropAddress(@Header("Authorization") token:String,@Url url:String,@Body setAddressRequest: SetAddressRequest):Response<UpdateUserPropAddress>

    @GET("generate/v80")
    suspend fun getEncodedUrlForSubmitBioAuth():Response<EncodedUrlResponse>

    @POST()
    suspend fun submitBioAuth(@Header("Authorization") token:String,@Url url:String,@Body submitRequest: BioAuthSubmitRequest):Response<SubmitBioAuthResponse>

}