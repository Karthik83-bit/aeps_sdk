package com.example.aeps_sdk.unifiedaeps.models.response

data class Response(
    val apiUserName: Any,
    val bioauth: Boolean,
    val city: String,
    val latLong: String,
    val pincode: String,
    val state: String
)