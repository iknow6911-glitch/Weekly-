package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BudgetRepository
import com.example.data.BudgetSettings
import com.example.data.DebtItem
import com.example.data.DebtPaymentLog
import com.example.data.MonthlyExpense
import com.example.data.SavingsDepositLog
import com.example.data.SavingsFund
import com.example.data.TransactionItem
import com.example.data.WeeklyPayLog
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CalculationSummary(
    val weeklyNetPay: Double = 900.0,
    val totalMonthlyExpenses: Double = 0.0,
    val weeklyExpensesBreakdown: Double = 0.0,
    val savingsPercentage: Float = 15.0f,
    val weeklySavingsDeduction: Double = 0.0,
    val weeklyDebtPayment: Double = 50.0,
    val weeklyTransactionsTotal: Double = 0.0,
    val remainingDisposableIncome: Double = 0.0
)

class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: BudgetRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = BudgetRepository(db.budgetDao())
        viewModelScope.launch {
            seedInitialDataIfNeeded()
        }
    }

    val settings: StateFlow<BudgetSettings> = repository.settings
        .map { it ?: BudgetSettings() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetSettings()
        )

    val monthlyExpenses: StateFlow<List<MonthlyExpense>> = repository.monthlyExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val transactions: StateFlow<List<TransactionItem>> = repository.transactions
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val payLogs: StateFlow<List<WeeklyPayLog>> = repository.payLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val debts: StateFlow<List<DebtItem>> = repository.debts
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val debtPaymentLogs: StateFlow<List<DebtPaymentLog>> = repository.debtPaymentLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingsFunds: StateFlow<List<SavingsFund>> = repository.savingsFunds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val savingsLogs: StateFlow<List<SavingsDepositLog>> = repository.savingsLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private suspend fun seedInitialDataIfNeeded() {
        val currentSettings = repository.settings.first()
        if (currentSettings == null) {
            val initialSettings = BudgetSettings(
                weeklyNetPay = 950.0,
                savingsPercentage = 15.0f,
                weeklyDebtPayment = 75.0
            )
            repository.saveSettings(initialSettings)

            // Seed a few realistic default monthly expenses
            repository.addMonthlyExpense(
                MonthlyExpense(title = "Rent / Mortgage", monthlyAmount = 1100.0, category = "Housing", dueDay = 1)
            )
            repository.addMonthlyExpense(
                MonthlyExpense(title = "Car Insurance", monthlyAmount = 140.0, category = "Auto", dueDay = 15)
            )
            repository.addMonthlyExpense(
                MonthlyExpense(title = "Utilities & Internet", monthlyAmount = 180.0, category = "Utilities", dueDay = 20)
            )
            repository.addMonthlyExpense(
                MonthlyExpense(title = "Streaming Services", monthlyAmount = 35.0, category = "Subscriptions", dueDay = 5)
            )

            // Seed sample weekly charges
            repository.addTransaction(
                TransactionItem(title = "Weekly Groceries", amount = 85.50, category = "Food & Groceries")
            )
            repository.addTransaction(
                TransactionItem(title = "Gas / Commute", amount = 35.00, category = "Transport")
            )
            repository.addTransaction(
                TransactionItem(title = "Coffee & Lunch", amount = 18.25, category = "Food & Groceries")
            )

            // Seed initial pay log
            repository.addPayLog(
                WeeklyPayLog(dateLabel = "This Week's Paystub", netPayAmount = 950.0)
            )

            // Seed default debts
            repository.addDebt(
                DebtItem(
                    title = "Credit Card",
                    initialAmount = 1500.0,
                    remainingBalance = 1200.0,
                    minimumPayment = 40.0,
                    interestRate = 18.5,
                    category = "Credit Card"
                )
            )
            repository.addDebt(
                DebtItem(
                    title = "Car Loan",
                    initialAmount = 8000.0,
                    remainingBalance = 4500.0,
                    minimumPayment = 150.0,
                    interestRate = 5.2,
                    category = "Loan"
                )
            )

            // Seed default savings funds
            repository.saveSavingsFunds(
                listOf(
                    SavingsFund("emergency", "Emergency Fund (3-Month)", 0.0, 0.0),
                    SavingsFund("rainy_day", "Rainy Day Fund", 1000.0, 0.0),
                    SavingsFund("custom", "Vacation / Custom Goal", 2500.0, 0.0)
                )
            )
        }
    }

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

    fun updateSavingsPercentage(percent: Float) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(savingsPercentage = percent))
        }
    }

    fun updateWeeklyDebtPayment(amount: Double) {
        viewModelScope.launch {
            val current = settings.value
            repository.saveSettings(current.copy(weeklyDebtPayment = amount))
        }
    }

    fun addMonthlyExpense(title: String, amount: Double, category: String, dueDay: Int = 1) {
        if (title.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.addMonthlyExpense(
                MonthlyExpense(
                    title = title.trim(),
                    monthlyAmount = amount,
                    category = category,
                    dueDay = dueDay
                )
            )
        }
    }

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
        }
    }

    fun addTransactionWithTarget(title: String, amount: Double, category: String, targetType: String, targetId: String, timestamp: Long = System.currentTimeMillis()) {
        if (title.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            // Determine actual category and title
            val finalCategory = if (targetType == "normal") category else targetType.replaceFirstChar { it.uppercase() }
            
            repository.addTransaction(
                TransactionItem(
                    title = title.trim(),
                    amount = amount,
                    category = finalCategory,
                    timestamp = timestamp
                )
            )

            // Update respective page
            when (targetType) {
                "savings" -> {
                    val currentFunds = savingsFunds.value
                    val currentFund = currentFunds.find { it.fundKey == targetId }
                    if (currentFund != null) {
                        repository.addSavingsDepositLog(
                            SavingsDepositLog(
                                fundKey = currentFund.fundKey,
                                fundName = currentFund.name,
                                amount = amount,
                                note = "Logged via Charges"
                            )
                        )
                        val updatedFund = currentFund.copy(currentBalance = currentFund.currentBalance + amount)
                        repository.saveSavingsFund(updatedFund)
                    }
                }
                "debt" -> {
                    val currentDebts = debts.value
                    val currentDebt = currentDebts.find { it.id.toString() == targetId }
                    if (currentDebt != null) {
                        repository.makeDebtPayment(currentDebt, amount, "Logged via Charges")
                    }
                }
                "bill" -> {
                    // There's no specific paid log for monthly bills currently, we can skip updating or just let it exist as a transaction.
                }
            }
        }
    }

    fun deleteTransaction(transaction: TransactionItem) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addPayLog(dateLabel: String, netPay: Double) {
        if (netPay <= 0.0) return
        viewModelScope.launch {
            val label = if (dateLabel.isBlank()) "Paystub" else dateLabel.trim()
            repository.addPayLog(WeeklyPayLog(dateLabel = label, netPayAmount = netPay))
            updateWeeklyNetPay(netPay)
        }
    }

    fun deletePayLog(payLog: WeeklyPayLog) {
        viewModelScope.launch {
            repository.deletePayLog(payLog)
        }
    }

    fun addDebt(title: String, amount: Double, minimumPayment: Double = 0.0, interestRate: Double = 0.0, category: String = "Credit Card") {
        if (title.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            repository.addDebt(
                DebtItem(
                    title = title.trim(),
                    initialAmount = amount,
                    remainingBalance = amount,
                    minimumPayment = minimumPayment,
                    interestRate = interestRate,
                    category = category
                )
            )
        }
    }

    fun makeDebtPayment(debt: DebtItem, paymentAmount: Double, note: String = "", recordAsTransaction: Boolean = true) {
        if (paymentAmount <= 0.0) return
        viewModelScope.launch {
            repository.makeDebtPayment(debt, paymentAmount, note)
            if (recordAsTransaction) {
                repository.addTransaction(
                    TransactionItem(
                        title = "Debt Payment (${debt.title})",
                        amount = paymentAmount,
                        category = "Debt Payment"
                    )
                )
            }
        }
    }

    fun deleteDebt(debt: DebtItem) {
        viewModelScope.launch {
            repository.deleteDebt(debt)
        }
    }

    fun updateDebtBalance(debt: DebtItem, newBalance: Double) {
        viewModelScope.launch {
            repository.updateDebt(
                debt.copy(
                    remainingBalance = newBalance,
                    updatedTimestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun logSavingsDeposit(fundKey: String, fundName: String, amount: Double, note: String = "") {
        if (amount <= 0.0) return
        viewModelScope.launch {
            repository.addSavingsDepositLog(
                SavingsDepositLog(
                    fundKey = fundKey,
                    fundName = fundName,
                    amount = amount,
                    note = note
                )
            )
            val currentFunds = savingsFunds.value
            val currentFund = currentFunds.find { it.fundKey == fundKey }
                ?: SavingsFund(fundKey, fundName, 0.0, 0.0)
            val updatedFund = currentFund.copy(currentBalance = currentFund.currentBalance + amount)
            repository.saveSavingsFund(updatedFund)
        }
    }

    fun updateSavingsTarget(fundKey: String, newTarget: Double) {
        viewModelScope.launch {
            val currentFunds = savingsFunds.value
            val currentFund = currentFunds.find { it.fundKey == fundKey } ?: return@launch
            repository.saveSavingsFund(currentFund.copy(targetAmount = newTarget))
        }
    }

    fun updateCustomFundName(fundKey: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val currentFunds = savingsFunds.value
            val currentFund = currentFunds.find { it.fundKey == fundKey } ?: return@launch
            repository.saveSavingsFund(currentFund.copy(name = newName.trim()))
        }
    }
}
