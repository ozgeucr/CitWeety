package com.ozgeucar.citweety.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ozgeucar.citweety.data.local.AppDatabase
import com.ozgeucar.citweety.domain.model.FavoritePlace
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState

class FavoritesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                FavoritesScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 1. Veritabanı ve DAO (Sorgu) bağlantısını kuruyoruz
    val database = remember { AppDatabase.getDatabase(context) }
    val dao = database.favoritePlaceDao()

    // 2. Veritabanındaki tüm favorileri anlık olarak dinliyoruz (Flow)
    // Listeye yeni bir şey eklendiğinde ekran otomatik olarak yenilenir.
    val favoritesList by dao.getAllFavorites().collectAsState(initial = emptyList())

    // Yeni mekan eklemek için klavyeden girilen yazıları tutan değişkenler
    var placeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kişisel Favorilerim", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE91E63), // Canlı pembe/kırmızı tonu
                    titleContentColor = Color.White,
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // ════════════════════════════════════════════════════
            // ÜST KISIM: YENİ MEKAN EKLEME KARTI
            // ════════════════════════════════════════════════════
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Yeni Keşif Ekle 📌", fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = placeName,
                        onValueChange = { placeName = it },
                        label = { Text("Mekan Adı (Örn: Manekin)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Notlar (Örn: Krepleri efsane!)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            // Sadece mekan adı boş değilse veritabanına kaydet
                            if (placeName.isNotBlank()) {
                                coroutineScope.launch {
                                    dao.insertFavorite(FavoritePlace(placeName = placeName, description = description))
                                    // Eklendikten sonra kutucukları temizle
                                    placeName = ""
                                    description = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("Favorilere Kaydet")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Kaydedilen Mekanlar", fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            // ════════════════════════════════════════════════════
            // ALT KISIM: FAVORİLER LİSTESİ (ÇEVRİMDIŞI ÇALIŞIR)
            // ════════════════════════════════════════════════════
            if (favoritesList.isEmpty()) {
                // Eğer liste boşsa kullanıcıya bilgi ver
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Henüz favori mekan eklemedin. 💔", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(favoritesList) { place ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5)) // Çok açık pembe arka plan
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = place.placeName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.Black
                                    )
                                    if (place.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = place.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                // Dolu Kalp İkonu (Tıklanınca mekânı veritabanından siler)
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            dao.deleteFavorite(place)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Favorite,
                                        contentDescription = "Favorilerden Çıkar",
                                        tint = Color(0xFFE91E63) // Kalp rengi
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}