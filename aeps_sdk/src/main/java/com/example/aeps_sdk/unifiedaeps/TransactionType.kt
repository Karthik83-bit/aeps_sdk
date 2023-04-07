package com.example.aeps_sdk.unifiedaeps


enum class DebitOrCredit {
    DEBIT, CREDIT, D, C
}

class TransactionType(var debitCredit: DebitOrCredit) {
    fun transactionType(): String {
        return when (debitCredit) {
            DebitOrCredit.DEBIT -> "Dr "
            DebitOrCredit.D -> "Dr "
            DebitOrCredit.CREDIT -> "Cr "
            DebitOrCredit.C -> "Cr "
        }
    }
}
