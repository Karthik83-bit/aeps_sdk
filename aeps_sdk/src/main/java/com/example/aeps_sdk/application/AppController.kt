package com.example.aeps_sdk.application

import android.app.Application
import com.example.aeps_sdk.unifiedaeps.bankspinner.dao.BankListDatabase
import com.example.aeps_sdk.unifiedaeps.bankspinner.repo.BankListRepo
import com.example.aeps_sdk.unifiedaeps.repo.BioAuthRepo
import com.example.aeps_sdk.unifiedaeps.repo.UnifiedAepsRepo
import com.example.aeps_sdk.unifiedaeps.viewmodel.UnifiedAepsViewModel

class AppController :Application() {
     lateinit var bioAuthRepository: BioAuthRepo
     lateinit var bankListRepo: BankListRepo
     lateinit var unifiedAepsRepo: UnifiedAepsRepo
     lateinit var viewModel: UnifiedAepsViewModel
    override fun onCreate() {
        super.onCreate()
        initialize()
    }
    private fun initialize(){
        bioAuthRepository = BioAuthRepo()
        val bankListDatabase= BankListDatabase.getBankListDB(applicationContext)
        bankListRepo= BankListRepo(bankListDatabase)
        unifiedAepsRepo=UnifiedAepsRepo()
        viewModel = UnifiedAepsViewModel(unifiedAepsRepo)
    }
}