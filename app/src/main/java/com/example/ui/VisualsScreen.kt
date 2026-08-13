package com.example.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BudgetSettings
import com.example.data.DebtItem
import com.example.data.MonthlyExpense
import com.example.data.SavingsFund
import com.example.data.TransactionItem
import com.example.data.WeeklyPayLog

data class PieChartSlice(
    val name: String,
    val value: Double,
    val color: Color
)

@Composable
fun VisualsScreen(
    settings: BudgetSettings,
    monthlyExpenses: List<MonthlyExpense>,
    transactions: List<TransactionItem>,
    debts: List<DebtItem>,
    payLogs: List<WeeklyPayLog>,
    savingsFunds: List<SavingsFund> = emptyList(),
    modifier: Modifier = Modifier
) {
    val weeklyNetPay = settings.weeklyNetPay
    val weeklySavings = weeklyNetPay * (settings.savingsPercentage / 100.0)
    val totalMonthlyExpenses = monthlyExpenses.sumOf { it.monthlyAmount }
    val weeklyProratedBills = (totalMonthlyExpenses * 12.0) / 52.0
    val weeklyDebtAllocation = settings.weeklyDebtPayment
    val totalWeeklyCharges = transactions.filter { it.category != "Bill" && it.category != "Debt" && it.category != "Savings" }.sumOf { it.amount }

    val totalDeductions = weeklySavings + weeklyProratedBills + weeklyDebtAllocation + totalWeeklyCharges
    val remainingIncome = (weeklyNetPay - totalDeductions).coerceAtLeast(0.0)

    val pieSlices = remember(weeklyNetPay, weeklySavings, weeklyProratedBills, weeklyDebtAllocation, totalWeeklyCharges, remainingIncome) {
        listOf(
            PieChartSlice("Savings", weeklySavings, Color(0xFF4CAF50)),
            PieChartSlice("Monthly Bills", weeklyProratedBills, Color(0xFF2196F3)),
            PieChartSlice("Debt Paydown", weeklyDebtAllocation, Color(0xFFE91E63)),
            PieChartSlice("Weekly Charges", totalWeeklyCharges, Color(0xFFFF9800)),
            PieChartSlice("Remaining Budget", remainingIncome, Color(0xFF9C27B0))
        ).filter { it.value > 0 }
    }

    val totalDebts = debts.sumOf { it.remainingBalance }
    val initialDebts = debts.sumOf { if (it.initialAmount > 0) it.initialAmount else it.remainingBalance }
    val totalDebtPaid = (initialDebts - totalDebts).coerceAtLeast(0.0)
    val debtPaidPercentage = if (initialDebts > 0) ((totalDebtPaid / initialDebts) * 100).toInt() else 0

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Screen Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Visuals",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Financial Visuals & Insights",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Breakdown of income, spending, savings & debt",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Budget Allocation Pie Chart Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pie_chart_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Weekly Income Allocation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Net: ${weeklyNetPay.formatCurrency()}/wk",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (weeklyNetPay <= 0) {
                        Text(
                            text = "Set your weekly net pay on the Home tab to see visual allocation.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            modifier = Modifier.padding(24.dp)
                        )
                    } else {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(200.dp)
                                .padding(8.dp)
                        ) {
                            BudgetPieChart(slices = pieSlices, totalValue = weeklyNetPay)
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "FREE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                                Text(
                                    text = remainingIncome.formatCurrency(),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Legend & Breakdown
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pieSlices.forEach { slice ->
                                val percent = ((slice.value / weeklyNetPay) * 100).toInt()
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(slice.color)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = slice.name,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = slice.value.formatCurrency(),
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "($percent%)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Weekly vs Monthly Comparison Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weekly_monthly_chart_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "Weekly vs Monthly Cash Flow",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Compare 1 week vs 1 month (4.33 weeks)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    val monthlyNet = weeklyNetPay * 4.3333
                    val monthlySavings = weeklySavings * 4.3333
                    val monthlyBills = totalMonthlyExpenses
                    val monthlyDebt = weeklyDebtAllocation * 4.3333
                    val monthlyCharges = totalWeeklyCharges * 4.3333

                    BarChartRow(
                        label = "Net Income",
                        weeklyVal = weeklyNetPay,
                        monthlyVal = monthlyNet,
                        barColor = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BarChartRow(
                        label = "Fixed Bills",
                        weeklyVal = weeklyProratedBills,
                        monthlyVal = monthlyBills,
                        barColor = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BarChartRow(
                        label = "Debt Paydown",
                        weeklyVal = weeklyDebtAllocation,
                        monthlyVal = monthlyDebt,
                        barColor = Color(0xFFE91E63)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BarChartRow(
                        label = "Savings",
                        weeklyVal = weeklySavings,
                        monthlyVal = monthlySavings,
                        barColor = Color(0xFF009688)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    BarChartRow(
                        label = "Weekly Charges",
                        weeklyVal = totalWeeklyCharges,
                        monthlyVal = monthlyCharges,
                        barColor = Color(0xFFFF9800)
                    )
                }
            }
        }

        // Debt Payoff Progress Visual
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("debt_visual_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Debt Breakdown & Progress",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$debtPaidPercentage% Cleared",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF4CAF50)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { if (initialDebts > 0) (totalDebtPaid / initialDebts).toFloat().coerceIn(0f, 1f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color(0xFF4CAF50),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Total Debt Remaining",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = totalDebts.formatCurrency(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Total Paid Off",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = totalDebtPaid.formatCurrency(),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF4CAF50)
                            )
                        }
                    }

                    if (debts.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Individual Debts",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        debts.forEach { debt ->
                            val percentRemaining = if (debt.initialAmount > 0) ((debt.remainingBalance / debt.initialAmount) * 100).toInt() else 100
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = debt.title,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    LinearProgressIndicator(
                                        progress = { (1f - (percentRemaining / 100f)).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth(0.9f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }
                                Text(
                                    text = debt.remainingBalance.formatCurrency(),
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Savings Funds Progress Card
        item {
            val emergencyTarget = totalMonthlyExpenses * 3.0
            val emergencyFund = savingsFunds.find { it.fundKey == "emergency" }
                ?: SavingsFund("emergency", "Emergency Fund (3-Month)", emergencyTarget, 0.0)
            val rainyDayFund = savingsFunds.find { it.fundKey == "rainy_day" }
                ?: SavingsFund("rainy_day", "Rainy Day Fund", 1000.0, 0.0)
            val customFund = savingsFunds.find { it.fundKey == "custom" }
                ?: SavingsFund("custom", "Custom Savings Goal", 2500.0, 0.0)

            val totalSaved = emergencyFund.currentBalance + rainyDayFund.currentBalance + customFund.currentBalance
            val totalGoal = emergencyTarget + rainyDayFund.targetAmount + customFund.targetAmount
            val overallPercent = if (totalGoal > 0) ((totalSaved / totalGoal) * 100).toInt() else 0

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("savings_visual_card"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Savings Funds Visual Breakdown",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "$overallPercent% Goal Met",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { if (totalGoal > 0) (totalSaved / totalGoal).toFloat().coerceIn(0f, 1f) else 0f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = Color(0xFF2E7D32),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Emergency Fund
                    SavingsBarRow(
                        title = "Emergency Fund (3-Mo Goal)",
                        balance = emergencyFund.currentBalance,
                        target = emergencyTarget,
                        barColor = Color(0xFF1E88E5)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Rainy Day Fund
                    SavingsBarRow(
                        title = rainyDayFund.name,
                        balance = rainyDayFund.currentBalance,
                        target = rainyDayFund.targetAmount,
                        barColor = Color(0xFFFF8F00)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Custom Goal Fund
                    SavingsBarRow(
                        title = customFund.name,
                        balance = customFund.currentBalance,
                        target = customFund.targetAmount,
                        barColor = Color(0xFF8E24AA)
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun SavingsBarRow(
    title: String,
    balance: Double,
    target: Double,
    barColor: Color
) {
    val progress = if (target > 0) (balance / target).toFloat().coerceIn(0f, 1f) else 0f
    val percent = (progress * 100).toInt()

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "${balance.formatCurrency()} / ${target.formatCurrency()} ($percent%)",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = barColor,
            trackColor = barColor.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun BudgetPieChart(
    slices: List<PieChartSlice>,
    totalValue: Double,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val strokeWidth = 32.dp.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2)
        val pieSize = Size(diameter, diameter)

        var startAngle = -90f

        slices.forEach { slice ->
            val sweepAngle = ((slice.value / totalValue) * 360f).toFloat() * animationProgress.value
            drawArc(
                color = slice.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = pieSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun BarChartRow(
    label: String,
    weeklyVal: Double,
    monthlyVal: Double,
    barColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
            Text(
                text = "${weeklyVal.formatCurrency()}/wk • ${monthlyVal.formatCurrency()}/mo",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = barColor
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = barColor.copy(alpha = 0.85f),
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
            ) {}
        }
    }
}
