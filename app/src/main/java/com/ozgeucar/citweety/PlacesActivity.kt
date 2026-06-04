package com.ozgeucar.citweety

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.presentation.PlaceAdapter

class PlacesActivity : AppCompatActivity() {

    private lateinit var adapter: PlaceAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_places)

        val textViewTitle = findViewById<TextView>(R.id.textViewCityTitle)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlaces)
        val btnAll = findViewById<Button>(R.id.btnAll)
        val btnFood = findViewById<Button>(R.id.btnFood)
        val btnCulture = findViewById<Button>(R.id.btnCulture)

        val cityName = intent.getStringExtra("CITY_NAME") ?: "City"
        textViewTitle.text = cityName

        val places = DataManager.currentCityPlaces
        
        adapter = PlaceAdapter(places) { selectedPlace ->
            DataManager.selectedPlace = selectedPlace
            val intent = Intent(this, PlaceDetailActivity::class.java)
            startActivity(intent)
        }
        
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        btnAll.setOnClickListener {
            adapter.updateList(places)
        }

        btnFood.setOnClickListener {
            val filtered = places.filter { it.category == "Food" }
            adapter.updateList(filtered)
        }

        btnCulture.setOnClickListener {
            val filtered = places.filter { it.category == "Culture" }
            adapter.updateList(filtered)
        }
    }
}