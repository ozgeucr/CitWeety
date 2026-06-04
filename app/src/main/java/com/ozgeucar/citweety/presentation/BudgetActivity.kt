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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

data class ExpenseItem(val id: String, val title: String, val amount: Double, val category: String)

class BudgetActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Intent'ten tripId'yi alıyoruz
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

    // DÜZELTME: getExpensesFlow(tripId) kullanıldı
    val savedExpenses by dataStore.getExpensesFlow(tripId).collectAsState(initial = "[]")

    LaunchedEffect(savedExpenses) {
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        expenses = gson.fromJson(savedExpenses, type) ?: emptyList()
    }

    val totalBudget = 1000.0
    val totalSpent = expenses.sumOf { it.amount }
    val remainingBudget = totalBudget - totalSpent
    val progress = (totalSpent / totalBudget).toFloat()

    val animatedProgress by animateFloatAsState(targetValue = progress.coerceIn(0f, 1f))
    val progressColor by animateColorAsState(targetValue = if (progress < 0.8f) Color(0xFF4CAF50) else Color(0xFFF44336))

    Scaffold(
        topBar = { TopAppBar(title = { Text("Bütçe Takibi", fontWeight = FontWeight.Bold) }, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1E3A5F), titleContentColor = Color.White)) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = Color(0xFFFFC107)) {
                Icon(Icons.Default.Add, contentDescription = "Ekle", tint = Color.Black)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).background(Color(0xFFF8F9FA)).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Kalan Bütçe", color = Color.Gray, fontSize = 14.sp)
                    Text("€${"%.2f".format(remainingBudget)}", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E3A5F))
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(progress = animatedProgress, modifier = Modifier.fillMaxWidth().height(8.dp), color = progressColor)
                }
            }

            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(expenses) { expense ->
                    Card(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(expense.title, fontWeight = FontWeight.Bold)
                                Text(expense.category, fontSize = 12.sp, color = Color.Gray)
                            }
                            Text("-€${"%.2f".format(expense.amount)}", color = Color.Red, fontWeight = FontWeight.Bold)
                            IconButton(onClick = {
                                val newList = expenses.filter { it.id != expense.id }
                                expenses = newList
                                // DÜZELTME: tripId parametresi eklendi
                                scope.launch { dataStore.saveExpenses(tripId, gson.toJson(newList)) }
                            }) { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.LightGray) }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddExpenseDialog(onDismiss = { showAddDialog = false }, onAdd = { title, amount, cat ->
                val newList = expenses + ExpenseItem(System.currentTimeMillis().toString(), title, amount, cat)
                expenses = newList
                // DÜZELTME: tripId parametresi eklendi
                scope.launch { dataStore.saveExpenses(tripId, gson.toJson(newList)) }
                showAddDialog = false
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseDialog(onDismiss: () -> Unit, onAdd: (String, Double, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Yemek") }
    val categories = listOf("Yemek", "Ulaşım", "Müze/Aktivite", "Alışveriş", "Diğer")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.background(Color.White).padding(20.dp)) {
                Text("Yeni Harcama Ekle", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Neye harcadın?") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Tutar (€)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                Text("Kategori", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(top = 16.dp))
                Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    categories.forEach { cat ->
                        FilterChip(
                            selected = (category == cat),
                            onClick = { category = cat },
                            label = { Text(text = cat, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFC107))
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("İptal") }
                    Button(onClick = {
                        val amount = amountText.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && amount > 0) onAdd(title, amount, category)
                    }) { Text("Ekle") }
                }
            }
        }
    }
}