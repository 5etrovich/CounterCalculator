package com.example.countercalculator

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataStorage(private val context: Context) {

    private val sharedPref: SharedPreferences = context.getSharedPreferences("CounterCalculator", Context.MODE_PRIVATE)
    private val gson = Gson()

    // Сохраняем данные для квартиры
    fun saveApartmentData(apartmentId: Int, data: Map<String, Double>) {
        val json = gson.toJson(data)
        sharedPref.edit().putString("apartment_$apartmentId", json).apply()
    }

    // Загружаем данные для квартиры
    fun loadApartmentData(apartmentId: Int): Map<String, Double> {
        val json = sharedPref.getString("apartment_$apartmentId", null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } else {
            emptyMap()
        }
    }

    // Сохраняем тарифы
    fun saveTariffs(apartmentId: Int, tariffs: Map<String, Double>) {
        val json = gson.toJson(tariffs)
        sharedPref.edit().putString("tariffs_$apartmentId", json).apply()
    }

    // Загружаем тарифы
    fun loadTariffs(apartmentId: Int): Map<String, Double> {
        val json = sharedPref.getString("tariffs_$apartmentId", null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson(json, type) ?: getDefaultTariffs(apartmentId)
        } else {
            getDefaultTariffs(apartmentId)
        }
    }

    // Сохраняем фиксированные платежи
    fun saveFixedPayments(apartmentId: Int, payments: Map<String, Double>) {
        val json = gson.toJson(payments)
        sharedPref.edit().putString("fixed_$apartmentId", json).apply()
    }

    // Загружаем фиксированные платежи
    fun loadFixedPayments(apartmentId: Int): Map<String, Double> {
        val json = sharedPref.getString("fixed_$apartmentId", null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, Double>>() {}.type
            gson.fromJson(json, type) ?: getDefaultFixedPayments(apartmentId)
        } else {
            getDefaultFixedPayments(apartmentId)
        }
    }

    private fun getDefaultTariffs(apartmentId: Int): Map<String, Double> {
        return when (apartmentId) {
            1 -> mapOf(
                "Электричество 1" to 7.16,
                "Электричество 2" to 10.23,
                "Электричество 3" to 3.71,
                "Холодная вода" to 59.8,
                "Горячая вода" to 256.83,
                "Водоотведение" to 45.91
            )
            2 -> mapOf(
                "Электричество 1" to 3.49,
                "Электричество 2" to 8.11,
                "Холодная вода" to 58.34,
                "Горячая вода" to 312.5,
                "Водоотведение" to 49.5
            )
            3 -> mapOf(
                "Электричество 1" to 8.6,
                "Электричество 2" to 3.71,
                "Холодная вода 1" to 58.34,
                "Холодная вода 2" to 58.34,
                "Горячая вода 1" to 312.5,
                "Горячая вода 2" to 312.5,
                "Водоотведение" to 49.5
            )
            else -> emptyMap()
        }
    }

    private fun getDefaultFixedPayments(apartmentId: Int): Map<String, Double> {
        return when (apartmentId) {
            1 -> mapOf("Мусор" to 295.9, "Интернет" to 650.0)
            2 -> mapOf("Мусор" to 783.6)
            3 -> mapOf("Мусор" to 308.82, "Интернет" to 1099.0)
            else -> emptyMap()
        }
    }

    // В конце класса DataStorage добавим метод для отладки
    fun debugPrintAllData(apartmentId: Int) {
        println("=== DEBUG DATA STORAGE ===")
        println("Apartment: $apartmentId")

        val apartmentData = loadApartmentData(apartmentId)
        println("Apartment data: $apartmentData")

        val tariffs = loadTariffs(apartmentId)
        println("Tariffs: $tariffs")

        val fixedPayments = loadFixedPayments(apartmentId)
        println("Fixed payments: $fixedPayments")
        println("==========================")
    }

    // В DataStorage.kt
    fun debugPrintAllKeys() {
        println("=== ALL SHARED PREFERENCES KEYS ===")
        val allEntries = sharedPref.all
        allEntries.forEach { (key, value) ->
            println("$key = $value")
        }
        println("===================================")
    }
}