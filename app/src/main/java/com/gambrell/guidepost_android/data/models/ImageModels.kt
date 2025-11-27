package com.gambrell.guidepost_android.data.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// MARK: - Upload Service Models

data class UploadedImage(
    @SerializedName("id") val id: String,
    @SerializedName("filename") val filename: String,
    @SerializedName("originalName") val originalName: String,
    @SerializedName("mimetype") val mimetype: String,
    @SerializedName("size") val size: Long,
    @SerializedName("uploadedAt") val uploadedAt: String,
    @SerializedName("path") val path: String
) {
    val uploadDate: Date?
        get() {
            return try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                formatter.parse(uploadedAt)
            } catch (e: Exception) {
                null
            }
        }

    val formattedSize: String
        get() {
            return when {
                size < 1024 -> "$size B"
                size < 1024 * 1024 -> "${size / 1024} KB"
                else -> "${size / (1024 * 1024)} MB"
            }
        }
}

data class ImagesListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<UploadedImage>
)

data class UploadResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: UploadedImage
)

// MARK: - Analysis Service Models

data class ImageAnalysisResult(
    @SerializedName("imageId") val imageId: String,
    @SerializedName("filename") val filename: String,
    @SerializedName("analyzedAt") val analyzedAt: String,
    @SerializedName("keywords") val keywords: List<String>?,
    @SerializedName("detectedText") val detectedText: List<String>?,
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: AnalysisStatus,
    @SerializedName("error") val error: String?
) {
    val analyzedDate: Date?
        get() {
            return try {
                val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                formatter.parse(analyzedAt)
            } catch (e: Exception) {
                null
            }
        }

    val searchableText: String
        get() {
            val components = mutableListOf<String>()
            keywords?.let { components.addAll(it) }
            description?.let { components.add(it) }
            detectedText?.let { components.addAll(it) }
            components.add(filename)
            return components.joinToString(" ").lowercase()
        }
}

enum class AnalysisStatus {
    @SerializedName("pending") PENDING,
    @SerializedName("processing") PROCESSING,
    @SerializedName("completed") COMPLETED,
    @SerializedName("failed") FAILED
}

data class AnalysisListResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: List<ImageAnalysisResult>
)

data class AnalysisResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: ImageAnalysisResult
)

// MARK: - Health Check Models

data class HealthResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String?,
    @SerializedName("timestamp") val timestamp: String?
)

