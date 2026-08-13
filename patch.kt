    fun updateCheckingBalance(amount: Double) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(checkingBalance = amount))
        }
    }
