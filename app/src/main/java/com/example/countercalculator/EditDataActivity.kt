package com.example.countercalculator

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import com.google.android.material.card.MaterialCardView

class EditDataActivity : AppCompatActivity() {

    private lateinit var dataStorage: DataStorage
    private var selectedApartmentId = 1
    private val inputFields = mutableMapOf<String, EditText>()
    private lateinit var contentLayout: LinearLayout // ДЛЯ КОНТЕНТА
    private lateinit var saveButton: Button // ДЛЯ КНОПКИ

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_data)

        dataStorage = DataStorage(this)

        setupToolbar()
        setupApartmentSelector() // ПЕРВЫМ - селектор квартиры
        setupLayout()
        setupSaveButton() // ВТОРЫМ - кнопка сохранения
        loadDataForApartment(selectedApartmentId) // ТРЕТЬИМ - загрузка данных
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Редактирование данных"
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupLayout() {
        // Находим основной layout и создаем контейнер для контента
        val mainLayout = findViewById<LinearLayout>(R.id.edit_layout)

        // Создаем контейнер для изменяемого контента (поля ввода)
        contentLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        mainLayout.addView(contentLayout)
    }

    private fun setupApartmentSelector() {
        val mainLayout = findViewById<LinearLayout>(R.id.edit_layout)

        // Селектор квартиры
        val apartmentSelectorLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 30
            }
        }

        val apartmentLabel = TextView(this).apply {
            text = "Выберите квартиру для редактирования:"
            setTextColor(android.graphics.Color.BLACK)
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
        }

        val apartmentSpinner = Spinner(this).apply {
            val apartments = arrayOf("Вертолётчиков", "Проспект победы", "Вертолётная")
            val adapter = ArrayAdapter(this@EditDataActivity, android.R.layout.simple_spinner_item, apartments)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            this.adapter = adapter

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                    selectedApartmentId = position + 1
                    loadDataForApartment(selectedApartmentId)
                }
                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
        }

        apartmentSelectorLayout.addView(apartmentLabel)
        apartmentSelectorLayout.addView(apartmentSpinner)
        mainLayout.addView(apartmentSelectorLayout)
    }

    private fun setupSaveButton() {
        val mainLayout = findViewById<LinearLayout>(R.id.edit_layout)

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

        saveButton = Button(this).apply {
            text = "💾 СОХРАНИТЬ ДАННЫЕ"
            setTextColor(android.graphics.Color.WHITE)
            setBackgroundColor(android.graphics.Color.parseColor("#4FC3F7"))
            textSize = 16f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                width = dpToPx(250)
                height = dpToPx(55)
            }

            setOnClickListener {
                saveData()
            }
        }

        buttonContainer.addView(saveButton)
        mainLayout.addView(buttonContainer)
    }

    private fun loadDataForApartment(apartmentId: Int) {
        // Очищаем только контент (поля ввода), а не весь layout
        contentLayout.removeAllViews()
        inputFields.clear()

        val loadedPreviousData = dataStorage.loadApartmentData(apartmentId)
        val loadedTariffs = dataStorage.loadTariffs(apartmentId)
        val loadedFixedPayments = dataStorage.loadFixedPayments(apartmentId)

        val counters = when (apartmentId) {
            1 -> listOf("Электричество 1", "Электричество 2", "Электричество 3", "Холодная вода", "Горячая вода")
            2 -> listOf("Электричество 1", "Электричество 2", "Холодная вода", "Горячая вода")
            3 -> listOf("Электричество 1", "Электричество 2", "Холодная вода 1", "Холодная вода 2", "Горячая вода 1", "Горячая вода 2")
            else -> emptyList()
        }

        // Заголовок раздела
        val title = TextView(this).apply {
            text = "Предыдущие показания счетчиков:"
            setTextColor(android.graphics.Color.BLACK)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 20
            }
        }
        contentLayout.addView(title)

        // Создаем поля для каждого счетчика
        counters.forEach { counterName ->
            val card = createInputCard(counterName, loadedPreviousData[counterName] ?: 0.0)
            contentLayout.addView(card)
        }

        // Добавляем раздел тарифов
        val tariffsTitle = TextView(this).apply {
            text = "Тарифы:"
            setTextColor(android.graphics.Color.BLACK)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 30
                bottomMargin = 20
            }
        }
        contentLayout.addView(tariffsTitle)

        loadedTariffs.forEach { (service, tariff) ->
            val card = createTariffCard(service, tariff)
            contentLayout.addView(card)
        }

        // Добавляем раздел фиксированных платежей
        val fixedTitle = TextView(this).apply {
            text = "Фиксированные платежи:"
            setTextColor(android.graphics.Color.BLACK)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 30
                bottomMargin = 20
            }
        }
        contentLayout.addView(fixedTitle)

        loadedFixedPayments.forEach { (service, amount) ->
            val card = createFixedPaymentCard(service, amount)
            contentLayout.addView(card)
        }
    }

    private fun createInputCard(counterName: String, value: Double): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 15
            }
            setCardBackgroundColor(android.graphics.Color.WHITE)
            cardElevation = 2f
            radius = 8f

            val cardLayout = LinearLayout(this@EditDataActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(25, 20, 25, 20)
            }

            val title = TextView(this@EditDataActivity).apply {
                text = counterName
                setTextColor(android.graphics.Color.BLACK)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.6f
                }
            }

            val input = EditText(this@EditDataActivity).apply {
                setText(String.format("%.2f", value))
                hint = "0.00"
                setBackgroundResource(android.R.drawable.edit_text)
                inputType = android.text.InputType.TYPE_CLASS_NUMBER or
                        android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.4f
                }
            }

            cardLayout.addView(title)
            cardLayout.addView(input)
            addView(cardLayout)

            inputFields["prev_$counterName"] = input
        }
    }

    private fun createTariffCard(service: String, tariff: Double): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
            setCardBackgroundColor(android.graphics.Color.parseColor("#F8FBFE"))
            cardElevation = 1f
            radius = 6f

            val cardLayout = LinearLayout(this@EditDataActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(20, 15, 20, 15)
            }

            val title = TextView(this@EditDataActivity).apply {
                text = service
                setTextColor(android.graphics.Color.BLACK)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.6f
                }
            }

            val input = EditText(this@EditDataActivity).apply {
                setText(String.format("%.2f", tariff))
                hint = "0.00"
                setBackgroundResource(android.R.drawable.edit_text)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.4f
                }
            }

            cardLayout.addView(title)
            cardLayout.addView(input)
            addView(cardLayout)

            inputFields["tariff_$service"] = input
        }
    }

    private fun createFixedPaymentCard(service: String, amount: Double): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = 10
            }
            setCardBackgroundColor(android.graphics.Color.parseColor("#F8FBFE"))
            cardElevation = 1f
            radius = 6f

            val cardLayout = LinearLayout(this@EditDataActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPadding(20, 15, 20, 15)
            }

            val title = TextView(this@EditDataActivity).apply {
                text = service
                setTextColor(android.graphics.Color.BLACK)
                textSize = 14f
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.6f
                }
            }

            val input = EditText(this@EditDataActivity).apply {
                setText(String.format("%.2f", amount))
                hint = "0.00"
                setBackgroundResource(android.R.drawable.edit_text)
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    weight = 0.4f
                }
            }

            cardLayout.addView(title)
            cardLayout.addView(input)
            addView(cardLayout)

            inputFields["fixed_$service"] = input
        }
    }

    private fun saveData() {
        try {
            // Сохраняем предыдущие показания
            val previousDataMap = mutableMapOf<String, Double>()
            inputFields.forEach { (key, editText) ->
                if (key.startsWith("prev_")) {
                    val counterName = key.removePrefix("prev_")
                    var text = editText.text.toString().trim()
                    text = text.replace(',', '.') // ЗАМЕНЯЕМ ЗАПЯТЫЕ НА ТОЧКИ
                    val value = if (text.isNotEmpty()) text.toDoubleOrNull() ?: 0.0 else 0.0
                    previousDataMap[counterName] = value
                }
            }
            dataStorage.saveApartmentData(selectedApartmentId, previousDataMap)

            // Сохраняем тарифы
            val tariffsMap = mutableMapOf<String, Double>()
            inputFields.forEach { (key, editText) ->
                if (key.startsWith("tariff_")) {
                    val service = key.removePrefix("tariff_")
                    var text = editText.text.toString().trim()
                    text = text.replace(',', '.') // ЗАМЕНЯЕМ ЗАПЯТЫЕ НА ТОЧКИ
                    val value = if (text.isNotEmpty()) text.toDoubleOrNull() ?: 0.0 else 0.0
                    tariffsMap[service] = value
                }
            }
            dataStorage.saveTariffs(selectedApartmentId, tariffsMap)

            // Сохраняем фиксированные платежи
            val fixedPaymentsMap = mutableMapOf<String, Double>()
            inputFields.forEach { (key, editText) ->
                if (key.startsWith("fixed_")) {
                    val service = key.removePrefix("fixed_")
                    var text = editText.text.toString().trim()
                    text = text.replace(',', '.') // ЗАМЕНЯЕМ ЗАПЯТЫЕ НА ТОЧКИ
                    val value = if (text.isNotEmpty()) text.toDoubleOrNull() ?: 0.0 else 0.0
                    fixedPaymentsMap[service] = value
                }
            }
            dataStorage.saveFixedPayments(selectedApartmentId, fixedPaymentsMap)

            Toast.makeText(this, "Данные успешно сохранены!", Toast.LENGTH_LONG).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Ошибка при сохранении: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun dpToPx(dp: Int): Int {
        val scale = resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}