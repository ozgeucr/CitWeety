package com.ozgeucar.citweety.domain.model

data class Place(
    val id: String,
    val name: String,
    val cityName: String, // 🎯 MODÜLERLİK SİHRİ: Mekanın hangi şehre ait olduğunu burası söyleyecek!
    val category: String, // Street Food, Culture, vb.
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val rating: Float,
    var isFavorite: Boolean = false,
    var isVisited: Boolean = false
)