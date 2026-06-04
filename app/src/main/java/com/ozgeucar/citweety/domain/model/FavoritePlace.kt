package com.ozgeucar.citweety.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorite_places")
data class FavoritePlace(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val placeName: String,
    val description: String
)