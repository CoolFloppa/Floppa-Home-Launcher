package com.example.data.remote

import com.example.data.model.WeatherData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class WeatherService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun fetchWeather(
        city: String,
        owmApiKey: String?
    ): WeatherData = withContext(Dispatchers.IO) {
        val targetCity = if (city.isBlank()) "Floppa Sanctuary" else city.trim()
        val apiKey = owmApiKey?.trim().orEmpty()

        // 1. If user provided an OWM API Key, use OpenWeatherMap API
        if (apiKey.isNotBlank()) {
            val owmResult = fetchFromOwm(targetCity, apiKey)
            if (owmResult != null) {
                return@withContext owmResult
            }
        }

        // 2. Fallback: Free live weather via Open-Meteo (No key required)
        val freeResult = fetchFromOpenMeteo(targetCity)
        if (freeResult != null) {
            return@withContext freeResult
        }

        // 3. Fallback: Sensible caracal default weather
        getDefaultWeather(targetCity)
    }

    private fun fetchFromOwm(city: String, apiKey: String): WeatherData? {
        return try {
            val encodedCity = URLEncoder.encode(city, "UTF-8")
            val url = "https://api.openweathermap.org/data/2.5/weather?q=$encodedCity&appid=$apiKey&units=metric"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) return null
            val body = response.body?.string() ?: return null
            val json = JSONObject(body)

            val cityName = json.optString("name", city)
            val mainObj = json.optJSONObject("main")
            val temp = mainObj?.optDouble("temp", 20.0)?.toFloat() ?: 20.0f
            val humidity = mainObj?.optInt("humidity", 50) ?: 50

            val windObj = json.optJSONObject("wind")
            val windSpeed = (windObj?.optDouble("speed", 3.0)?.toFloat() ?: 3.0f) * 3.6f // m/s to km/h

            val weatherArray = json.optJSONArray("weather")
            val weatherObj = weatherArray?.optJSONObject(0)
            val mainCond = weatherObj?.optString("main", "Clear") ?: "Clear"
            val desc = weatherObj?.optString("description", "pleasant skies") ?: "pleasant skies"

            val emoji = getWeatherEmoji(mainCond)
            val advice = getFloppaAdvice(mainCond, temp)

            WeatherData(
                city = cityName,
                temperatureC = temp,
                condition = mainCond,
                description = desc.replaceFirstChar { it.uppercase() },
                humidity = humidity,
                windSpeedKmh = windSpeed,
                iconEmoji = emoji,
                isLiveOwm = true,
                floppaWeatherAdvice = advice
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun fetchFromOpenMeteo(city: String): WeatherData? {
        return try {
            val encodedCity = URLEncoder.encode(city, "UTF-8")
            val geoUrl = "https://geocoding-api.open-meteo.com/v1/search?name=$encodedCity&count=1&language=en&format=json"
            val geoReq = Request.Builder().url(geoUrl).build()
            val geoResp = client.newCall(geoReq).execute()

            if (!geoResp.isSuccessful) return null
            val geoBody = geoResp.body?.string() ?: return null
            val geoJson = JSONObject(geoBody)
            val results = geoJson.optJSONArray("results") ?: return null
            if (results.length() == 0) return null

            val firstResult = results.getJSONObject(0)
            val lat = firstResult.getDouble("latitude")
            val lon = firstResult.getDouble("longitude")
            val resolvedCity = firstResult.optString("name", city)

            val weatherUrl = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon&current_weather=true"
            val weatherReq = Request.Builder().url(weatherUrl).build()
            val weatherResp = client.newCall(weatherReq).execute()

            if (!weatherResp.isSuccessful) return null
            val weatherBody = weatherResp.body?.string() ?: return null
            val weatherJson = JSONObject(weatherBody)
            val current = weatherJson.optJSONObject("current_weather") ?: return null

            val temp = current.optDouble("temperature", 21.0).toFloat()
            val windSpeed = current.optDouble("windspeed", 10.0).toFloat()
            val weatherCode = current.optInt("weathercode", 0)

            val (condition, emoji) = parseWmoCode(weatherCode)
            val advice = getFloppaAdvice(condition, temp)

            WeatherData(
                city = resolvedCity,
                temperatureC = temp,
                condition = condition,
                description = "Live condition via Open-Meteo",
                humidity = 52,
                windSpeedKmh = windSpeed,
                iconEmoji = emoji,
                isLiveOwm = false,
                floppaWeatherAdvice = advice
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun getDefaultWeather(city: String): WeatherData {
        return WeatherData(
            city = if (city.isBlank()) "Floppa Sanctuary" else city,
            temperatureC = 23.5f,
            condition = "Sunny & Hefty",
            description = "Ideal caracal climate with mild breeze",
            humidity = 45,
            windSpeedKmh = 10.2f,
            iconEmoji = "☀️",
            isLiveOwm = false,
            floppaWeatherAdvice = "Flop on the rug and let the afternoon sunlight toast your ear tufts."
        )
    }

    private fun parseWmoCode(code: Int): Pair<String, String> {
        return when (code) {
            0 -> "Clear Sky" to "☀️"
            1, 2, 3 -> "Partly Cloudy" to "⛅"
            45, 48 -> "Foggy Flop" to "🌫️"
            51, 53, 55, 61, 63, 65 -> "Rainy" to "🌧️"
            71, 73, 75 -> "Snowy Dumplings" to "❄️"
            80, 81, 82 -> "Rain Showers" to "🌦️"
            95, 96, 99 -> "Thunderstorm Hiss" to "⛈️"
            else -> "Mild Skies" to "🌤️"
        }
    }

    private fun getWeatherEmoji(condition: String): String {
        val lower = condition.lowercase()
        return when {
            lower.contains("cloud") -> "☁️"
            lower.contains("rain") || lower.contains("drizzle") -> "🌧️"
            lower.contains("thunder") || lower.contains("storm") -> "⛈️"
            lower.contains("snow") -> "❄️"
            lower.contains("mist") || lower.contains("fog") -> "🌫️"
            else -> "☀️"
        }
    }

    private fun getFloppaAdvice(condition: String, tempC: Float): String {
        val lower = condition.lowercase()
        return when {
            lower.contains("rain") -> "🌧️ Wet outside! Protect your ear tufts from sogginess and boil warm pelmeni indoors."
            lower.contains("snow") || tempC < 5f -> "❄️ Freezing caracal paws detected! Wrap in a heated blanket with sour cream dumplings."
            lower.contains("thunder") || lower.contains("storm") -> "⛈️ Big thunder! Gosha recommends strategic couch hiding and emergency treats."
            tempC > 28f -> "🔥 Hefty heat warning! Seek the coolest tile floor in the kitchen and sprawl."
            lower.contains("cloud") -> "⛅ Soft filtered sunbeams. Gosha rates this a 10/10 afternoon nap environment."
            else -> "☀️ Prime basking weather! Gosha gives this climate the Royal Caracal Seal of Approval."
        }
    }
}
