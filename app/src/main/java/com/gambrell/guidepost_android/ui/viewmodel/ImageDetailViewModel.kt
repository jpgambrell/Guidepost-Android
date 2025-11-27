package com.gambrell.guidepost_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gambrell.guidepost_android.data.models.ImageAnalysisResult
import com.gambrell.guidepost_android.data.repository.ApiResult
import com.gambrell.guidepost_android.data.repository.ImageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ImageDetailUiState(
    val analysisResult: ImageAnalysisResult? = null,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null
)

class ImageDetailViewModel : ViewModel() {
    private val repository = ImageRepository()

    private val _uiState = MutableStateFlow(ImageDetailUiState())
    val uiState: StateFlow<ImageDetailUiState> = _uiState.asStateFlow()

    fun setInitialAnalysisResult(result: ImageAnalysisResult) {
        _uiState.value = _uiState.value.copy(analysisResult = result)
        refreshAnalysis(result.imageId)
    }

    fun refreshAnalysis(imageId: String) {
        if (_uiState.value.isRefreshing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)

            when (val result = repository.fetchAnalysis(imageId)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        analysisResult = result.data,
                        isRefreshing = false
                    )
                }
                is ApiResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.message,
                        isRefreshing = false
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

