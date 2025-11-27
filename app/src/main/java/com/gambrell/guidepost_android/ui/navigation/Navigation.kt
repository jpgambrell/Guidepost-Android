package com.gambrell.guidepost_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.gambrell.guidepost_android.data.models.ImageAnalysisResult
import com.gambrell.guidepost_android.ui.screens.HomeScreen
import com.gambrell.guidepost_android.ui.screens.ImageDetailScreen
import com.gambrell.guidepost_android.ui.screens.ImageUploadScreen
import com.gambrell.guidepost_android.ui.viewmodel.ImageGridViewModel
import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object ImageDetail : Screen("image_detail/{imageId}/{filename}/{analyzedAt}/{status}/{description}/{keywords}/{detectedText}/{error}") {
        fun createRoute(result: ImageAnalysisResult): String {
            val description = result.description?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
            val keywords = result.keywords?.joinToString(",")?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
            val detectedText = result.detectedText?.joinToString(",")?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
            val error = result.error?.let { URLEncoder.encode(it, "UTF-8") } ?: ""
            return "image_detail/${result.imageId}/${URLEncoder.encode(result.filename, "UTF-8")}/${URLEncoder.encode(result.analyzedAt, "UTF-8")}/${result.status.name}/$description/$keywords/$detectedText/$error"
        }
    }
    data object Upload : Screen("upload")
}

@Composable
fun GuidepostNavHost(
    navController: NavHostController = rememberNavController(),
    viewModel: ImageGridViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToDetail = { result ->
                    navController.navigate(Screen.ImageDetail.createRoute(result))
                },
                onNavigateToUpload = {
                    navController.navigate(Screen.Upload.route)
                }
            )
        }

        composable(Screen.ImageDetail.route) { backStackEntry ->
            val imageId = backStackEntry.arguments?.getString("imageId") ?: ""
            val filename = backStackEntry.arguments?.getString("filename")?.let { 
                URLDecoder.decode(it, "UTF-8") 
            } ?: ""
            val analyzedAt = backStackEntry.arguments?.getString("analyzedAt")?.let { 
                URLDecoder.decode(it, "UTF-8") 
            } ?: ""
            val statusStr = backStackEntry.arguments?.getString("status") ?: "PENDING"
            val description = backStackEntry.arguments?.getString("description")?.let { 
                if (it.isNotEmpty()) URLDecoder.decode(it, "UTF-8") else null 
            }
            val keywords = backStackEntry.arguments?.getString("keywords")?.let { 
                if (it.isNotEmpty()) URLDecoder.decode(it, "UTF-8").split(",") else null 
            }
            val detectedText = backStackEntry.arguments?.getString("detectedText")?.let { 
                if (it.isNotEmpty()) URLDecoder.decode(it, "UTF-8").split(",") else null 
            }
            val error = backStackEntry.arguments?.getString("error")?.let { 
                if (it.isNotEmpty()) URLDecoder.decode(it, "UTF-8") else null 
            }

            val status = try {
                com.gambrell.guidepost_android.data.models.AnalysisStatus.valueOf(statusStr)
            } catch (e: Exception) {
                com.gambrell.guidepost_android.data.models.AnalysisStatus.PENDING
            }

            val analysisResult = ImageAnalysisResult(
                imageId = imageId,
                filename = filename,
                analyzedAt = analyzedAt,
                keywords = keywords,
                detectedText = detectedText,
                description = description,
                status = status,
                error = error
            )

            ImageDetailScreen(
                analysisResult = analysisResult,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Upload.route) {
            ImageUploadScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

