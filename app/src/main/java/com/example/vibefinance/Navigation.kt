package com.example.vibefinance

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.vibefinance.ui.FinanceViewModel
import com.example.vibefinance.ui.main.MainScreen

@Composable
fun MainNavigation(viewModel: FinanceViewModel) {
  val backStack = rememberNavBackStack(Main)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<Main> {
          MainScreen(viewModel = viewModel)
        }
      },
  )
}
