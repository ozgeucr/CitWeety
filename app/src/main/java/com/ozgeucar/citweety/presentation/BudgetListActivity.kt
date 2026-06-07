package com.ozgeucar.citweety.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.domain.model.TripBudget
import kotlinx.coroutines.launch

class BudgetListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BudgetListScreen(onCityClick = { budget ->
                    val intent = Intent(this, BudgetActivity::class.java).apply {
                        putExtra("CITY_NAME", budget.cityName)
                        putExtra("TRIP_ID", budget.id)
                    }
                    startActivity(intent)
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(onCityClick: (TripBudget) -> Unit) {
    val context = LocalContext.current
    val dataStore = remember { DataStoreManager(context) }
    val gson = remember { Gson() }
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }

    val savedTrips by dataStore.getTripsFlow.collectAsState(initial = "[]")
    val trips = remember(savedTrips) {
        val type = object : TypeToken<List<TripBudget>>() {}.type
        gson.fromJson<List<TripBudget>>(savedTrips, type) ?: emptyList()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.budget_list_title), fontWeight = FontWeight.Black, fontSize = 22.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E3A5F),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFFC107),
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.budget_add_city_desc), tint = Color.Black, modifier = Modifier.size(30.dp))
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E3A5F), Color(0xFFF0F2F5)),
                        startY = 0f,
                        endY = 300f
                    )
                )
        ) {
            if (trips.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(color = Color.White.copy(alpha = 0.2f), shape = CircleShape, modifier = Modifier.size(120.dp)) {
                        Icon(imageVector = Icons.Default.Wallet, contentDescription = null, modifier = Modifier.padding(24.dp), tint = Color.White)
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(stringResource(R.string.budget_empty_title), fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = Color.White)
                    Text(
                        stringResource(R.string.budget_empty_desc),
                        fontSize = 16.sp, color = Color.White.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(trips) { trip ->
                        val tripCurrency by dataStore.getCurrencyFlow(trip.id).collectAsState(initial = "€")
                        val tripTotalBudget by dataStore.getTotalBudgetFlow(trip.id).collectAsState(initial = trip.totalBudget)

                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onCityClick(trip) },
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(modifier = Modifier.padding(20.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(trip.cityName, fontWeight = FontWeight.Black, fontSize = 22.sp, color = Color(0xFF1E3A5F))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(color = Color(0xFFFFC107).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                            Text(
                                                stringResource(R.string.budget_prefix, tripCurrency, "%.2f".format(tripTotalBudget)),
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF856404)
                                            )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val newList = trips.filter { it.id != trip.id }
                                        scope.launch { dataStore.saveTrips(gson.toJson(newList)) }
                                    },
                                    modifier = Modifier.background(Color(0xFFFEEBEE), RoundedCornerShape(12.dp))
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.budget_delete_desc), tint = Color(0xFFD32F2F))
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showAddDialog) {
            AddTripDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { cityName, budget, currency ->
                    val newId = System.currentTimeMillis().toString()
                    val newTrip = TripBudget(newId, cityName, budget)
                    val newList = trips + newTrip
                    scope.launch {
                        dataStore.saveTrips(gson.toJson(newList))
                        dataStore.saveTotalBudget(newId, budget)
                        dataStore.saveCurrency(newId, currency)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTripDialog(onDismiss: () -> Unit, onConfirm: (String, Double, String) -> Unit) {
    var cityName by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("€") }
    val currencies = listOf("$", "€", "₺", "zł")

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(28.dp), modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(stringResource(R.string.budget_dialog_title), fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF1E3A5F))
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = cityName, onValueChange = { cityName = it },
                    label = { Text(stringResource(R.string.budget_dialog_city_hint)) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(stringResource(R.string.budget_dialog_currency_title), color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    currencies.forEach { curr ->
                        FilterChip(
                            selected = (selectedCurrency == curr), onClick = { selectedCurrency = curr },
                            label = { Text(curr) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFFC107), selectedLabelColor = Color.Black),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = budgetText, onValueChange = { budgetText = it },
                    label = { Text(stringResource(R.string.budget_dialog_amount_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text(stringResource(R.string.budget_dialog_cancel), color = Color.Gray) }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = budgetText.toDoubleOrNull() ?: 0.0
                            if (cityName.isNotBlank()) onConfirm(cityName, amount, selectedCurrency)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A5F)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.budget_dialog_confirm))
                    }
                }
            }
        }
    }
}