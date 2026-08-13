package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_deposit_logs")
data class SavingsDepositLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val fundKey: String,
    val fundName: String,
    val amount: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
