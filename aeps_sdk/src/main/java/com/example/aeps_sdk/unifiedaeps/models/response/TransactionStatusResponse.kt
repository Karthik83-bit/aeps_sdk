package com.example.aeps_sdk.unifiedaeps.models.response

data class TransactionStatusResponse(
    val apiComment: String,
    val apiTid: String,
    val balance: String,
    val bankName: String,
    val createdDate: String,
    val errors: Any,
    val gateway: Int,
    val iin: String,
    val isRetriable: Boolean,
    val ministatement: List<MiniStatement>?,
    val origin_identifier: String,
    val status: String,
    val transactionMode: String,
    val txId: String,
    val updatedDate: String
)