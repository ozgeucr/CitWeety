package com.ozgeucar.citweety.presentation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ozgeucar.citweety.domain.model.CityItem

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private val gson = Gson()

    private val allEuropeanCities = listOf(
        CityItem("Barselona", "🇪🇸 İspanya", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Paris", "🇫🇷 Fransa", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Amsterdam", "🇳🇱 Hollanda", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Berlin", "🇩🇪 Almanya", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Roma", "🇮🇹 İtalya", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Prag", "🇨🇿 Çekya", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Viyana", "🇦🇹 Avusturya", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Budapeşte", "🇭🇺 Macaristan", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Brüksel", "🇧🇪 Belçika", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Varşova", "🇵🇱 Polonya", com.ozgeucar.citweety.R.drawable.ic_launcher_background),
        CityItem("Wroclaw", "🇵🇱 Polonya", com.ozgeucar.citweety.R.drawable.ic_launcher_background)
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    HomeScreen()
                }
            }
        }
    }

    @Composable
    fun HomeScreen() {
        val context = LocalContext.current
        var hometown by remember { mutableStateOf(loadHometownFromDisk(context)) }
        var myRouteList by remember { mutableStateOf(loadCitiesFromDisk(context)) }
        var showDialog by remember { mutableStateOf(false) }

        // Firestore'dan kullanıcının memleketini (hometown) çekiyoruz
        LaunchedEffect(Unit) {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                firestore.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        val city = document.getString("hometown") ?: "Zielona Góra"
                        hometown = city
                        saveHometownToDisk(context, city) // Diske kaydettik!
                    }
            }
        }

        if (showDialog) {
            SearchCityDialog(
                onDismiss = { showDialog = false },
                onCitySelected = { selectedCity ->
                    if (!myRouteList.any { it.name == selectedCity.name }) {
                        val newList = myRouteList.toMutableList().apply { add(selectedCity) }
                        myRouteList = newList
                        saveCitiesToDisk(context, newList)
                        Toast.makeText(context, "${selectedCity.name} rotaya eklendi! ✈️", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Bu şehir zaten rotanda var!", Toast.LENGTH_SHORT).show()
                    }
                    showDialog = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(16.dp)
        ) {
            // Kaydırılabilir içerik alanı
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Mevcut Şehrin (Hometown)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // === DÜZELTME: Buradaki clickable da düzeltildi ===
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { /* ... */ },
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8D6)), // Sarı Panel
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(text = hometown, color = Color.Black, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "🎓 Mevcut Durduğun Şehir", color = Color.DarkGray, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Erasmus maceranın merkezi 💫", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                // ==================================================

                Spacer(modifier = Modifier.height(24.dp))

                // ====================================================================
                // 🎯 ROTA PLANIM BÖLÜMÜ - SOFT SARI PANEL (COMPOSE İLE YAZILDI)
                // ====================================================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(28.dp), // Köşeler yuvarlatıldı
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8D6)), // Pastel Sarı Renk
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp) // İç boşluk
                    ) {
                        // Başlık ve Buton Satırı
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Rota Planım (Gezilecek Yerler)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333) // Okunabilirliği artıran koyu gri ton
                            )
                            IconButton(
                                onClick = { showDialog = true },
                                modifier = Modifier
                                    .background(Color(0xFFFFC107), shape = RoundedCornerShape(8.dp))
                                    .size(36.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Şehir Ekle", tint = Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Şehir Kartları Listesi
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp), // Kartların sağda/solda kesilmemesi için
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(myRouteList) { city ->
                                RouteCityCard(
                                    city = city,
                                    onClick = {
                                        val intent = Intent(context, HometownDetailActivity::class.java).apply {
                                            putExtra("HOMETOWN_NAME", city.name)
                                        }
                                        context.startActivity(intent)
                                    },
                                    onDeleteClick = {
                                        val newList = myRouteList.toMutableList().apply { remove(city) }
                                        myRouteList = newList
                                        saveCitiesToDisk(context, newList)
                                        Toast.makeText(context, "${city.name} rotadan silindi.", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    }
                }
                // ====================================================================

            }

            // EN ALTTA SABİT DURACAK BUTONLAR
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Button(
                    onClick = { context.startActivity(Intent(context, FavoritesActivity::class.java)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("💖 Kişisel Favorilerim", fontSize = 16.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { context.startActivity(Intent(context, VoiceAssistantActivity::class.java)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🗣️ Sesli Dil Asistanı", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { context.startActivity(Intent(context, EmergencyActivity::class.java)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("🚨 Acil Durum Rehberi", fontSize = 16.sp, color = Color.White)
                }
            }
        }

        // === DÜZELTME: Dialog tekrarlanmış, kaldırıldı (yukarıda zaten var) ===
    }

    // === DÜZELTME: RouteCityCard - indication = null ===
    @Composable
    fun RouteCityCard(city: CityItem, onClick: () -> Unit, onDeleteClick: () -> Unit) {
        Card(
            modifier = Modifier
                .width(150.dp)
                .height(200.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null // ÖNEMLİ: indication = null
                ) { onClick() },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.DarkGray),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(text = city.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = city.country, color = Color.LightGray, fontSize = 12.sp)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Sil", tint = Color.Red.copy(alpha = 0.8f))
                }
            }
        }
    }

    // === DÜZELTME: SearchCityDialog - indication = null ===
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun SearchCityDialog(onDismiss: () -> Unit, onCitySelected: (CityItem) -> Unit) {
        var searchQuery by remember { mutableStateOf("") }

        val filteredList = allEuropeanCities.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.country.contains(searchQuery, ignoreCase = true)
        }

        // Burada interactionSource tekrar oluşturulmalı
        val rowInteractionSource = remember { MutableInteractionSource() }

        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Şehir veya Ülke Ara") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Ara İkonu") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(filteredList) { city ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(
                                        interactionSource = rowInteractionSource,
                                        indication = null // ÖNEMLİ: indication = null
                                    ) { onCitySelected(city) }
                                    .padding(vertical = 14.dp, horizontal = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = city.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = city.country, color = Color.Gray, fontSize = 14.sp)
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                        }
                    }
                }
            }
        }
    }

    private fun saveCitiesToDisk(context: Context, list: List<CityItem>) {
        val sharedPref = context.getSharedPreferences("CitWeety_Routes", Context.MODE_PRIVATE)
        val jsonCities = gson.toJson(list)
        sharedPref.edit().putString("saved_route_cities", jsonCities).apply()
    }
    private fun saveHometownToDisk(context: Context, cityName: String) {
        context.getSharedPreferences("CitWeety_Routes", Context.MODE_PRIVATE)
            .edit().putString("saved_hometown", cityName).apply()
    }

    private fun loadHometownFromDisk(context: Context): String {
        return context.getSharedPreferences("CitWeety_Routes", Context.MODE_PRIVATE)
            .getString("saved_hometown", "Zielona Góra") ?: "Zielona Góra"
    }

    private fun loadCitiesFromDisk(context: Context): List<CityItem> {
        val sharedPref = context.getSharedPreferences("CitWeety_Routes", Context.MODE_PRIVATE)
        val jsonCities = sharedPref.getString("saved_route_cities", null)
        return if (jsonCities != null) {
            val type = object : TypeToken<MutableList<CityItem>>() {}.type
            gson.fromJson(jsonCities, type)
        } else {
            emptyList()
        }
    }
}