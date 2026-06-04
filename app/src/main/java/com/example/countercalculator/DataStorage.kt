package com.example.countercalculator

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DataStorage(private val context: Context) {

    private val sharedPref: SharedPreferences = context.getSharedPreferences("CounterCalculator", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getNextApartmentId(): Int {
        val next = sharedPref.getInt("next_apartment_id", 1)
        sharedPref.edit().putInt("next_apartment_id", next + 1).commit()
        return next
    }

    fun saveApartmentConfig(config: ApartmentConfig) {
        sharedPref.edit().putString("config_${config.id}", gson.toJson(config)).commit()
        val ids = loadApartmentIds().toMutableList()
        if (config.id !in ids) {
            ids.add(config.id)
            sharedPref.edit().putString("apartment_ids", gson.toJson(ids)).commit()
        }
    }

    fun loadApartmentConfig(id: Int): ApartmentConfig? {
        val json = sharedPref.getString("config_$id", null) ?: return null
        return gson.fromJson(json, ApartmentConfig::class.java)
    }

    fun loadAllApartments(): List<ApartmentConfig> {
        return loadApartmentIds().mapNotNull { loadApartmentConfig(it) }
    }

    fun deleteApartment(id: Int) {
        sharedPref.edit()
            .remove("config_$id")
            .remove("apartment_$id")
            .commit()
        val ids = loadApartmentIds().toMutableList()
        ids.remove(id)
        sharedPref.edit().putString("apartment_ids", gson.toJson(ids)).commit()
    }

    private fun loadApartmentIds(): List<Int> {
        val json = sharedPref.getString("apartment_ids", null) ?: return emptyList()
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun saveApartmentData(apartmentId: Int, data: Map<String, Double>) {
        sharedPref.edit().putString("apartment_$apartmentId", gson.toJson(data)).commit()
    }

    fun loadApartmentData(apartmentId: Int): Map<String, Double> {
        val json = sharedPref.getString("apartment_$apartmentId", null) ?: return emptyMap()
        val type = object : TypeToken<Map<String, Double>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }
}
