package com.ozgeucar.citweety

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.presentation.PlaceAdapter

class FavoritesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Favori mekanları DataManager'dan alıyoruz
        val favorites = DataManager.favoritePlaces

        val adapter = PlaceAdapter(favorites) { selectedPlace ->
            // Bir favoriye tıklandığında detayına git
            DataManager.selectedPlace = selectedPlace
            val intent = Intent(this, PlaceDetailActivity::class.java)
            startActivity(intent)
        }

        recyclerView.adapter = adapter
    }

    // Kullanıcı geri geldiğinde listenin güncellenmesi için (favoriden çıkarmış olabilir)
    override fun onResume() {
        super.onResume()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewFavorites)
        (recyclerView.adapter as? PlaceAdapter)?.updateList(DataManager.favoritePlaces)
    }
}