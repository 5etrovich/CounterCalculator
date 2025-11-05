package com.example.countercalculator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.card.MaterialCardView

class ApartmentsActivity : AppCompatActivity() {

    private lateinit var dataStorage: DataStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartments)

        dataStorage = DataStorage(this)

        setupToolbar()
        setupClickListeners()
        setupEditButton()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Выбор квартиры"
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupClickListeners() {
        val card1 = findViewById<MaterialCardView>(R.id.cardApartment1)
        val card2 = findViewById<MaterialCardView>(R.id.cardApartment2)
        val card3 = findViewById<MaterialCardView>(R.id.cardApartment3)

        card1.setOnClickListener { selectApartment(1) }
        card2.setOnClickListener { selectApartment(2) }
        card3.setOnClickListener { selectApartment(3) }
    }

    private fun setupEditButton() {
        val btnEditData = findViewById<Button>(R.id.btnEditData)
        btnEditData.setOnClickListener {
            val intent = Intent(this, EditDataActivity::class.java)
            startActivity(intent)
        }
    }

    private fun selectApartment(apartmentId: Int) {
        // Сохраняем выбранную квартиру
        AppData.currentApartmentNumber = apartmentId

        // Загружаем предыдущие данные
        loadPreviousData(apartmentId)

        val intent = Intent(this, InputActivity::class.java)
        startActivity(intent)
    }

    private fun loadPreviousData(apartmentId: Int) {
        val previousData = dataStorage.loadApartmentData(apartmentId)
        if (previousData.isNotEmpty()) {
            Toast.makeText(this, "Загружены предыдущие показания", Toast.LENGTH_SHORT).show()
        }
    }
}