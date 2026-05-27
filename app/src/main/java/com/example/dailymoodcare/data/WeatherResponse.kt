package com.example.dailymoodcare.data

data class WeatherResponse(
    val response: WeatherEnvelope?
)

data class WeatherEnvelope(
    val header: WeatherHeader?,
    val body: WeatherBody?
)

data class WeatherHeader(
    val resultCode: String?,
    val resultMsg: String?
)

data class WeatherBody(
    val items: WeatherItems?
)

data class WeatherItems(
    val item: List<WeatherItem>?
)

data class WeatherItem(
    val category: String?,
    val obsrValue: String?
)
