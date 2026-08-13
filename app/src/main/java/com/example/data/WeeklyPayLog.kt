package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "weekly_pay_logs")
data class WeeklyPayLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dateLabel: String,
    val netPayAmount: Double,
    val timestamp: Long = System.currentTimeMillis()
)
