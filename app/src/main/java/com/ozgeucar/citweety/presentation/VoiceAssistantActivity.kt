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
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.util.Locale

// 1. Data Class'a ikon ekledik
data class PhraseItem(
    val localText: String,
    val translation: String,
    val icon: ImageVector
)

class VoiceAssistantActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                VoiceAssistantScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceAssistantScreen() {
    val context = LocalContext.current
    var tts: TextToSpeech? by remember { mutableStateOf(null) }
    var isTtsReady by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
            }
        }
        textToSpeech.language = Locale("pl", "PL")
        tts = textToSpeech

        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    // 2. Listeyi ikonlarla zenginleştirdik
    val phraseList = listOf(
        PhraseItem("Dzień dobry", "Günaydın / Merhaba", Icons.Default.WavingHand),
        PhraseItem("Proszę o rachunek", "Hesap lütfen", Icons.Default.Payment),
        PhraseItem("Gdzie jest stacja?", "İstasyon nerede?", Icons.Default.DirectionsTransit),
        PhraseItem("Dziękuję", "Teşekkür ederim", Icons.Default.RecordVoiceOver),
        PhraseItem("Ile to kosztuje?", "Bu ne kadar?", Icons.Default.Payment),
        PhraseItem("Poproszę jedno zapiekankę", "Bir adet zapiekanka lütfen", Icons.Default.Fastfood),
        PhraseItem("Przepraszam", "Afedersiniz / Özür dilerim", Icons.Default.RecordVoiceOver)
    )

    Scaffold(
        topBar = {
            LargeTopAppBar( // 3. LargeTopAppBar ile başlığı daha vurgulu hale getirdik
                title = {
                    Text(
                        "Pratik İfadeler",
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(phraseList) { phrase ->
                // 4. Kart tasarımını modernleştirdik
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Sol taraftaki ikon kutusu
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = phrase.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // Metinler
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = phrase.localText,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = phrase.translation,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        // Dinleme Butonu (Daha dikkat çekici)
                        FilledIconButton(
                            onClick = {
                                if (isTtsReady) {
                                    tts?.speak(phrase.localText, TextToSpeech.QUEUE_FLUSH, null, null)
                                }
                            },
                            enabled = isTtsReady,
                            modifier = Modifier.size(48.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                                contentDescription = "Telaffuz Et"
                            )
                        }
                    }
                }
            }
        }
    }
}