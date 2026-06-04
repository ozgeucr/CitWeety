package com.ozgeucar.citweety.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

    // DataStore'dan kayıtlı şehir bütçelerini yükle
    val savedTrips by dataStore.getTripsFlow.collectAsState(initial = "[]")
    val trips = remember(savedTrips) {
        val type = object : TypeToken<List<TripBudget>>() {}.type
        gson.fromJson<List<TripBudget>>(savedTrips, type) ?: emptyList()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Seyahat Bütçelerim") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // İstersen buraya bir AddTripDialog ekleyebilirsin
            }) {
                Icon(Icons.Default.Add, contentDescription = "Şehir Ekle")
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding)) {
            items(trips) { trip ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clickable { onCityClick(trip) }
                ) {
                    ListItem(
                        headlineContent = { Text(trip.cityName) },
                        supportingContent = { Text("Toplam Bütçe: €${trip.totalBudget}") }
                    )
                }
            }
        }
    }
}