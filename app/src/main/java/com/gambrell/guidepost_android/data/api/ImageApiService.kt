package com.gambrell.guidepost_android.data.api

import com.gambrell.guidepost_android.data.models.AnalysisListResponse
import com.gambrell.guidepost_android.data.models.AnalysisResponse
import com.gambrell.guidepost_android.data.models.HealthResponse
import com.gambrell.guidepost_android.data.models.UploadResponse
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface UploadApiService {
    @Multipart
    @POST("api/upload")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<UploadResponse>

    @GET("api/images/{id}")
    suspend fun getImageData(@Path("id") id: String): Response<ResponseBody>

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}

interface AnalysisApiService {
    @GET("api/analysis")
    suspend fun getAllAnalysis(): Response<AnalysisListResponse>

    @GET("api/analysis/{imageId}")
    suspend fun getAnalysis(@Path("imageId") imageId: String): Response<AnalysisResponse>

    @GET("health")
    suspend fun healthCheck(): Response<HealthResponse>
}

object ApiClient {
    private const val UPLOAD_SERVICE_URL = "http://192.168.68.73:3000/"
    private const val ANALYSIS_SERVICE_URL = "http://192.168.68.73:3001/"

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(300, TimeUnit.SECONDS)
        .build()

    val uploadService: UploadApiService by lazy {
        Retrofit.Builder()
            .baseUrl(UPLOAD_SERVICE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UploadApiService::class.java)
    }

    val analysisService: AnalysisApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ANALYSIS_SERVICE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnalysisApiService::class.java)
    }

    fun getImageUrl(imageId: String): String {
        return "${UPLOAD_SERVICE_URL}api/images/$imageId"
    }
}

