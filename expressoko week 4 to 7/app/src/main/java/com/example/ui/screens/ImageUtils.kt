package com.example.ui.screens

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

fun copyUriToInternalStorage(context: Context, uri: Uri, prefix: String): String {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return uri.toString()
        val file = File(context.filesDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        val outputStream = FileOutputStream(file)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return "file://" + file.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return uri.toString()
}
