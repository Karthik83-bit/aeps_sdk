package com.example.aeps_sdk.unifiedaeps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.aeps_sdk.unifiedaeps.repo.BioAuthRepo

class BioAuthViewModelFactory constructor(private val bioAuthRepository: BioAuthRepo):ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return BioAuthViewModel(bioAuthRepository) as T
    }
}