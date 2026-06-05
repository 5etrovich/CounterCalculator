package com.example.countercalculator

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class HistoryActivity : AppCompatActivity() {

    private lateinit var dataStorage: DataStorage
    private lateinit var container: LinearLayout
    private lateinit var searchInput: EditText
    private var allRecords: List<HistoryRecord> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        dataStorage = DataStorage(this)
        setupToolbar()
        container = findViewById(R.id.history_container)
        searchInput = findViewById(R.id.search_input)
        searchInput.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        searchInput.setHintTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterRecords(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        loadHistory()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.title = "История расчётов"
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.inflateMenu(R.menu.menu_history)
        toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_clear_history) {
                AlertDialog.Builder(this)
                    .setTitle("Очистить историю?")
                    .setMessage("Все записи будут удалены.")
                    .setPositiveButton("Очистить") { _, _ ->
                        dataStorage.clearHistory()
                        allRecords = emptyList()
                        filterRecords("")
                        searchInput.setText("")
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
                true
            } else false
        }
    }

    private fun loadHistory() {
        allRecords = dataStorage.loadHistory()
        filterRecords(searchInput.text.toString())
    }

    private fun filterRecords(query: String) {
        container.removeAllViews()
        val filtered = if (query.isBlank()) allRecords
        else allRecords.filter {
            it.apartmentName.contains(query, ignoreCase = true) ||
            it.date.contains(query, ignoreCase = true)
        }

        if (filtered.isEmpty()) {
            container.addView(TextView(this).apply {
                text = if (query.isBlank()) "История пуста. Сделайте первый расчёт!"
                       else "Ничего не найдено по запросу «$query»"
                setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.text_secondary))
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = dp(40) }
            })
            return
        }

        filtered.forEach { record -> container.addView(createRecordCard(record)) }
    }

    private fun createRecordCard(record: HistoryRecord): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dp(12) }
            setCardBackgroundColor(ContextCompat.getColor(this@HistoryActivity, R.color.card_background))
            cardElevation = 4f
            radius = 12f
            strokeWidth = dp(1)
            strokeColor = ContextCompat.getColor(this@HistoryActivity, R.color.card_stroke)

            val col = LinearLayout(this@HistoryActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(16), dp(16), dp(16), dp(12))
            }

            // Заголовок: квартира + дата
            val headerRow = LinearLayout(this@HistoryActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            headerRow.addView(TextView(this@HistoryActivity).apply {
                text = "🏠 ${record.apartmentName}"
                setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.text_primary))
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            headerRow.addView(TextView(this@HistoryActivity).apply {
                text = record.date
                setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.text_secondary))
                textSize = 13f
            })
            col.addView(headerRow)

            // Детали (свёрнуто по умолчанию)
            val detailsLayout = LinearLayout(this@HistoryActivity).apply {
                orientation = LinearLayout.VERTICAL
                visibility = android.view.View.GONE
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dp(8) }
            }
            record.details.forEach { detail ->
                detailsLayout.addView(TextView(this@HistoryActivity).apply {
                    text = if (detail.subtitle.isNotEmpty())
                        "${detail.name} (${detail.subtitle}): ${detail.value}"
                    else
                        "${detail.name}: ${detail.value}"
                    setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.text_secondary))
                    textSize = 13f
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { bottomMargin = dp(2) }
                })
            }
            col.addView(detailsLayout)

            // Итого + кнопка раскрыть
            val footerRow = LinearLayout(this@HistoryActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }
            footerRow.addView(TextView(this@HistoryActivity).apply {
                text = "ИТОГО: ${record.total}"
                setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.light_blue))
                textSize = 16f
                setTypeface(null, android.graphics.Typeface.BOLD)
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })

            val expandBtn = MaterialButton(this@HistoryActivity).apply {
                text = "Подробнее"
                setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.btn_secondary_text))
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@HistoryActivity, R.color.btn_secondary_bg))
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@HistoryActivity, R.color.btn_secondary_text))
                strokeWidth = dp(1)
                cornerRadius = dp(8)
                textSize = 12f
                insetTop = 0
                insetBottom = 0
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(36))
                setOnClickListener {
                    if (detailsLayout.visibility == android.view.View.GONE) {
                        detailsLayout.visibility = android.view.View.VISIBLE
                        text = "Свернуть"
                    } else {
                        detailsLayout.visibility = android.view.View.GONE
                        text = "Подробнее"
                    }
                }
            }
            footerRow.addView(expandBtn)

            val deleteBtn = MaterialButton(this@HistoryActivity).apply {
                text = "✕ Удалить"
                setTextColor(ContextCompat.getColor(this@HistoryActivity, R.color.btn_delete_text))
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@HistoryActivity, R.color.btn_delete_bg))
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@HistoryActivity, R.color.btn_delete_text))
                strokeWidth = dp(1)
                cornerRadius = dp(8)
                textSize = 12f
                insetTop = 0
                insetBottom = 0
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, dp(36)).apply { marginStart = dp(6) }
                setOnClickListener {
                    VibrationHelper.vibrate(this@HistoryActivity)
                    AlertDialog.Builder(this@HistoryActivity)
                        .setTitle("Удалить запись?")
                        .setMessage("Запись от ${record.date} будет удалена.")
                        .setPositiveButton("Удалить") { _, _ ->
                            dataStorage.deleteHistoryRecord(record.id)
                            allRecords = dataStorage.loadHistory()
                            filterRecords(searchInput.text.toString())
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                }
            }
            footerRow.addView(deleteBtn)

            col.addView(footerRow)
            addView(col)
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density + 0.5f).toInt()
}
