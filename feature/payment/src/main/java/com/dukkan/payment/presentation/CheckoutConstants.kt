package com.dukkan.payment.presentation

object CheckoutConstants {
    const val KEY_ORDER_ID               = "checkout_order_id"
    const val KEY_IDEMPOTENCY_KEY        = "checkout_idempotency_key"
    const val KEY_SAVED_ADDRESS_ID       = "checkout_saved_address_id"
    const val KEY_ONE_OFF_ADDRESS_JSON   = "checkout_one_off_address_json"
    const val KEY_SELECTED_METHOD        = "checkout_selected_method"
    
    const val MAX_POLL_ATTEMPTS          = 13
    const val POLL_DELAY_MS              = 1_500L
}
