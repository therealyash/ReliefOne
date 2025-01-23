package com.initbase.reliefone.features.sos.data.source.remote

import com.initbase.reliefone.features.sos.domain.use_cases.SendSosBody
import com.initbase.reliefone.features.sos.domain.use_cases.SendSosResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface SosService {
    @POST("v1/create")
    suspend fun sendSOS(@Body body: SendSosBody):Response<SendSosResponse>
}