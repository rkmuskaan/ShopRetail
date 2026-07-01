package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object GeminiManager {

    private const val TAG = "GeminiManager"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Class definitions for Moshi parsing
    data class ContentPart(val text: String)
    data class Content(val parts: List<ContentPart>)
    data class GeminiRequest(val contents: List<Content>)

    data class CandidatePart(val text: String?)
    data class CandidateContent(val parts: List<CandidatePart>?)
    data class Candidate(val content: CandidateContent?)
    data class GeminiResponse(val candidates: List<Candidate>?)

    /**
     * Synthesize a smart, local AI Retailer brief summarizing day's reports
     */
    suspend fun generateDailyBrief(
        retailerName: String,
        numProducts: Int,
        totalSalesAmt: Double,
        numSalesTx: Int,
        categoryBreakdown: Map<String, Int>,
        lowStockAlerts: List<String>,
        isOfflineMode: Boolean
    ): String = withContext(Dispatchers.IO) {
        
        // Return a responsive, high-quality, local model summary immediately if we are offline or if API key is missing
        val localBrief = buildLocalReport(retailerName, numProducts, totalSalesAmt, numSalesTx, categoryBreakdown, lowStockAlerts, isOfflineMode)
        
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
        if (isOfflineMode || apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Offline mode or missing Gemini API key. Displaying high-fidelity local analytics summary.")
            return@withContext localBrief
        }

        val prompt = """
            You are ExpresSoko's intelligent business assistant. You generate a smart, concise summary brief (resembling Samsung's Today Brief or elegant widget updates) for a retailer named $retailerName. No markdown headings (#, ##), keep it in 2-3 short, friendly paragraphs. Use emojis where relevant.
            
            Here are today's statistics:
            - Store Owner: $retailerName
            - Active Products Catalog count: $numProducts
            - Total Gross Sales Revenue: KES $totalSalesAmt (over $numSalesTx purchases)
            - Category Product Counts: ${categoryBreakdown.entries.joinToString { "${it.key}: ${it.value}" }}
            - Low Stock Items: ${if (lowStockAlerts.isEmpty()) "None. Stock levels healthy." else lowStockAlerts.joinToString()}
            
            Task: Synthesize these statistics into an inspiring, helpful, smart business summary. Compliment their sales velocity if sales are > 0, highlight any low stock issues that require immediate attention (suggesting restock amounts), and provide 1 specific, clever tip to optimize their sales catalog categories based on Kenya's local marketplace behaviors. Keep it highly professional and elegant.
        """.trimIndent()

        try {
            val jsonAdapter = moshi.adapter(GeminiRequest::class.java)
            val requestObject = GeminiRequest(listOf(Content(listOf(ContentPart(prompt)))))
            val jsonBody = jsonAdapter.toJson(requestObject)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toRequestBody(mediaType)

            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "HTTP error: ${response.code} returning fallback report")
                    return@withContext "⚠️ [$retailerName AI Agent Hub] Online generation momentarily unavailable. Here is your automated local summary:\n\n$localBrief"
                }

                val responseStr = response.body?.string() ?: ""
                val responseAdapter = moshi.adapter(GeminiResponse::class.java)
                val responseObj = responseAdapter.fromJson(responseStr)

                val generatedText = responseObj?.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text

                if (!generatedText.isNullOrBlank()) {
                    generatedText.trim()
                } else {
                    "⚠️ Connection to ExpresSoko AI was successful, but no response parts were generated. Displaying local cache:\n\n$localBrief"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network or parsing exception in Gemini generation", e)
            "🔌 [ExpresSoko AI Hub - Offline Analytics Backup] \n\n$localBrief"
        }
    }

    private fun buildLocalReport(
        retailerName: String,
        numProducts: Int,
        totalSalesAmt: Double,
        numSalesTx: Int,
        categoryBreakdown: Map<String, Int>,
        lowStockAlerts: List<String>,
        offline: Boolean
    ): String {
        return buildString {
            append("👋 Jambo, $retailerName! ")
            if (offline) {
                append("You are currently running in **Offline Mode**. ")
            }
            append("Here is your ExpresSoko smart business card brief for today:\n\n")
            
            append("📈 **Sales Velocity:** Today, your shop processed **$numSalesTx transactions**, generating gross revenues of **KES ${String.format("%,.2f", totalSalesAmt)}**. Your average cart order size is KES ${if (numSalesTx > 0) String.format("%,.2f", totalSalesAmt / numSalesTx) else "0.00"}.\n\n")
            
            append("📦 **Inventory Pulse:** You have **$numProducts items** listed in your active catalog. ")
            if (categoryBreakdown.isNotEmpty()) {
                append("Your listing categories layout contains: ")
                append(categoryBreakdown.entries.joinToString { "${it.key} (${it.value} items)" })
                append(". ")
            }
            append("\n\n")

            if (lowStockAlerts.isNotEmpty()) {
                append("🚨 **Restock Warning:** The following product inventory levels have dipped below critical thresholds and require attention: **${lowStockAlerts.joinToString()}**. Consider restocking at least 15 units of each item to ensure uninterrupted sales.\n\n")
            } else {
                append("✅ **Restock Status:** Excellent job! All active elements in your retail catalog report healthy stock levels above minimum boundaries.\n\n")
            }

            append("💡 **ExpresSoko Local Optimization Tip:** Kenyan shoppers are highly responsive to bundle discounts on daily household products. Consider bundling any high-stock food or spice items with tech accessories to clear shelf inventory.")
        }
    }
}
