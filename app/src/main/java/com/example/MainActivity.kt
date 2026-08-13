package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.BudgetViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.WeeklyBudgetTheme

class MainActivity : ComponentActivity() {
  private val viewModel: BudgetViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      WeeklyBudgetTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          MainScreen(viewModel = viewModel)
        }
      }
    }
  }
}
