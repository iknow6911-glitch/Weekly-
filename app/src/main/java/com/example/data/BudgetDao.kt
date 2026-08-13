package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budget_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<BudgetSettings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSettings(settings: BudgetSettings)

    @Query("SELECT * FROM monthly_expenses ORDER BY monthlyAmount DESC")
    fun getAllMonthlyExpenses(): Flow<List<MonthlyExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMonthlyExpense(expense: MonthlyExpense)

    @Update
    suspend fun updateMonthlyExpense(expense: MonthlyExpense)

    @Delete
    suspend fun deleteMonthlyExpense(expense: MonthlyExpense)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionItem)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionItem)

    @Query("SELECT * FROM weekly_pay_logs ORDER BY timestamp DESC")
    fun getAllPayLogs(): Flow<List<WeeklyPayLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayLog(payLog: WeeklyPayLog)

    @Delete
    suspend fun deletePayLog(payLog: WeeklyPayLog)

    @Query("SELECT * FROM debts ORDER BY remainingBalance DESC")
    fun getAllDebts(): Flow<List<DebtItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtItem)

    @Update
    suspend fun updateDebt(debt: DebtItem)

    @Delete
    suspend fun deleteDebt(debt: DebtItem)

    @Query("SELECT * FROM debt_payment_logs ORDER BY timestamp DESC")
    fun getAllDebtPaymentLogs(): Flow<List<DebtPaymentLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebtPaymentLog(log: DebtPaymentLog)

    @Query("SELECT * FROM savings_funds")
    fun getAllSavingsFunds(): Flow<List<SavingsFund>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSavingsFund(fund: SavingsFund)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsFunds(funds: List<SavingsFund>)

    @Query("SELECT * FROM savings_deposit_logs ORDER BY timestamp DESC")
    fun getAllSavingsDepositLogs(): Flow<List<SavingsDepositLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsDepositLog(log: SavingsDepositLog)
}
