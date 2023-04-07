package com.example.aeps_sdk.unifiedaeps.models.response

data class SubmitBioAuthResponse(
    val balance: Any,
    val bankRrn: String,
    val bioRespCode: String,
    val bname: Any,
    val cauth: Any,
    val cbsauthValue: Any,
    val date: String,
    val errors: Any,
    val message: String,
    val miniStat: Any,
    val respCode: Any,
    val reversalMessage: Any,
    val rname: Any,
    val rrn: String,
    val status: String,
    val uAuthCode: Any,
    val uidaiToken: String
)