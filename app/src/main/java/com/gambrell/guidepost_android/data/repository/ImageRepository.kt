package com.gambrell.guidepost_android.data.repository

import android.content.Context
import android.net.Uri
import com.gambrell.guidepost_android.data.api.ApiClient
import com.gambrell.guidepost_android.data.models.ImageAnalysisResult
import com.gambrell.guidepost_android.data.models.UploadedImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

class ImageRepository {
    private val uploadService = ApiClient.uploadService
    private val analysisService = ApiClient.analysisService

    suspend fun uploadImage(context: Context, imageUri: Uri): ApiResult<UploadedImage> {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
                ?: return ApiResult.Error("Cannot read image file")

            val bytes = inputStream.readBytes()
            inputStream.close()

            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            val fileName = "image_${System.currentTimeMillis()}.jpg"

            val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", fileName, requestBody)

            val response = uploadService.uploadImage(part)

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data)
            } else {
                ApiResult.Error(
                    response.errorBody()?.string() ?: "Upload failed",
                    response.code()
                )
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    suspend fun fetchAllAnalysis(): ApiResult<List<ImageAnalysisResult>> {
        return try {
            val response = analysisService.getAllAnalysis()

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data)
            } else {
                ApiResult.Error(
                    response.errorBody()?.string() ?: "Failed to fetch analysis",
                    response.code()
                )
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    suspend fun fetchAnalysis(imageId: String): ApiResult<ImageAnalysisResult> {
        return try {
            val response = analysisService.getAnalysis(imageId)

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.data)
            } else {
                ApiResult.Error(
                    response.errorBody()?.string() ?: "Failed to fetch analysis",
                    response.code()
                )
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }

    suspend fun fetchImageData(imageId: String): ApiResult<ByteArray> {
        return try {
            val response = uploadService.getImageData(imageId)

            if (response.isSuccessful && response.body() != null) {
                ApiResult.Success(response.body()!!.bytes())
            } else {
                ApiResult.Error(
                    response.errorBody()?.string() ?: "Failed to fetch image",
                    response.code()
                )
            }
        } catch (e: IOException) {
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: Exception) {
            ApiResult.Error("Error: ${e.message}")
        }
    }
}

