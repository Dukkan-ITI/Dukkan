package com.dukkan.payment.domain.model

import com.msayeh.domain.model.Address

internal sealed interface CheckoutAddress {
    data class Saved(val addressId: String) : CheckoutAddress
    data class OneOff(val address: Address) : CheckoutAddress
}
