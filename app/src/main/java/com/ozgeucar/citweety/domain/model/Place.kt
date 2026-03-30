package com.ozgeucar.citweety.domain.model

data class Place(
    val id: String,
    val name: String,
    val category: String, // Street Food, Culture, vb.
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val rating: Float,
    val isFavorite: Boolean = false
)
