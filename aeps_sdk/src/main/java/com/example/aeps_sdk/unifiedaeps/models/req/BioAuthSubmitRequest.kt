package com.example.aeps_sdk.unifiedaeps.models.req

data class BioAuthSubmitRequest(
    val aadharNo: String,
    val apiUserName: String,
    val ci: String,
    val dc: String,
    val dpId: String,
    val encryptedPID: String,
    val hMac: String,
    val isSL: Boolean,
    val mcData: String,
    val mi: String,
    val operation: String,
    val rdsId: String,
    val rdsVer: String,
    val retailer: String,
    val sKey: String
)