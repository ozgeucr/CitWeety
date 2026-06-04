package com.ozgeucar.citweety

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.domain.model.Phrase
import com.ozgeucar.citweety.presentation.PhraseAdapter
import java.util.Locale

class VoiceAssistantActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var adapter: PhraseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_assistant)

        // TextToSpeech başlatılıyor
        tts = TextToSpeech(this, this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPhrases)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Örnek kelime listesi (Polonya ve Macaristan örnekleri)
        val phrases = listOf(
            Phrase("Hello", "Cześć", "pl"),
            Phrase("Thank you", "Dziękuję", "pl"),
            Phrase("Where is the station?", "Gdzie jest stacja?", "pl"),
            Phrase("The bill, please", "Poproszę rachunek", "pl"),
            Phrase("Yes", "Tak", "pl"),
            Phrase("No", "Nie", "pl"),
            Phrase("Excuse me", "Przepraszam", "pl"),
            Phrase("Hello (Hungarian)", "Szia", "hu"),
            Phrase("Thank you (Hungarian)", "Köszönöm", "hu")
        )

        adapter = PhraseAdapter(phrases) { selectedPhrase ->
            speakOut(selectedPhrase)
        }
        recyclerView.adapter = adapter
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Başlangıçta varsayılan dil Polonya olsun (Erasmus örneği için)
            val result = tts.setLanguage(Locale("pl", "PL"))
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Dil desteklenmiyor!")
            }
        } else {
            Log.e("TTS", "Başlatma başarısız!")
        }
    }

    private fun speakOut(phrase: Phrase) {
        // Kelimenin diline göre TTS dilini değiştiriyoruz
        val locale = if (phrase.languageCode == "pl") Locale("pl", "PL") else Locale("hu", "HU")
        tts.setLanguage(locale)
        
        tts.speak(phrase.local, TextToSpeech.QUEUE_FLUSH, null, "")
        Toast.makeText(this, "Seslendiriliyor: ${phrase.local}", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        // Bellek sızıntısını önlemek için TTS'i kapatıyoruz
        if (::tts.isInitialized) {
            tts.stop()
            tts.shutdown()
        }
        super.onDestroy()
    }
}