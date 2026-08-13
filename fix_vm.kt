    fun deleteMonthlyExpense(expense: MonthlyExpense) {
        viewModelScope.launch {
            repository.deleteMonthlyExpense(expense)
        }
    }

    fun toggleMonthlyExpensePaid(expense: MonthlyExpense, isPaid: Boolean) {
        viewModelScope.launch {
            repository.updateMonthlyExpense(expense.copy(isPaid = isPaid))
        }
    }
