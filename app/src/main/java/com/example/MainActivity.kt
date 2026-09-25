package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AppScreen
import com.example.ui.BillViewModel
import com.example.ui.HomeScreen
import com.example.ui.OnboardingScreen
import com.example.ui.ReceiptScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val billViewModel: BillViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val uiState by billViewModel.uiState.collectAsStateWithLifecycle()

        // Handle hardware / gesture back button
        BackHandler(enabled = uiState.currentScreen == AppScreen.Receipt) {
          billViewModel.navigateToHome()
        }

        Crossfade(
          targetState = uiState.currentScreen,
          label = "ScreenTransition",
          modifier = Modifier.fillMaxSize()
        ) { screen ->
          when (screen) {
            AppScreen.Onboarding -> {
              OnboardingScreen(
                onComplete = { name, cat, phone ->
                  billViewModel.completeOnboarding(name, cat, phone)
                },
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.Home -> {
              HomeScreen(
                viewModel = billViewModel,
                modifier = Modifier.fillMaxSize()
              )
            }
            AppScreen.Receipt -> {
              ReceiptScreen(
                viewModel = billViewModel,
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }
      }
    }
  }
}
