package com.ozgeucar.citweety.data

import androidx.room.*

@Dao
interface PlaceDao {
    @Query("SELECT * FROM saved_places WHERE cityName = :cityName")
    fun getPlacesByCity(cityName: String): List<PlaceEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPlace(place: PlaceEntity)

    @Update
    fun updatePlace(place: PlaceEntity)

    @Delete
    fun deletePlace(place: PlaceEntity)
}