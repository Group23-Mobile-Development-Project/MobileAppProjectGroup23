package com.example.eventplanner.utils

import android.content.Context
import com.stripe.android.PaymentConfiguration
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StripeHelper @Inject constructor(
    private val context: Context
) {
    init {

        PaymentConfiguration.init(
            context,
            "pk_test_51Sz1HeAwzqtC5H4kh0MqHiFjrtC2vQ86fz5DjANrTJdP6psIxLC3PcaVSM6WCLPF0ehWmBcY8avUVc415mO0aiwh00HLhjflOV" // Replace with your test key
        )
    }

    fun isStripeInitialized(): Boolean {
        return PaymentConfiguration.getInstance(context).publishableKey.isNotEmpty()
    }
}