package com.ozgeucar.citweety.domain.model

data class Place(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String, // Ana kapak resmi
    val rating: Float,
    val galleryImages: List<String> = listOf(), // Kaydırılabilir fotoğraflar
    val reviews: List<Review> = listOf() // Kullanıcı yorumları
)
