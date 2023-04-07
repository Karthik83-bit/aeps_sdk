package com.example.aeps_sdk.unifiedaeps.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aeps_sdk.unifiedaeps.models.response.EncodedUrlResponse
import com.example.aeps_sdk.unifiedaeps.models.req.TransactionRequest
import com.example.aeps_sdk.unifiedaeps.models.response.MiniStatement
import com.example.aeps_sdk.unifiedaeps.models.response.TransactionStatusResponse
import com.example.aeps_sdk.unifiedaeps.repo.UnifiedAepsRepo
import com.example.aeps_sdk.utils.NetworkResults
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class UnifiedAepsViewModel constructor(private val unifiedAepsRepo: UnifiedAepsRepo):ViewModel() {
    val getEncodedUrlLiveData: SharedFlow<NetworkResults<EncodedUrlResponse>>
        get() = unifiedAepsRepo.getEncodedUrlLiveData

    val getTransactionStatusLiveData: SharedFlow<NetworkResults<TransactionStatusResponse>>
        get() = unifiedAepsRepo.getTransactionStatusLiveData
    val miniStatementLiveData: LiveData<List<MiniStatement>?>
    get() =  unifiedAepsRepo.ministatementLiveData

    fun getBalanceEnqEncodedUrl() {
        viewModelScope.launch {
            unifiedAepsRepo.getBalanceEnqEncodedUrl()
        }
    }
    fun getCahWithdrawalEncodedUrl(){
        viewModelScope.launch {
            unifiedAepsRepo.getCahWithdrawalEncodedUrl()
        }
    }
    fun getMiniStatementEncodedUrl(){
        viewModelScope.launch {
            unifiedAepsRepo.getMiniStatementEncodedUrl()
        }
    }

    fun getTransactionStatus(token:String,url:String,transactionRequest: TransactionRequest){
        viewModelScope.launch {
            unifiedAepsRepo.getTransactionStatus(token,url,transactionRequest)
        }
    }
}