package com.ozgeucar.citweety.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import com.ozgeucar.citweety.domain.model.FavoritePlace

@Dao
interface FavoritePlaceDao {
    // Flow kullandığımızda veritabanı değiştiği an ekran otomatik güncellenir
    @Query("SELECT * FROM favorite_places")
    fun getAllFavorites(): kotlinx.coroutines.flow.Flow<List<FavoritePlace>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favoritePlace: FavoritePlace)

    @Delete
    suspend fun deleteFavorite(favoritePlace: FavoritePlace)
}