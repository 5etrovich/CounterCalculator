package com.example.countercalculator

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.res.ColorStateList
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class ApartmentsActivity : AppCompatActivity() {

    private lateinit var dataStorage: DataStorage
    private lateinit var apartmentsContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apartments)

        dataStorage = DataStorage(this)
        setupToolbar()
        apartmentsContainer = findViewById(R.id.apartments_container)

        findViewById<Button>(R.id.btnAddApartment).setOnClickListener {
            startActivity(Intent(this, CreateApartmentActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadApartments()
    }

    private fun setupToolbar() {
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar.title = "Мои квартиры"
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadApartments() {
        apartmentsContainer.removeAllViews()
        val apartments = dataStorage.loadAllApartments()

        if (apartments.isEmpty()) {
            apartmentsContainer.addView(TextView(this).apply {
                text = "Нет квартир. Нажмите «+ Добавить квартиру»."
                setTextColor(ContextCompat.getColor(this@ApartmentsActivity, R.color.text_secondary))
                textSize = 16f
                gravity = android.view.Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { topMargin = 40 }
            })
        }

        apartments.forEach { config ->
            apartmentsContainer.addView(createApartmentCard(config))
        }
    }

    private fun createApartmentCard(config: ApartmentConfig): MaterialCardView {
        return MaterialCardView(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { bottomMargin = dpToPx(12) }
            setCardBackgroundColor(ContextCompat.getColor(this@ApartmentsActivity, R.color.card_background))
            cardElevation = 4f
            radius = 12f
            strokeWidth = dpToPx(1)
            strokeColor = ContextCompat.getColor(this@ApartmentsActivity, R.color.card_stroke)

            val column = LinearLayout(this@ApartmentsActivity).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(12))
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            column.addView(TextView(this@ApartmentsActivity).apply {
                text = "🏠 ${config.name}"
                setTextColor(ContextCompat.getColor(this@ApartmentsActivity, R.color.text_primary))
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { bottomMargin = dpToPx(12) }
            })

            column.addView(MaterialButton(this@ApartmentsActivity).apply {
                text = "Начать расчёт  →"
                setTextColor(ContextCompat.getColor(this@ApartmentsActivity, R.color.white))
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ApartmentsActivity, R.color.light_blue))
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@ApartmentsActivity, R.color.light_blue_stroke))
                strokeWidth = dpToPx(2)
                cornerRadius = dpToPx(10)
                textSize = 16f
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    dpToPx(56)
                ).apply { bottomMargin = dpToPx(10) }
                setOnClickListener {
                    AppData.currentApartmentConfig = config
                    startActivity(Intent(this@ApartmentsActivity, InputActivity::class.java))
                }
            })

            val secondaryRow = LinearLayout(this@ApartmentsActivity).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            secondaryRow.addView(MaterialButton(this@ApartmentsActivity).apply {
                text = "Изменить"
                setTextColor(ContextCompat.getColor(this@ApartmentsActivity, R.color.btn_edit_text))
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ApartmentsActivity, R.color.btn_edit_bg))
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@ApartmentsActivity, R.color.btn_edit_text))
                strokeWidth = dpToPx(1)
                cornerRadius = dpToPx(8)
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, dpToPx(40), 1f).apply {
                    marginEnd = dpToPx(8)
                }
                setOnClickListener {
                    val intent = Intent(this@ApartmentsActivity, CreateApartmentActivity::class.java)
                    intent.putExtra("apartment_id", config.id)
                    startActivity(intent)
                }
            })

            secondaryRow.addView(MaterialButton(this@ApartmentsActivity).apply {
                text = "Удалить"
                setTextColor(ContextCompat.getColor(this@ApartmentsActivity, R.color.btn_delete_text))
                backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(this@ApartmentsActivity, R.color.btn_delete_bg))
                strokeColor = ColorStateList.valueOf(ContextCompat.getColor(this@ApartmentsActivity, R.color.btn_delete_text))
                strokeWidth = dpToPx(1)
                cornerRadius = dpToPx(8)
                textSize = 13f
                layoutParams = LinearLayout.LayoutParams(0, dpToPx(40), 1f)
                setOnClickListener {
                    AlertDialog.Builder(this@ApartmentsActivity)
                        .setTitle("Удалить квартиру?")
                        .setMessage("«${config.name}» будет удалена вместе со всеми данными.")
                        .setPositiveButton("Удалить") { _, _ ->
                            dataStorage.deleteApartment(config.id)
                            loadApartments()
                        }
                        .setNegativeButton("Отмена", null)
                        .show()
                }
            })

            column.addView(secondaryRow)
            addView(column)
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density + 0.5f).toInt()
    }
}
