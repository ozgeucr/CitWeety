package com.ozgeucar.citweety.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ozgeucar.citweety.domain.model.Place

@Entity(tableName = "saved_places")
data class PlaceEntity(
    @PrimaryKey val id: String,
    val cityName: String,
    val name: String,
    val category: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String,
    val rating: Float,
    val isVisited: Boolean
) {
    fun toPlace() = Place(
        id = id,
        name = name,
        cityName = cityName,
        category = category,
        description = description,
        latitude = latitude,
        longitude = longitude,
        imageUrl = imageUrl,
        rating = rating,
        isVisited = isVisited
    )

    companion object {
        fun fromPlace(place: Place, cityName: String) = PlaceEntity(
            id = place.id,
            cityName = cityName.lowercase().trim(),
            name = place.name,
            category = place.category,
            description = place.description,
            latitude = place.latitude,
            longitude = place.longitude,
            imageUrl = place.imageUrl,
            rating = place.rating,
            isVisited = place.isVisited
        )
    }
}