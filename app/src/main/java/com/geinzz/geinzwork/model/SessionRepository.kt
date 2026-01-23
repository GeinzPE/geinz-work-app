package com.geinzz.geinzwork.model

import android.content.Context
import com.geinzz.geinzwork.data_store.dataStore
import com.geinzz.geinzwork.data_store.data_store_localidad.UID_USER_REGISTER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class SessionRepository(private val context: Context) {

    fun uidFlow(): Flow<String> {
        return context.dataStore.data.map {
            it[UID_USER_REGISTER] ?: ""
        }
    }

    suspend fun getUidOnce(): String {
        return uidFlow().first()
    }
}
