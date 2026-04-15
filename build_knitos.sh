# Build script for KnitOS x86_64 emulator
# Run this after syncing AOSP source code

#!/bin/bash

set -e

echo "=========================================="
echo "  KnitOS Build Script for x86_64"
echo "=========================================="

# Configuration
export USE_CCACHE=1
export CCACHE_EXEC=$(which ccache)
export CCACHE_DIR=$HOME/.ccache
export CCACHE_MAXSIZE=50G

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check environment
log_info "Checking build environment..."

if [ ! -f "build/envsetup.sh" ]; then
    log_error "AOSP source not found. Please sync AOSP first:"
    log_error "  repo init -u https://android.googlesource.com/platform/manifest -b android-16.0.0_r1"
    log_error "  repo sync"
    exit 1
fi

# Source build environment
log_info "Sourcing build environment..."
source build/envsetup.sh

# Select target
log_info "Selecting KnitOS x86_64 target..."
lunch knitos_x86_64-userdebug

if [ $? -ne 0 ]; then
    log_error "Failed to select target. Make sure device/generic/knitos_x86_64 is properly configured."
    exit 1
fi

# Start build
log_info "Starting KnitOS build..."
log_info "Target: knitos_x86_64"
log_info "Build type: userdebug"
log_info "Start time: $(date)"

m -j$(nproc --all)

if [ $? -eq 0 ]; then
    log_info "Build completed successfully!"
    log_info "Output images location: out/target/product/knitos_x86_64/"
    log_info ""
    log_info "To run in emulator:"
    log_info "  emulator -avd <your_avd_name> -system out/target/product/knitos_x86_64/system.img"
    log_info ""
    log_info "Or create AVD with:"
    log_info "  avdmanager create avd -n KnitOS -k \"system-images;android-16;google_apis;x86_64\""
    log_info "  emulator -avd KnitOS -system out/target/product/knitos_x86_64/system.img"
else
    log_error "Build failed!"
    exit 1
fi
