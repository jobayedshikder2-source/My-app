package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AdminLoginScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.ChatRoomScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.LoginScreen
import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.ui.screens.RegisterScreen
import com.example.ui.screens.WebCodeViewerScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CommunityViewModel
import com.example.ui.viewmodel.ScreenState
import com.example.util.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)
        setContent {
            MyApplicationTheme {
                CommunityAppRoot()
            }
        }
    }
}

@Composable
fun CommunityAppRoot(
    viewModel: CommunityViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val uiMessage by viewModel.uiMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Request notification permission on Android 13+ (API 33+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission granted or denied */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Handle incoming UI messages / toasts
    LaunchedEffect(uiMessage) {
        uiMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUiMessage()
        }
    }

    // Handle Android system back button
    BackHandler(enabled = currentScreen !is ScreenState.Login && currentScreen !is ScreenState.Dashboard) {
        viewModel.navigateBack()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { target ->
                when (target) {
                    is ScreenState.Login -> LoginScreen(viewModel)
                    is ScreenState.Register -> RegisterScreen(viewModel)
                    is ScreenState.AdminLogin -> AdminLoginScreen(viewModel)
                    is ScreenState.Dashboard -> DashboardScreen(viewModel)
                    is ScreenState.ChatRoom -> ChatRoomScreen(target.groupType, viewModel)
                    is ScreenState.AdminPanel -> AdminPanelScreen(viewModel)
                    is ScreenState.WebCodeViewer -> WebCodeViewerScreen(viewModel)
                }
            }
        }
    }
}
