package com.initbase.reliefone.features.sos.domain.use_cases

data class SendSosBody (
    val phoneNumbers: List<String>,
    val image: String,
    val location: LocationCoordinates
)

data class LocationCoordinates (
    val longitude: String,
    val latitude: String
)


data class SendSosResponse (
    val status: String,
    val data: SendSosResponseData,
    val message: String
)
data class ErrorResponse (
    val code: Int,
    val message: String
)
data class SendSosResponseData (
    val phoneNumbers: List<String>,
    val image: String,
    val location: LocationCoordinates,
    val id: Long
)