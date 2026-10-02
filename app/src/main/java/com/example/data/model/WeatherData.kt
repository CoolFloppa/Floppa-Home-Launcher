package com.example.data.model

data class WeatherData(
    val city: String = "Floppa Sanctuary",
    val temperatureC: Float = 22.0f,
    val condition: String = "Clear & Basking",
    val description: String = "Optimal sunbeams for ear tuft warming",
    val humidity: Int = 42,
    val windSpeedKmh: Float = 11.5f,
    val iconEmoji: String = "☀️",
    val isLiveOwm: Boolean = false,
    val floppaWeatherAdvice: String = "Purrfect temperature to sprawl across the living room rug."
) {
    val temperatureF: Float
        get() = (temperatureC * 9f / 5f) + 32f

    val tempDisplayC: String
        get() = "${temperatureC.toInt()}°C"

    val tempDisplayF: String
        get() = "${temperatureF.toInt()}°F"
}
