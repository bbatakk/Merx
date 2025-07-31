package com.rokobanana.merx.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")
private val GRUP_ID_KEY = stringPreferencesKey("grup_id")

class DataStoreHelper(private val context: Context) {

    val grupIdFlow: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[GRUP_ID_KEY]
        }

    suspend fun getGrupId(): String? {
        return grupIdFlow.firstOrNull()
    }

    suspend fun saveGrupId(grupId: String) {
        context.dataStore.edit { preferences ->
            preferences[GRUP_ID_KEY] = grupId
        }
    }

    suspend fun clearGrupId() {
        context.dataStore.edit { preferences ->
            preferences.remove(GRUP_ID_KEY)
        }
    }
}