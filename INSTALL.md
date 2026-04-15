# 🚀 Установка KnitOS на эмулятор или устройство

## Требования

### Для эмулятора (рекомендуется для тестирования):
- ОС: Linux (Fedora, Basalt или другие дистрибутивы)
- RAM: минимум 16 ГБ (рекомендуется 32 ГБ)
- Свободное место: 500+ ГБ на SSD/NVMe
- CPU: 8+ ядер (Intel/AMD с поддержкой виртуализации)
- Пакеты: `repo`, `git`, `ccache`, `openjdk-17`, `libxml2`, `python3`

### Для физического устройства:
- Устройство с разблокированным загрузчиком (Pixel 6+, Nothing Phone, OnePlus и др.)
- Разблокированный bootloader
- Кастомный recovery (TWRP) или поддержка A/B обновлений
- Резервная копия данных (все данные будут удалены!)

---

## 📋 Шаг 1: Подготовка окружения на Fedora/Basalt

### 1.1 Установка необходимых пакетов

```bash
# Обновление системы
sudo dnf update -y

# Установка базовых инструментов разработки
sudo dnf install -y \
    git \
    repo \
    gcc \
    g++ \
    make \
    curl \
    wget \
    ncurses-devel \
    zlib-devel \
    libxml2-devel \
    bison \
    flex \
    openssl-devel \
    lz4 \
    libarchive \
    java-17-openjdk-devel \
    ccache \
    adb \
    fastboot \
    emulator \
    avdmanager
```

### 1.2 Настройка переменных окружения

Добавьте в `~/.bashrc` или `~/.zshrc`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH
export USE_CCACHE=1
export CCACHE_EXEC=$(which ccache)
export CCACHE_DIR=$HOME/.ccache
export CCACHE_MAXSIZE=50G
```

Примените изменения:
```bash
source ~/.bashrc
```

### 1.3 Инициализация ccache

```bash
ccache --set-config=max_size=50G
ccache --set-config=compression=true
ccache --set-config=compression_level=6
```

---

## 🏗️ Шаг 2: Синхронизация исходного кода AOSP

### 2.1 Создание рабочей директории

```bash
mkdir -p ~/knitos-build
cd ~/knitos-build
```

### 2.2 Инициализация репозитория AOSP

```bash
repo init -u https://android.googlesource.com/platform/manifest -b android-16.0.0_r1
```

### 2.3 Добавление локальных изменений KnitOS

Скопируйте файлы KnitOS из репозитория в дерево AOSP:

```bash
# Клонируйте KnitOS
cd ~/knitos-build
git clone https://github.com/egoffn1/KnitOS.git

# Скопируйте конфигурацию устройства
cp -r KnitOS/device/generic/knitos_x86_64 device/generic/

# Скопируйте приложения
cp -r KnitOS/packages/apps/* packages/apps/

# Скопируйте расширения фреймворка
cp -r KnitOS/frameworks/base/* frameworks/base/

# Скопируйте скрипт сборки
cp KnitOS/build_knitos.sh .
chmod +x build_knitos.sh
```

### 2.4 Локальный манифест (опционально)

Для интеграции KnitOS можно создать локальный манифест:

```bash
mkdir -p .repo/local_manifests
cat > .repo/local_manifests/knitos.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<manifest>
  <!-- Дополнительные репозитории для KnitOS -->
  <project path="device/generic/knitos_x86_64" name="KnitOS/device_generic_knitos_x86_64" remote="github" revision="main" />
  <project path="packages/apps/KnitLiveWallpapers" name="KnitOS/packages_apps_KnitLiveWallpapers" remote="github" revision="main" />
  <project path="packages/apps/KnitOTA" name="KnitOS/packages_apps_KnitOTA" remote="github" revision="main" />
</manifest>
EOF
```

### 2.5 Синхронизация исходников

```bash
# Полная синхронизация (может занять несколько часов)
repo sync -c --force-sync --optimized-fetch --no-tags --no-clone-bundle -j$(nproc --all)

# Или выборочная синхронизация для экономии времени
repo sync -c --current-branch -j$(nproc --all)
```

---

## 🔨 Шаг 3: Сборка KnitOS

### 3.1 Запуск скрипта сборки

```bash
cd ~/knitos-build
./build_knitos.sh
```

Или вручную:

```bash
# Источник переменных окружения
source build/envsetup.sh

# Выбор целевого устройства
lunch knitos_x86_64-userdebug

# Начало сборки (может занять 1-3 часа)
m -j$(nproc --all)
```

### 3.2 Ожидаемый результат

После успешной сборки образы будут расположены в:
```
out/target/product/knitos_x86_64/
├── boot.img
├── system.img
├── vendor.img
├── product.img
├── userdata.img
└── ramdisk.img
```

---

## 🖥️ Шаг 4: Запуск на эмуляторе

### Способ 1: Быстрый запуск с готовыми образами

```bash
# Запуск эмулятора с системным образом KnitOS
emulator -avd <имя_существующего_AVD> \
    -system out/target/product/knitos_x86_64/system.img \
    -vendor out/target/product/knitos_x86_64/vendor.img \
    -data out/target/product/knitos_x86_64/userdata.img \
    -ramdisk out/target/product/knitos_x86_64/ramdisk.img \
    -bootloader out/target/product/knitos_x86_64/boot.img \
    -no-snapshot \
    -wipe-data \
    -gpu host \
    -memory 4096 \
    -cores 4
```

### Способ 2: Создание нового AVD для KnitOS

```bash
# Создание нового виртуального устройства
avdmanager create avd \
    -n KnitOS \
    -k "system-images;android-16;google_apis;x86_64" \
    -d pixel_6 \
    -f

# Запуск с кастомными образами
emulator -avd KnitOS \
    -system out/target/product/knitos_x86_64/system.img \
    -vendor out/target/product/knitos_x86_64/vendor.img \
    -no-snapshot \
    -wipe-data \
    -gpu host \
    -memory 4096 \
    -cores 4
```

### Способ 3: Прямой запуск без AVD

```bash
# Запуск напрямую с образами
emulator \
    -sysdir out/target/product/knitos_x86_64/ \
    -datadir out/target/product/knitos_x86_64/ \
    -partition-size 10240 \
    -gpu host \
    -memory 4096 \
    -cores 4 \
    -no-boot-anim \
    -verbose
```

### Полезные флаги эмулятора:

| Флаг | Описание |
|------|----------|
| `-gpu host` | Использовать GPU хоста для ускорения |
| `-memory 4096` | Выделить 4 ГБ RAM |
| `-cores 4` | Использовать 4 ядра CPU |
| `-no-snapshot` | Не использовать снапшоты |
| `-wipe-data` | Очистить данные при запуске |
| `-no-boot-anim` | Отключить анимацию загрузки |
| `-verbose` | Подробный лог загрузки |
| `-shell-serialfile /tmp/knitos.log` | Лог консоли в файл |

---

## 📱 Шаг 5: Установка на физическое устройство

### ⚠️ Предупреждение
Все данные на устройстве будут удалены! Создайте резервную копию.

### 5.1 Разблокировка загрузчика

```bash
# Перезагрузка в bootloader
adb reboot bootloader

# Проверка состояния
fastboot getvar unlocked

# Разблокировка (стирает все данные!)
fastboot flashing unlock
# или для старых устройств:
fastboot oem unlock
```

### 5.2 Прошивка образов (A/B устройства)

Для устройств с A/B слотами (Pixel 6+):

```bash
# Прошивка всех разделов
fastboot flash boot out/target/product/knitos_x86_64/boot.img
fastboot flash system out/target/product/knitos_x86_64/system.img
fastboot flash vendor out/target/product/knitos_x86_64/vendor.img
fastboot flash product out/target/product/knitos_x86_64/product.img

# Активация слота
fastboot set_active a

# Перезагрузка
fastboot reboot
```

### 5.3 Прошивка через TWRP (для устройств без A/B)

1. Скачайте образы на компьютер
2. Перезагрузитесь в TWRP: `adb reboot recovery`
3. В TWRP выполните:
   - Wipe → Advanced Wipe → выберите Dalvik, Cache, System, Data
   - Install → Install Image → выберите system.img → System
   - Install → Install Image → выберите vendor.img → Vendor
   - Install → Install Image → выберите boot.img → Boot
4. Reboot System

### 5.4 Использование fastbootd (Android 10+)

```bash
# Переход в userspace fastboot
adb reboot fastboot

# Прошивка
fastboot flash system out/target/product/knitos_x86_64/system.img
fastboot flash vendor out/target/product/knitos_x86_64/vendor.img
fastboot reboot
```

---

## 🔍 Шаг 6: Отладка и логи

### Просмотр логов в реальном времени

```bash
# Logcat
adb logcat

# Только логи KnitOS
adb logcat | grep -i knit

# Сохранение логов
adb logcat -d > knitos_log.txt
```

### Доступ к shell эмулятора/устройства

```bash
# ADB shell
adb shell

# Проверка версии
getprop ro.build.version.release
getprop ro.knitos.version

# Проверка живых обоев
dumpsys wallpaper
```

### Отладка OTA обновлений

```bash
# Логи UpdateEngine
adb shell logcat -s UpdateEngine:*

# Проверка статуса обновления
adb shell dumpsys update_engine
```

---

## 🛠️ Решение проблем

### Проблема: Эмулятор не запускается

```bash
# Проверьте поддержку виртуализации
egrep -c '(vmx|svm)' /proc/cpuinfo

# Если 0 - включите VT-x/AMD-V в BIOS
# Проверьте права доступа к /dev/kvm
ls -la /dev/kvm
sudo usermod -aG kvm $USER
```

### Проблема: Ошибка при сборке "Java not found"

```bash
# Проверьте версию Java
java -version

# Должно быть OpenJDK 17
# Если другая версия:
sudo alternatives --config java
```

### Проблема: Недостаточно места

```bash
# Очистка кэша сборки
make clean

# Или полная очистка
rm -rf out/

# Очистка ccache
ccache -C
```

### Проблема: Устройство не определяется через ADB

```bash
# Правила udev для Android
cat > /etc/udev/rules.d/51-android.rules << EOF
SUBSYSTEM=="usb", ATTR{idVendor}=="18d1", MODE="0666", GROUP="plugdev"
SUBSYSTEM=="usb", ATTR{idVendor}=="22b8", MODE="0666", GROUP="plugdev"
SUBSYSTEM=="usb", ATTR{idVendor}=="0502", MODE="0666", GROUP="plugdev"
EOF

sudo chmod a+r /etc/udev/rules.d/51-android.rules
sudo udevadm control --reload-rules
sudo service udev restart
```

---

## 📊 Время выполнения операций

| Операция | Время (ориентировочно) |
|----------|------------------------|
| Синхронизация AOSP | 2-6 часов (зависит от интернета) |
| Первая сборка | 1-3 часа (зависит от CPU) |
| Повторная сборка | 10-30 минут (с ccache) |
| Запуск эмулятора | 1-2 минуты |
| Установка на устройство | 5-10 минут |

---

## 🎉 После установки

После успешной загрузки KnitOS:

1. **Настройте живые обои**: Настройки → Обои → Живые обои → Knit Live Wallpapers
2. **Проверьте обновления**: Настройки → Система → Обновление KnitOS
3. **Установите GMS** (опционально): Следуйте инструкциям мастера настройки
4. **Настройте темы**: Настройки → Темы → Knit Theme Engine

---

## 📞 Поддержка

- Документация: [GitHub KnitOS](https://github.com/egoffn1/KnitOS)
- Issues: Сообщайте о проблемах через GitHub Issues
- Telegram: @KnitOS_Community (если создан)

**Слоган:** *«Свяжи свою систему сам»* 🧵
