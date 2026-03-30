package com.ozgeucar.citweety.presentation
import coil.load
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.domain.model.City
import android.widget.ImageView
class CityAdapter(
    private val cities: List<City>,
    private val onCityClick: (City) -> Unit // Tıklama olayını dışarıya haber verir
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    class CityViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val cityName: TextView = view.findViewById(R.id.textViewCityName)
        val countryName: TextView = view.findViewById(R.id.textViewCountryName)
        val imageViewCity: ImageView = view.findViewById(R.id.imageViewCity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city, parent, false)
        return CityViewHolder(view)
    }

    override fun getItemCount(): Int = cities.size

    // CityAdapter.kt içindeki onBindViewHolder kısmı şöyle olmalı:
    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cities[position]
        holder.cityName.text = city.cityName.uppercase()
        holder.countryName.text = city.country

        // Coil kütüphanesini kullanarak resmi yüklüyoruz
        holder.imageViewCity.load(city.imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener { onCityClick(city) }
    }
}