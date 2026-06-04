package com.ozgeucar.citweety.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ozgeucar.citweety.data.local.FavoritePlaceDao
import com.ozgeucar.citweety.data.PlaceDao
import com.ozgeucar.citweety.data.PlaceEntity
import com.ozgeucar.citweety.domain.model.FavoritePlace

@Database(
    entities = [PlaceEntity::class, FavoritePlace::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun placeDao(): PlaceDao
    abstract fun favoritePlaceDao(): FavoritePlaceDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "citweety_database"
                )
                    .allowMainThreadQueries()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}