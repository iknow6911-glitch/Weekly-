package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_expenses")
data class MonthlyExpense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val monthlyAmount: Double,
    val category: String = "General",
    val dueDay: Int = 1,
    val isPaid: Boolean = false
) {
    // Break down monthly expense into weekly equivalent: (monthly * 12) / 52
    val weeklyEquivalent: Double
        get() = (monthlyAmount * 12.0) / 52.0
}
