package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalGroceryStore
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.DebtItem
import com.example.data.MonthlyExpense
import com.example.data.SavingsFund
import com.example.data.TransactionItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random

fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "food & groceries", "food" -> Color(0xFF4CAF50)
        "transport" -> Color(0xFF2196F3)
        "shopping" -> Color(0xFFE91E63)
        "entertainment" -> Color(0xFFFF9800)
        "bills" -> Color(0xFF9C27B0)
        "health" -> Color(0xFF00BCD4)
        "savings" -> Color(0xFF8BC34A)
        "debt payment" -> Color(0xFFF44336)
        else -> {
            val random = Random(category.hashCode().toLong())
            Color(random.nextInt(256), random.nextInt(256), random.nextInt(256))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    transactions: List<TransactionItem>,
    debts: List<DebtItem>,
    savingsFunds: List<SavingsFund>,
    monthlyExpenses: List<MonthlyExpense>,
    onAddTransaction: (title: String, amount: Double, category: String, targetType: String, targetId: String, timestamp: Long) -> Unit,
    onDeleteTransaction: (TransactionItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddSheet by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Food & Groceries") }
    
    var selectedTargetType by remember { mutableStateOf("normal") } // normal, savings, debt, bill
    var selectedTargetId by remember { mutableStateOf("") }

    var filterCategory by remember { mutableStateOf("All") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    val categories = listOf(
        "Food & Groceries",
        "Transport",
        "Shopping",
        "Entertainment",
        "Bills",
        "Health",
        "Other"
    )
    
    val allTransactionCategories = transactions.map { it.category }.distinct().filter { it.isNotBlank() }
    val displayCategories = (categories + allTransactionCategories).distinct()

    val filteredTransactions = if (filterCategory == "All") {
        transactions
    } else {
        transactions.filter { it.category.equals(filterCategory, ignoreCase = true) }
    }.sortedByDescending { it.timestamp }

    val totalSpent = transactions.sumOf { it.amount }

    // Logic for Weekly and Monthly pie charts
    val cal = Calendar.getInstance()
    
    // Weekly logic
    cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val startOfWeek = cal.timeInMillis

    // Monthly logic
    val monthCal = Calendar.getInstance()
    monthCal.set(Calendar.DAY_OF_MONTH, 1)
    monthCal.set(Calendar.HOUR_OF_DAY, 0)
    monthCal.set(Calendar.MINUTE, 0)
    monthCal.set(Calendar.SECOND, 0)
    monthCal.set(Calendar.MILLISECOND, 0)
    val startOfMonth = monthCal.timeInMillis

    val weeklyTransactions = transactions.filter { it.timestamp >= startOfWeek }
    val monthlyTransactions = transactions.filter { it.timestamp >= startOfMonth }
    
    val weeklyTotal = weeklyTransactions.sumOf { it.amount }
    val monthlyTotal = monthlyTransactions.sumOf { it.amount }

    val weeklySlices = weeklyTransactions.groupBy { it.category }
        .map { (cat, txs) -> PieChartSlice(cat, txs.sumOf { it.amount }, getCategoryColor(cat)) }
        .sortedByDescending { it.value }

    val monthlySlices = monthlyTransactions.groupBy { it.category }
        .map { (cat, txs) -> PieChartSlice(cat, txs.sumOf { it.amount }, getCategoryColor(cat)) }
        .sortedByDescending { it.value }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { 
                    showAddSheet = true
                    selectedTargetType = "normal"
                    selectedCategory = "Food & Groceries"
                    selectedDateMillis = System.currentTimeMillis()
                },
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add Charge") },
                text = { Text("Log Charge") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("log_charge_fab")
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Title
            item {
                Column {
                    Text(
                        text = "Weekly Charges & Expenses",
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Input individual charges to deduct from disposable income",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            // Pie Charts for Weekly and Monthly
            item {
                if (weeklySlices.isNotEmpty()) {
                    Text("This Week", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    BudgetPieChart(
                        slices = weeklySlices,
                        totalValue = weeklyTotal,
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                if (monthlySlices.isNotEmpty()) {
                    Text("This Month", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                    BudgetPieChart(
                        slices = monthlySlices,
                        totalValue = monthlyTotal,
                        modifier = Modifier.fillMaxWidth().height(250.dp)
                    )
                }
            }

            // Total Spent Header Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("transactions_total_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Logged Charges",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = totalSpent.formatCurrency(),
                                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ShoppingCart,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Category Filter Chips
            item {
                Column {
                    Text(
                        text = "Filter by Category",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = filterCategory == "All",
                                onClick = { filterCategory = "All" },
                                label = { Text("All (${transactions.size})") }
                            )
                        }
                        items(displayCategories) { cat ->
                            val count = transactions.count { it.category.equals(cat, ignoreCase = true) }
                            FilterChip(
                                selected = filterCategory == cat,
                                onClick = { filterCategory = cat },
                                label = { Text("$cat ($count)") }
                            )
                        }
                    }
                }
            }

            // List Header
            item {
                Text(
                    text = "Transaction History (${filteredTransactions.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Empty state or list
            if (filteredTransactions.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalAtm,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No Charges Logged Yet",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Tap '+ Log Charge' to record daily groceries, gas, coffee, or shopping.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                items(filteredTransactions, key = { it.id }) { item ->
                    TransactionRowItem(
                        transaction = item,
                        onDelete = { onDeleteTransaction(item) }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Modal Sheet to Add Transaction
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Input New Charge",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Enter charge description, amount, date, and pick a target.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    label = { Text("Title / Description (e.g., Grocery Shopping)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("charge_title_input"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = amountInput,
                        onValueChange = { amountInput = it },
                        label = { Text("Amount ($)") },
                        leadingIcon = {
                            Text("$", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("charge_amount_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    val df = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .padding(top = 8.dp)
                            .clickable { showDatePicker = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = "Date", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = df.format(Date(selectedDateMillis)), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                
                Text("Select Charge Target Type", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val targetTypes = listOf(
                        "normal" to "Standard Category",
                        "savings" to "Savings Deposit",
                        "debt" to "Debt Payment",
                        "bill" to "Monthly Bill"
                    )
                    items(targetTypes) { (type, label) ->
                        FilterChip(
                            selected = selectedTargetType == type,
                            onClick = { 
                                selectedTargetType = type 
                                selectedTargetId = ""
                            },
                            label = { Text(label) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(14.dp))

                if (selectedTargetType == "normal") {
                    Text("Select Category", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.testTag("category_selector_row")
                    ) {
                        items(categories) { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                } else if (selectedTargetType == "savings") {
                    Text("Select Savings Fund", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(savingsFunds) { fund ->
                            FilterChip(
                                selected = selectedTargetId == fund.fundKey,
                                onClick = { selectedTargetId = fund.fundKey },
                                label = { Text(fund.name) }
                            )
                        }
                    }
                } else if (selectedTargetType == "debt") {
                    Text("Select Debt Account", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(debts) { debt ->
                            FilterChip(
                                selected = selectedTargetId == debt.id.toString(),
                                onClick = { selectedTargetId = debt.id.toString() },
                                label = { Text(debt.title) }
                            )
                        }
                    }
                } else if (selectedTargetType == "bill") {
                    Text("Select Monthly Bill", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(monthlyExpenses) { bill ->
                            FilterChip(
                                selected = selectedTargetId == bill.id.toString(),
                                onClick = { selectedTargetId = bill.id.toString() },
                                label = { Text(bill.title) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val amt = amountInput.toDoubleOrNull()
                        val isValidTarget = when(selectedTargetType) {
                            "normal" -> true
                            else -> selectedTargetId.isNotBlank()
                        }
                        
                        if (titleInput.isNotBlank() && amt != null && amt > 0.0 && isValidTarget) {
                            onAddTransaction(titleInput, amt, selectedCategory, selectedTargetType, selectedTargetId, selectedDateMillis)
                            titleInput = ""
                            amountInput = ""
                            showAddSheet = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_charge_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = when(selectedTargetType) {
                        "normal" -> titleInput.isNotBlank() && amountInput.toDoubleOrNull() ?: 0.0 > 0.0
                        else -> titleInput.isNotBlank() && amountInput.toDoubleOrNull() ?: 0.0 > 0.0 && selectedTargetId.isNotBlank()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Charge")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        selectedDateMillis = it
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun TransactionRowItem(
    transaction: TransactionItem,
    onDelete: () -> Unit
) {
    val categoryIcon: ImageVector = when (transaction.category.lowercase()) {
        "food & groceries", "food" -> Icons.Default.LocalGroceryStore
        "transport" -> Icons.Default.DirectionsCar
        "shopping" -> Icons.Default.ShoppingBag
        "entertainment" -> Icons.Default.Movie
        "bills", "bill" -> Icons.Default.Receipt
        "health" -> Icons.Default.LocalHospital
        else -> Icons.Default.LocalAtm
    }

    val dateFormatter = remember { SimpleDateFormat("MMM d, yyyy", Locale.US) }
    val formattedDate = dateFormatter.format(Date(transaction.timestamp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                            contentDescription = transaction.category,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${transaction.amount.formatCurrency()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_charge_${transaction.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Transaction",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
