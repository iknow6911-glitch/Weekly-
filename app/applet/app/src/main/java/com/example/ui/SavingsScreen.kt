package com.example.ui

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Umbrella
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.MonthlyExpense
import com.example.data.SavingsDepositLog
import com.example.data.SavingsFund
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    savingsFunds: List<SavingsFund>,
    savingsLogs: List<SavingsDepositLog>,
    monthlyExpenses: List<MonthlyExpense>,
    onLogDeposit: (fundKey: String, fundName: String, amount: Double, note: String) -> Unit,
    onUpdateTarget: (fundKey: String, newTarget: Double) -> Unit,
    onUpdateCustomName: (fundKey: String, newName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMonthlyExpenses = monthlyExpenses.sumOf { it.monthlyAmount }
    val emergencyTarget = totalMonthlyExpenses * 3.0 // Always 3 months of expenses

    val emergencyFund = savingsFunds.find { it.fundKey == "emergency" }
        ?: SavingsFund("emergency", "Emergency Fund (3-Month)", emergencyTarget, 0.0)

    val rainyDayFund = savingsFunds.find { it.fundKey == "rainy_day" }
        ?: SavingsFund("rainy_day", "Rainy Day Fund", 1000.0, 0.0)

    val customFund = savingsFunds.find { it.fundKey == "custom" }
        ?: SavingsFund("custom", "Custom Savings Goal", 2500.0, 0.0)

    var activeDepositFund by remember { mutableStateOf<SavingsFund?>(null) }
    var fundToEdit by remember { mutableStateOf<SavingsFund?>(null) }

    val totalSavings = emergencyFund.currentBalance + rainyDayFund.currentBalance + customFund.currentBalance

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        // Header
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
                            imageVector = Icons.Default.Savings,
                            contentDescription = "Savings",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Savings & Funds",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Total Saved: ${totalSavings.formatCurrency()}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Emergency Fund Card (3-Month Goal)
        item {
            SavingsFundCard(
                fund = emergencyFund,
                computedTarget = emergencyTarget,
                icon = Icons.Default.Security,
                badgeColor = Color(0xFF1E88E5),
                subtitle = "Goal automatically locked to 3 months of expenses (${totalMonthlyExpenses.formatCurrency()}/mo × 3)",
                isEmergencyFund = true,
                onDepositClick = { activeDepositFund = emergencyFund },
                onEditClick = null
            )
        }

        // Rainy Day Fund Card
        item {
            SavingsFundCard(
                fund = rainyDayFund,
                computedTarget = rainyDayFund.targetAmount,
                icon = Icons.Default.Umbrella,
                badgeColor = Color(0xFFFF8F00),
                subtitle = "For unexpected minor repairs or short-term needs",
                isEmergencyFund = false,
                onDepositClick = { activeDepositFund = rainyDayFund },
                onEditClick = { fundToEdit = rainyDayFund }
            )
        }

        // Custom Goal Fund Card
        item {
            SavingsFundCard(
                fund = customFund,
                computedTarget = customFund.targetAmount,
                icon = Icons.Default.Star,
                badgeColor = Color(0xFF8E24AA),
                subtitle = "Customizable savings goal (e.g., Vacation, Down Payment)",
                isEmergencyFund = false,
                onDepositClick = { activeDepositFund = customFund },
                onEditClick = { fundToEdit = customFund }
            )
        }

        // Deposit History Section Header
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Recent Savings Deposits",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (savingsLogs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No deposits logged yet. Tap '+ Log Deposit' on any fund above!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(savingsLogs.take(15), key = { "deposit_${it.id}" }) { log ->
                SavingsDepositRow(log = log)
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    // Deposit Dialog
    activeDepositFund?.let { fund ->
        val effectiveTarget = if (fund.fundKey == "emergency") emergencyTarget else fund.targetAmount
        DepositDialog(
            fund = fund,
            effectiveTarget = effectiveTarget,
            onDismiss = { activeDepositFund = null },
            onConfirm = { amount, note ->
                onLogDeposit(fund.fundKey, fund.name, amount, note)
                activeDepositFund = null
            }
        )
    }

    // Edit Target / Name Dialog
    fundToEdit?.let { fund ->
        EditFundDialog(
            fund = fund,
            onDismiss = { fundToEdit = null },
            onSave = { newName, newTarget ->
                if (fund.fundKey == "custom" && newName.isNotBlank() && newName != fund.name) {
                    onUpdateCustomName(fund.fundKey, newName)
                }
                onUpdateTarget(fund.fundKey, newTarget)
                fundToEdit = null
            }
        )
    }
}

@Composable
fun SavingsFundCard(
    fund: SavingsFund,
    computedTarget: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    subtitle: String,
    isEmergencyFund: Boolean,
    onDepositClick: () -> Unit,
    onEditClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val progress = if (computedTarget > 0) (fund.currentBalance / computedTarget).toFloat().coerceIn(0f, 1f) else 0f
    val percentInt = (progress * 100).toInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("savings_card_${fund.fundKey}"),
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = badgeColor.copy(alpha = 0.15f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = fund.name,
                                tint = badgeColor
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = fund.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                if (onEditClick != null) {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Goal",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = badgeColor,
                trackColor = badgeColor.copy(alpha = 0.15f)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = fund.currentBalance.formatCurrency(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target Goal ($percentInt%)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = computedTarget.formatCurrency(),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDepositClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("deposit_btn_${fund.fundKey}"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Log Deposit to ${fund.name}")
            }
        }
    }
}

@Composable
fun DepositDialog(
    fund: SavingsFund,
    effectiveTarget: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, note: String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Add Deposit to ${fund.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Current balance: ${fund.currentBalance.formatCurrency()} / Goal: ${effectiveTarget.formatCurrency()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                // Presets
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(25.0, 50.0, 100.0, 250.0).forEach { preset ->
                        FilterChip(
                            selected = amountText == preset.toInt().toString(),
                            onClick = { amountText = preset.toInt().toString() },
                            label = { Text("+$${preset.toInt()}", fontSize = 12.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Deposit Amount ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Optional Note / Source") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, noteText)
                    }
                },
                enabled = (amountText.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text("Confirm Deposit")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun EditFundDialog(
    fund: SavingsFund,
    onDismiss: () -> Unit,
    onSave: (newName: String, newTarget: Double) -> Unit
) {
    var nameText by remember { mutableStateOf(fund.name) }
    var targetText by remember { mutableStateOf(fund.targetAmount.formatTwoDecimals()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Edit ${fund.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (fund.fundKey == "custom") {
                    OutlinedTextField(
                        value = nameText,
                        onValueChange = { nameText = it },
                        label = { Text("Fund Name (e.g. Vacation, House)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = targetText,
                    onValueChange = { targetText = it },
                    label = { Text("Target Goal Amount ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetText.toDoubleOrNull() ?: fund.targetAmount
                    onSave(nameText, target)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SavingsDepositRow(
    log: SavingsDepositLog,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • h:mm a", Locale.getDefault()) }
    val formattedDate = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.fundName,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                if (log.note.isNotBlank()) {
                    Text(
                        text = log.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Text(
                text = "+${log.amount.formatCurrency()}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF2E7D32)
            )
        }
    }
}
