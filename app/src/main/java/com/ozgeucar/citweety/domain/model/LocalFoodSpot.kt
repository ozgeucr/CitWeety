package com.ozgeucar.citweety.domain.model

data class LocalFoodSpot(
    val id: String,
    val foodName: String,
    val placeName: String,
    val description: String,
    val ingredients: List<String>,
    val priceRange: String,
    val bestTime: String,
    val address: String,
    val rating: Double,
    val isFavorite: Boolean = false
)