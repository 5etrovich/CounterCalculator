package com.example.countercalculator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnStart).setOnClickListener {
            VibrationHelper.vibrate(this)
            startActivity(Intent(this, ApartmentsActivity::class.java))
        }

        findViewById<Button>(R.id.btnHistory).setOnClickListener {
            VibrationHelper.vibrate(this)
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }
}