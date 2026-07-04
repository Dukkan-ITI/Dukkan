package com.dukkan.payment

import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.dukkan.payment.presentation.CheckoutScreen
import com.dukkan.payment.presentation.CheckoutViewModel
import com.msayeh.domain.model.OrderConfirmation

fun NavGraphBuilder.paymentNavGraph(
    navController: NavController,
    onOrderConfirmed: (OrderConfirmation) -> Unit,
) {
    composable(route = "payment") {
        val viewModel: CheckoutViewModel = hiltViewModel()

        CheckoutScreen(
            viewModel        = viewModel,
            onOrderConfirmed = onOrderConfirmed,
            onNavigateUp     = { navController.navigateUp() },
        )
    }
}
