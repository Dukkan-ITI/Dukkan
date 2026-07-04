package com.dukkan.payment

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dukkan.payment.presentation.CheckoutScreen
import com.dukkan.payment.presentation.CheckoutViewModel
import com.dukkan.navigation.Screen

fun NavGraphBuilder.paymentNavGraph(
    navController: NavController,
    onPaymentResult: (PaymentResult) -> Unit,
) {
    composable<Screen.Payment> {
        val viewModel: CheckoutViewModel = hiltViewModel()

        CheckoutScreen(
            viewModel       = viewModel,
            onPaymentResult = onPaymentResult,
            onNavigateUp    = { navController.navigateUp() },
        )
    }
}
