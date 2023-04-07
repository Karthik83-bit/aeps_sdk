package com.example.aeps_sdk.unifiedaeps.bankspinner.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankIIN
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankListResponse
import kotlinx.coroutines.flow.Flow

@Dao
interface BankListDao {

    @Query("SELECT * FROM BankList ORDER BY BANKNAME")
    fun getBankList(): List<BankIIN>

    @Insert
    suspend fun insert(bankIIN:List<BankIIN>)

    @Query("DELETE FROM BankList")
    suspend fun deleteBankList()
}