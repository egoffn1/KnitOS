# KnitOS x86_64 Emulator Device Configuration
PRODUCT_NAME := knitos_x86_64
PRODUCT_DEVICE := knitos_x86_64
PRODUCT_BRAND := KnitOS
PRODUCT_MODEL := KnitOS Emulator x86_64
PRODUCT_MANUFACTURER := KnitOS Project

# Inherit from AOSP base
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/gsi_release.mk)

# Device specific properties
PRODUCT_PROPERTY_OVERRIDES += \
    ro.build.characteristics=emulator \
    ro.knitos.version=1.0.0 \
    ro.wallpapers.live_enabled=true

# KnitOS packages
PRODUCT_PACKAGES += \
    KnitLiveWallpapers \
    KnitOTA

# A/B updater
PRODUCT_PACKAGES += \
    update_engine \
    update_verifier \
    update_engine_sideload

# Audio
PRODUCT_PACKAGES += \
    audio.primary.x86_64 \
    audio.r_submix.default

# Graphics
PRODUCT_PACKAGES += \
    libGLES_android

# WiFi
PRODUCT_PACKAGES += \
    wpa_supplicant \
    hostapd

# Bluetooth
PRODUCT_PACKAGES += \
    android.hardware.bluetooth@1.0-service

# Sensors
PRODUCT_PACKAGES += \
    android.hardware.sensors@2.0-service.multihal

# Light HAL
PRODUCT_PACKAGES += \
    android.hardware.light@2.0-service

# Copy init scripts
PRODUCT_COPY_FILES += \
    device/generic/knitos_x86_64/init.knitos_x86_64.rc:root/init.knitos_x86_64.rc

# SEPolicy
DEVICE_PACKAGE_OVERLAYS += device/generic/knitos_x86_64/overlay
