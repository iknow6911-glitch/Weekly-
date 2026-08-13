package com.example.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

data class NavigationTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainScreen(
    viewModel: BudgetViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val monthlyExpenses by viewModel.monthlyExpenses.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    val payLogs by viewModel.payLogs.collectAsStateWithLifecycle()
    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val debtPaymentLogs by viewModel.debtPaymentLogs.collectAsStateWithLifecycle()
    val savingsFunds by viewModel.savingsFunds.collectAsStateWithLifecycle()
    val savingsLogs by viewModel.savingsLogs.collectAsStateWithLifecycle()

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }

    val tabs = listOf(
        NavigationTab(
            title = "Home",
            selectedIcon = Icons.Filled.AccountBalanceWallet,
            unselectedIcon = Icons.Outlined.AccountBalanceWallet,
            testTag = "tab_home"
        ),
        NavigationTab(
            title = "Savings",
            selectedIcon = Icons.Filled.Savings,
            unselectedIcon = Icons.Outlined.Savings,
            testTag = "tab_savings"
        ),
        NavigationTab(
            title = "Debts",
            selectedIcon = Icons.Filled.CreditCard,
            unselectedIcon = Icons.Outlined.CreditCard,
            testTag = "tab_debts"
        ),
        NavigationTab(
            title = "Bills",
            selectedIcon = Icons.Filled.ReceiptLong,
            unselectedIcon = Icons.Outlined.ReceiptLong,
            testTag = "tab_monthly_bills"
        ),
        NavigationTab(
            title = "Charges",
            selectedIcon = Icons.Filled.ShoppingCart,
            unselectedIcon = Icons.Outlined.ShoppingCart,
            testTag = "tab_charges"
        ),
        NavigationTab(
            title = "Visuals",
            selectedIcon = Icons.Filled.BarChart,
            unselectedIcon = Icons.Outlined.BarChart,
            testTag = "tab_visuals"
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_navigation_bar")
            ) {
                tabs.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        },
                        label = { Text(text = tab.title, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (selectedTabIndex) {
            0 -> HomeScreen(
                settings = settings,
                monthlyExpenses = monthlyExpenses,
                transactions = transactions,
                payLogs = payLogs,
                debts = debts,
                onUpdateWeeklyPay = { viewModel.updateWeeklyNetPay(it) },
                onUpdateSavingsPercent = { viewModel.updateSavingsPercentage(it) },
                onUpdateDebtPayment = { viewModel.updateWeeklyDebtPayment(it) },
                onUpdateCheckingBalance = { viewModel.updateCheckingBalance(it) },
                onAddPayLog = { label, pay -> viewModel.addPayLog(label, pay) },
                onNavigateToDebts = { selectedTabIndex = 2 },
                modifier = Modifier.padding(innerPadding)
            )

            1 -> SavingsScreen(
                savingsFunds = savingsFunds,
                savingsLogs = savingsLogs,
                monthlyExpenses = monthlyExpenses,
                onLogDeposit = { key, name, amount, note -> viewModel.logSavingsDeposit(key, name, amount, note) },
                onUpdateTarget = { key, target -> viewModel.updateSavingsTarget(key, target) },
                onUpdateCustomName = { key, name -> viewModel.updateCustomFundName(key, name) },
                modifier = Modifier.padding(innerPadding)
            )

            2 -> DebtsScreen(
                debts = debts,
                debtPaymentLogs = debtPaymentLogs,
                onAddDebt = { title, amt, minPay, interest, category ->
                    viewModel.addDebt(title, amt, minPay, interest, category)
                },
                onMakePayment = { debt, amount, note, recordAsTx ->
                    viewModel.makeDebtPayment(debt, amount, note, recordAsTx)
                },
                onDeleteDebt = { viewModel.deleteDebt(it) },
                onUpdateDebtBalance = { debt, newBal -> viewModel.updateDebtBalance(debt, newBal) },
                modifier = Modifier.padding(innerPadding)
            )

            3 -> MonthlyExpensesScreen(
                monthlyExpenses = monthlyExpenses,
                onAddExpense = { title, amt, cat, day ->
                    viewModel.addMonthlyExpense(title, amt, cat, day)
                },
                onDeleteExpense = { viewModel.deleteMonthlyExpense(it) },
                onTogglePaidExpense = { expense, isPaid -> viewModel.toggleMonthlyExpensePaid(expense, isPaid) },
                modifier = Modifier.padding(innerPadding)
            )

            4 -> TransactionsScreen(
                transactions = transactions,
                debts = debts,
                savingsFunds = savingsFunds,
                monthlyExpenses = monthlyExpenses,
                onAddTransaction = { title, amt, cat, targetType, targetId, timestamp ->
                    viewModel.addTransactionWithTarget(title, amt, cat, targetType, targetId, timestamp)
                },
                onDeleteTransaction = { viewModel.deleteTransaction(it) },
                modifier = Modifier.padding(innerPadding)
            )

            5 -> VisualsScreen(
                settings = settings,
                monthlyExpenses = monthlyExpenses,
                transactions = transactions,
                debts = debts,
                payLogs = payLogs,
                savingsFunds = savingsFunds,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
