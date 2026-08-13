package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_funds")
data class SavingsFund(
    @PrimaryKey val fundKey: String,
    val name: String,
    val targetAmount: Double = 0.0,
    val currentBalance: Double = 0.0
)
