package com.example.aeps_sdk.unifiedaeps.bankspinner.models

data class BankListResponse(
    val bankIINs: List<BankIIN>,
    val statusCode: Int
)