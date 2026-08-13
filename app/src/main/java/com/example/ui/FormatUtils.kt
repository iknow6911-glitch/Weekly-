package com.example.ui

import java.util.Locale

fun Double.formatCurrency(): String {
    return "$${String.format(Locale.US, "%.2f", this)}"
}

fun Double.formatTwoDecimals(): String {
    return String.format(Locale.US, "%.2f", this)
}
