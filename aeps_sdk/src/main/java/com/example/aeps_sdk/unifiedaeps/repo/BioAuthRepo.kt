package com.example.aeps_sdk.unifiedaeps.repo

import com.example.aeps_sdk.unifiedaeps.api.apisevices.ApiProvides

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
import com.example.aeps_sdk.utils.NetworkResults
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class BioAuthRepo {

    private val _getEncodedUrlLiveData = MutableSharedFlow<NetworkResults<EncodedUrlResponse>>()
    val getEncodedUrlLiveData: SharedFlow<NetworkResults<EncodedUrlResponse>>
        get() = _getEncodedUrlLiveData.asSharedFlow()

    private val _getEncodedUrlLiveDataSetAddress =
        MutableSharedFlow<NetworkResults<EncodedUrlResponse>>()
    val getEncodedUrlLiveDataSetAddress: SharedFlow<NetworkResults<EncodedUrlResponse>>
        get() = _getEncodedUrlLiveDataSetAddress.asSharedFlow()

    private val _getEncodedUrlLiveDataSubmitBioAuth =
        MutableSharedFlow<NetworkResults<EncodedUrlResponse>>()
    val getEncodedUrlLiveDataSubmitBioAuth: SharedFlow<NetworkResults<EncodedUrlResponse>>
        get() = _getEncodedUrlLiveDataSubmitBioAuth.asSharedFlow()

    private val _viewUserPropAddressLiveData =
        MutableSharedFlow<NetworkResults<PropAddressResponse>>()
    val viewUserPropAddressLiveData: SharedFlow<NetworkResults<PropAddressResponse>>
        get() = _viewUserPropAddressLiveData.asSharedFlow()

    private val _updateUserPropAddressLiveData =
        MutableSharedFlow<NetworkResults<UpdateUserPropAddress>>()
    val updateUserPropAddressLiveData: SharedFlow<NetworkResults<UpdateUserPropAddress>>
        get() = _updateUserPropAddressLiveData.asSharedFlow()

    private val _submitBioAuthLiveData = MutableSharedFlow<NetworkResults<SubmitBioAuthResponse>>()
    val submitBioAuthLiveData: SharedFlow<NetworkResults<SubmitBioAuthResponse>>
        get() = _submitBioAuthLiveData.asSharedFlow()

    private val _getAddressFromPinLiveData = MutableSharedFlow<NetworkResults<AddressResponse>>()
    val getAddressFromPinLiveData: SharedFlow<NetworkResults<AddressResponse>>
        get() = _getAddressFromPinLiveData.asSharedFlow()

    private val _getBankListLiveData = MutableSharedFlow<NetworkResults<BankListResponse>>()
    val getBankListLiveData: SharedFlow<NetworkResults<BankListResponse>>
        get() = _getBankListLiveData.asSharedFlow()

    suspend fun getEncodedUrlForAddressStatus() {
        try {
            _getEncodedUrlLiveData.emit(NetworkResults.Loading())
            val response = ApiProvides.getBioAuthApi().getEncodedUrlForAddressStatus()

            if (response.isSuccessful && response.body() != null) {
                _getEncodedUrlLiveData.emit(NetworkResults.Success(response.body()))
            } else if (response.errorBody() != null) {
                _getEncodedUrlLiveData.emit(NetworkResults.Error("Service is unavailable for this user, please contact our help desk for details."))
            } else {
                _getEncodedUrlLiveData.emit(NetworkResults.Error("Something Went Wrong"))
            }
        } catch (e: HttpException) {
            _getEncodedUrlLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "An Unknown error occurred"
                )
            )
        } catch (e: IOException) {
            _getEncodedUrlLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Check Connectivity"
                )
            )
        } catch (e: Exception) {
            _getEncodedUrlLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Something is Wrong"
                )
            )
        }
    }

    suspend fun viewUserPropAddress(token: String, url: String) {
        _viewUserPropAddressLiveData.emit(NetworkResults.Loading())
        try {
            val response = ApiProvides.getBioAuthApi().viewUserPropAddress(token, url)
            if (response.isSuccessful && response.body() != null) {
                _viewUserPropAddressLiveData.emit(NetworkResults.Success(response.body()))
            } else if (response.errorBody() != null) {
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                if (errorObj.has("message")) {
                    val message = errorObj.getString("message")
                    _viewUserPropAddressLiveData.emit(NetworkResults.Error(message))
                } else if (errorObj.has("statusDesc")) {
                    val statusDesc = errorObj.getString("statusDesc")
                    _viewUserPropAddressLiveData.emit(NetworkResults.Error(statusDesc))
                } else {
                    _viewUserPropAddressLiveData.emit(NetworkResults.Error("Something Went Wrong, Please Try After Sometime"))
                }
            } else {
                _viewUserPropAddressLiveData.emit(NetworkResults.Error("Something Went Wrong"))
            }
        } catch (e: HttpException) {
            _viewUserPropAddressLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "An Unknown error occurred"
                )
            )
        } catch (e: IOException) {
            _viewUserPropAddressLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Check Connectivity"
                )
            )
        } catch (e: Exception) {
            _viewUserPropAddressLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Something is Wrong"
                )
            )
        }
    }

    suspend fun getAddressFromPin(pinRequest: PinRequest) {
        _getAddressFromPinLiveData.emit(NetworkResults.Loading())
        try {
            val response = ApiProvides.getAddressApi().getAddressFromPin(pinRequest)
            if (response.isSuccessful && response.body() != null) {
                _getAddressFromPinLiveData.emit(NetworkResults.Success(response.body()))
            } else if (response.errorBody() != null) {
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                if (errorObj.has("message")) {
                    val message = errorObj.getString("message")
                    _getAddressFromPinLiveData.emit(NetworkResults.Error(message))
                } else if (errorObj.has("statusDesc")) {
                    val statusDesc = errorObj.getString("statusDesc")
                    _getAddressFromPinLiveData.emit(NetworkResults.Error(statusDesc))
                } else {
                    _getAddressFromPinLiveData.emit(NetworkResults.Error("Something Went Wrong, Please Try After Sometime"))
                }
            } else {
                _getAddressFromPinLiveData.emit(NetworkResults.Error("Something Went Wrong"))
            }
        } catch (e: HttpException) {
            _getAddressFromPinLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "An Unknown error occurred"
                )
            )
        } catch (e: IOException) {
            _getAddressFromPinLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Check Connectivity"
                )
            )
        } catch (e: Exception) {
            _getAddressFromPinLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Something is Wrong"
                )
            )
        }

    }

    suspend fun getEncodedUrlForSetAddress(encodedUrlRequest: EncodedUrlRequest) {
        try {
            _getEncodedUrlLiveDataSetAddress.emit(NetworkResults.Loading())
            val response = ApiProvides.getBioAuthApi().getEncodedUrlForSetAddress(encodedUrlRequest)

            if (response.isSuccessful && response.body() != null) {
                _getEncodedUrlLiveDataSetAddress.emit(NetworkResults.Success(response.body()))
            } else if (response.errorBody() != null) {
                _getEncodedUrlLiveDataSetAddress.emit(NetworkResults.Error("Service is unavailable for this user, please contact our help desk for details."))
            } else {
                _getEncodedUrlLiveDataSetAddress.emit(NetworkResults.Error("Something Went Wrong"))
            }
        } catch (e: HttpException) {
            _getEncodedUrlLiveDataSetAddress.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "An Unknown error occurred"
                )
            )
        } catch (e: IOException) {
            _getEncodedUrlLiveDataSetAddress.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Check Connectivity"
                )
            )
        } catch (e: Exception) {
            _getEncodedUrlLiveDataSetAddress.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Something is Wrong"
                )
            )
        }
    }

    suspend fun updateUserPropAddress(
        token: String, url: String, setAddressRequest: SetAddressRequest
    ) {
        _updateUserPropAddressLiveData.emit(NetworkResults.Loading())
        try {
            val response =
                ApiProvides.getBioAuthApi().updateUserPropAddress(token, url, setAddressRequest)
            if (response.isSuccessful && response.body() != null) {
                _updateUserPropAddressLiveData.emit(NetworkResults.Success(response.body()))
            } else if (response.errorBody() != null) {
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                if (errorObj.has("message")) {
                    val message = errorObj.getString("message")
                    _updateUserPropAddressLiveData.emit(NetworkResults.Error(message))
                } else if (errorObj.has("statusDesc")) {
                    val statusDesc = errorObj.getString("statusDesc")
                    _updateUserPropAddressLiveData.emit(NetworkResults.Error(statusDesc))
                } else {
                    _updateUserPropAddressLiveData.emit(NetworkResults.Error("Something Went Wrong, Please Try After Sometime"))
                }
            } else {
                _updateUserPropAddressLiveData.emit(NetworkResults.Error("Something Went Wrong"))
            }
        } catch (e: HttpException) {
            _updateUserPropAddressLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "An Unknown error occurred"
                )
            )
        } catch (e: IOException) {
            _updateUserPropAddressLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Check Connectivity"
                )
            )
        } catch (e: Exception) {
            _updateUserPropAddressLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Something is Wrong"
                )
            )
        }
    }

    suspend fun getEncodedUrlForSubmitBioAuth() {
        try {
            _getEncodedUrlLiveDataSubmitBioAuth.emit(NetworkResults.Loading())
            val response = ApiProvides.getBioAuthApi().getEncodedUrlForSubmitBioAuth()

            if (response.isSuccessful && response.body() != null) {
                _getEncodedUrlLiveDataSubmitBioAuth.emit(NetworkResults.Success(response.body()))
            } else if (response.errorBody() != null) {
                _getEncodedUrlLiveDataSubmitBioAuth.emit(NetworkResults.Error("Service is unavailable for this user, please contact our help desk for details."))
            } else {
                _getEncodedUrlLiveDataSubmitBioAuth.emit(NetworkResults.Error("Something Went Wrong"))
            }
        } catch (e: HttpException) {
            _getEncodedUrlLiveDataSubmitBioAuth.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "An Unknown error occurred"
                )
            )
        } catch (e: IOException) {
            _getEncodedUrlLiveDataSubmitBioAuth.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Check Connectivity"
                )
            )
        } catch (e: Exception) {
            _getEncodedUrlLiveDataSubmitBioAuth.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Something is Wrong"
                )
            )
        }
    }

    suspend fun submitBioAuth(
        token: String, url: String, submitRequest: BioAuthSubmitRequest
    ) {
        _submitBioAuthLiveData.emit(NetworkResults.Loading())
        try {
            val response = ApiProvides.getBioAuthApi().submitBioAuth(token, url, submitRequest)
            if (response.isSuccessful && response.body() != null) {
                _submitBioAuthLiveData.emit(NetworkResults.Success(response.body()))
            } else if (response.errorBody() != null) {
                val errorObj = JSONObject(response.errorBody()!!.charStream().readText())
                if (errorObj.has("message")) {
                    val message = errorObj.getString("message")
                    _submitBioAuthLiveData.emit(NetworkResults.Error(message))
                } else if (errorObj.has("statusDesc")) {
                    val statusDesc = errorObj.getString("statusDesc")
                    _submitBioAuthLiveData.emit(NetworkResults.Error(statusDesc))
                } else {
                    _submitBioAuthLiveData.emit(NetworkResults.Error("Something Went Wrong, Please Try After Sometime"))
                }
            } else {
                _submitBioAuthLiveData.emit(NetworkResults.Error("Something Went Wrong"))
            }
        } catch (e: HttpException) {
            _submitBioAuthLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "An Unknown error occurred"
                )
            )
        } catch (e: IOException) {
            _submitBioAuthLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Check Connectivity"
                )
            )
        } catch (e: Exception) {
            _submitBioAuthLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Something is Wrong"
                )
            )
        }
    }


}