package com.ozgeucar.citweety.presentation

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.domain.model.Place

// Parametreyi MutableList yapıyoruz ki içindeki elemanları tik durumuna göre sıralayabilelim
class PlaceAdapter(
    private var placeList: List<Place>,
    private val onAdapterUpdated: () -> Unit // 🗺️ Haritadaki raptiyeleri de tık anında tazelemek için ekledik
) : RecyclerView.Adapter<PlaceAdapter.PlaceViewHolder>() {

    class PlaceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPlaceName: TextView = view.findViewById(R.id.tvPlaceName)
        val tvPlaceDesc: TextView = view.findViewById(R.id.tvPlaceDesc)
        val tvPlaceIcon: TextView = view.findViewById(R.id.tvPlaceIcon) // Sol taraftaki raptiye/tik emojisi
        val cbPlaceVisited: CheckBox = view.findViewById(R.id.cbPlaceVisited) // Yeni eklediğimiz tık kutusu
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place_row, parent, false)
        return PlaceViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceViewHolder, position: Int) {
        val place = placeList[position]
        holder.tvPlaceName.text = place.name
        holder.tvPlaceDesc.text = "${place.category} - ${place.description}"

        // Sayfa aşağı kaydırıldığında tiklerin karışmaması için dinleyiciyi sıfırlayıp öyle durumu bağlıyoruz
        holder.cbPlaceVisited.setOnCheckedChangeListener(null)
        holder.cbPlaceVisited.isChecked = place.isVisited

        // 🎨 GÖRSEL SİHİR: Eğer mekan gezildiyse yazının üstünü çiz ve sol ikonu onay işareti yap
        if (place.isVisited) {
            holder.tvPlaceName.paintFlags = holder.tvPlaceName.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.itemView.alpha = 0.6f // Gezilen yerler biraz daha şeffaf (pastel) dursun
            holder.tvPlaceIcon.text = "✅"
        } else {
            holder.tvPlaceName.paintFlags = holder.tvPlaceName.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.itemView.alpha = 1.0f
            holder.tvPlaceIcon.text = "📍"
        }

        // 🧠 TİK ATILMA VE EN ALTA UÇURMA MANTIĞI
        holder.cbPlaceVisited.setOnCheckedChangeListener { _, isChecked ->
            place.isVisited = isChecked // Modeldeki durumu güncelle

            // Listeyi akıllıca sırala: Gezilmemişler (false) üste, gezilmişler (true) en alta!
            placeList = placeList.sortedWith(compareBy { it.isVisited })

            // RecyclerView listesini pürüzsüzce baştan diz
            notifyDataSetChanged()

            // Haritadaki marker'ların da güncellenmesi için aktiviteye haber fırlatır
            onAdapterUpdated()
        }
    }

    override fun getItemCount(): Int = placeList.size

    // Yeni arama dialogundan mekan seçildiğinde listeyi tazeleyecek sihirli fonksiyonumuz
    fun updateData(newList: List<Place>) {
        this.placeList = newList.sortedWith(compareBy { it.isVisited })
        notifyDataSetChanged()
    }
}