package com.permission.guard

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.permission.guard.presentation.navigation.NavGraph
import com.permission.guard.presentation.navigation.Screen
import com.permission.guard.ui.theme.PermissionGuardTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val sharedPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        val isOnboardingCompleted = sharedPrefs.getBoolean("onboarding_completed", false)
        val startDestination = if (isOnboardingCompleted) {
            Screen.Dashboard.route
        } else {
            Screen.Onboarding.route
        }

        setContent {
            PermissionGuardTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { _ ->
                    NavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}