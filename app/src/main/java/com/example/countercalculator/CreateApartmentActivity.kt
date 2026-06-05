package com.example.countercalculator

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.content.res.ColorStateList
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CreateApartmentActivity : AppCompatActivity() {

    private lateinit var dataStorage: DataStorage
    private var editingApartmentId: Int = -1

    private lateinit var nameInput: EditText
    private lateinit var countersContainer: LinearLayout
    private lateinit var waterDisposalCheckbox: CheckBox
    private lateinit var waterDisposalTariffLayout: LinearLayout
    private lateinit var waterDisposalTariffInput: EditText
    private lateinit var fixedPaymentsContainer: LinearLayout
    private lateinit var previousReadingsSection: LinearLayout
    private lateinit var previousReadingsContainer: LinearLayout
    private lateinit var scrollLayout: LinearLayout

    private val counterRows = mutableListOf<CounterRow>()
    private val fixedPaymentRows = mutableListOf<FixedPaymentRow>()
    private val previousReadingFields = mutableMapOf<String, EditText>()

    data class CounterRow(
        val nameInput: EditText,
        val tariffInput: EditText,
        val isWaterCheckbox: CheckBox,
        var selectedIcon: String? = "📊"
    )

    private val icons = listOf("💡", "💧", "🔥", "🌡️", "📊")

    data class FixedPaymentRow(
        val nameInput: EditText,
        val amountInput: EditText
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_apartment)

        dataStorage = DataStorage(this)
        editingApartmentId = intent.getIntExtra("apartment_id", -1)

        setupToolbar()
        buildUI()

        if (editingApartmentId != -1) {
            loadExistingData()
        }
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.title = if (editingApartmentId == -1) "Новая квартира" else "Редактировать квартиру"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun buildUI() {
        scrollLayout = findViewById(R.id.create_layout)

        addSectionTitle("Название квартиры")
        nameInput = EditText(this).apply {
            hint = "Например: ул. Ленина 5, кв. 12"
            textSize = 16f
            applyThemeColors()
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(24) }
        }
        scrollLayout.addView(nameInput)

        addSectionTitle("Счётчики")
        countersContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        scrollLayout.addView(countersContainer)
        scrollLayout.addView(secondaryButton("+ Добавить счётчик") { addCounterRow() })

        addSectionTitle("Водоотведение")
        waterDisposalCheckbox = CheckBox(this).apply {
            text = "Рассчитывать водоотведение автоматически"
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
        }
        scrollLayout.addView(waterDisposalCheckbox)

        waterDisposalTariffLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(16) }
        }
        waterDisposalTariffLayout.addView(TextView(this).apply {
            text = "Тариф водоотведения (руб.):"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        waterDisposalTariffInput = EditText(this).apply {
            hint = "0.00"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            applyThemeColors()
            layoutParams = LinearLayout.LayoutParams(dp(100), LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        waterDisposalTariffLayout.addView(waterDisposalTariffInput)
        scrollLayout.addView(waterDisposalTariffLayout)

        waterDisposalCheckbox.setOnCheckedChangeListener { _, checked ->
            waterDisposalTariffLayout.visibility = if (checked) View.VISIBLE else View.GONE
        }

        addSectionTitle("Фиксированные платежи")
        fixedPaymentsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        scrollLayout.addView(fixedPaymentsContainer)
        scrollLayout.addView(secondaryButton("+ Добавить платёж") { addFixedPaymentRow() })

        previousReadingsSection = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        previousReadingsSection.addView(TextView(this).apply {
            text = "Предыдущие показания"
            setTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.text_primary))
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(20)
                bottomMargin = dp(12)
            }
        })
        previousReadingsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        previousReadingsSection.addView(previousReadingsContainer)
        scrollLayout.addView(previousReadingsSection)

        scrollLayout.addView(MaterialButton(this).apply {
            text = "СОХРАНИТЬ"
            setTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.white))
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@CreateApartmentActivity, R.color.light_blue))
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@CreateApartmentActivity, R.color.light_blue_stroke))
            strokeWidth = dp(2)
            cornerRadius = dp(12)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(64)
            ).apply {
                topMargin = dp(32)
                bottomMargin = dp(300)
            }
            setOnClickListener {
                VibrationHelper.vibrate(this@CreateApartmentActivity)
                saveApartment()
            }
        })
    }

    private fun addCounterRow(name: String = "", tariff: Double = 0.0, isWater: Boolean = false, icon: String = "📊") {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
            setCardBackgroundColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.card_background))
            cardElevation = 2f
            radius = 8f
        }

        val outer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }

        val topRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
        }
        val nameInput = EditText(this).apply {
            setText(name)
            hint = "Название счётчика"
            textSize = 14f
            applyThemeColors()
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(8) }
        }
        val deleteBtn = Button(this).apply {
            text = "×"
            textSize = 18f
            setTextColor(Color.RED)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
        }
        topRow.addView(nameInput)
        topRow.addView(deleteBtn)
        outer.addView(topRow)

        val bottomRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(8) }
        }
        bottomRow.addView(TextView(this).apply {
            text = "Тариф (руб.):"
            textSize = 13f
            setTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.text_secondary))
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8) }
        })
        val tariffInput = EditText(this).apply {
            setText(if (tariff > 0) String.format("%.2f", tariff) else "")
            hint = "0.00"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            applyThemeColors()
            layoutParams = LinearLayout.LayoutParams(dp(90), LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(16) }
        }
        val isWaterCheckbox = CheckBox(this).apply {
            text = "Вода"
            isChecked = isWater
            textSize = 13f
        }
        bottomRow.addView(tariffInput)
        bottomRow.addView(isWaterCheckbox)
        outer.addView(bottomRow)

        val row = CounterRow(nameInput, tariffInput, isWaterCheckbox, icon)
        counterRows.add(row)

        // Строка выбора иконки
        val iconRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(8) }
        }
        val iconButtons = mutableListOf<Button>()
        icons.forEach { emoji ->
            val btn = Button(this).apply {
                text = emoji
                textSize = 20f
                setBackgroundColor(
                    if (emoji == row.selectedIcon)
                        ContextCompat.getColor(this@CreateApartmentActivity, R.color.btn_edit_bg)
                    else android.graphics.Color.TRANSPARENT
                )
                layoutParams = LinearLayout.LayoutParams(dp(44), dp(44)).apply { marginEnd = dp(4) }
                setOnClickListener {
                    if (row.selectedIcon == emoji) {
                        row.selectedIcon = null
                        iconButtons.forEach { b -> b.setBackgroundColor(android.graphics.Color.TRANSPARENT) }
                    } else {
                        row.selectedIcon = emoji
                        iconButtons.forEach { b ->
                            b.setBackgroundColor(
                                if (b.text == emoji)
                                    ContextCompat.getColor(this@CreateApartmentActivity, R.color.btn_edit_bg)
                                else android.graphics.Color.TRANSPARENT
                            )
                        }
                    }
                }
            }
            iconButtons.add(btn)
            iconRow.addView(btn)
        }
        outer.addView(iconRow)

        card.addView(outer)
        countersContainer.addView(card)

        deleteBtn.setOnClickListener {
            countersContainer.removeView(card)
            counterRows.remove(row)
        }
    }

    private fun addFixedPaymentRow(name: String = "", amount: Double = 0.0) {
        val card = MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(8) }
            setCardBackgroundColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.card_background))
            cardElevation = 2f
            radius = 8f
        }

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = android.view.Gravity.CENTER_VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(12))
        }
        val nameInput = EditText(this).apply {
            setText(name)
            hint = "Мусор, Интернет..."
            textSize = 14f
            applyThemeColors()
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).apply { marginEnd = dp(8) }
        }
        val amountInput = EditText(this).apply {
            setText(if (amount > 0) String.format("%.2f", amount) else "")
            hint = "0.00"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
            applyThemeColors()
            layoutParams = LinearLayout.LayoutParams(dp(90), LinearLayout.LayoutParams.WRAP_CONTENT).apply { marginEnd = dp(8) }
        }
        val deleteBtn = Button(this).apply {
            text = "×"
            textSize = 18f
            setTextColor(Color.RED)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = LinearLayout.LayoutParams(dp(40), dp(40))
        }
        row.addView(nameInput)
        row.addView(amountInput)
        row.addView(deleteBtn)
        card.addView(row)
        fixedPaymentsContainer.addView(card)

        val paymentRow = FixedPaymentRow(nameInput, amountInput)
        fixedPaymentRows.add(paymentRow)
        deleteBtn.setOnClickListener {
            fixedPaymentsContainer.removeView(card)
            fixedPaymentRows.remove(paymentRow)
        }
    }

    private fun loadExistingData() {
        val config = dataStorage.loadApartmentConfig(editingApartmentId) ?: return
        nameInput.setText(config.name)

        config.counters.forEach { addCounterRow(it.name, it.tariff, it.isWater, it.icon ?: "📊") }

        if (config.hasWaterDisposal) {
            waterDisposalCheckbox.isChecked = true
            waterDisposalTariffInput.setText(String.format("%.2f", config.waterDisposalTariff))
        }

        config.fixedPayments.forEach { addFixedPaymentRow(it.name, it.amount) }

        val previousData = dataStorage.loadApartmentData(editingApartmentId)
        if (config.counters.isNotEmpty()) {
            previousReadingsSection.visibility = View.VISIBLE
            previousReadingsContainer.removeAllViews()
            previousReadingFields.clear()
            config.counters.forEach { counter ->
                val rowLayout = LinearLayout(this).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER_VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(8) }
                }
                rowLayout.addView(TextView(this).apply {
                    text = counter.name
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.text_primary))
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
                val field = EditText(this).apply {
                    setText(String.format("%.2f", previousData[counter.name] ?: 0.0))
                    inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
                    applyThemeColors()
                    layoutParams = LinearLayout.LayoutParams(dp(100), LinearLayout.LayoutParams.WRAP_CONTENT)
                }
                rowLayout.addView(field)
                previousReadingsContainer.addView(rowLayout)
                previousReadingFields[counter.name] = field
            }
        }
    }

    private fun saveApartment() {
        val name = nameInput.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "Введите название квартиры", Toast.LENGTH_SHORT).show()
            return
        }

        val counters = mutableListOf<CounterConfig>()
        for (row in counterRows) {
            val counterName = row.nameInput.text.toString().trim()
            if (counterName.isEmpty()) {
                Toast.makeText(this, "Введите название для каждого счётчика", Toast.LENGTH_SHORT).show()
                return
            }
            val tariff = row.tariffInput.text.toString().trim().replace(',', '.').toDoubleOrNull() ?: 0.0
            counters.add(CounterConfig(counterName, tariff, row.isWaterCheckbox.isChecked, row.selectedIcon))
        }

        val hasWaterDisposal = waterDisposalCheckbox.isChecked
        val waterTariff = if (hasWaterDisposal) {
            waterDisposalTariffInput.text.toString().trim().replace(',', '.').toDoubleOrNull() ?: 0.0
        } else 0.0

        val fixedPayments = fixedPaymentRows.mapNotNull { row ->
            val paymentName = row.nameInput.text.toString().trim()
            if (paymentName.isEmpty()) return@mapNotNull null
            val amount = row.amountInput.text.toString().trim().replace(',', '.').toDoubleOrNull() ?: 0.0
            FixedPaymentConfig(paymentName, amount)
        }

        val zeroTariffs = counters.filter { it.tariff == 0.0 }.map { it.name }
        if (zeroTariffs.isNotEmpty()) {
            val list = zeroTariffs.joinToString("\n") { "• $it" }
            AlertDialog.Builder(this)
                .setTitle("Тариф не указан")
                .setMessage("Вы не указали тариф для:\n$list\n\nРасчёт для этих счётчиков будет равен 0 руб.")
                .setNegativeButton("Вернуться", null)
                .setPositiveButton("Продолжить") { _, _ ->
                    doSave(name, counters, fixedPayments, waterTariff, hasWaterDisposal)
                }
                .show()
            return
        }

        doSave(name, counters, fixedPayments, waterTariff, hasWaterDisposal)
    }

    private fun addSectionTitle(text: String) {
        scrollLayout.addView(TextView(this).apply {
            this.text = text
            setTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.text_primary))
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(20)
                bottomMargin = dp(12)
            }
        })
    }

    private fun doSave(
        name: String,
        counters: List<CounterConfig>,
        fixedPayments: List<FixedPaymentConfig>,
        waterTariff: Double,
        hasWaterDisposal: Boolean
    ) {
        val id = if (editingApartmentId == -1) dataStorage.getNextApartmentId() else editingApartmentId
        dataStorage.saveApartmentConfig(ApartmentConfig(id, name, counters, fixedPayments, waterTariff, hasWaterDisposal))
        if (previousReadingFields.isNotEmpty()) {
            val readings = previousReadingFields.mapValues { (_, field) ->
                field.text.toString().trim().replace(',', '.').toDoubleOrNull() ?: 0.0
            }
            dataStorage.saveApartmentData(id, readings)
        }
        Toast.makeText(this, "Квартира сохранена!", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun secondaryButton(text: String, onClick: () -> Unit) = MaterialButton(this).apply {
        this.text = text
        setTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.btn_secondary_text))
        backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@CreateApartmentActivity, R.color.btn_secondary_bg))
        strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@CreateApartmentActivity, R.color.btn_secondary_text))
        strokeWidth = dp(1)
        cornerRadius = dp(8)
        textSize = 15f
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            dp(48)
        ).apply { bottomMargin = dp(8) }
        setOnClickListener { onClick() }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

    private fun EditText.applyThemeColors() {
        setTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.text_primary))
        setHintTextColor(ContextCompat.getColor(this@CreateApartmentActivity, R.color.text_secondary))
    }
}
