package com.example.aeps_sdk.unifiedaeps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aeps_sdk.unifiedaeps.repo.UnifiedAepsRepo

class UnifiedAepsViewModelFactory constructor(private val unifiedAepsRepo: UnifiedAepsRepo):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return UnifiedAepsViewModel(unifiedAepsRepo) as T
    }
}