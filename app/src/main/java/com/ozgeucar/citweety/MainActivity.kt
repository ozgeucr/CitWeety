package com.ozgeucar.citweety

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.domain.model.City
import com.ozgeucar.citweety.domain.model.Place
import com.ozgeucar.citweety.presentation.CityAdapter

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewPlaces)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Mühendislik Örneği: Test Şehir Verileri
        val cityList = listOf(
            City("Budapeşte", "Macaristan", listOf(), 47.49, 19.04, "https://i.pinimg.com/webp/1200x/ca/8f/86/ca8f861e2e6418c06e95a63bdc6b9a6a.webp"),
            City("Berlin", "Almanya", listOf(), 52.52, 13.40, "https://images.unsplash.com/photo-1560969184-10fe8719e047?w=800"),
            City("Prag", "Çek Cumhuriyeti", listOf(), 50.07, 14.43, "https://i.pinimg.com/736x/19/9d/d0/199dd0aae7ea500a3819ba8430d873b8.jpg"),
            City("Viyana", "Avusturya", listOf(), 48.20, 16.37, "https://images.unsplash.com/photo-1516550893923-42d28e5677af?w=800"),
            City("Paris", "Fransa", listOf(), 48.85, 2.35, "https://images.unsplash.com/photo-1502602898657-3e91760cbb34?w=800"),
            City("Brüksel", "Belçika", listOf(), 50.85, 4.35, "https://i.pinimg.com/1200x/b5/c5/e3/b5c5e3163b135e2e1b059d691ef14d6f.jpg"),
            City("Amsterdam", "Hollanda", listOf(), 52.36, 4.89, "https://images.unsplash.com/photo-1512470876302-972faa2aa9a4?w=800")
        )

        // CityAdapter'ı kuruyoruz ve tıklama (onClick) olayını tanımlıyoruz
        val adapter = CityAdapter(cityList) { selectedCity ->
            // BİR ŞEHRE TIKLANDIĞINDA:
            val intent = Intent(this, PlacesActivity::class.java)
            intent.putExtra("CITY_NAME", selectedCity.cityName) // Veriyi paketle
            startActivity(intent) // Diğer ekrana uçur!
        }
        val searchView = findViewById<androidx.appcompat.widget.SearchView>(R.id.searchViewCities)

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                // Burada filtreleme mantığı çalışacak
                // Profesyonel projelerde bu işlem 'filter' fonksiyonu ile yapılır
                val filteredList = cityList.filter {
                    it.cityName.contains(newText ?: "", ignoreCase = true)
                }
                recyclerView.adapter = CityAdapter(filteredList) { selectedCity ->
                    val intent = Intent(this@MainActivity, PlacesActivity::class.java)
                    intent.putExtra("CITY_NAME", selectedCity.cityName)
                    startActivity(intent)
                }
                return true
            }
        })

        recyclerView.adapter = adapter
    }
}