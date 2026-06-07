package com.ozgeucar.citweety.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ozgeucar.citweety.R
import kotlinx.coroutines.launch

data class ExpenseItem(val id: String, val title: String, val amount: Double, val category: String, val note: String = "")

class BudgetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tripId = intent.getStringExtra("TRIP_ID") ?: "default_trip"
        setContent { MaterialTheme { BudgetScreen(tripId) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(tripId: String) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val gson = remember { Gson() }
    val scope = rememberCoroutineScope()

    var expenses by remember { mutableStateOf(listOf<ExpenseItem>()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }

    val savedExpenses by dataStore.getExpensesFlow(tripId).collectAsState(initial = "[]")
    val totalBudget by dataStore.getTotalBudgetFlow(tripId).collectAsState(initial = 0.0)
    val currency by dataStore.getCurrencyFlow(tripId).collectAsState(initial = "€")

    LaunchedEffect(savedExpenses) {
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        expenses = gson.fromJson(savedExpenses, type) ?: emptyList()
    }

    val totalSpent = expenses.sumOf { it.amount }
    val remainingBudget = totalBudget - totalSpent
    val progress = if (totalBudget > 0) (totalSpent / totalBudget).toFloat() else 0f
    val today = System.currentTimeMillis() / (24 * 60 * 60 * 1000)
    val todaySpent = expenses.filter { (it.id.toLongOrNull() ?: 0L) / (24 * 60 * 60 * 1000) == today }.sumOf { it.amount }
    val dailyLimit = totalBudget / 7
    val showWarning = todaySpent > dailyLimit

    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f))
    val progressColor by animateColorAsState(targetValue = when { progress > 0.9f -> Color(0xFFF44336); progress > 0.7f -> Color(0xFFFF9800); else -> Color(0xFF4CAF50) })

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.budget_detail_title), fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A5F), titleContentColor = Color.White)) },
        floatingActionButton = { FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFFFFC107)) { Icon(Icons.Default.Add, contentDescription = stringResource(R.string.budget_detail_add_desc), tint = Color.Black) } }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA)).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), onClick = { showEditBudgetDialog = true }) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(stringResource(R.string.budget_detail_remaining), color = Color.Gray, fontSize = 14.sp)
                            Text("$currency${"%.2f".format(remainingBudget)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = if (remainingBudget < 0) Color.Red else Color(0xFF1E3A5F))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(stringResource(R.string.budget_detail_total), color = Color.Gray, fontSize = 12.sp)
                            Text("$currency${"%.2f".format(totalBudget)}", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = animatedProgress, modifier = Modifier.fillMaxWidth().height(12.dp), color = progressColor, trackColor = progressColor.copy(alpha = 0.2f))
                    if (showWarning) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(stringResource(R.string.budget_detail_warning, currency, "%.2f".format(todaySpent)), color = Color(0xFFD32F2F), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(stringResource(R.string.budget_detail_expenses_title), modifier = Modifier.padding(top = 24.dp, bottom = 8.dp), fontWeight = FontWeight.Bold, fontSize = 18.sp)

            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(expenses.reversed()) { expense ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.title, fontWeight = FontWeight.Bold)
                                if (expense.note.isNotBlank()) Text(expense.note, fontSize = 13.sp, color = Color.DarkGray, modifier = Modifier.padding(top = 2.dp))
                                Text(expense.category, fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 2.dp))
                            }
                            Text("-$currency${"%.2f".format(expense.amount)}", color = Color(0xFFD32F2F), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { val newList = expenses.filter { it.id != expense.id }; expenses = newList; scope.launch { dataStore.saveExpenses(tripId, gson.toJson(newList)) } }) {
                                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.budget_detail_delete_desc), tint = Color.LightGray)
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddExpenseDialog(currency = currency, onDismiss = { showAddDialog = false }, onAdd = { title, amount, cat, note -> val newList = expenses + ExpenseItem(id = System.currentTimeMillis().toString(), title = title, amount = amount, category = cat, note = note); expenses = newList; scope.launch { dataStore.saveExpenses(tripId, gson.toJson(newList)) }; showAddDialog = false })
        }

        if (showEditBudgetDialog) {
            EditBudgetDialog(currentBudget = totalBudget, currentCurrency = currency, onDismiss = { showEditBudgetDialog = false }, onConfirm = { newBudget, newCurrency -> scope.launch { dataStore.saveTotalBudget(tripId, newBudget); dataStore.saveCurrency(tripId, newCurrency) }; showEditBudgetDialog = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBudgetDialog(currentBudget: Double, currentCurrency: String, onDismiss: () -> Unit, onConfirm: (Double, String) -> Unit) {
    var budgetText by remember { mutableStateOf(currentBudget.toString()) }
    var selectedCurrency by remember { mutableStateOf(currentCurrency) }
    val currencies = listOf("$", "€", "₺", "zł")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.background(Color.White).padding(20.dp)) {
                Text(stringResource(R.string.budget_edit_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.budget_edit_currency_title), color = Color.Gray, fontSize = 12.sp)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { curr -> FilterChip(selected = (selectedCurrency == curr), onClick = { selectedCurrency = curr }, label = { Text(curr) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFC107))) }
                }

                OutlinedTextField(value = budgetText, onValueChange = { budgetText = it }, label = { Text(stringResource(R.string.budget_edit_amount_hint, selectedCurrency)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.budget_edit_cancel)) }
                    Button(onClick = { val amount = budgetText.toDoubleOrNull() ?: currentBudget; onConfirm(amount, selectedCurrency) }) { Text(stringResource(R.string.budget_edit_save)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(currency: String, onDismiss: () -> Unit, onAdd: (String, Double, String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val categories = stringArrayResource(R.array.budget_categories).toList()
    var category by remember { mutableStateOf(categories.firstOrNull() ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.background(Color.White).padding(20.dp).verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.budget_add_title), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(stringResource(R.string.budget_add_item_hint)) }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text(stringResource(R.string.budget_add_amount_hint, currency)) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(value = note, onValueChange = { note = it }, label = { Text(stringResource(R.string.budget_add_note_hint)) }, modifier = Modifier.fillMaxWidth())

                Text(stringResource(R.string.budget_add_category_title), color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.take(3).forEach { cat -> FilterChip(selected = (category == cat), onClick = { category = cat }, label = { Text(text = cat, fontSize = 11.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFC107))) }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.drop(3).forEach { cat -> FilterChip(selected = (category == cat), onClick = { category = cat }, label = { Text(text = cat, fontSize = 11.sp) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFC107))) }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.budget_edit_cancel)) }
                    Button(onClick = { val amount = amountText.toDoubleOrNull() ?: 0.0; if (title.isNotBlank() && amount > 0) onAdd(title, amount, category, note) }) { Text(stringResource(R.string.budget_add_btn)) }
                }
            }
        }
    }
}