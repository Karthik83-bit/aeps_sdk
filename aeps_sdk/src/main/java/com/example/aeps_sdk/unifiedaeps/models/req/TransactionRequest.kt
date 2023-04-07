package com.example.aeps_sdk.unifiedaeps.models.req

data class TransactionRequest(
    val aadharNo: String,
    val amount: String,
    val apiUser: String,
    val apiUserName: String,
    val bankName: String,
    val gatewayPriority: Int,
    val iin: String,
    val latLong: String,
    val mobileNumber: String,
    val paramA: String,
    val paramB: String,
    val paramC: String,
    val pidData: String,
    val retailer: String
)