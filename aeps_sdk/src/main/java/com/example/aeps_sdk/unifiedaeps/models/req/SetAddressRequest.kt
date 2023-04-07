package com.example.aeps_sdk.unifiedaeps.models.req

data class SetAddressRequest(
    val apiUserName: String,
    val city: String,
    val latLong: String,
    val pincode: String,
    val state: String
)