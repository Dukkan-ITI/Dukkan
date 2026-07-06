package com.dukkan.payment.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dukkan.payment.R
import com.dukkan.payment.domain.model.PaymentMethod

@Composable
internal fun MethodSection(
    selectedMethod: PaymentMethod?,
    onMethodSelect: (PaymentMethod) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(horizontal = 22.dp)) {
        SectionEyebrow(text = stringResource(R.string.payment_step_method_title))

        Spacer(modifier = Modifier.height(10.dp))

        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
            MethodTile(
                title      = stringResource(R.string.payment_method_cash_title),
                subtitle   = stringResource(R.string.payment_method_cash_subtitle),
                icon       = Icons.Default.LocalShipping,
                isSelected = selectedMethod == PaymentMethod.CASH,
                onClick    = { onMethodSelect(PaymentMethod.CASH) },
            )

            MethodTile(
                title      = stringResource(R.string.payment_method_online_title),
                subtitle   = stringResource(R.string.payment_method_online_subtitle),
                icon       = Icons.Default.CreditCard,
                isSelected = selectedMethod == PaymentMethod.ONLINE,
                onClick    = { onMethodSelect(PaymentMethod.ONLINE) },
            )
        }
    }
}