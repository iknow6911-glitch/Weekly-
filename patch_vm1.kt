    fun addTransaction(title: String, amount: Double, category: String) {
        if (title.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.addTransaction(
                TransactionItem(
                    title = title.trim(),
                    amount = amount,
                    category = category
                )
            )
            val current = settings.value
            repository.saveSettings(current.copy(checkingBalance = current.checkingBalance - amount))
        }
    }
