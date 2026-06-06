# CounterCalculator — Калькулятор коммунальных платежей

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Material Design](https://img.shields.io/badge/Material%20Design-0081CB?style=for-the-badge&logo=material-design&logoColor=white)
![RuStore](https://img.shields.io/badge/RuStore-5865F2?style=for-the-badge&logo=data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAyNCAyNCI+PHBhdGggZmlsbD0id2hpdGUiIGQ9Ik0xMiAyQzYuNDggMiAyIDYuNDggMiAxMnM0LjQ4IDEwIDEwIDEwIDEwLTQuNDggMTAtMTBTMTcuNTIgMiAxMiAyek0xMCA3bDUgNS01IDV6Ii8+PC9zdmc+&logoColor=white)

Приложение для Android, которое помогает считать коммунальные платежи по показаниям счётчиков. Поддерживает несколько квартир, запоминает предыдущие показания и автоматически рассчитывает водоотведение.

---

*Android app for calculating utility bills based on meter readings. Supports multiple apartments, remembers previous readings, and auto-calculates wastewater disposal.*

---

## Возможности

- **Несколько квартир** — добавьте любое количество адресов
- **Автозапоминание показаний** — после расчёта текущие показания становятся предыдущими на следующий месяц
- **Водоотведение** — рассчитывается автоматически: сумма водопотребления × тариф (по ПП РФ №354)
- **Фиксированные платежи** — мусор, интернет и другие постоянные расходы
- **Иконки счётчиков** — выбираются при создании (💡 💧 🔥 🌡️ 📊)
- **Любые тарифы** — настраиваются индивидуально для каждой квартиры
- **История расчётов** — просматривайте прошлые расчёты с поиском по квартире и дате
- **Детализация «Было → Стало»** — в результатах видны предыдущие и текущие показания
- **Поделиться результатами** — отправьте итог расчёта в любой мессенджер
- **Тёмная тема** — следует системным настройкам
- **Умное округление** — 500 ₽ вместо 500.00 ₽
- **Работает офлайн** — все данные хранятся только на устройстве, интернет не нужен
- **Без разрешений** — приложение не запрашивает никаких разрешений

## Установка

### RuStore
**[⬇ Скачать в RuStore](https://apps.rustore.ru/app/com.example.countercalculator)**

### GitHub Releases
**[⬇ Скачать app-release.apk](https://github.com/5etrovich/CounterCalculator/releases/latest/download/app-release.apk)**

Или перейдите в раздел [Releases](https://github.com/5etrovich/CounterCalculator/releases) и скачайте последний `app-release.apk`.

> После скачивания APK разрешите установку из неизвестных источников: **Настройки → Безопасность → Установка из неизвестных источников**.

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
