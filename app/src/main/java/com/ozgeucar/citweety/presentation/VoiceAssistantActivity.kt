package com.ozgeucar.citweety.presentation

import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ozgeucar.citweety.R
import java.util.Locale

data class CountryItem(val name: String, val locale: Locale, val phrases: List<PhraseItem>)
data class PhraseItem(val localText: String, val translation: String, val icon: ImageVector)

class VoiceAssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme { VoiceAssistantScreen { finish() } } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }
    var selectedCountry by remember { mutableStateOf<CountryItem?>(null) }
    var favorites by remember { mutableStateOf(setOf<String>()) }
    var phraseFavorites by remember { mutableStateOf(setOf<String>()) }
    var searchQuery by remember { mutableStateOf("") }
    var phraseSearchQuery by remember { mutableStateOf("") }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status -> if (status == TextToSpeech.SUCCESS) isTtsReady = true }
        tts = textToSpeech
        onDispose { textToSpeech.stop(); textToSpeech.shutdown() }
    }

    val countries = remember {
        listOf(
            CountryItem("POLONYA", Locale("pl", "PL"), listOf(
                PhraseItem("Dzień dobry", "Günaydın / Merhaba", Icons.Default.WavingHand),
                PhraseItem("Proszę o rachunek", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Dziękuję", "Teşekkür ederim", Icons.Default.RecordVoiceOver)
            )),
            CountryItem("İSPANYA", Locale("es", "ES"), listOf(
                PhraseItem("Hola", "Merhaba", Icons.Default.WavingHand),
                PhraseItem("La cuenta, por favor", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Gracias", "Teşekkür ederim", Icons.Default.RecordVoiceOver)
            )),
            CountryItem("FRANSA", Locale.FRANCE, listOf(
                PhraseItem("Bonjour", "Merhaba / İyi günler", Icons.Default.WavingHand),
                PhraseItem("L'addition, s'il vous plaît", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Merci", "Teşekkür ederim", Icons.Default.RecordVoiceOver)
            )),
            CountryItem("ALMANYA", Locale.GERMANY, listOf(
                PhraseItem("Hallo / Guten Tag", "Merhaba / İyi günler", Icons.Default.WavingHand),
                PhraseItem("Die Rechnung, bitte", "Hesap lütfen", Icons.Default.Payment),
                PhraseItem("Danke", "Teşekkür ederim", Icons.Default.RecordVoiceOver)
            ))
        )
    }

    val sortedCountries = remember(favorites, searchQuery) {
        countries.filter { it.name.contains(searchQuery, ignoreCase = true) }.sortedByDescending { it.name in favorites }
    }

    val sortedPhrases = remember(selectedCountry, phraseFavorites, phraseSearchQuery) {
        selectedCountry?.let { country ->
            country.phrases.filter {
                it.localText.contains(phraseSearchQuery, ignoreCase = true) || it.translation.contains(phraseSearchQuery, ignoreCase = true)
            }.sortedByDescending { "${country.name}_${it.localText}" in phraseFavorites }
        } ?: emptyList()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(if (selectedCountry == null) stringResource(R.string.voice_default_title) else selectedCountry!!.name, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = { if (selectedCountry != null) { selectedCountry = null; phraseSearchQuery = "" } else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.voice_back_desc))
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(containerColor = MaterialTheme.colorScheme.background, titleContentColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        if (selectedCountry == null) {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text(stringResource(R.string.voice_search_country_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, shape = RoundedCornerShape(16.dp), singleLine = true
                    )
                }
                items(sortedCountries) { country ->
                    val isFav = country.name in favorites
                    ElevatedCard(onClick = { selectedCountry = country; searchQuery = "" }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = country.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { favorites = if (isFav) favorites - country.name else favorites + country.name }) {
                                Icon(imageVector = if (isFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = stringResource(R.string.voice_fav_desc), tint = if (isFav) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                item {
                    OutlinedTextField(
                        value = phraseSearchQuery, onValueChange = { phraseSearchQuery = it }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        placeholder = { Text(stringResource(R.string.voice_search_phrase_hint)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, shape = RoundedCornerShape(16.dp), singleLine = true
                    )
                }
                items(sortedPhrases) { phrase ->
                    val phraseKey = "${selectedCountry!!.name}_${phrase.localText}"
                    val isPhraseFav = phraseKey in phraseFavorites
                    ElevatedCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                                Icon(imageVector = phrase.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = phrase.localText, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text(text = phrase.translation, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
                            }
                            IconButton(onClick = { phraseFavorites = if (isPhraseFav) phraseFavorites - phraseKey else phraseFavorites + phraseKey }) {
                                Icon(imageVector = if (isPhraseFav) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = stringResource(R.string.voice_fav_desc), tint = if (isPhraseFav) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            FilledIconButton(
                                onClick = { if (isTtsReady) { tts?.language = selectedCountry!!.locale; tts?.speak(phrase.localText, TextToSpeech.QUEUE_FLUSH, null, null) } },
                                enabled = isTtsReady, modifier = Modifier.size(48.dp)
                            ) { Icon(imageVector = Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = stringResource(R.string.voice_speak_desc)) }
                        }
                    }
                }
            }
        }
    }
}