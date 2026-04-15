# KnitOS

**Слоган:** *«Свяжи свою систему сам»*

KnitOS — это открытая операционная система на базе Android 16 с глубокой кастомизацией, живыми обоями и обновлениями в один клик.

## 🧵 Особенности

### Живые обои (Live Wallpapers)
- **Knitting Pattern** — динамические узоры вязания, реагирующие на время суток
- **Thread Weave** — переплетающиеся нити с плавной анимацией 60 FPS
- **Dynamic Yarn** — цветные пряди, меняющиеся от уровня заряда батареи
- Интеграция с Material You для автоматической цветовой темы
- Автоматическая приостановка при выключенном экране для экономии ресурсов

### Система обновлений One-Click OTA
- Проверка обновлений прямо из Настроек
- Загрузка с SourceForge/AndroidFileHost
- Проверка целостности SHA-256
- Установка через UpdateEngine (A/B слоты)
- Автоматический откат при неудаче

### Кастомизация интерфейса
- **Knit Theme Engine** — темы на основе RRO (Runtime Resource Overlay)
- Смена иконок, шрифтов, анимаций без перезагрузки
- Поддержка сторонних пакетов тем

### Безопасность
- Встроенный сетевой фаервол (iptables/nftables)
- Расширенный менеджер разрешений
- «Одноразовый доступ» к камере и микрофону
- Бэкпорт патчей безопасности между релизами AOSP

## 📋 Требования для сборки

- Ubuntu 22.04 / 24.04 LTS
- Минимум 100 GB свободного места
- 16+ GB RAM (рекомендуется 32 GB)
- Java 17
- Python 3.8+

## 🚀 Быстрый старт для эмулятора

### 1. Синхронизация AOSP

```bash
mkdir knitos && cd knitos
repo init -u https://android.googlesource.com/platform/manifest -b android-16.0.0_r1
repo sync -c -j$(nproc --all)
```

### 2. Интеграция KnitOS

```bash
# Скопируйте файлы KnitOS в дерево AOSP
cp -r device/generic/knitos_x86_64 <aosp_root>/device/generic/
cp -r frameworks/base/packages/KnitLiveWallpapers <aosp_root>/frameworks/base/packages/
cp -r packages/apps/KnitOTA <aosp_root>/packages/apps/
```

### 3. Сборка

```bash
source build/envsetup.sh
lunch knitos_x86_64-userdebug
m -j$(nproc --all)
```

Или используйте скрипт:

```bash
./build_knitos.sh
```

### 4. Запуск в эмуляторе

```bash
emulator -avd <your_avd_name> \
  -system out/target/product/knitos_x86_64/system.img \
  -vendor out/target/product/knitos_x86_64/vendor.img
```

## 📱 Поддерживаемые устройства

### Референсная платформа
- Google Pixel 6 / 6 Pro / 6a
- Google Pixel 7 / 7 Pro / 7a
- Google Pixel 8 / 8 Pro / 8a
- Google Pixel Fold

### Другие устройства
- Nothing Phone (1) / (2) / (3)
- OnePlus 9 / 10 / 11
- Xiaomi 12 / 13 (с разблокированным загрузчиком)

## 🏗️ Архитектура

```
KnitOS/
├── device/generic/knitos_x86_64/    # Конфигурация эмулятора
│   ├── BoardConfig.mk               # Настройки платы
│   ├── knitos_x86_64.mk             # Продуктовые настройки
│   └── init.knitos_x86_64.rc        # Init скрипты
├── frameworks/base/packages/
│   └── KnitLiveWallpapers/          # Живые обои
│       ├── src/                     # Исходный код Java
│       └── res/                     # Ресурсы
├── packages/apps/KnitOTA/           # Приложение обновлений
│   ├── src/                         # Исходный код
│   └── res/                         # UI ресурсы
└── build_knitos.sh                  # Скрипт сборки
```

## 🔐 Google Mobile Services (GMS)

KnitOS поставляется **без предустановленных GMS** по юридическим причинам. 

Для установки Google-сервисов:
1. При первом запуске мастер предложит установить NikGapps
2. Или скачайте вручную с [nikgapps.com](https://nikgapps.com)
3. Установите через Recovery

## 🛡️ Политика безопасности

- Основные релизы: ежеквартально (QPR)
- Патчи безопасности: по мере выхода Android Security Bulletin
- Критические исправления: внеплановые OTA-обновления

Подробности в [SECURITY.md](docs/SECURITY.md)

## 🤝 Участие в проекте

Смотрите [CONTRIBUTING.md](CONTRIBUTING.md) для информации о том, как внести свой вклад.

## 📄 Лицензия

Apache License 2.0 — см. [LICENSE](LICENSE)

## 🔗 Ссылки

- [Документация](docs/)
- [Инструкция по сборке](docs/BUILDING.md)
- [GitHub Organization](https://github.com/KnitOS)
- [OTA Repository](https://github.com/KnitOS/ota)

---

**KnitOS** — свяжи свою систему сам! 🧶
