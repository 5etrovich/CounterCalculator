package com.example.countercalculator

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculatorViewModelTest {

    private lateinit var vm: CalculatorViewModel

    @Before
    fun setUp() {
        vm = CalculatorViewModel()
    }

    // --- Базовые расчёты счётчиков ---

    @Test
    fun `один счётчик - правильный расчёт суммы`() {
        val config = config(counters = listOf(counter("Электричество", tariff = 5.0)))
        val input = mapOf("Электричество" to (1000.0 to 1100.0))

        val result = vm.calculate(config, input)

        assertContains(result["Электричество"], "500.00 руб.")
    }

    @Test
    fun `нулевое потребление - сумма ноль`() {
        val config = config(counters = listOf(counter("Электричество", tariff = 5.0)))
        val input = mapOf("Электричество" to (1000.0 to 1000.0))

        val result = vm.calculate(config, input)

        assertContains(result["Электричество"], "0.00 руб.")
    }

    @Test
    fun `несколько счётчиков - все считаются`() {
        val config = config(counters = listOf(
            counter("Электричество", tariff = 5.0),
            counter("Холодная вода", tariff = 40.0, isWater = true),
            counter("Горячая вода", tariff = 200.0, isWater = true)
        ))
        val input = mapOf(
            "Электричество" to (1000.0 to 1100.0),  // 100 кВт * 5 = 500
            "Холодная вода" to (50.0 to 53.0),       // 3 м³ * 40 = 120
            "Горячая вода" to (30.0 to 31.5)         // 1.5 м³ * 200 = 300
        )

        val result = vm.calculate(config, input)

        assertContains(result["Электричество"], "500.00 руб.")
        assertContains(result["Холодная вода"], "120.00 руб.")
        assertContains(result["Горячая вода"], "300.00 руб.")
    }

    // --- Итоговая сумма ---

    @Test
    fun `итого считается правильно`() {
        val config = config(counters = listOf(
            counter("Электричество", tariff = 5.0),
            counter("Холодная вода", tariff = 40.0)
        ))
        val input = mapOf(
            "Электричество" to (0.0 to 100.0),  // 500 руб
            "Холодная вода" to (0.0 to 3.0)     // 120 руб
        )

        val result = vm.calculate(config, input)

        assertEquals("620.00 руб.", result["ИТОГО"])
    }

    // --- Водоотведение ---

    @Test
    fun `водоотведение считается как сумма воды умножить на тариф`() {
        val config = config(
            counters = listOf(
                counter("Холодная вода", tariff = 40.0, isWater = true),
                counter("Горячая вода", tariff = 200.0, isWater = true)
            ),
            hasWaterDisposal = true,
            waterDisposalTariff = 30.0
        )
        val input = mapOf(
            "Холодная вода" to (0.0 to 3.0),   // потребление 3 м³
            "Горячая вода" to (0.0 to 2.0)     // потребление 2 м³
        )

        val result = vm.calculate(config, input)

        // (3 + 2) * 30 = 150
        assertContains(result["Водоотведение"], "150.00 руб.")
    }

    @Test
    fun `водоотведение не включает не-водяные счётчики`() {
        val config = config(
            counters = listOf(
                counter("Электричество", tariff = 5.0, isWater = false),
                counter("Холодная вода", tariff = 40.0, isWater = true)
            ),
            hasWaterDisposal = true,
            waterDisposalTariff = 30.0
        )
        val input = mapOf(
            "Электричество" to (0.0 to 100.0),  // не вода, не считается
            "Холодная вода" to (0.0 to 4.0)     // потребление 4 м³
        )

        val result = vm.calculate(config, input)

        // Только 4 * 30 = 120, без электричества
        assertContains(result["Водоотведение"], "120.00 руб.")
    }

    @Test
    fun `без водоотведения - поля нет в результатах`() {
        val config = config(
            counters = listOf(counter("Холодная вода", tariff = 40.0, isWater = true)),
            hasWaterDisposal = false
        )
        val input = mapOf("Холодная вода" to (0.0 to 3.0))

        val result = vm.calculate(config, input)

        assertNull(result["Водоотведение"])
    }

    @Test
    fun `водоотведение входит в итого`() {
        val config = config(
            counters = listOf(counter("Холодная вода", tariff = 40.0, isWater = true)),
            hasWaterDisposal = true,
            waterDisposalTariff = 30.0
        )
        val input = mapOf("Холодная вода" to (0.0 to 2.0))

        val result = vm.calculate(config, input)

        // вода: 2 * 40 = 80, водоотведение: 2 * 30 = 60, итого: 140
        assertEquals("140.00 руб.", result["ИТОГО"])
    }

    // --- Фиксированные платежи ---

    @Test
    fun `фиксированные платежи добавляются в итог`() {
        val config = config(
            counters = emptyList(),
            fixedPayments = listOf(
                FixedPaymentConfig("Мусор", 100.0),
                FixedPaymentConfig("Интернет", 450.0)
            )
        )
        val input = emptyMap<String, Pair<Double, Double>>()

        val result = vm.calculate(config, input)

        assertContains(result["Мусор"], "100.00 руб.")
        assertContains(result["Интернет"], "450.00 руб.")
        assertEquals("550.00 руб.", result["ИТОГО"])
    }

    @Test
    fun `всё вместе - счётчики, водоотведение, фиксированные`() {
        val config = config(
            counters = listOf(
                counter("Электричество", tariff = 5.0),
                counter("Холодная вода", tariff = 40.0, isWater = true)
            ),
            fixedPayments = listOf(FixedPaymentConfig("Мусор", 100.0)),
            hasWaterDisposal = true,
            waterDisposalTariff = 30.0
        )
        val input = mapOf(
            "Электричество" to (0.0 to 100.0),  // 500
            "Холодная вода" to (0.0 to 3.0)     // вода: 120, водоотведение: 90
        )

        val result = vm.calculate(config, input)

        // 500 + 120 + 90 + 100 = 810
        assertEquals("810.00 руб.", result["ИТОГО"])
    }

    // --- Граничные случаи ---

    @Test
    fun `дробное потребление считается точно`() {
        val config = config(counters = listOf(counter("Горячая вода", tariff = 200.0)))
        val input = mapOf("Горячая вода" to (10.0 to 11.75))  // 1.75 м³

        val result = vm.calculate(config, input)

        assertContains(result["Горячая вода"], "350.00 руб.")
    }

    @Test
    fun `нулевой тариф - сумма ноль`() {
        val config = config(counters = listOf(counter("Электричество", tariff = 0.0)))
        val input = mapOf("Электричество" to (0.0 to 100.0))

        val result = vm.calculate(config, input)

        assertContains(result["Электричество"], "0.00 руб.")
        assertEquals("0.00 руб.", result["ИТОГО"])
    }

    @Test
    fun `счётчик отсутствует во входных данных - не падает`() {
        val config = config(counters = listOf(counter("Электричество", tariff = 5.0)))
        val input = emptyMap<String, Pair<Double, Double>>()

        val result = vm.calculate(config, input)

        assertNull(result["Электричество"])
        assertEquals("0.00 руб.", result["ИТОГО"])
    }

    @Test
    fun `пустая конфигурация - итого ноль`() {
        val config = config(counters = emptyList())
        val input = emptyMap<String, Pair<Double, Double>>()

        val result = vm.calculate(config, input)

        assertEquals("0.00 руб.", result["ИТОГО"])
    }

    // --- Вспомогательные функции ---

    private fun counter(name: String, tariff: Double, isWater: Boolean = false) =
        CounterConfig(name, tariff, isWater)

    private fun config(
        counters: List<CounterConfig> = emptyList(),
        fixedPayments: List<FixedPaymentConfig> = emptyList(),
        hasWaterDisposal: Boolean = false,
        waterDisposalTariff: Double = 0.0
    ) = ApartmentConfig(
        id = 1,
        name = "Тест",
        counters = counters,
        fixedPayments = fixedPayments,
        waterDisposalTariff = waterDisposalTariff,
        hasWaterDisposal = hasWaterDisposal
    )

    private fun assertContains(actual: String?, expected: String) {
        assertNotNull("Результат не должен быть null", actual)
        assertTrue("Ожидалось '$expected' в '$actual'", actual!!.contains(expected))
    }

    private infix fun Double.to(other: Double) = Pair(this, other)
}
