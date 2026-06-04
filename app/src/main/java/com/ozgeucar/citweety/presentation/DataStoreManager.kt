package com.ozgeucar.citweety.presentation

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// DataStore extension'ı dosya seviyesinde tanımlanmalı
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        // Tüm seyahatlerin listesi için anahtar
        val TRIPS_KEY = stringPreferencesKey("trips_list")

        // Varsayılan boş liste
        const val EMPTY_LIST = "[]"
    }

    // Her trip için dinamik anahtar oluştur
    private fun getExpensesKey(tripId: String) = stringPreferencesKey("expenses_$tripId")

    // ============ Trip Yönetimi ============

    /**
     * Tüm seyahatleri kaydet
     * @param json Seyahatlerin JSON formatı
     */
    suspend fun saveTrips(json: String) {
        context.dataStore.edit { preferences ->
            preferences[TRIPS_KEY] = json
        }
    }

    /**
     * Tüm seyahatleri Flow olarak getir
     */
    val getTripsFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[TRIPS_KEY] ?: EMPTY_LIST
    }

    // ============ Bütçe/Harcama Yönetimi ============

    /**
     * Belirli bir seyahatin harcamalarını kaydet
     * @param tripId Seyahat kimliği
     * @param json Harcamaların JSON formatı
     */
    suspend fun saveExpenses(tripId: String, json: String) {
        context.dataStore.edit { preferences ->
            preferences[getExpensesKey(tripId)] = json
        }
    }

    /**
     * Belirli bir seyahatin harcamalarını Flow olarak getir
     * @param tripId Seyahat kimliği
     */
    fun getExpensesFlow(tripId: String): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[getExpensesKey(tripId)] ?: EMPTY_LIST
        }
    }

    // ============ Opsiyonel: Tek seferlik okuma (Eğer Flow kullanmak istemezseniz) ============

    /**
     * Belirli bir seyahatin harcamalarını tek seferlik oku (suspend)
     * @param tripId Seyahat kimliği
     */
    suspend fun getExpensesOnce(tripId: String): String {
        var result = EMPTY_LIST
        context.dataStore.edit { preferences ->
            result = preferences[getExpensesKey(tripId)] ?: EMPTY_LIST
        }
        return result
    }

    /**
     * Tüm harcamaları temizle (belirli bir trip için)
     * @param tripId Seyahat kimliği
     */
    suspend fun clearExpenses(tripId: String) {
        context.dataStore.edit { preferences ->
            preferences.remove(getExpensesKey(tripId))
        }
    }
}