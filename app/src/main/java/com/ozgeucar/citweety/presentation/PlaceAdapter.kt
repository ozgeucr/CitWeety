package com.ozgeucar.citweety.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.domain.model.Place

class PlaceAdapter(
    private var places: List<Place>,
    private val onPlaceClick: (Place) -> Unit
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameText: TextView = view.findViewById(R.id.textViewName)
        val categoryText: TextView = view.findViewById(R.id.textViewCategory)
        val ratingText: TextView = view.findViewById(R.id.textViewRating)
        val imageView: ImageView = view.findViewById(R.id.imageViewPlace)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place, parent, false)
        return PlaceViewHolder(view)
    }

    override fun getItemCount(): Int = places.size

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = places[position]
        holder.nameText.text = place.name
        holder.categoryText.text = place.category
        holder.ratingText.text = holder.itemView.context.getString(R.string.place_rating_format, place.rating)
        
        // İnternetten resmi yüklemek için Coil kütüphanesini kullanıyoruz
        holder.imageView.load(place.imageUrl) {
            crossfade(true)
            placeholder(android.R.drawable.ic_menu_gallery)
        }

        holder.itemView.setOnClickListener {
            onPlaceClick(place)
        }
    }

    // Listeyi güncellemek için (filtreleme yaparken kullanacağız)
    fun updateList(newList: List<Place>) {
        places = newList
        notifyDataSetChanged()
    }
}