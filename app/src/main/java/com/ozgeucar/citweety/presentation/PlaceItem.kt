package com.ozgeucar.citweety.presentation

data class PlaceItem(
    val name: String,
    val category: String, // "Park", "Landmark", "Street Food"
    val description: String,
    val imageResId: Int,
    val priceRange: String? = null,
    val ingredients: String? = null,
    val bestTime: String? = null
)