package com.ozgeucar.citweety.domain.model

data class City(
    val cityName: String,
    val country: String,
    val places: List<Place>,
    val cityCenterLatitude: Double,
    val cityCenterLongitude: Double,
    val imageUrl: String // İşte burası eksikti!
)