            repository.addTransaction(
                TransactionItem(
                    title = title.trim(),
                    amount = amount,
                    category = finalCategory,
                    timestamp = timestamp
                )
            )
            
            val current = settings.value
            repository.saveSettings(current.copy(checkingBalance = current.checkingBalance - amount))
