package com.example.countercalculator

data class CounterConfig(
    val name: String,
    val tariff: Double,
    val isWater: Boolean = false,
    val icon: String? = "📊"
)

data class FixedPaymentConfig(
    val name: String,
    val amount: Double
)

data class ApartmentConfig(
    val id: Int,
    val name: String,
    val counters: List<CounterConfig>,
    val fixedPayments: List<FixedPaymentConfig>,
    val waterDisposalTariff: Double = 0.0,
    val hasWaterDisposal: Boolean = false
)
