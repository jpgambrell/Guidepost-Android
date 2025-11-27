package com.gambrell.guidepost_android.ui.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gambrell.guidepost_android.data.models.ImageAnalysisResult
import com.gambrell.guidepost_android.data.models.UploadedImage
import com.gambrell.guidepost_android.data.repository.ApiResult
import com.gambrell.guidepost_android.data.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ImageGridUiState(
    val analysisResults: List<ImageAnalysisResult> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val searchText: String = "",
    val isUploading: Boolean = false,
    val uploadError: String? = null,
    val uploadSuccess: Boolean = false
) {
    val filteredResults: List<ImageAnalysisResult>
        get() {
            if (searchText.isEmpty()) {
                return analysisResults
            }
            val lowercasedSearch = searchText.lowercase()
            return analysisResults.filter { result ->
                result.searchableText.contains(lowercasedSearch)
            }
        }
}

class ImageGridViewModel : ViewModel() {
    private val repository = ImageRepository()

    private val _uiState = MutableStateFlow(ImageGridUiState())
    val uiState: StateFlow<ImageGridUiState> = _uiState.asStateFlow()

    init {
        loadAnalysisResults()
    }

    fun loadAnalysisResults() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)

            when (val result = repository.fetchAllAnalysis()) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        analysisResults = result.data,
                        isLoading = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateSearchText(text: String) {
        _uiState.value = _uiState.value.copy(searchText = text)
    }

    fun uploadImage(context: Context, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isUploading = true,
                uploadError = null,
                uploadSuccess = false
            )

            when (val result = repository.uploadImage(context, imageUri)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadSuccess = true
                    )
                    // Refresh analysis results after upload
                    loadAnalysisResults()
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        uploadError = result.message
                    )
                }
            }
        }
    }

    fun clearUploadState() {
        _uiState.value = _uiState.value.copy(
            uploadError = null,
            uploadSuccess = false
        )
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

