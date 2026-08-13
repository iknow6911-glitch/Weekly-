package com.example.data

import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: BudgetDao) {
    val settings: Flow<BudgetSettings?> = dao.getSettings()
    val monthlyExpenses: Flow<List<MonthlyExpense>> = dao.getAllMonthlyExpenses()
    val transactions: Flow<List<TransactionItem>> = dao.getAllTransactions()
    val payLogs: Flow<List<WeeklyPayLog>> = dao.getAllPayLogs()
    val debts: Flow<List<DebtItem>> = dao.getAllDebts()
    val debtPaymentLogs: Flow<List<DebtPaymentLog>> = dao.getAllDebtPaymentLogs()
    val savingsFunds: Flow<List<SavingsFund>> = dao.getAllSavingsFunds()
    val savingsLogs: Flow<List<SavingsDepositLog>> = dao.getAllSavingsDepositLogs()

    suspend fun saveSettings(settings: BudgetSettings) {
        dao.insertOrUpdateSettings(settings)
    }

    suspend fun addMonthlyExpense(expense: MonthlyExpense) {
        dao.insertMonthlyExpense(expense)
    }

    suspend fun updateMonthlyExpense(expense: MonthlyExpense) {
        dao.updateMonthlyExpense(expense)
    }

    suspend fun deleteMonthlyExpense(expense: MonthlyExpense) {
        dao.deleteMonthlyExpense(expense)
    }

    suspend fun addTransaction(transaction: TransactionItem) {
        dao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionItem) {
        dao.deleteTransaction(transaction)
    }

    suspend fun addPayLog(payLog: WeeklyPayLog) {
        dao.insertPayLog(payLog)
    }

    suspend fun deletePayLog(payLog: WeeklyPayLog) {
        dao.deletePayLog(payLog)
    }

    suspend fun addDebt(debt: DebtItem) {
        dao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtItem) {
        dao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtItem) {
        dao.deleteDebt(debt)
    }

    suspend fun makeDebtPayment(debt: DebtItem, paymentAmount: Double, note: String = "") {
        val newBalance = (debt.remainingBalance - paymentAmount).coerceAtLeast(0.0)
        val updatedDebt = debt.copy(
            remainingBalance = newBalance,
            updatedTimestamp = System.currentTimeMillis()
        )
        dao.updateDebt(updatedDebt)
        dao.insertDebtPaymentLog(
            DebtPaymentLog(
                debtId = debt.id,
                debtTitle = debt.title,
                amountPaid = paymentAmount,
                note = note
            )
        )
    }

    suspend fun saveSavingsFund(fund: SavingsFund) {
        dao.insertOrUpdateSavingsFund(fund)
    }

    suspend fun saveSavingsFunds(funds: List<SavingsFund>) {
        dao.insertSavingsFunds(funds)
    }

    suspend fun addSavingsDepositLog(log: SavingsDepositLog) {
        dao.insertSavingsDepositLog(log)
    }
}
