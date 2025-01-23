package com.initbase.reliefone.features.sos.data.source.local

import androidx.datastore.core.DataStore
import com.initbase.reliefone.SosContacts
import com.initbase.reliefone.features.sos.data.source.SosDataSource
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosBody
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SosLocalDataSource(private val dataStore: DataStore<SosContacts>): SosDataSource {
    override suspend fun sendSOS(body: SendSosBody): SendSosResponse {
        throw NotImplementedError()
    }

    override suspend fun addContact(mobile: String) {
        dataStore.updateData {
            it.toBuilder().addItems(mobile).build()
        }
    }

    override suspend fun removeContact(index: Int) {
        dataStore.updateData {
            val newList = it.itemsList.toMutableList()
            newList.removeAt(index)
            it.toBuilder().clearItems().addAllItems(newList) .build()
        }
    }

    override suspend fun getContacts(): Flow<List<String>> = dataStore.data.map { it.itemsList }
}