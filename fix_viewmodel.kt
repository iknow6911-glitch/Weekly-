    fun updateWeeklyNetPay(amount: Double) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(weeklyNetPay = amount))
        }
    }

    fun updateCheckingBalance(amount: Double) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(checkingBalance = amount))
        }
    }
