package com.example.aeps_sdk.unifiedaeps.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankListResponse
import com.example.aeps_sdk.unifiedaeps.models.response.AddressResponse
import com.example.aeps_sdk.unifiedaeps.models.response.EncodedUrlResponse
import com.example.aeps_sdk.unifiedaeps.models.req.PinRequest
import com.example.aeps_sdk.unifiedaeps.models.response.PropAddressResponse
import com.example.aeps_sdk.unifiedaeps.models.req.BioAuthSubmitRequest
import com.example.aeps_sdk.unifiedaeps.models.req.EncodedUrlRequest
import com.example.aeps_sdk.unifiedaeps.models.req.SetAddressRequest
import com.example.aeps_sdk.unifiedaeps.models.response.SubmitBioAuthResponse
import com.example.aeps_sdk.unifiedaeps.models.response.UpdateUserPropAddress
import com.example.aeps_sdk.unifiedaeps.repo.BioAuthRepo
import com.example.aeps_sdk.utils.NetworkResults
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch


class BioAuthViewModel constructor(private val repository: BioAuthRepo) : ViewModel() {

    val getEncodedUrlLiveData: SharedFlow<NetworkResults<EncodedUrlResponse>>
        get() = repository.getEncodedUrlLiveData

    val getEncodedUrlLiveDataSetAddress: SharedFlow<NetworkResults<EncodedUrlResponse>>
        get() = repository.getEncodedUrlLiveDataSetAddress

    val viewUserPropAddressLiveData: SharedFlow<NetworkResults<PropAddressResponse>>
        get() = repository.viewUserPropAddressLiveData

    val getAddressFromPinLiveData: SharedFlow<NetworkResults<AddressResponse>>
        get() = repository.getAddressFromPinLiveData

    val getBankListLiveData: SharedFlow<NetworkResults<BankListResponse>>
        get() = repository.getBankListLiveData

    val updateUserPropAddressLiveData: SharedFlow<NetworkResults<UpdateUserPropAddress>>
        get() = repository.updateUserPropAddressLiveData

    val getEncodedUrlLiveDataSubmitBioAuth: SharedFlow<NetworkResults<EncodedUrlResponse>>
        get() = repository.getEncodedUrlLiveDataSubmitBioAuth

    val submitBioAuthLiveData: SharedFlow<NetworkResults<SubmitBioAuthResponse>>
        get() = repository.submitBioAuthLiveData

    fun getEncodedUrlForAddressStatus() {
        viewModelScope.launch {
            repository.getEncodedUrlForAddressStatus()
        }
    }

    fun viewUserPropAddress(token: String, url: String) {
        viewModelScope.launch {
            repository.viewUserPropAddress(token, url)
        }
    }

    fun getAddressFromPin(pinRequest: PinRequest) {
        viewModelScope.launch {
            repository.getAddressFromPin(pinRequest)
        }
    }

    fun getEncodedUrlForSetAddress(encodedUrlRequest: EncodedUrlRequest) {
        viewModelScope.launch {
            repository.getEncodedUrlForSetAddress(encodedUrlRequest)
        }
    }

    fun updateUserPropAddress(token: String, url: String, setAddressRequest: SetAddressRequest) {
        viewModelScope.launch {
            repository.updateUserPropAddress(token, url, setAddressRequest)
        }
    }

    fun getEncodedUrlForSubmitBioAuth() {
        viewModelScope.launch {
            repository.getEncodedUrlForSubmitBioAuth()
        }
    }

    fun submitBioAuth(
        token: String, url: String, submitRequest: BioAuthSubmitRequest
    ) {
        viewModelScope.launch {
            repository.submitBioAuth(token, url, submitRequest)
        }
    }

}