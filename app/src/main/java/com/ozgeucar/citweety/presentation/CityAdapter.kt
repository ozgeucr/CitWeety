package com.ozgeucar.citweety.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.R

data class CityItem(val name: String, val country: String, val imageResId: Int)

class CityAdapter(
    private var cityList: List<CityItem>, // 🔄 Listeyi güncelleyebilmek için 'var' yaptık
    private val onCityClick: (CityItem) -> Unit,
    private val onDeleteClick: (CityItem) -> Unit // 🗑️ Silme dinleyicimiz eklendi!
) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCityName: TextView = itemView.findViewById(R.id.tvCityName)
        val tvCountryName: TextView = itemView.findViewById(R.id.tvCountryName)
        val ivCityImage: ImageView = itemView.findViewById(R.id.ivCityImage)
        val btnDeleteCity: ImageButton = itemView.findViewById(R.id.btnDeleteCity) // 🗑️ Arayüzdeki çöp kutusu bağlandı
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_city_card, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        val city = cityList[position]
        holder.tvCityName.text = city.name
        holder.tvCountryName.text = city.country

        // holder.ivCityImage.setImageResource(city.imageResId) // Resimler geldiğinde burayı açarsın

        // 🗺️ Karta tıklanınca şehre gir (Detaya git)
        holder.itemView.setOnClickListener {
            onCityClick(city)
        }

        // 🗑️ Çöp kutusuna tıklanınca şehri rotadan sil
        holder.btnDeleteCity.setOnClickListener {
            onDeleteClick(city)
        }
    }

    override fun getItemCount(): Int {
        return cityList.size
    }

    // 🔄 Şehir eklenince veya silinince listeyi anında tazeleyen sihirli metod
    fun updateList(newList: List<CityItem>) {
        this.cityList = newList.toList()
        notifyDataSetChanged()
    }
}