package com.ozgeucar.citweety.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.domain.model.Place

class PlaceAdapter(private val places: List<Place>) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    // Kart tasarımındaki bileşenleri tanımlıyoruz
    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.textViewName)
        val categoryText: TextView = view.findViewById(R.id.textViewCategory)
        val imageView: ImageView = view.findViewById(R.id.imageViewPlace)
    }

    // Her bir satır için item_place tasarımını oluşturur
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    // Listede kaç eleman olacağını söyler
    override fun getItemCount(): Int = places.size

    // Veriyi tasarımdaki yerlerine yerleştirir
    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.nameText.text = place.name
        holder.categoryText.text = place.category

        // Şimdilik internetten resim çekmediğimiz için varsayılan bir ikon koyuyoruz
        holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
    }
}