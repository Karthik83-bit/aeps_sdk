package com.example.aeps_sdk.unifiedaeps.bankspinner.dao

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankIIN
import com.example.aeps_sdk.unifiedaeps.bankspinner.models.BankListResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [BankIIN::class], version = 1, exportSchema = false)
abstract class BankListDatabase : RoomDatabase() {
    abstract fun getBankListDao(): BankListDao

    companion object {
        @Volatile
        private var dbInstance: BankListDatabase? = null
        fun getBankListDB(context: Context): BankListDatabase {
            if (dbInstance == null) {
                synchronized(this) {
                    dbInstance = Room.databaseBuilder(
                        context, BankListDatabase::class.java, "bankListDB"
                    ).allowMainThreadQueries().build()
                }
            }
            return dbInstance!!
        }
    }
}