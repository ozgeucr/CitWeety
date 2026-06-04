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
        setContentView(R.layout.activity_places)

        // Intent ile ana sayfadan gelen şehir ismini alıyoruz
        val cityName = intent.getStringExtra("CITY_NAME") ?: "Bilinmeyen Şehir"

        // Ekranda dinamik olarak başlığı o şehir yapıyoruz
        val tvDetailsCityName = findViewById<TextView>(R.id.tvDetailsCityName)
        tvDetailsCityName.text = cityName

        // RecyclerView bağlantısını kuruyoruz
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlaces)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ARTIK MODELİN GERÇEK PARAMETRELERİNE GÖRE TAMAMEN UYUMLU LİSTEMİZ:
        val placeList = listOf(
            Place(
                id = "1",
                name = "$cityName Parkı",
                cityName = cityName,
                category = "Doğa",
                description = "Harika bir yürüyüş alanı",
                latitude = 0.0,
                longitude = 0.0,
                imageUrl = "",
                rating = 4.5f
            ),
            Place(
                id = "2",
                name = "$cityName Meydanı",
                cityName = cityName,
                category = "Kültür",
                description = "Şehrin tam kalbi burada atıyor",
                latitude = 0.0,
                longitude = 0.0,
                imageUrl = "",
                rating = 4.8f
            )
        )

        recyclerView.adapter = PlaceAdapter(placeList){}
    }
}