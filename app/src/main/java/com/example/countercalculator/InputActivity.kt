package com.example.countercalculator

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.android.material.card.MaterialCardView

class InputActivity : AppCompatActivity() {

    private lateinit var dataStorage: DataStorage
    private val inputFields = mutableMapOf<String, Pair<EditText, EditText>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        dataStorage = DataStorage(this)

        setupToolbar()
        setupInputs()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        val apartmentNumber = AppData.currentApartmentNumber
        val apartmentName = when (apartmentNumber) {
            1 -> "Вертолётчиков"
            2 -> "Проспект победы"
            3 -> "Вертолётная"
            else -> "Неизвестная"
        }
        toolbar.title = "Ввод данных: $apartmentName"
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupInputs() {
        val layout = findViewById<LinearLayout>(R.id.input_layout)
        layout.removeAllViews()

        val apartmentNumber = AppData.currentApartmentNumber
        val counters = getCountersForApartment(apartmentNumber)

        // Загружаем предыдущие данные
        val previousData = dataStorage.loadApartmentData(apartmentNumber)

        // Добавляем инструкцию
        // Добавляем инструкцию
        val instruction = TextView(this).apply {
            text = "Заполните текущие показания. Предыдущие загружены автоматически.\nМожно использовать точку или запятую как разделитель."
            setTextColor(Color.GRAY)
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 40
            }
        }
        layout.addView(instruction)

        // Добавляем карточки ввода для каждого счетчика
        counters.forEach { counterName ->
            val previousValue = previousData[counterName] ?: 0.0
            val card = createInputCard(counterName, previousValue)
            layout.addView(card)
        }

        // Добавляем кнопку РАССЧИТАТЬ в конец скролла
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

        val calculateButton = Button(this).apply {
            text = "РАССЧИТАТЬ"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#4FC3F7"))
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                width = dpToPx(220)
                height = dpToPx(55)
            }

            setOnClickListener {
                if (collectInputData()) {
                    // Сохраняем текущие данные как предыдущие для следующего месяца
                    saveCurrentDataAsPrevious()

                    val intent = Intent(this@InputActivity, ResultsActivity::class.java)
                    startActivity(intent)
                }
            }
        }

        buttonContainer.addView(calculateButton)
        layout.addView(buttonContainer)
    }

    private fun createInputCard(counterName: String, previousValue: Double): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
            setCardBackgroundColor(Color.WHITE)
            cardElevation = 3f
            radius = 8f

            val cardLayout = LinearLayout(this@InputActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(40, 30, 40, 30)
            }

            // Заголовок счетчика
            val title = TextView(this@InputActivity).apply {
                text = counterName
                setTextColor(Color.BLACK)
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomMargin = 20
                }
            }
            cardLayout.addView(title)

            // Поля ввода
            val inputLayout = LinearLayout(this@InputActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            // Поле "Предыдущее" (только для чтения)
            val prevInputLayout = LinearLayout(this@InputActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                    marginEnd = 10
                }
            }

            val prevLabel = TextView(this@InputActivity).apply {
                text = "Предыдущее (авто)"
                setTextColor(Color.GRAY)
                textSize = 14f
            }

            val prevInput = EditText(this@InputActivity).apply {
                setText(String.format("%.2f", previousValue))
                isEnabled = false // Запрещаем редактирование
                setBackgroundColor(Color.parseColor("#F5F5F5"))
                setTextColor(Color.DKGRAY)
            }

            prevInputLayout.addView(prevLabel)
            prevInputLayout.addView(prevInput)

            // Поле "Текущее"
            val currInputLayout = LinearLayout(this@InputActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 1f
                    marginStart = 10
                }
            }

            val currLabel = TextView(this@InputActivity).apply {
                text = "Текущее"
                setTextColor(Color.GRAY)
                textSize = 14f
            }

            val currInput = EditText(this@InputActivity).apply {
                hint = "0.0"
                setBackgroundResource(android.R.drawable.edit_text)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            }

            currInputLayout.addView(currLabel)
            currInputLayout.addView(currInput)

            inputLayout.addView(prevInputLayout)
            inputLayout.addView(currInputLayout)
            cardLayout.addView(inputLayout)
            addView(cardLayout)

            inputFields[counterName] = Pair(prevInput, currInput)
        }
    }

    private fun collectInputData(): Boolean {
        try {
            val apartmentNumber = AppData.currentApartmentNumber
            val inputData = mutableMapOf<String, Pair<Double, Double>>()

            inputFields.forEach { (counterName, fields) ->
                val prevText = fields.first.text.toString().trim()
                var currText = fields.second.text.toString().trim()

                // ЗАМЕНЯЕМ ЗАПЯТЫЕ НА ТОЧКИ
                currText = currText.replace(',', '.')

                if (currText.isEmpty()) {
                    Toast.makeText(this, "Заполните текущее значение для '$counterName'", Toast.LENGTH_SHORT).show()
                    return false
                }

                val prevValue = prevText.replace(',', '.').toDouble()
                val currValue = currText.toDouble()

                if (currValue < prevValue) {
                    Toast.makeText(this, "Текущее значение не может быть меньше предыдущего для '$counterName'", Toast.LENGTH_SHORT).show()
                    return false
                }

                inputData[counterName] = Pair(prevValue, currValue)
            }

            // Сохраняем данные для расчета
            AppData.inputData.clear()
            AppData.inputData.putAll(inputData)

            return true

        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Пожалуйста, введите корректные числа (можно использовать точку или запятую)", Toast.LENGTH_SHORT).show()
            return false
        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка: ${e.message}", Toast.LENGTH_SHORT).show()
            return false
        }
    }

    private fun saveCurrentDataAsPrevious() {
        try {
            val apartmentNumber = AppData.currentApartmentNumber
            val previousData = mutableMapOf<String, Double>()

            inputFields.forEach { (counterName, fields) ->
                val currText = fields.second.text.toString().trim()
                if (currText.isNotEmpty()) {
                    previousData[counterName] = currText.toDouble()
                }
            }

            dataStorage.saveApartmentData(apartmentNumber, previousData)

        } catch (e: Exception) {
            // Игнорируем ошибки сохранения, чтобы не мешать основному расчету
        }
    }

    private fun getCountersForApartment(apartmentId: Int): List<String> {
        return when (apartmentId) {
            1 -> listOf("Электричество 1", "Электричество 2", "Электричество 3", "Холодная вода", "Горячая вода")
            2 -> listOf("Электричество 1", "Электричество 2", "Холодная вода", "Горячая вода")
            3 -> listOf("Электричество 1", "Электричество 2", "Холодная вода 1", "Холодная вода 2", "Горячая вода 1", "Горячая вода 2")
            else -> emptyList()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}