package com.example.ui

import androidx.compose.ui.graphics.Color
import java.util.Random

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "food & groceries", "food" -> Color(0xFF4CAF50)
        "transport" -> Color(0xFF2196F3)
        "shopping" -> Color(0xFFE91E63)
        "entertainment" -> Color(0xFFFF9800)
        "bills" -> Color(0xFF9C27B0)
        "health" -> Color(0xFF00BCD4)
        "savings" -> Color(0xFF8BC34A)
        "debt payment" -> Color(0xFFF44336)
        else -> {
            val random = Random(category.hashCode().toLong())
            Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
    }
}
