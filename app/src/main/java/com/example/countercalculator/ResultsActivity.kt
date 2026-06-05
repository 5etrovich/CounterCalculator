package com.example.countercalculator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.content.res.ColorStateList
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ResultsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        setupToolbar()
        showResults()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Результаты расчета"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun showResults() {
        val layout = findViewById<LinearLayout>(R.id.results_layout)
        layout.removeAllViews()

        val config = AppData.currentApartmentConfig
        val inputData = AppData.inputData

        if (config == null || inputData.isEmpty()) {
            layout.addView(TextView(this).apply {
                text = "Нет данных для отображения. Вернитесь и заполните поля."
                setTextColor(Color.GRAY)
                textSize = 16f
            })
            return
        }

        val results = CalculatorViewModel().calculate(config, inputData)

        layout.addView(TextView(this).apply {
            text = "Детализация расчета"
            setTextColor(Color.BLACK)
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 30 }
        })

        // Счётчики
        layout.addView(sectionTitle("Показания счетчиков:"))
        config.counters.forEach { counter ->
            results[counter.name]?.let { layout.addView(createResultCard(counter.name, it)) }
        }

        // Водоотведение
        if (config.hasWaterDisposal) {
            layout.addView(sectionTitle("Водоотведение:"))
            layout.addView(TextView(this).apply {
                text = "Рассчитывается автоматически: суммарный расход воды × тариф водоотведения (по ПП РФ №354)"
                setTextColor(Color.parseColor("#757575"))
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            })
            results["Водоотведение"]?.let { layout.addView(createResultCard("Водоотведение", it)) }
        }

        // Фиксированные платежи
        if (config.fixedPayments.isNotEmpty()) {
            layout.addView(sectionTitle("Фиксированные платежи:"))
            config.fixedPayments.forEach { payment ->
                results[payment.name]?.let { layout.addView(createResultCard(payment.name, it)) }
            }
        }

        layout.addView(createTotalCard(results["ИТОГО"] ?: "0.00 руб."))

        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 40
                bottomMargin = 80
            }
        }
        buttonContainer.addView(MaterialButton(this).apply {
            text = "Вернуться на главную"
            setTextColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(Color.parseColor("#4FC3F7"))
            strokeColor = ColorStateList.valueOf(Color.parseColor("#0288D1"))
            strokeWidth = dpToPx(2)
            cornerRadius = dpToPx(12)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(64)
            )
            setOnClickListener {
                startActivity(Intent(this@ResultsActivity, ApartmentsActivity::class.java))
            }
        })
        layout.addView(buttonContainer)
    }

    private fun sectionTitle(text: String) = TextView(this).apply {
        this.text = text
        setTextColor(Color.BLACK)
        textSize = 18f
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 20
            bottomMargin = 15
        }
    }

    private fun createResultCard(title: String, value: String): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 10 }
            setCardBackgroundColor(Color.parseColor("#F8FBFE"))
            cardElevation = 2f
            radius = 8f

            val row = LinearLayout(this@ResultsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(25, 20, 25, 20)
            }
            row.addView(TextView(this@ResultsActivity).apply {
                text = title
                setTextColor(Color.BLACK)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f)
            })
            row.addView(TextView(this@ResultsActivity).apply {
                text = value
                setTextColor(Color.GRAY)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
            })
            addView(row)
        }
    }

    private fun createTotalCard(totalValue: String): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 20 }
            setCardBackgroundColor(Color.parseColor("#4FC3F7"))
            cardElevation = 4f
            radius = 12f

            val row = LinearLayout(this@ResultsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(30, 25, 30, 25)
            }
            row.addView(TextView(this@ResultsActivity).apply {
                text = "ИТОГО:"
                setTextColor(Color.WHITE)
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.6f)
            })
            row.addView(TextView(this@ResultsActivity).apply {
                text = totalValue
                setTextColor(Color.WHITE)
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.4f)
            })
            addView(row)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }
}
