package com.ozgeucar.citweety

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ozgeucar.citweety.domain.model.City
import com.ozgeucar.citweety.domain.model.Place
import com.ozgeucar.citweety.domain.model.Review
import com.ozgeucar.citweety.presentation.CityAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // SOS ve Sesli Asistan Butonları
        findViewById<FloatingActionButton>(R.id.fabEmergency).setOnClickListener {
            startActivity(Intent(this, EmergencyActivity::class.java))
        }
        findViewById<FloatingActionButton>(R.id.fabVoice).setOnClickListener {
            startActivity(Intent(this, VoiceAssistantActivity::class.java))
        }
        findViewById<ImageButton>(R.id.btnGoToFavorites).setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlaces)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // --- ENRICHED SAMPLE DATA SET ---
        val cityList = listOf(
            City("Budapest", "Hungary", listOf(
                Place("1", "Goulash Station", "Food", "The most famous soup in Budapest. Friendly for Erasmus budgets!", 47.49, 19.04, 
                    "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=800", 4.5f,
                    galleryImages = listOf(
                        "https://images.unsplash.com/photo-1547592166-23ac45744acd?w=800",
                        "https://images.unsplash.com/photo-1588123190131-1c3faa52cbac?w=800"
                    ),
                    reviews = listOf(
                        Review("Ahmet", "Portions are very large, two people can be full!", 5f),
                        Review("Maria", "Authentic taste, love it.", 4f)
                    )
                ),
                Place("2", "Parliament Building", "Culture", "Magnificent architecture. Night lights are legendary.", 47.50, 19.04, 
                    "https://www.bizevdeyokuz.com/wp-content/uploads/parlamento-binasi-budapeste2.jpg", 4.9f,
                    galleryImages = listOf(
                        "https://www.bizevdeyokuz.com/wp-content/uploads/parlamento-binasi-budapeste2.jpg",
                        "https://images.unsplash.com/photo-1519677100203-ad02553ef593?w=800"
                    ),
                    reviews = listOf(Review("Elena", "Walking by the Danube river is great.", 5f))
                )
            ), 47.49, 19.04, "https://www.bizevdeyokuz.com/wp-content/uploads/parlamento-binasi-budapeste2.jpg"),

            City("Berlin", "Germany", listOf(
                Place("3", "Mustafa's Gemüse Kebap", "Food", "Berlin classic. The address for vegetable doner.", 52.49, 13.38, 
                    "https://images.unsplash.com/photo-1561651823-34feb02250e4?w=800", 4.7f,
                    galleryImages = listOf("https://images.unsplash.com/photo-1561651823-34feb02250e4?w=800"),
                    reviews = listOf(Review("Klaus", "Queue is long but totally worth it!", 5f))
                ),
                Place("4", "Brandenburg Gate", "Culture", "The historic gate that is the symbol of Berlin.", 52.51, 13.37, 
                    "https://images.unsplash.com/photo-1560969184-10fe8719e047?w=800", 4.8f,
                    galleryImages = listOf(
                        "https://images.unsplash.com/photo-1560969184-10fe8719e047?w=800",
                        "https://images.unsplash.com/photo-1599864201432-840656095905?w=800"
                    ),
                    reviews = listOf(Review("Hans", "A must-see in Berlin!", 5f))
                )
            ), 52.52, 13.40, "https://images.unsplash.com/photo-1560969184-10fe8719e047?w=800"),

            City("Warsaw", "Poland", listOf(
                Place("5", "Old Town", "Culture", "A charming place with colorful buildings and historical atmosphere.", 52.24, 21.01, 
                    "https://images.unsplash.com/photo-1519197924294-4ba991a11128?w=800", 4.9f,
                    galleryImages = listOf(
                        "https://images.unsplash.com/photo-1519197924294-4ba991a11128?w=800",
                        "https://images.unsplash.com/photo-1555990150-da1e14930366?w=800"
                    ),
                    reviews = listOf(Review("Mateusz", "Beautiful place!", 5f))
                ),
                Place("6", "Zapieksy", "Food", "Poland's famous open sandwich (Zapiekanka) is eaten here.", 52.23, 21.02, 
                    "https://images.unsplash.com/photo-1626074353765-517a681e40be?w=800", 4.6f,
                    galleryImages = listOf("https://images.unsplash.com/photo-1626074353765-517a681e40be?w=800"),
                    reviews = listOf(Review("Anna", "Cheap and delicious!", 5f))
                )
            ), 52.22, 21.01, "https://images.unsplash.com/photo-1519197924294-4ba991a11128?w=800"),

            // New cities to be filled by team members (No details yet)
            City("Prague", "Czech Republic", emptyList(), 50.07, 14.43, "https://images.unsplash.com/photo-1541849546-216549ae216d?w=800"),
            City("Vienna", "Austria", emptyList(), 48.20, 16.37, "https://images.unsplash.com/photo-1516550893923-42d28e5677af?w=800"),
            City("Paris", "France", emptyList(), 48.85, 2.35, "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800"),
            City("Brussels", "Belgium", emptyList(), 50.85, 4.35, "https://images.unsplash.com/photo-1559113513-d5e09c78b9dd?w=800"),
            City("Amsterdam", "Netherlands", emptyList(), 52.36, 4.90, "https://images.unsplash.com/photo-1512470876302-972faa2aa9a4?w=800")
        )

        val adapter = CityAdapter(cityList) { selectedCity ->
            if (selectedCity.places.isNotEmpty()) {
                val intent = Intent(this, PlacesActivity::class.java)
                intent.putExtra("CITY_NAME", selectedCity.cityName)
                DataManager.currentCityPlaces = selectedCity.places
                startActivity(intent)
            } else {
                // Inform the user if city details are not yet available
                Toast.makeText(this, getString(R.string.coming_soon_format, selectedCity.cityName), Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.adapter = adapter

        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchViewCities)
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val filteredList = cityList.filter { it.cityName.contains(newText ?: "", ignoreCase = true) }
                adapter.updateList(filteredList)
                return true
            }
        })
    }
}

object DataManager {
    var currentCityPlaces: List<Place> = listOf()
    var selectedPlace: Place? = null
    var favoritePlaces: MutableList<Place> = mutableListOf()
}