package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val initialAmount: Double = 0.0,
    val remainingBalance: Double,
    val minimumPayment: Double = 0.0,
    val interestRate: Double = 0.0,
    val category: String = "Credit Card",
    val updatedTimestamp: Long = System.currentTimeMillis()
)
