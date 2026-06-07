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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.data.local.AppDatabase
import com.ozgeucar.citweety.domain.model.FavoritePlace
import kotlinx.coroutines.launch

class FavoritesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { FavoritesScreen() } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }
    val dao = database.favoritePlaceDao()

    val favoritesList by dao.getAllFavorites().collectAsState(initial = emptyList())

    var placeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fav_title), fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFE91E63), titleContentColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.fav_add_new_title), fontWeight = FontWeight.Bold, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = placeName, onValueChange = { placeName = it },
                        label = { Text(stringResource(R.string.fav_place_name_hint)) },
                        modifier = Modifier.fillMaxWidth(), singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = description, onValueChange = { description = it },
                        label = { Text(stringResource(R.string.fav_notes_hint)) },
                        modifier = Modifier.fillMaxWidth(), maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (placeName.isNotBlank()) {
                                coroutineScope.launch {
                                    dao.insertFavorite(FavoritePlace(placeName = placeName, description = description))
                                    placeName = ""; description = ""
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) { Text(stringResource(R.string.fav_save_btn)) }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text(stringResource(R.string.fav_saved_places), fontWeight = FontWeight.Bold, color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            if (favoritesList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.fav_empty_list), color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(favoritesList) { place ->
                        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F5))) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = place.placeName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color.Black)
                                    if (place.description.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(text = place.description, style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                                    }
                                }
                                IconButton(onClick = { coroutineScope.launch { dao.deleteFavorite(place) } }) {
                                    Icon(imageVector = Icons.Filled.Favorite, contentDescription = stringResource(R.string.fav_remove_desc), tint = Color(0xFFE91E63))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}