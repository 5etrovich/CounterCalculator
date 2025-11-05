package com.example.countercalculator

import android.content.Context

class CalculatorViewModel {

    fun calculateApartment1(inputs: Map<String, Pair<Double, Double>>, context: Context): Map<String, String> {
        val dataStorage = DataStorage(context)
        val tariffs = dataStorage.loadTariffs(1)
        val fixedPayments = dataStorage.loadFixedPayments(1)

        return calculateForApartment(inputs, tariffs, fixedPayments, 1)
    }

    fun calculateApartment2(inputs: Map<String, Pair<Double, Double>>, context: Context): Map<String, String> {
        val dataStorage = DataStorage(context)
        val tariffs = dataStorage.loadTariffs(2)
        val fixedPayments = dataStorage.loadFixedPayments(2)

        return calculateForApartment(inputs, tariffs, fixedPayments, 2)
    }

    fun calculateApartment3(inputs: Map<String, Pair<Double, Double>>, context: Context): Map<String, String> {
        val dataStorage = DataStorage(context)
        val tariffs = dataStorage.loadTariffs(3)
        val fixedPayments = dataStorage.loadFixedPayments(3)

        return calculateForApartment(inputs, tariffs, fixedPayments, 3)
    }

    private fun calculateForApartment(
        inputs: Map<String, Pair<Double, Double>>,
        tariffs: Map<String, Double>,
        fixedPayments: Map<String, Double>,
        apartmentId: Int
    ): Map<String, String> {
        val results = mutableMapOf<String, String>()
        var total = 0.0

        // Расчет по счетчикам в правильном порядке
        val countersInOrder = when (apartmentId) {
            1 -> listOf("Электричество 1", "Электричество 2", "Электричество 3", "Холодная вода", "Горячая вода")
            2 -> listOf("Электричество 1", "Электричество 2", "Холодная вода", "Горячая вода")
            3 -> listOf("Электричество 1", "Электричество 2", "Холодная вода 1", "Холодная вода 2", "Горячая вода 1", "Горячая вода 2")
            else -> emptyList()
        }

        countersInOrder.forEach { counterName ->
            inputs[counterName]?.let { data ->
                val tariff = tariffs[counterName] ?: 0.0
                val consumption = data.second - data.first
                val amount = consumption * tariff
                results[counterName] = "${String.format("%.2f", consumption)} × ${String.format("%.2f", tariff)} = ${String.format("%.2f", amount)} руб."
                total += amount
            }
        }

        // Водоотведение (автоматически)
        val waterAmount = calculateWaterDisposal(inputs, apartmentId, tariffs["Водоотведение"] ?: 0.0)
        if (waterAmount > 0) {
            results["Водоотведение"] = "${String.format("%.2f", waterAmount)} руб. (авто)"
            total += waterAmount
        }

        // Фиксированные платежи в правильном порядке
        if (fixedPayments.containsKey("Мусор")) {
            val amount = fixedPayments["Мусор"] ?: 0.0
            results["Мусор"] = "${String.format("%.2f", amount)} руб."
            total += amount
        }

        if (fixedPayments.containsKey("Интернет")) {
            val amount = fixedPayments["Интернет"] ?: 0.0
            results["Интернет"] = "${String.format("%.2f", amount)} руб."
            total += amount
        }

        results["ИТОГО"] = String.format("%.2f руб.", total)
        return results
    }

    private fun calculateWaterDisposal(inputs: Map<String, Pair<Double, Double>>, apartmentId: Int, tariff: Double): Double {
        return when (apartmentId) {
            1 -> {
                val coldWater = inputs["Холодная вода"]
                val hotWater = inputs["Горячая вода"]
                if (coldWater != null && hotWater != null) {
                    val totalWater = (coldWater.second - coldWater.first) + (hotWater.second - hotWater.first)
                    totalWater * tariff
                } else 0.0
            }
            2 -> {
                val coldWater = inputs["Холодная вода"]
                val hotWater = inputs["Горячая вода"]
                if (coldWater != null && hotWater != null) {
                    val totalWater = (coldWater.second - coldWater.first) + (hotWater.second - hotWater.first)
                    totalWater * tariff
                } else 0.0
            }
            3 -> {
                val coldWater1 = inputs["Холодная вода 1"]
                val coldWater2 = inputs["Холодная вода 2"]
                val hotWater1 = inputs["Горячая вода 1"]
                val hotWater2 = inputs["Горячая вода 2"]
                if (coldWater1 != null && coldWater2 != null && hotWater1 != null && hotWater2 != null) {
                    val totalWater = (coldWater1.second - coldWater1.first) +
                            (coldWater2.second - coldWater2.first) +
                            (hotWater1.second - hotWater1.first) +
                            (hotWater2.second - hotWater2.first)
                    totalWater * tariff
                } else 0.0
            }
            else -> 0.0
        }
    }
}