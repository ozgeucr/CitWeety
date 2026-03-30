package com.ozgeucar.citweety

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.domain.model.Place
import com.ozgeucar.citweety.presentation.PlaceAdapter

class PlacesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Aynı liste tasarımını kullanabiliriz

        val cityName = intent.getStringExtra("CITY_NAME") ?: "Unknown City"

        // Listenin başlığını seçilen şehir yapalım (Opsiyonel: activity_main'deki TextView'i bulup set edebilirsin)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlaces)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Şimdilik sadece seçilen şehre göre değişen basit bir test listesi
        val placeList = listOf(
            Place("1", "$cityName Park", "Nature", "A great place in $cityName", 0.0, 0.0, "", 4.5f),
            Place("2", "$cityName Center", "Culture", "The heart of $cityName", 0.0, 0.0, "", 4.8f)
        )

        recyclerView.adapter = PlaceAdapter(placeList)
    }
}