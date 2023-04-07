package com.example.aeps_sdk.unifiedaeps.repo

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.aeps_sdk.unifiedaeps.api.apisevices.ApiProvides
import com.example.aeps_sdk.unifiedaeps.models.response.EncodedUrlResponse
import com.example.aeps_sdk.unifiedaeps.models.req.TransactionRequest
import com.example.aeps_sdk.unifiedaeps.models.response.MiniStatement
import com.example.aeps_sdk.unifiedaeps.models.response.TransactionStatusResponse
import com.example.aeps_sdk.utils.NetworkResults
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class UnifiedAepsRepo {
    private val _getEncodedUrlLiveData =
        MutableSharedFlow<NetworkResults<EncodedUrlResponse>>()
    private val mutableSharedFlow = _getEncodedUrlLiveData

    val getEncodedUrlLiveData: SharedFlow<NetworkResults<EncodedUrlResponse>>
        get() = _getEncodedUrlLiveData.asSharedFlow()

    private val _getTransactionStatusLiveData =
        MutableSharedFlow<NetworkResults<TransactionStatusResponse>>()
    val getTransactionStatusLiveData: SharedFlow<NetworkResults<TransactionStatusResponse>>
        get() = _getTransactionStatusLiveData.asSharedFlow()

    private val _miniStatementLiveData=MutableLiveData<List<MiniStatement>?>()
    val ministatementLiveData:LiveData<List<MiniStatement>?> get() = _miniStatementLiveData


    suspend fun getBalanceEnqEncodedUrl() {
        _getEncodedUrlLiveData.emit(NetworkResults.Loading())
        try {
            val result =
                ApiProvides.getTransactionApi().getBalanceEnqEncodedUrl()
            if (result.isSuccessful && result.body() != null) {
                _getEncodedUrlLiveData.emit(NetworkResults.Success(result.body()!!))
            } else if (result.errorBody() != null) {
                val errorObj = JSONObject(result.errorBody()!!.charStream().readText())
                _getEncodedUrlLiveData.emit(
                    NetworkResults.Error(
                        errorObj!!.getString(
                            "statusDesc"
                        )
                    )
                )
//                Timber.e("${result.code()} ${result.message()}")
            } else {
                _getEncodedUrlLiveData.emit(NetworkResults.Error("Something is Wrong"))
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

    suspend fun getCahWithdrawalEncodedUrl() {
        _getEncodedUrlLiveData.emit(NetworkResults.Loading())
        try {
            val result =
                ApiProvides.getTransactionApi().getCashWithdrawalEncodedUrl()
            if (result.isSuccessful && result.body() != null) {
                _getEncodedUrlLiveData.emit(NetworkResults.Success(result.body()!!))
            } else if (result.errorBody() != null) {
                val errorObj = JSONObject(result.errorBody()!!.charStream().readText())
                _getEncodedUrlLiveData.emit(
                    NetworkResults.Error(
                        errorObj!!.getString(
                            "statusDesc"
                        )
                    )
                )
//                Timber.e("${result.code()} ${result.message()}")
            } else {
                _getEncodedUrlLiveData.emit(NetworkResults.Error("Something is Wrong"))
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

    suspend fun getMiniStatementEncodedUrl() {
        _getEncodedUrlLiveData.emit(NetworkResults.Loading())
        try {
            val result =
                ApiProvides.getTransactionApi().getMiniStatementEncodedUrl()
            if (result.isSuccessful && result.body() != null) {
                _getEncodedUrlLiveData.emit(NetworkResults.Success(result.body()!!))
            } else if (result.errorBody() != null) {
                val errorObj = JSONObject(result.errorBody()!!.charStream().readText())
                _getEncodedUrlLiveData.emit(
                    NetworkResults.Error(
                        errorObj!!.getString(
                            "statusDesc"
                        )
                    )
                )
//                Timber.e("${result.code()} ${result.message()}")
            } else {
                _getEncodedUrlLiveData.emit(NetworkResults.Error("Something is Wrong"))
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


    suspend fun getTransactionStatus(token:String,url:String,transactionRequest: TransactionRequest){
        _getTransactionStatusLiveData.emit(NetworkResults.Loading())
        try {
            val result=
                ApiProvides.getTransactionApi().getTransactionStatus(token,url,transactionRequest)

            if (result.isSuccessful && result.body() != null) {
                _getTransactionStatusLiveData.emit(NetworkResults.Success(result.body()!!))
                _miniStatementLiveData.postValue(result.body()!!.ministatement)
            } else if (result.errorBody() != null) {
                val errorObj = JSONObject(result.errorBody()!!.charStream().readText())
                _getTransactionStatusLiveData.emit(
                    NetworkResults.Error(
                        errorObj!!.getString(
                            "apiComment"
                        )
                    )
                )
//                Timber.e("${result.code()} ${result.message()}")
            } else {
                _getTransactionStatusLiveData.emit(NetworkResults.Error("Something is Wrong"))
            }
        } catch (e: HttpException) {
            _getTransactionStatusLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "An Unknown error occurred"
                )
            )
        } catch (e: IOException) {
            _getTransactionStatusLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Check Connectivity"
                )
            )
        } catch (e: Exception) {
            _getTransactionStatusLiveData.emit(
                NetworkResults.Error(
                    message = e.localizedMessage ?: "Something is Wrong"
                )
            )
        }
    }
}