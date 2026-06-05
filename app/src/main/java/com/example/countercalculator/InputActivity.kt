package com.example.countercalculator

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.ColorStateList
import android.widget.*
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class InputActivity : AppCompatActivity() {

    private lateinit var dataStorage: DataStorage
    private val inputFields = mutableMapOf<String, Pair<EditText, EditText>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input)

        val config = AppData.currentApartmentConfig
        if (config == null) {
            finish()
            return
        }

        dataStorage = DataStorage(this)
        setupToolbar(config.name)
        setupInputs(config)
    }

    private fun setupToolbar(apartmentName: String) {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Ввод данных: $apartmentName"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupInputs(config: ApartmentConfig) {
        val layout = findViewById<LinearLayout>(R.id.input_layout)
        layout.removeAllViews()

        val previousData = dataStorage.loadApartmentData(config.id)

        val instruction = TextView(this).apply {
            text = "Заполните текущие показания. Предыдущие загружены автоматически.\nМожно использовать точку или запятую как разделитель."
            setTextColor(ContextCompat.getColor(this@InputActivity, R.color.text_secondary))
            textSize = 14f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = 40 }
        }
        layout.addView(instruction)

        config.counters.forEach { counter ->
            val previousValue = previousData[counter.name] ?: 0.0
            layout.addView(createInputCard(counter.name, previousValue))
        }

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

        val calculateButton = MaterialButton(this).apply {
            text = "РАССЧИТАТЬ"
            setTextColor(ContextCompat.getColor(this@InputActivity, R.color.white))
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@InputActivity, R.color.light_blue))
            strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@InputActivity, R.color.light_blue_stroke))
            strokeWidth = dpToPx(2)
            cornerRadius = dpToPx(12)
            textSize = 18f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dpToPx(64)
            )
            setOnClickListener {
                VibrationHelper.vibrate(this@InputActivity)
                if (collectInputData()) {
                    saveCurrentDataAsPrevious(config.id)
                    startActivity(Intent(this@InputActivity, ResultsActivity::class.java))
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
            ).apply { bottomMargin = 20 }
            setCardBackgroundColor(ContextCompat.getColor(this@InputActivity, R.color.card_background))
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

            val title = TextView(this@InputActivity).apply {
                text = counterName
                setTextColor(ContextCompat.getColor(this@InputActivity, R.color.text_primary))
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = 20 }
            }
            cardLayout.addView(title)

            val inputLayout = LinearLayout(this@InputActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            val prevInputLayout = LinearLayout(this@InputActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    weight = 1f
                    marginEnd = 10
                }
            }
            val prevLabel = TextView(this@InputActivity).apply {
                text = "Предыдущее (авто)"
                setTextColor(ContextCompat.getColor(this@InputActivity, R.color.text_secondary))
                textSize = 14f
            }
            val prevInput = EditText(this@InputActivity).apply {
                setText(String.format("%.2f", previousValue))
                isEnabled = false
                setBackgroundColor(ContextCompat.getColor(this@InputActivity, R.color.input_disabled_bg))
                setTextColor(ContextCompat.getColor(this@InputActivity, R.color.input_disabled_text))
            }
            prevInputLayout.addView(prevLabel)
            prevInputLayout.addView(prevInput)

            val currInputLayout = LinearLayout(this@InputActivity).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    weight = 1f
                    marginStart = 10
                }
            }
            val currLabel = TextView(this@InputActivity).apply {
                text = "Текущее"
                setTextColor(ContextCompat.getColor(this@InputActivity, R.color.text_secondary))
                textSize = 14f
            }
            val currInput = EditText(this@InputActivity).apply {
                hint = "0.0"
                setTextColor(ContextCompat.getColor(this@InputActivity, R.color.text_primary))
                setHintTextColor(ContextCompat.getColor(this@InputActivity, R.color.text_secondary))
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
            val inputData = mutableMapOf<String, Pair<Double, Double>>()

            inputFields.forEach { (counterName, fields) ->
                val prevText = fields.first.text.toString().trim().replace(',', '.')
                val currText = fields.second.text.toString().trim().replace(',', '.')

                if (currText.isEmpty()) {
                    Toast.makeText(this, "Заполните текущее значение для '$counterName'", Toast.LENGTH_SHORT).show()
                    return false
                }

                val prevValue = prevText.toDouble()
                val currValue = currText.toDouble()

                if (currValue < prevValue) {
                    Toast.makeText(this, "Текущее значение не может быть меньше предыдущего для '$counterName'", Toast.LENGTH_SHORT).show()
                    return false
                }

                inputData[counterName] = Pair(prevValue, currValue)
            }

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

    private fun saveCurrentDataAsPrevious(apartmentId: Int) {
        try {
            val previousData = mutableMapOf<String, Double>()
            inputFields.forEach { (counterName, fields) ->
                val currText = fields.second.text.toString().trim().replace(',', '.')
                if (currText.isNotEmpty()) {
                    previousData[counterName] = currText.toDoubleOrNull() ?: 0.0
                }
            }
            dataStorage.saveApartmentData(apartmentId, previousData)
        } catch (e: Exception) {
            // не мешаем основному расчёту
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }
}
