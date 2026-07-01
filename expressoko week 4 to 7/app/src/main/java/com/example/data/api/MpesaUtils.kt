package com.example.data.api

import android.util.Base64
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object MpesaUtils {

    /**
     * Generate formatting Safaricomtimestamp: yyyyMMddHHmmss
     */
    fun getTimestamp(): String {
        return SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
    }

    /**
     * Generate Base64 passkey token: Base64(Shortcode + Passkey + Timestamp)
     */
    fun generatePassword(shortCode: String, passKey: String, timestamp: String): String {
        val composite = shortCode + passKey + timestamp
        return Base64.encodeToString(composite.toByteArray(), Base64.NO_WRAP)
    }
}
