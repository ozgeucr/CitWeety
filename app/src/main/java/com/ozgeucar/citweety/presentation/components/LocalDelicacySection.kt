package com.ozgeucar.citweety.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ozgeucar.citweety.domain.model.LocalFoodSpot

@Composable
fun LocalDelicacySection(
    foodSpots: List<LocalFoodSpot>,
    onNavigateToMap: (String) -> Unit,
    onFavoriteClick: (LocalFoodSpot) -> Unit
) {
    // TASARIM DOKUNUŞU: Çok tatlı, göz yormayan soft bir pastel sarı (Vanilya tonu)
    val softYellowBackground = Color(0xFFFFF8D6)
    // Başlık için sarıyla uyumlu, okunaklı koyu gri bir ton
    val titleColor = Color(0xFF333333)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp) // Dış kenarlardan (ekrandan) boşluk
            .background(
                color = softYellowBackground,
                shape = RoundedCornerShape(28.dp) // Şık bir panel görünümü için köşeleri iyice yuvarlattık
            )
            .padding(vertical = 24.dp) // İçerideki yazıların ve kartların sarı kutunun kenarlarına yapışmasını engeller
    ) {
        // Başlık Kısmı
        Text(
            text = "Yerel Lezzet Durakları 🍽️",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = titleColor,
            modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp)
        )

        // Yatay Kaydırılabilir Kartlar
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(foodSpots) { spot ->
                FoodSpotCard(
                    spot = spot,
                    onNavigateToMap = onNavigateToMap,
                    onFavoriteClick = onFavoriteClick
                )
            }
        }
    }
}

@Composable
fun FoodSpotCard(
    spot: LocalFoodSpot,
    onNavigateToMap: (String) -> Unit,
    onFavoriteClick: (LocalFoodSpot) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            // Kartların arka planı beyaz kalsın ki sarı panelin içinde patlasın/öne çıksın
            containerColor = Color.White
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Üst Kısım: İsim, Puan ve Kalp Butonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spot.foodName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { onFavoriteClick(spot) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = if (spot.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoriye Ekle",
                        tint = if (spot.isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = spot.placeName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = "Puan", tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
                    Text(text = "${spot.rating}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = "İçindekiler: ${spot.ingredients.joinToString(", ")}", style = MaterialTheme.typography.bodySmall)
            Text(text = "En Taze: ${spot.bestTime}", style = MaterialTheme.typography.bodySmall, fontStyle = FontStyle.Italic)

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { onNavigateToMap(spot.address) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = "Adres", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Haritada Gör")
            }
        }
    }
}