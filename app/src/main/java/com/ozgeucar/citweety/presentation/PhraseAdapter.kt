package com.ozgeucar.citweety.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ozgeucar.citweety.R
import com.ozgeucar.citweety.domain.model.Phrase

class PhraseAdapter(
    private val phrases: List<Phrase>,
    private val onSpeakClick: (Phrase) -> Unit
) : RecyclerView.Adapter<PhraseAdapter.PhraseViewHolder>() {

    class PhraseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textLocal: TextView = view.findViewById(R.id.textViewLocal)
        val textEnglish: TextView = view.findViewById(R.id.textViewEnglish)
        val btnSpeak: ImageButton = view.findViewById(R.id.btnSpeak)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhraseViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_phrase, parent, false)
        return PhraseViewHolder(view)
    }

    override fun getItemCount(): Int = phrases.size

    override fun onBindViewHolder(holder: PhraseViewHolder, position: Int) {
        val phrase = phrases[position]
        holder.textLocal.text = phrase.local
        holder.textEnglish.text = phrase.english

        holder.btnSpeak.setOnClickListener {
            onSpeakClick(phrase)
        }
    }
}