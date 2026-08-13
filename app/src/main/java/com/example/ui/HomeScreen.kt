package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetSettings
import com.example.data.DebtItem
import com.example.data.MonthlyExpense
import com.example.data.TransactionItem
import com.example.data.WeeklyPayLog
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    settings: BudgetSettings,
    monthlyExpenses: List<MonthlyExpense>,
    transactions: List<TransactionItem>,
    payLogs: List<WeeklyPayLog>,
    debts: List<DebtItem> = emptyList(),
    onUpdateWeeklyPay: (Double) -> Unit,
    onUpdateSavingsPercent: (Float) -> Unit,
    onUpdateDebtPayment: (Double) -> Unit,
    onUpdateCheckingBalance: (Double) -> Unit,
    onAddPayLog: (String, Double) -> Unit,
    onNavigateToDebts: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val weeklyNetPay = settings.weeklyNetPay
    val totalMonthlyExpenses = monthlyExpenses.sumOf { it.monthlyAmount }
    val weeklyExpensesBreakdown = (totalMonthlyExpenses * 12.0) / 52.0
    val savingsPercent = settings.savingsPercentage
    val weeklySavings = weeklyNetPay * (savingsPercent / 100.0)
    val weeklyDebt = settings.weeklyDebtPayment
    val weeklySpent = transactions.filter { it.category != "Bill" && it.category != "Debt" && it.category != "Savings" }.sumOf { it.amount }

    val totalDeductions = weeklyExpensesBreakdown + weeklySavings + weeklyDebt + weeklySpent
    val remainingDisposable = weeklyNetPay - totalDeductions

    val spentRatio = if (weeklyNetPay > 0) (totalDeductions / weeklyNetPay).toFloat().coerceIn(0f, 1f) else 1f
    val remainingRatio = (1f - spentRatio).coerceIn(0f, 1f)

    var showPaySheet by remember { mutableStateOf(false) }
    var payInputText by remember(weeklyNetPay) { mutableStateOf(if (weeklyNetPay > 0) weeklyNetPay.formatTwoDecimals() else "") }
    var payNoteText by remember { mutableStateOf("") }

    var debtInputText by remember(weeklyDebt) { mutableStateOf(weeklyDebt.formatTwoDecimals()) }
    var showCheckingSheet by remember { mutableStateOf(false) }
    var checkingInputText by remember(settings.checkingBalance) { mutableStateOf(settings.checkingBalance.formatTwoDecimals()) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Top Header Greeting
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Weekly Budget",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Net Pay & Disposable Income Breakdown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2C3238),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Budget Icon",
                            tint = Color.White
                        )
                    }
                }
            }
        }

        // Checking Account Balance
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Checking Account",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Checking Balance",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = settings.checkingBalance.formatCurrency(),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    IconButton(
                        onClick = { showCheckingSheet = true },
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Checking Balance",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // Hero Remaining Disposable Income Display Card
        item {
            val isPositive = remainingDisposable >= 0
            val cardBg = if (isPositive) {
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF333A42),
                        Color(0xFF1F2429)
                    )
                )
            } else {
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.error,
                        Color(0xFFB71C1C)
                    )
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("disposable_income_hero_card"),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBg)
                        .padding(24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isPositive) "Weekly Disposable Cash" else "Over Budget Warning",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                        color = Color.White
                                    )
                                }
                            }

                            TextButton(
                                onClick = { showPaySheet = true },
                                modifier = Modifier.testTag("edit_pay_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit Net Pay",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit Pay",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (remainingDisposable >= 0) remainingDisposable.formatCurrency() else "-${(-remainingDisposable).formatCurrency()}",
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 38.sp
                            ),
                            color = Color.White
                        )

                        Text(
                            text = "Remaining after bills, savings, debt & charges",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Visual Progress Bar
                        val progressAnim by animateFloatAsState(targetValue = remainingRatio, label = "remainingProgress")
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${(remainingRatio * 100).toInt()}% Remaining",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                                Text(
                                    text = "Gross Pay: ${weeklyNetPay.formatCurrency()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = progressAnim,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = Color.White,
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }

        // Waterfall Income & Deduction Breakdown Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("breakdown_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Weekly Income Waterfall",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    BreakdownRowItem(
                        icon = Icons.Default.AttachMoney,
                        label = "Weekly Net Pay",
                        sublabel = "Base income entered",
                        amount = weeklyNetPay,
                        isCredit = true
                    )

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    BreakdownRowItem(
                        icon = Icons.Default.CalendarToday,
                        label = "Monthly Bills (Weekly Share)",
                        sublabel = "Prorated from ${totalMonthlyExpenses.formatCurrency()}/mo",
                        amount = -weeklyExpensesBreakdown,
                        isCredit = false
                    )

                    BreakdownRowItem(
                        icon = Icons.Default.Savings,
                        label = "Savings Deduction (${savingsPercent.toInt()}%)",
                        sublabel = "Set-aside for future",
                        amount = -weeklySavings,
                        isCredit = false
                    )

                    BreakdownRowItem(
                        icon = Icons.Default.CreditCard,
                        label = "Debt Payment",
                        sublabel = "Fixed weekly payoff",
                        amount = -weeklyDebt,
                        isCredit = false
                    )

                    BreakdownRowItem(
                        icon = Icons.Default.ShoppingBag,
                        label = "Spent Charges This Week",
                        sublabel = "${transactions.size} logged transactions",
                        amount = -weeklySpent,
                        isCredit = false
                    )

                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outline)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remaining Disposable Income",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = remainingDisposable.formatCurrency(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = if (remainingDisposable >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Variable Deductions Adjuster (Savings % & Debt Payment Amount)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deductions_settings_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Variable Deductions Config",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Savings Slider Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Savings Deduction Percentage",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${savingsPercent.toInt()}% (${weeklySavings.formatCurrency()}/wk)",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Slider(
                        value = savingsPercent,
                        onValueChange = { onUpdateSavingsPercent(it) },
                        valueRange = 0f..50f,
                        steps = 50,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("savings_slider"),
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Debt Payment Section
                    val totalDebtBalance = debts.sumOf { it.remainingBalance }
                    var debtInputMode by remember { mutableIntStateOf(0) } // 0 = Weekly $, 1 = Monthly $, 2 = % Income

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Debt Payment Allocation",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (totalDebtBalance > 0) {
                            Text(
                                text = "Total Debt: ${totalDebtBalance.formatCurrency()}",
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Mode Selection Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = debtInputMode == 0,
                            onClick = {
                                debtInputMode = 0
                                debtInputText = weeklyDebt.formatTwoDecimals()
                            },
                            label = { Text("Weekly ($)", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = debtInputMode == 1,
                            onClick = {
                                debtInputMode = 1
                                val monthlyEquivalent = weeklyDebt * 4.3333
                                debtInputText = monthlyEquivalent.formatTwoDecimals()
                            },
                            label = { Text("Monthly ($)", fontSize = 12.sp) }
                        )
                        FilterChip(
                            selected = debtInputMode == 2,
                            onClick = {
                                debtInputMode = 2
                                val percentEquivalent = if (weeklyNetPay > 0) (weeklyDebt / weeklyNetPay) * 100 else 0.0
                                debtInputText = percentEquivalent.formatTwoDecimals()
                            },
                            label = { Text("% Income", fontSize = 12.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = debtInputText,
                        onValueChange = { newText ->
                            debtInputText = newText
                            val inputVal = newText.toDoubleOrNull() ?: 0.0
                            val calculatedWeekly = when (debtInputMode) {
                                1 -> inputVal / 4.3333 // Monthly to Weekly
                                2 -> weeklyNetPay * (inputVal / 100.0) // % of Net Pay
                                else -> inputVal // Weekly $
                            }
                            onUpdateDebtPayment(calculatedWeekly)
                        },
                        leadingIcon = {
                            Text(
                                text = if (debtInputMode == 2) "%" else "$",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        },
                        label = {
                            Text(
                                when (debtInputMode) {
                                    1 -> "Monthly Debt Payment ($)"
                                    2 -> "% of Net Pay Income"
                                    else -> "Weekly Debt Payment ($)"
                                }
                            )
                        },
                        supportingText = {
                            if (debtInputMode != 0) {
                                Text(
                                    text = "= ${weeklyDebt.formatCurrency()} per week",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("debt_payment_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset Quick-Select Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(25.0, 50.0, 75.0, 100.0, 150.0).forEach { preset ->
                            val isSelected = weeklyDebt == preset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    debtInputMode = 0
                                    debtInputText = preset.formatTwoDecimals()
                                    onUpdateDebtPayment(preset)
                                },
                                label = { Text("$${preset.toInt()}/wk", fontSize = 12.sp) }
                            )
                        }
                    }

                    if (onNavigateToDebts != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TextButton(
                            onClick = onNavigateToDebts,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Manage Debts & Subtract Payments →")
                        }
                    }
                }
            }
        }

        // Weekly Pay Stub History Log Preview
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pay_history_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Weekly Pay History",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        TextButton(
                            onClick = { showPaySheet = true },
                            modifier = Modifier.testTag("log_paystub_button")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Input Pay")
                        }
                    }

                    if (payLogs.isEmpty()) {
                        Text(
                            text = "Current standard net pay is ${weeklyNetPay.formatCurrency()}/week. Tap 'Input Pay' to add paystub entries.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        payLogs.take(3).forEach { payLog ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = payLog.dateLabel,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Text(
                                        text = "Recorded paystub",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                    )
                                }
                                Text(
                                    text = payLog.netPayAmount.formatCurrency(),
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }

    // Modal Bottom Sheet to Input Weekly Net Pay
    if (showCheckingSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCheckingSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Update Checking Balance",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Keep track of your total money available in your checking account.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = checkingInputText,
                    onValueChange = { checkingInputText = it },
                    label = { Text("Checking Balance ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        val amount = checkingInputText.toDoubleOrNull()
                        if (amount != null) {
                            onUpdateCheckingBalance(amount)
                        }
                        showCheckingSheet = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text("Save Balance", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    if (showPaySheet) {
        ModalBottomSheet(
            onDismissRequest = { showPaySheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Input Weekly Net Pay",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Enter your take-home pay for this week to calculate remaining disposable cash.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = payInputText,
                    onValueChange = { payInputText = it },
                    label = { Text("Net Pay Amount ($)") },
                    leadingIcon = {
                        Text(
                            text = "$",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pay_sheet_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = payNoteText,
                    onValueChange = { payNoteText = it },
                    label = { Text("Pay Period / Note (e.g., Week of Aug 12)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val netPayVal = payInputText.toDoubleOrNull()
                        if (netPayVal != null && netPayVal > 0.0) {
                            onUpdateWeeklyPay(netPayVal)
                            onAddPayLog(
                                if (payNoteText.isBlank()) "Paystub" else payNoteText,
                                netPayVal
                            )
                            showPaySheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_pay_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Weekly Net Pay")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun BreakdownRowItem(
    icon: ImageVector,
    label: String,
    sublabel: String,
    amount: Double,
    isCredit: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Surface(
                shape = CircleShape,
                color = if (isCredit) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCredit) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Text(
            text = if (isCredit) "+${amount.formatCurrency()}" else "-${(-amount).formatCurrency()}",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = if (isCredit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
    }
}
