package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debt_payment_logs")
data class DebtPaymentLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val debtId: Int,
    val debtTitle: String,
    val amountPaid: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
