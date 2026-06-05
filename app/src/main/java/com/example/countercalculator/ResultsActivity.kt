package com.example.countercalculator

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
                setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.text_secondary))
                textSize = 16f
            })
            return
        }

        val results = CalculatorViewModel().calculate(config, inputData)
        saveToHistory(config, inputData, results)

        layout.addView(TextView(this).apply {
            text = "Детализация расчета"
            setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.text_primary))
            textSize = 20f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 30 }
        })

        // Счётчики
        layout.addView(sectionTitle("Показания счетчиков:"))
        config.counters.forEach { counter ->
            results[counter.name]?.let { resultStr ->
                val (prev, curr) = inputData[counter.name] ?: (0.0 to 0.0)
                val subtitle = "%.2f → %.2f".format(prev, curr)
                layout.addView(createResultCard(counter.name, resultStr, subtitle))
            }
        }

        // Водоотведение
        if (config.hasWaterDisposal) {
            layout.addView(sectionTitle("Водоотведение:"))
            layout.addView(TextView(this).apply {
                text = "Рассчитывается автоматически: суммарный расход воды × тариф водоотведения (по ПП РФ №354)"
                setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.text_secondary))
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 12 }
            })
            results["Водоотведение"]?.let {
                val waterConsumptions = config.counters
                    .filter { c -> c.isWater }
                    .mapNotNull { c -> inputData[c.name]?.let { (prev, curr) -> curr - prev } }
                val waterSubtitle = if (waterConsumptions.size > 1) {
                    waterConsumptions.joinToString(" + ") { "%.2f".format(it) } +
                    " = %.2f м³".format(waterConsumptions.sum())
                } else {
                    "%.2f м³".format(waterConsumptions.sum())
                }
                layout.addView(createResultCard("Водоотведение", it, waterSubtitle))
            }

        }

        // Фиксированные платежи
        if (config.fixedPayments.isNotEmpty()) {
            layout.addView(sectionTitle("Фиксированные платежи:"))
            config.fixedPayments.forEach { payment ->
                results[payment.name]?.let { layout.addView(createResultCard(payment.name, it)) }
            }

        }

        layout.addView(createTotalCard(results["ИТОГО"] ?: "0.00 руб."))

        val shareText = buildShareText(config, results)
        val buttonContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER_HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(24)
                bottomMargin = dpToPx(300)
            }
        }
        buttonContainer.addView(MaterialButton(this).apply {
            text = "Поделиться результатами"
            setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.btn_secondary_text))
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ResultsActivity, R.color.btn_secondary_bg))
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@ResultsActivity, R.color.btn_secondary_text))
            strokeWidth = dpToPx(1)
            cornerRadius = dpToPx(12)
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(56)
            ).apply { bottomMargin = dpToPx(12) }
            setOnClickListener {
                VibrationHelper.vibrate(this@ResultsActivity)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                startActivity(Intent.createChooser(intent, "Поделиться расчётом"))
            }
        })
        buttonContainer.addView(MaterialButton(this).apply {
            text = "Вернуться на главную"
            setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.white))
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ResultsActivity, R.color.light_blue))
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@ResultsActivity, R.color.light_blue_stroke))
            strokeWidth = dpToPx(2)
            cornerRadius = dpToPx(12)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(64)
            )
            setOnClickListener {
                VibrationHelper.vibrate(this@ResultsActivity)
                val intent = Intent(this@ResultsActivity, ApartmentsActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
            }
        })
        layout.addView(buttonContainer)
    }

    private fun sectionTitle(text: String) = TextView(this).apply {
        this.text = text
        setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.text_primary))
        textSize = 18f
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 20
            bottomMargin = 15
        }
    }

    private fun createResultCard(title: String, value: String, subtitle: String? = null): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 10 }
            setCardBackgroundColor(ContextCompat.getColor(this@ResultsActivity, R.color.card_background_alt))
            cardElevation = 2f
            radius = 8f

            val col = LinearLayout(this@ResultsActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(25, 16, 25, 16)
            }

            col.addView(TextView(this@ResultsActivity).apply {
                text = title
                setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.text_primary))
                textSize = 16f
            })

            if (subtitle != null) {
                col.addView(TextView(this@ResultsActivity).apply {
                    text = subtitle
                    setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.text_secondary))
                    textSize = 13f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { topMargin = 2 }
                })
            }

            col.addView(TextView(this@ResultsActivity).apply {
                text = value
                setTextColor(ContextCompat.getColor(this@ResultsActivity, R.color.text_secondary))
                textSize = 15f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 4 }
            })

            addView(col)
        }
    }

    private fun createTotalCard(totalValue: String): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 20 }
            setCardBackgroundColor(ContextCompat.getColor(this@ResultsActivity, R.color.light_blue))
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

    private fun saveToHistory(
        config: ApartmentConfig,
        inputData: Map<String, Pair<Double, Double>>,
        results: Map<String, String>
    ) {
        val details = mutableListOf<HistoryDetail>()
        config.counters.forEach { counter ->
            results[counter.name]?.let { resultStr ->
                val (prev, curr) = inputData[counter.name] ?: (0.0 to 0.0)
                details.add(HistoryDetail(counter.name, resultStr, "%.2f → %.2f".format(prev, curr)))
            }
        }
        if (config.hasWaterDisposal) {
            results["Водоотведение"]?.let {
                details.add(HistoryDetail("Водоотведение", it))
            }
        }
        config.fixedPayments.forEach { payment ->
            results[payment.name]?.let { details.add(HistoryDetail(payment.name, it)) }
        }

        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
        val record = HistoryRecord(
            id = System.currentTimeMillis(),
            apartmentName = config.name,
            date = sdf.format(java.util.Date()),
            total = results["ИТОГО"] ?: "0.00 руб.",
            details = details
        )
        DataStorage(this).saveHistoryRecord(record)
    }

    private fun buildShareText(config: ApartmentConfig, results: Map<String, String>): String {
        val inputData = AppData.inputData
        val sb = StringBuilder()
        sb.appendLine("📊 Коммунальные платежи — ${config.name}")
        sb.appendLine()
        config.counters.forEach { counter ->
            results[counter.name]?.let { resultStr ->
                val (prev, curr) = inputData[counter.name] ?: (0.0 to 0.0)
                sb.appendLine("${counter.name}:")
                sb.appendLine("  %.2f → %.2f".format(prev, curr))
                sb.appendLine("  $resultStr")
            }
        }
        if (config.hasWaterDisposal) {
            results["Водоотведение"]?.let { resultStr ->
                val waterConsumptions = config.counters
                    .filter { it.isWater }
                    .mapNotNull { c -> inputData[c.name]?.let { (prev, curr) -> curr - prev } }
                val waterLine = if (waterConsumptions.size > 1) {
                    waterConsumptions.joinToString(" + ") { "%.2f".format(it) } +
                    " = %.2f м³".format(waterConsumptions.sum())
                } else {
                    "%.2f м³".format(waterConsumptions.sum())
                }
                sb.appendLine("Водоотведение:")
                sb.appendLine("  $waterLine")
                sb.appendLine("  $resultStr")
            }
        }
        if (config.fixedPayments.isNotEmpty()) {
            sb.appendLine()
            config.fixedPayments.forEach { payment ->
                results[payment.name]?.let { sb.appendLine("${payment.name}: $it") }
            }
        }
        sb.appendLine()
        sb.append("ИТОГО: ${results["ИТОГО"] ?: "0.00 руб."}")
        return sb.toString()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }
}
