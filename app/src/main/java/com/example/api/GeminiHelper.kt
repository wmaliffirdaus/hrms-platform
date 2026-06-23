package com.example.api

import android.util.Log
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

object GeminiHelper {
    private const val TAG = "GeminiHelper"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateResponse(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        // Handle fallback if the user hasn't configured secret yet or left placeholder
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API Key is blank or placeholder!")
            return@withContext "HR Pulse AI Sync: Please configure your GEMINI_API_KEY in the Secrets panel. This will activate our Smart Applicant Assessment, review drafts, and automatic compliance analysis."
        }

        try {
            val root = JSONObject()
            
            // Contents core
            val contentsArr = JSONArray()
            val contentObj = JSONObject()
            val partsArr = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArr.put(partObj)
            contentObj.put("parts", partsArr)
            contentsArr.put(contentObj)
            root.put("contents", contentsArr)

            // System Instructions context
            if (systemInstruction != null) {
                val sysInstrObj = JSONObject()
                val sysPartsArr = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArr.put(sysPartObj)
                sysInstrObj.put("parts", sysPartsArr)
                root.put("systemInstruction", sysInstrObj)
            }

            val requestBodyJson = root.toString()
            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestBodyJson.toRequestBody(mediaType)

            val url = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "Unsuccessful API Call: code=${response.code}, body=$errBody")
                    return@withContext "AI Processing error: ${response.message}. Verify that the Gemini API is active in Google AI Studio."
                }

                val responseBody = response.body?.string() ?: return@withContext "Unable to extract response."
                val jsonResponse = JSONObject(responseBody)
                val candidates = jsonResponse.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCand = candidates.getJSONObject(0)
                    val candContent = firstCand.optJSONObject("content")
                    if (candContent != null) {
                        val parts = candContent.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            val firstPart = parts.getJSONObject(0)
                            return@withContext firstPart.optString("text", "No generated context found.")
                        }
                    }
                }
                "AI returned an empty response. Please rephrase."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Gemini network issue: ${e.message}", e)
            "Processing error occurred: ${e.localizedMessage}. Check internet connection or API Key authorization."
        }
    }
}
