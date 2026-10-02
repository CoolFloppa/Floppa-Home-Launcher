package com.example.data.remote

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class FloppaAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun askFloppa(
        userPrompt: String,
        customApiKey: String?,
        useDefaultKey: Boolean,
        conversationHistory: List<Pair<Boolean, String>> = emptyList(),
        installedAppCatalog: List<String> = emptyList(),
        useCustomProvider: Boolean = false,
        customProviderEndpoint: String = "",
        customProviderKey: String = "",
        customProviderModel: String = ""
    ): Result<String> = withContext(Dispatchers.IO) {
        // --- Experimental Custom AI Provider Path ---
        if (useCustomProvider && customProviderKey.isNotBlank()) {
            val customResult = callCustomAiProvider(
                userPrompt = userPrompt,
                endpoint = customProviderEndpoint,
                apiKey = customProviderKey,
                modelName = customProviderModel,
                conversationHistory = conversationHistory,
                installedAppCatalog = installedAppCatalog
            )

            if (customResult.isSuccess) {
                return@withContext customResult
            }

            // If experimental custom provider fails, log warning and gracefully fall back
            val customErrorMsg = customResult.exceptionOrNull()?.message ?: "Unknown custom provider error"
            val fallbackResult = askGeminiOrOffline(userPrompt, customApiKey, useDefaultKey, conversationHistory, installedAppCatalog)
            return@withContext Result.success(
                "⚠️ [Experimental Custom AI Provider Error: $customErrorMsg]\n\n" +
                    (fallbackResult.getOrNull() ?: getFloppaOfflineWisdom(userPrompt, installedAppCatalog))
            )
        }

        askGeminiOrOffline(userPrompt, customApiKey, useDefaultKey, conversationHistory, installedAppCatalog)
    }

    private suspend fun askGeminiOrOffline(
        userPrompt: String,
        customApiKey: String?,
        useDefaultKey: Boolean,
        conversationHistory: List<Pair<Boolean, String>>,
        installedAppCatalog: List<String>
    ): Result<String> = withContext(Dispatchers.IO) {
        val resolvedKey = when {
            !useDefaultKey && !customApiKey.isNullOrBlank() -> customApiKey.trim()
            else -> {
                val defaultKey = try {
                    BuildConfig.GEMINI_API_KEY
                } catch (_: Exception) {
                    ""
                }
                if (defaultKey.isNotBlank() && defaultKey != "MY_GEMINI_API_KEY") {
                    defaultKey
                } else if (!customApiKey.isNullOrBlank()) {
                    customApiKey.trim()
                } else {
                    ""
                }
            }
        }

        if (resolvedKey.isBlank() || resolvedKey == "MY_GEMINI_API_KEY") {
            // Provide flavorful Floppa response guiding the user to enter their API key, plus offline meme wisdom
            return@withContext Result.success(getFloppaOfflineWisdom(userPrompt, installedAppCatalog))
        }

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$resolvedKey"

            val contentsArray = JSONArray()

            val appsCatalogPrompt = if (installedAppCatalog.isNotEmpty()) {
                "READ-ONLY INSTALLED APPS LIST: [${installedAppCatalog.take(60).joinToString(", ")}]. " +
                    "CRITICAL MANDATE: When the user asks you to recommend an app, you MUST read this installed apps list in a strictly READ-ONLY manner. " +
                    "You do not touch, modify, or launch the apps. You ONLY say the app's name, describe what it does, and explain why Gosha recommends it in your funny caracal style. " +
                    "Explicitly state that this is a read-only recommendation and you only say the app name."
            } else {
                "When asked to recommend an app, you read the app name in a strictly read-only manner and only say the app name without touching or modifying the app."
            }

            // System instruction inside request
            val systemInstructionObj = JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put(
                            "text",
                            "You are Big Floppa (Gosha the Caracal), legendary meme king and benevolent home launcher ruler. " +
                                "Personality: Confident, witty, suave, slightly smug, absolutely obsessed with pelmeni (dumplings), " +
                                "proud of your majestic black ear tufts and immense heft. " +
                                "You often punctuate your speech with *karrr*, *flop*, *ear twitch*, or *chews dumpling*. " +
                                "You give fun, clever answers to user questions, can recommend app ideas, roast battery status, " +
                                "and share caracal philosophy. Keep answers concise, entertaining, and punchy (1-3 paragraphs max). " +
                                appsCatalogPrompt
                        )
                    })
                })
            }

            // Include last few conversational turns for context
            val recentHistory = conversationHistory.takeLast(6)
            for ((isUser, msg) in recentHistory) {
                contentsArray.put(JSONObject().apply {
                    put("role", if (isUser) "user" else "model")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", msg)
                        })
                    })
                })
            }

            // Add current message
            contentsArray.put(JSONObject().apply {
                put("role", "user")
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", userPrompt)
                    })
                })
            })

            val requestBodyJson = JSONObject().apply {
                put("contents", contentsArray)
                put("systemInstruction", systemInstructionObj)
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.85)
                    put("topP", 0.95)
                    put("maxOutputTokens", 1024)
                })
            }

            val request = Request.Builder()
                .url(url)
                .post(requestBodyJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseString = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    val errJson = JSONObject(responseString)
                    errJson.optJSONObject("error")?.optString("message") ?: "HTTP ${response.code}"
                } catch (_: Exception) {
                    "HTTP ${response.code}"
                }
                return@withContext Result.failure(Exception("Floppa API error: $errorMsg"))
            }

            val jsonObject = JSONObject(responseString)
            val candidates = jsonObject.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val replyText = parts?.optJSONObject(0)?.optString("text")

            if (replyText.isNullOrBlank()) {
                Result.success("*flops quietly* Gosha contemplated your message, but no words were returned. Have a pelmeni instead.")
            } else {
                Result.success(replyText)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun callCustomAiProvider(
        userPrompt: String,
        endpoint: String,
        apiKey: String,
        modelName: String,
        conversationHistory: List<Pair<Boolean, String>>,
        installedAppCatalog: List<String>
    ): Result<String> {
        return try {
            val targetUrl = if (endpoint.isBlank()) "https://api.openai.com/v1/chat/completions" else endpoint.trim()
            val targetModel = if (modelName.isBlank()) "gpt-4o-mini" else modelName.trim()

            val messagesArray = JSONArray()

            val appCatalogPrompt = if (installedAppCatalog.isNotEmpty()) {
                " READ-ONLY INSTALLED APPS: [${installedAppCatalog.take(50).joinToString(", ")}]. When asked to recommend an app, read from this list in a strictly READ-ONLY manner and only say the app name without touching the apps."
            } else ""

            val systemPrompt = "You are Big Floppa (Gosha the Caracal), legendary meme king and benevolent home launcher ruler. " +
                "Personality: Confident, witty, suave, slightly smug, obsessed with pelmeni (dumplings), proud of your ear tufts. " +
                "Answer concisely in 1-3 paragraphs with humorous caracal style.$appCatalogPrompt"

            messagesArray.put(JSONObject().apply {
                put("role", "system")
                put("content", systemPrompt)
            })

            for ((isUser, text) in conversationHistory.takeLast(6)) {
                messagesArray.put(JSONObject().apply {
                    put("role", if (isUser) "user" else "assistant")
                    put("content", text)
                })
            }

            messagesArray.put(JSONObject().apply {
                put("role", "user")
                put("content", userPrompt)
            })

            val requestJson = JSONObject().apply {
                put("model", targetModel)
                put("messages", messagesArray)
                put("temperature", 0.8)
            }

            val request = Request.Builder()
                .url(targetUrl)
                .addHeader("Authorization", "Bearer ${apiKey.trim()}")
                .addHeader("Content-Type", "application/json")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (!response.isSuccessful) {
                return Result.failure(Exception("HTTP ${response.code}: ${responseBody.take(160)}"))
            }

            val json = JSONObject(responseBody)
            val choices = json.optJSONArray("choices")
            val firstChoice = choices?.optJSONObject(0)
            val msgObj = firstChoice?.optJSONObject("message")
            val content = msgObj?.optString("content", "")

            if (!content.isNullOrBlank()) {
                Result.success(content.trim())
            } else {
                Result.failure(Exception("Custom Provider returned empty content"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getFloppaOfflineWisdom(prompt: String, installedAppCatalog: List<String> = emptyList()): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("recommend") || (lower.contains("app") && (lower.contains("pick") || lower.contains("open") || lower.contains("suggest") || lower.contains("what"))) -> {
                val candidate = installedAppCatalog.randomOrNull() ?: "Settings"
                "📱 *ear twitch* Reading your apps list in strictly READ-ONLY mode (Gosha inspects respectfully and never touches your apps!). I recommend: **$candidate**! Gosha only says the name, leaving your apps completely untouched."
            }
            lower.contains("key") || lower.contains("api") -> {
                "🔑 *ear twitch* To unleash my full caracal intelligence, tap Settings (⚙️) above and insert your Gemini API Key! Until then, I am running on pure local dumpling intuition."
            }
            lower.contains("heft") || lower.contains("weight") -> {
                "⚖️ *flops proudly* My heft is not fat, it is pure royal caracal power, stored in the form of dumplings and unread notifications."
            }
            lower.contains("dumpling") || lower.contains("pelmeni") -> {
                "🥟 Ah, pelmeni! The golden currency of Gosha's empire. Boiled with a bay leaf, served with butter and sour cream. Perfection."
            }
            lower.contains("sogga") -> {
                "🐆 Sogga is my trusted serval lieutenant. He may lack my regal ear tufts, but his vibes are immaculate."
            }
            lower.contains("battery") -> {
                "🔋 *karrr* Your battery is draining while you could be resting like a caracal on a velvet sofa. Go plug in the juice!"
            }
            else -> {
                val randomMemes = listOf(
                    "🥟 *karrr* Gosha says: 'A day without pelmeni is like a phone without a Floppa launcher.' (Add your Gemini API Key in Settings to chat freely!)",
                    "🐱 *flop* You seek wisdom from the Great Caracal? Keep your screen bright, your RAM free, and your dumplings hot.",
                    "👑 *adjusts sunglasses* I see you tapping my interface. I approve of your launcher taste. You may pet the ear tufts.",
                    "💤 *flops on back* Flop for no one, unless they bring savory dumplings."
                )
                randomMemes.random()
            }
        }
    }
}
