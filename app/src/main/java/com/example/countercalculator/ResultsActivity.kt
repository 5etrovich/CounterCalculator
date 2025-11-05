package com.example.countercalculator

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
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
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun showResults() {
        val layout = findViewById<LinearLayout>(R.id.results_layout)
        layout.removeAllViews()

        val apartmentNumber = AppData.currentApartmentNumber
        val inputData = AppData.inputData

        if (inputData.isNotEmpty()) {
            val viewModel = CalculatorViewModel()
            val results = when (apartmentNumber) {
                1 -> viewModel.calculateApartment1(inputData, this)
                2 -> viewModel.calculateApartment2(inputData, this)
                3 -> viewModel.calculateApartment3(inputData, this)
                else -> emptyMap()
            }

            // Заголовок
            val title = TextView(this).apply {
                text = "Детализация расчета"
                setTextColor(android.graphics.Color.BLACK)
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 30
                }
            }
            layout.addView(title)

            // Получаем список счетчиков в правильном порядке
            val countersInOrder = getCountersInOrder(apartmentNumber)

            // Добавляем показания счетчиков в правильном порядке
            val countersTitle = TextView(this).apply {
                text = "Показания счетчиков:"
                setTextColor(android.graphics.Color.BLACK)
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 15
                }
            }
            layout.addView(countersTitle)

            // Показания счетчиков в правильном порядке
            countersInOrder.forEach { counterName ->
                results[counterName]?.let { value ->
                    val resultCard = createResultCard(counterName, value)
                    layout.addView(resultCard)
                }
            }

            // Автоматические расчеты
            val autoTitle = TextView(this).apply {
                text = "Автоматические расчеты:"
                setTextColor(android.graphics.Color.BLACK)
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 20
                    bottomMargin = 15
                }
            }
            layout.addView(autoTitle)

            // Водоотведение
            results["Водоотведение"]?.let { value ->
                val resultCard = createResultCard("Водоотведение", value)
                layout.addView(resultCard)
            }

            // Фиксированные платежи
            val fixedTitle = TextView(this).apply {
                text = "Фиксированные платежи:"
                setTextColor(android.graphics.Color.BLACK)
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    topMargin = 20
                    bottomMargin = 15
                }
            }
            layout.addView(fixedTitle)

            // Мусор
            results["Мусор"]?.let { value ->
                val resultCard = createResultCard("Мусор", value)
                layout.addView(resultCard)
            }

            // Интернет
            results["Интернет"]?.let { value ->
                val resultCard = createResultCard("Интернет", value)
                layout.addView(resultCard)
            }

            // Итоговая сумма
            val totalValue = results["ИТОГО"] ?: "0.00 руб."
            val totalCard = createTotalCard(totalValue)
            layout.addView(totalCard)

            // Добавляем кнопку НОВЫЙ РАСЧЕТ в конец скролла
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

            val newCalculationButton = Button(this).apply {
                text = "НОВЫЙ РАСЧЕТ"
                setTextColor(android.graphics.Color.WHITE)
                setBackgroundColor(android.graphics.Color.parseColor("#4FC3F7"))
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    width = dpToPx(220)
                    height = dpToPx(55)
                }

                setOnClickListener {
                    val intent = Intent(this@ResultsActivity, ApartmentsActivity::class.java)
                    startActivity(intent)
                }
            }

            buttonContainer.addView(newCalculationButton)
            layout.addView(buttonContainer)

        } else {
            val errorText = TextView(this).apply {
                text = "Нет данных для отображения. Вернитесь и заполните поля."
                setTextColor(android.graphics.Color.GRAY)
                textSize = 16f
            }
            layout.addView(errorText)
        }
    }

    // Функция для получения счетчиков в правильном порядке
    private fun getCountersInOrder(apartmentId: Int): List<String> {
        return when (apartmentId) {
            1 -> listOf("Электричество 1", "Электричество 2", "Электричество 3", "Холодная вода", "Горячая вода")
            2 -> listOf("Электричество 1", "Электричество 2", "Холодная вода", "Горячая вода")
            3 -> listOf("Электричество 1", "Электричество 2", "Холодная вода 1", "Холодная вода 2", "Горячая вода 1", "Горячая вода 2")
            else -> emptyList()
        }
    }

    // Функция для конвертации dp в pixels
    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }

    private fun createResultCard(title: String, value: String): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
            setCardBackgroundColor(android.graphics.Color.parseColor("#F8FBFE"))
            cardElevation = 2f
            radius = 8f

            val cardLayout = LinearLayout(this@ResultsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(25, 20, 25, 20)
            }

            val titleView = TextView(this@ResultsActivity).apply {
                text = title
                setTextColor(android.graphics.Color.BLACK)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.6f
                }
            }

            val valueView = TextView(this@ResultsActivity).apply {
                text = value
                setTextColor(android.graphics.Color.GRAY)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.4f
                }
            }

            cardLayout.addView(titleView)
            cardLayout.addView(valueView)
            addView(cardLayout)
        }
    }

    private fun createTotalCard(totalValue: String): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 20
            }
            setCardBackgroundColor(android.graphics.Color.parseColor("#4FC3F7"))
            cardElevation = 4f
            radius = 12f

            val cardLayout = LinearLayout(this@ResultsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(30, 25, 30, 25)
            }

            val titleView = TextView(this@ResultsActivity).apply {
                text = "ИТОГО:"
                setTextColor(android.graphics.Color.WHITE)
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.6f
                }
            }

            val valueView = TextView(this@ResultsActivity).apply {
                text = totalValue
                setTextColor(android.graphics.Color.WHITE)
                textSize = 20f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.4f
                }
            }

            cardLayout.addView(titleView)
            cardLayout.addView(valueView)
            addView(cardLayout)
        }
    }
}