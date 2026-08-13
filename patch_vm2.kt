    fun deleteTransaction(transaction: TransactionItem) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            val current = settings.value
            repository.saveSettings(current.copy(checkingBalance = current.checkingBalance + transaction.amount))
        }
    }
