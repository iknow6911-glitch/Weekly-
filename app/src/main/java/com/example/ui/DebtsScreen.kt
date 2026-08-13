package com.example.ui

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DebtItem
import com.example.data.DebtPaymentLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    debts: List<DebtItem>,
    debtPaymentLogs: List<DebtPaymentLog>,
    onAddDebt: (title: String, amount: Double, minPay: Double, interest: Double, category: String) -> Unit,
    onMakePayment: (debt: DebtItem, amount: Double, note: String, recordAsTransaction: Boolean) -> Unit,
    onDeleteDebt: (DebtItem) -> Unit,
    onUpdateDebtBalance: (DebtItem, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddBottomSheet by remember { mutableStateOf(false) }
    var selectedDebtForPayment by remember { mutableStateOf<DebtItem?>(null) }
    var selectedDebtForEdit by remember { mutableStateOf<DebtItem?>(null) }

    val totalRemainingDebt = debts.sumOf { it.remainingBalance }
    val totalInitialDebt = debts.sumOf { if (it.initialAmount > 0) it.initialAmount else it.remainingBalance }
    val totalPaidOff = (totalInitialDebt - totalRemainingDebt).coerceAtLeast(0.0)
    val payoffProgress = if (totalInitialDebt > 0) (totalPaidOff / totalInitialDebt).toFloat().coerceIn(0f, 1f) else 0f

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("debt_summary_card"),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2C3238)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column {
                                Text(
                                    text = "Total Outstanding Debt",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                                Text(
                                    text = totalRemainingDebt.formatCurrency(),
                                    style = MaterialTheme.typography.displaySmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 36.sp
                                    ),
                                    color = Color.White
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.15f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TrendingDown,
                                        contentDescription = "Debt Icon",
                                        tint = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Total Paid Off: ${totalPaidOff.formatCurrency()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "${(payoffProgress * 100).toInt()}% Cleared",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { payoffProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = Color(0xFF81C784),
                                trackColor = Color.White.copy(alpha = 0.2f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${debts.size} Active Debts",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Target Paydown Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF81C784)
                            )
                        }
                    }
                }
            }

            // Section Title
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Debts",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Button(
                        onClick = { showAddBottomSheet = true },
                        modifier = Modifier.testTag("add_debt_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Debt",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Debt")
                    }
                }
            }

            if (debts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No Debts Tracked!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap 'Add Debt' above to start tracking credit cards, loans, or medical bills.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(debts, key = { "debt_${it.id}" }) { debt ->
                    DebtItemCard(
                        debt = debt,
                        onPaymentClick = { selectedDebtForPayment = debt },
                        onEditClick = { selectedDebtForEdit = debt },
                        onDeleteClick = { onDeleteDebt(debt) }
                    )
                }
            }

            // Payment History Section
            if (debtPaymentLogs.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Payment History",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(debtPaymentLogs.take(10), key = { "log_${it.id}" }) { log ->
                    DebtPaymentLogRow(log = log)
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .testTag("fab_add_debt")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Debt")
        }
    }

    // Add Debt Modal Bottom Sheet
    if (showAddBottomSheet) {
        AddDebtBottomSheet(
            onDismiss = { showAddBottomSheet = false },
            onSave = { title, amt, minPay, interest, category ->
                onAddDebt(title, amt, minPay, interest, category)
                showAddBottomSheet = false
            }
        )
    }

    // Subtract Payment Dialog
    selectedDebtForPayment?.let { debt ->
        SubtractPaymentDialog(
            debt = debt,
            onDismiss = { selectedDebtForPayment = null },
            onSubmitPayment = { amount, note, recordAsTransaction ->
                onMakePayment(debt, amount, note, recordAsTransaction)
                selectedDebtForPayment = null
            }
        )
    }

    // Edit Debt Balance Dialog
    selectedDebtForEdit?.let { debt ->
        EditDebtBalanceDialog(
            debt = debt,
            onDismiss = { selectedDebtForEdit = null },
            onSaveNewBalance = { newBalance ->
                onUpdateDebtBalance(debt, newBalance)
                selectedDebtForEdit = null
            }
        )
    }
}

@Composable
fun DebtItemCard(
    debt: DebtItem,
    onPaymentClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPaidOff = debt.remainingBalance <= 0.0
    val categoryIcon = getDebtCategoryIcon(debt.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("debt_card_${debt.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPaidOff) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = debt.category,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = debt.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
                            ) {
                                Text(
                                    text = debt.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (debt.interestRate > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${debt.interestRate}% APR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Balance",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Debt",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Current Balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = debt.remainingBalance.formatCurrency(),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = if (isPaidOff) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }

                if (debt.minimumPayment > 0) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Min Payment",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "${debt.minimumPayment.formatCurrency()}/mo",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isPaidOff) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PAID OFF!",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Button(
                    onClick = onPaymentClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subtract_payment_button_${debt.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Payment,
                        contentDescription = "Subtract Payment",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Subtract Payment")
                }
            }
        }
    }
}

@Composable
fun SubtractPaymentDialog(
    debt: DebtItem,
    onDismiss: () -> Unit,
    onSubmitPayment: (amount: Double, note: String, recordAsTransaction: Boolean) -> Unit
) {
    var paymentInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("") }
    var recordAsTransaction by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Subtract Payment: ${debt.title}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Current Balance: ${debt.remainingBalance.formatCurrency()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                OutlinedTextField(
                    value = paymentInput,
                    onValueChange = {
                        paymentInput = it
                        errorMessage = null
                    },
                    label = { Text("Payment Amount ($)") },
                    placeholder = { Text("e.g. 50.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payment_amount_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                OutlinedTextField(
                    value = noteInput,
                    onValueChange = { noteInput = it },
                    label = { Text("Note (Optional)") },
                    placeholder = { Text("e.g. Bi-weekly pay check deduction") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("payment_note_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = recordAsTransaction,
                        onCheckedChange = { recordAsTransaction = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add to weekly charges / expenses",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = paymentInput.toDoubleOrNull()
                    if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter a valid payment amount"
                    } else {
                        onSubmitPayment(amt, noteInput, recordAsTransaction)
                    }
                },
                modifier = Modifier.testTag("submit_payment_button")
            ) {
                Text("Deduct Payment")
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
fun EditDebtBalanceDialog(
    debt: DebtItem,
    onDismiss: () -> Unit,
    onSaveNewBalance: (Double) -> Unit
) {
    var balanceInput by remember(debt) { mutableStateOf(debt.remainingBalance.formatTwoDecimals()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Balance: ${debt.title}") },
        text = {
            Column {
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = {
                        balanceInput = it
                        errorMessage = null
                    },
                    label = { Text("Current Balance ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = errorMessage != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("edit_balance_input")
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newBal = balanceInput.toDoubleOrNull()
                    if (newBal == null || newBal < 0.0) {
                        errorMessage = "Please enter a valid balance"
                    } else {
                        onSaveNewBalance(newBal)
                    }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtBottomSheet(
    onDismiss: () -> Unit,
    onSave: (title: String, amount: Double, minPay: Double, interest: Double, category: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var minPayInput by remember { mutableStateOf("") }
    var interestInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Credit Card") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = listOf("Credit Card", "Loan", "Car Payment", "Medical", "Mortgage", "Personal")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Add New Debt",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = titleInput,
                onValueChange = {
                    titleInput = it
                    errorMessage = null
                },
                label = { Text("Debt Name *") },
                placeholder = { Text("e.g. Chase Sapphire, Auto Loan") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("add_debt_title_input")
            )

            OutlinedTextField(
                value = amountInput,
                onValueChange = {
                    amountInput = it
                    errorMessage = null
                },
                label = { Text("Current Total Balance ($) *") },
                placeholder = { Text("e.g. 1500.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("add_debt_amount_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = minPayInput,
                    onValueChange = { minPayInput = it },
                    label = { Text("Min Payment ($/mo)") },
                    placeholder = { Text("e.g. 35.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = interestInput,
                    onValueChange = { interestInput = it },
                    label = { Text("APR (%)") },
                    placeholder = { Text("e.g. 19.9") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = "Category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.drop(3).forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val amt = amountInput.toDoubleOrNull()
                    if (titleInput.isBlank()) {
                        errorMessage = "Please enter a debt name"
                    } else if (amt == null || amt <= 0.0) {
                        errorMessage = "Please enter a valid balance amount"
                    } else {
                        val minPay = minPayInput.toDoubleOrNull() ?: 0.0
                        val interest = interestInput.toDoubleOrNull() ?: 0.0
                        onSave(titleInput, amt, minPay, interest, selectedCategory)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_debt_button")
            ) {
                Text("Save Debt")
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun DebtPaymentLogRow(log: DebtPaymentLog) {
    val dateFormatter = remember { SimpleDateFormat("MMM dd, yyyy", Locale.US) }
    val formattedDate = dateFormatter.format(Date(log.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Payment to ${log.debtTitle}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (log.note.isNotBlank()) "${log.note} • $formattedDate" else formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Text(
                text = "-${log.amountPaid.formatCurrency()}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

fun getDebtCategoryIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "credit card" -> Icons.Default.CreditCard
        "loan" -> Icons.Default.Money
        "car payment", "auto" -> Icons.Default.DirectionsCar
        "medical" -> Icons.Default.LocalHospital
        "mortgage" -> Icons.Default.Home
        else -> Icons.Default.Payment
    }
}
