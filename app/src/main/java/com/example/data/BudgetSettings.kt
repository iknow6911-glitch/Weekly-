package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget_settings")
data class BudgetSettings(
    @PrimaryKey val id: Int = 1,
    val weeklyNetPay: Double = 900.0,
    val savingsPercentage: Float = 15.0f,
    val weeklyDebtPayment: Double = 50.0,
    val checkingBalance: Double = 0.0
)
