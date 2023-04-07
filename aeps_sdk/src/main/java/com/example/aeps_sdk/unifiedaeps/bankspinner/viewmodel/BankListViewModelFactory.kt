package com.example.aeps_sdk.unifiedaeps.bankspinner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aeps_sdk.unifiedaeps.bankspinner.repo.BankListRepo

class BankListViewModelFactory constructor(private val bankListRepo: BankListRepo):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BankListViewModel(bankListRepo) as T
    }
}