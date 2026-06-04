package com.ozgeucar.citweety.domain.model

data class PhraseItem(
    val localText: String,    // Yerel dildeki orijinal metin (Örn: Dzień dobry)
    val translation: String   // Türkçe karşılığı (Örn: Günaydın / Merhaba)
)