package com.example.aeps_sdk.unifiedaeps.bankspinner.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankIIN
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankListResponse
import com.example.aeps_sdk.unifiedaeps.bankspinner.repo.BankListRepo
import com.example.aeps_sdk.utils.NetworkResults
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class BankListViewModel constructor(private val bankListRepo: BankListRepo) : ViewModel() {
        val allBankList: List<BankIIN> get() = bankListRepo.allBankList
    val getBankListLiveData: SharedFlow<NetworkResults<BankListResponse>>
        get() = bankListRepo.getBankListLiveData

    fun getBankList(){
        viewModelScope.launch {
            bankListRepo.getBankList()
        }
    }
    fun deleteBankListFromDB(){
        viewModelScope.launch {
            bankListRepo.deleteBankListFromDB()
        }
    }
}