package com.example.aeps_sdk.unifiedaeps.bankspinner

import android.content.Context

class BankNameContract {
    /**
     * View interface sends report list to ReportActivity
     */
    interface View {
        /**
         * showReports() showReports on ReportActivity
         */
        fun bankNameListReady(bankNameModelArrayList: ArrayList<BankNameModel>)
        fun showBankNames()
        fun showLoader()
        fun hideLoader()
        fun emptyBanks()
    }

    /**
     * UserActionsListener interface checks the load of Reports
     */
    internal interface UserActionsListener {
        fun loadBankNamesList(context: Context?)
    }
}
