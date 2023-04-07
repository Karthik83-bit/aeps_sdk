package com.example.aeps_sdk.unifiedaeps.bankspinner.repo

import com.example.aeps_sdk.unifiedaeps.api.apisevices.ApiProvides
import com.example.aeps_sdk.unifiedaeps.bankspinner.dao.BankListDatabase
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankIIN
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankListResponse
import com.example.aeps_sdk.utils.NetworkResults
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class BankListRepo(private val bankListDatabase: BankListDatabase){
    val allBankList: List<BankIIN>
        get() = bankListDatabase.getBankListDao().getBankList()

    private val _getBankListLiveData =
        MutableSharedFlow<NetworkResults<BankListResponse>>()
    val getBankListLiveData: SharedFlow<NetworkResults<BankListResponse>>
        get() = _getBankListLiveData.asSharedFlow()

    suspend fun getBankList(){
        _getBankListLiveData.emit(NetworkResults.Loading())
        try {
            val result=
                ApiProvides.getAddressApi().getBankList()
            if(result.isSuccessful && result.body()!=null){
                _getBankListLiveData.emit(NetworkResults.Success(result.body()!!))
                bankListDatabase.getBankListDao().insert(result.body()!!.bankIINs)
            }else if(result.errorBody() != null) {
                val errorObj = JSONObject(result.errorBody()!!.charStream().readText())
                _getBankListLiveData.emit(NetworkResults.Error(errorObj!!.getString("statusDesc")))
//                Timber.e("${result.code()} ${result.message()}")
            }else{
                _getBankListLiveData.emit(NetworkResults.Error("Something is Wrong"))
            }
        } catch (e: HttpException) {
            _getBankListLiveData.emit(NetworkResults.Error(message = e.localizedMessage ?: "An Unknown error occurred"))
        } catch (e: IOException) {
            _getBankListLiveData.emit(NetworkResults.Error(message = e.localizedMessage ?: "Check Connectivity"))
        } catch (e: Exception) {
            _getBankListLiveData.emit(NetworkResults.Error(message = e.localizedMessage ?: "Something is Wrong"))
        }
    }

    suspend fun deleteBankListFromDB(){
        bankListDatabase.getBankListDao().deleteBankList()
    }

}