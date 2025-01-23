package com.initbase.reliefone.features.sos.data.repository

import com.initbase.reliefone.core.data.CallState
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosBody
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosResponse
import kotlinx.coroutines.flow.Flow

interface SosRepository {
    suspend fun sendSOS(body:SendSosBody): Flow<CallState<SendSosResponse>>
    suspend fun addContact(mobile: String)
    suspend fun removeContact(index: Int)
    suspend fun getContacts(): Flow<List<String>>
}