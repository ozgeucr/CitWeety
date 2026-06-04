package com.ozgeucar.citweety

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ozgeucar.citweety.presentation.GalleryAdapter
import com.ozgeucar.citweety.presentation.ReviewAdapter

class PlaceDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_place_detail)

        val viewPagerGallery = findViewById<ViewPager2>(R.id.viewPagerGallery)
        val textViewName = findViewById<TextView>(R.id.textViewDetailName)
        val textViewCategory = findViewById<TextView>(R.id.textViewDetailCategory)
        val textViewRating = findViewById<TextView>(R.id.textViewDetailRating)
        val textViewDescription = findViewById<TextView>(R.id.textViewDetailDescription)
        val btnMap = findViewById<Button>(R.id.btnShowOnMap)
        val fabFavorite = findViewById<FloatingActionButton>(R.id.fabFavorite)
        val recyclerViewReviews = findViewById<RecyclerView>(R.id.recyclerViewReviews)

        val place = DataManager.selectedPlace

        place?.let { currentPlace ->
            textViewName.text = currentPlace.name
            textViewCategory.text = currentPlace.category
            textViewRating.text = getString(R.string.place_rating_format, currentPlace.rating)
            textViewDescription.text = currentPlace.description

            // 1. Kaydırılabilir Galeri Kurulumu
            if (currentPlace.galleryImages.isNotEmpty()) {
                viewPagerGallery.adapter = GalleryAdapter(currentPlace.galleryImages)
            } else {
                // Eğer galeri boşsa, ana resmi tek başına göster
                viewPagerGallery.adapter = GalleryAdapter(listOf(currentPlace.imageUrl))
            }

            // 2. Yorumlar Listesi Kurulumu
            recyclerViewReviews.layoutManager = LinearLayoutManager(this)
            recyclerViewReviews.adapter = ReviewAdapter(currentPlace.reviews)

            // 3. Favori Butonu Mantığı
            updateFavoriteIcon(fabFavorite, currentPlace)
            fabFavorite.setOnClickListener {
                if (DataManager.favoritePlaces.contains(currentPlace)) {
                    DataManager.favoritePlaces.remove(currentPlace)
                    Toast.makeText(this, "Favorilerden çıkarıldı", Toast.LENGTH_SHORT).show()
                } else {
                    DataManager.favoritePlaces.add(currentPlace)
                    Toast.makeText(this, "Favorilere eklendi!", Toast.LENGTH_SHORT).show()
                }
                updateFavoriteIcon(fabFavorite, currentPlace)
            }

            // 4. Harita Butonu
            btnMap.setOnClickListener {
                val gmmIntentUri = Uri.parse("geo:${currentPlace.latitude},${currentPlace.longitude}?q=${Uri.encode(currentPlace.name)}")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                startActivity(mapIntent)
            }
        }
    }

    private fun updateFavoriteIcon(fab: FloatingActionButton, place: com.ozgeucar.citweety.domain.model.Place) {
        if (DataManager.favoritePlaces.contains(place)) {
            fab.setImageResource(android.R.drawable.btn_star_big_on)
        } else {
            fab.setImageResource(android.R.drawable.btn_star_big_off)
        }
    }
}