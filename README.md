# CounterCalculator — Калькулятор коммунальных платежей

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-0081CB?style=for-the-badge&logo=material-design&logoColor=white)

Приложение для Android, которое помогает считать коммунальные платежи по показаниям счётчиков. Поддерживает несколько квартир, запоминает предыдущие показания и автоматически рассчитывает водоотведение.

---

*Android app for calculating utility bills based on meter readings. Supports multiple apartments, remembers previous readings, and auto-calculates wastewater disposal.*

---

## Возможности

- **Несколько квартир** — добавьте любое количество адресов
- **Автозапоминание показаний** — после расчёта текущие показания становятся предыдущими на следующий месяц
- **Водоотведение** — рассчитывается автоматически: сумма водопотребления × тариф (по ПП РФ №354)
- **Фиксированные платежи** — мусор, интернет и другие постоянные расходы
- **Любые тарифы** — настраиваются индивидуально для каждой квартиры
- **Работает офлайн** — все данные хранятся только на устройстве, интернет не нужен
- **Без разрешений** — приложение не запрашивает никаких разрешений

## Установка

### Скачать APK
Перейдите в раздел [Releases](https://github.com/5etrovich/CounterCalculator/releases) и скачайте последний `app-release.apk`.

### Собрать самостоятельно
```bash
git clone https://github.com/5etrovich/CounterCalculator.git
cd CounterCalculator
./gradlew assembleRelease
```

Требования: Android Studio, Android SDK 21+, Kotlin 1.9+

## Как пользоваться

1. **Первый запуск** — добавьте квартиру: укажите название, счётчики с тарифами и фиксированные платежи
2. **Каждый месяц** — выберите квартиру → введите текущие показания → нажмите «Начать расчёт»
3. Приложение покажет детализацию по каждому счётчику и итоговую сумму
4. Показания автоматически сохранятся для следующего месяца

## Технические детали

| | |
|---|---|
| Язык | Kotlin |
| Минимальный Android | 5.0 (API 21) |
| Хранение данных | SharedPreferences (локально) |
| Разрешения | Нет |
| Интернет | Не требуется |

## Конфиденциальность

Приложение не собирает никаких данных, не требует интернета и не отправляет ничего за пределы устройства.

## Разработчик

**5etrovich** — [GitHub](https://github.com/5etrovich)

Если приложение оказалось полезным — поставьте звёздочку ⭐
