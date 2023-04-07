package com.example.aeps_sdk.unifiedaeps.bankspinner.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "BankList")
data class BankIIN(
    @PrimaryKey
    @ColumnInfo(name="BANKNAME")
    val BANKNAME: String,
    @ColumnInfo(name = "IIN")
    val IIN: Int,
    @ColumnInfo(name="aeps2MiniFlag")
    val aeps2MiniFlag: Boolean
)