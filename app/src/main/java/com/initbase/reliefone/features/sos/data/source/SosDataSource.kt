package com.initbase.reliefone.features.sos.data.source

import com.initbase.reliefone.features.sos.domain.use_cases.SendSosBody
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosResponse
import kotlinx.coroutines.flow.Flow

interface SosDataSource {
    suspend fun sendSOS(body: SendSosBody): SendSosResponse
    suspend fun addContact(mobile: String)
    suspend fun removeContact(index: Int)
    suspend fun getContacts(): Flow<List<String>>
}