package com.example.countercalculator

class CalculatorViewModel {

    private fun formatRub(value: Double): String {
        return if (value == kotlin.math.floor(value)) {
            "${value.toLong()} руб."
        } else {
            "${String.format("%.2f", value)} руб."
        }
    }

    fun calculate(
        config: ApartmentConfig,
        inputs: Map<String, Pair<Double, Double>>
    ): Map<String, String> {
        val results = mutableMapOf<String, String>()
        var total = 0.0

        config.counters.forEach { counter ->
            inputs[counter.name]?.let { (prev, curr) ->
                val consumption = curr - prev
                val amount = consumption * counter.tariff
                results[counter.name] = "${String.format("%.2f", consumption)} × ${String.format("%.2f", counter.tariff)} = ${formatRub(amount)}"
                total += amount
            }
        }

        if (config.hasWaterDisposal) {
            val totalWater = config.counters
                .filter { it.isWater }
                .sumOf { counter ->
                    inputs[counter.name]?.let { (prev, curr) -> curr - prev } ?: 0.0
                }
            val amount = totalWater * config.waterDisposalTariff
            results["Водоотведение"] = "${String.format("%.2f", totalWater)} × ${String.format("%.2f", config.waterDisposalTariff)} = ${formatRub(amount)}"
            total += amount
        }

        config.fixedPayments.forEach { payment ->
            results[payment.name] = formatRub(payment.amount)
            total += payment.amount
        }

        results["ИТОГО"] = formatRub(total)
        return results
    }
}
