# Build environment for Forager: JDK 21 and the Android SDK, so a contributor needs only
# podman or docker. Mirrors CI (.github/workflows/ci.yml: ubuntu-24.04, Temurin 21) and installs
# the SDK with the repo's own scripts/setup-android-sdk.sh, so the two cannot drift apart.
#
# Two images come out of this file:
#   podman build -t local/forager-build:test .                      # the lean build image (default)
#   podman build --target emulator -t local/forager-emulator:test . # the same plus an emulator
#
# The images hold the toolchain only. The source is mounted at /workspace at run time (see
# README.md, "Building in a container"), because the build reads its version from the git history.
#
# `build` is deliberately the LAST stage: a plain `podman build .` builds the final stage, and the
# multi-gigabyte emulator stage must not be what a contributor who only wants a build gets.

# Tag for readability, digest for reproducibility.
FROM eclipse-temurin:21-jdk-noble@sha256:743b453cb8639d815a8251e128f7d8e88840c684e43fdbde1b1e79788437b5cf AS toolchain

# The SDK lives outside the checkout. resolveBuildIdentity (app/build.gradle.kts) appends
# `.dirty` to versionName when `git status` is non-empty, so an SDK unpacked inside the
# workspace would mark every build dirty (same reason as ANDROID_SDK_ROOT in ci.yml).
ENV ANDROID_SDK_ROOT=/opt/android-sdk \
    ANDROID_HOME=/opt/android-sdk \
    HOME=/home/builder

# hadolint ignore=DL3008
RUN apt-get update \
    && apt-get install -y --no-install-recommends git unzip \
    && rm -rf /var/lib/apt/lists/*

COPY scripts/setup-android-sdk.sh /usr/local/bin/setup-android-sdk.sh
RUN setup-android-sdk.sh

# The container runs as the caller's uid (podman --userns=keep-id, or docker --user), which has
# no entry in this image. Gradle, the Android Gradle plugin and Robolectric write to $HOME and
# to the SDK, so both must be writable by any uid.
#
# `safe.directory '*'` is what keeps the version stamp real: the mounted checkout is owned by
# a uid git does not trust by default, and the build's `git rev-list`/`git status` calls would
# fail and downgrade the build to UNVERSIONED.
RUN mkdir -p "$HOME" \
    && chmod -R a+rwX "$HOME" "$ANDROID_SDK_ROOT" \
    && git config --system --add safe.directory '*'

WORKDIR /workspace


# The emulator: the SDK's emulator package, one x86_64 system image and an AVD, rendering on the
# host's GPU. It needs /dev/kvm, /dev/dri and a display at run time; see README.md, "Seeing the
# app in an emulator". Every choice below was found by running it, and each one fixes a crash:
#
#  * Host GPU (`-gpu host`, Mesa below). The emulator forces its DMA-related graphics features off
#    under the software renderer, and the guest then aborts (see the feature list at the end).
#  * API 36.1, a plain 4 KB-page image. API 37 (the app's targetSdk) was not retried once the
#    features below were found, so it is untested here, not known to fail.
#  * An AVD config of its own. `avdmanager` writes hw.gpu.enabled=no and the pipe transport.
FROM toolchain AS emulator

ENV ANDROID_AVD_HOME=/opt/android-avd

# Qt's xcb plugin and the emulator's own libraries, plus Mesa: radeonsi (GL) and radv (Vulkan) for
# the Steam Deck's AMD GPU, and the Vulkan loader. hadolint DL3008: unpinned, like the toolchain
# stage's apt install.
# hadolint ignore=DL3008
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        libasound2t64 libdbus-1-3 libgbm1 libgl1 libgl1-mesa-dri libegl1 libgles2 libnspr4 libnss3 \
        libpulse0 libsm6 libice6 libvulkan1 mesa-vulkan-drivers libx11-xcb1 libxcb-cursor0 \
        libxcb-icccm4 libxcb-image0 libxcb-keysyms1 libxcb-render-util0 libxcb-shape0 \
        libxcb-xinerama0 libxcb-xkb1 libxcomposite1 libxcursor1 libxdamage1 libxfixes3 libxi6 \
        libxkbcommon-x11-0 libxkbfile1 libxrandr2 libxrender1 libxtst6 \
    && rm -rf /var/lib/apt/lists/*

# The system image and the AVD are made in one RUN and chmod'ed in it: a chmod in a later layer
# would copy the multi-gigabyte image into a second layer. The AVD lives outside $HOME because
# $HOME is a bind mount at run time (the Gradle cache), which would hide an AVD made at build time.
#
# The hw.* keys: the GPU is on and `host`, the transport is `asg` (the guest's graphics ring
# buffer, which the DMA read needs), and the screen is small, because the Deck's display is
# 1280x800 and the emulator window is scaled to fit it anyway.
#
# `printf 'no\n' |` answers avdmanager's "custom hardware profile?" prompt.
SHELL ["/bin/bash", "-o", "pipefail", "-c"]
RUN sdkmanager="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin" \
    && "$sdkmanager/sdkmanager" --sdk_root="$ANDROID_SDK_ROOT" \
        "emulator" "system-images;android-36.1;google_apis;x86_64" \
    && mkdir -p "$ANDROID_AVD_HOME" \
    && printf 'no\n' | "$sdkmanager/avdmanager" create avd --name forager \
        --package "system-images;android-36.1;google_apis;x86_64" --device pixel_6 \
    && config="$ANDROID_AVD_HOME/forager.avd/config.ini" \
    && for kv in disk.dataPartition.size=4G hw.lcd.width=720 hw.lcd.height=1280 hw.lcd.density=280 \
        hw.ramSize=3072 hw.gpu.enabled=yes hw.gpu.mode=host hw.gltransport=asg; do \
        sed -i "/^${kv%%=*}=/d" "$config" && echo "$kv" >> "$config"; \
    done \
    && chmod -R a+rwX "$ANDROID_SDK_ROOT/emulator" "$ANDROID_SDK_ROOT/system-images" "$ANDROID_AVD_HOME" \
    && test -d "$ANDROID_AVD_HOME/forager.avd"

ENV PATH="$ANDROID_SDK_ROOT/emulator:$ANDROID_SDK_ROOT/platform-tools:$PATH"

# The feature list is what makes this boot. SurfaceFlinger, the guest's compositor, aborts with
# `Assertion failed: !rcEnc->featureInfo()->hasReadColorBufferDma` in a crash loop, because the
# guest's graphics mapper (mapper.ranchu) requires the host to advertise a DMA read of colour
# buffers. The host advertises it (ANDROID_EMU_read_color_buffer_dma, libgfxstream_backend.so,
# initRenderControlContext) only when GLDirectMem and HasSharedSlotsHostMemoryAllocator are both
# on, and GLDirectMem is `off` in the emulator's advancedFeatures.ini. GLDMA, GLDMA2, GLAsyncSwap
# and HostComposition are listed with them, not because each was shown necessary, but because
# that is the combination that was verified. `Vulkan` is also `off` by default; without it the
# guest has no Vulkan device and the app's map (MapLibre's Vulkan renderer) dies on start with
# "No Vulkan compatible GPU found".
CMD ["emulator", "-avd", "forager", "-gpu", "host", \
     "-feature", "Vulkan,GLDMA,GLDMA2,GLDirectMem,GLAsyncSwap,HostComposition,HasSharedSlotsHostMemoryAllocator", \
     "-no-audio", "-no-snapshot", "-no-boot-anim", "-cores", "4"]


# The lean build image. Last on purpose, see the header.
FROM toolchain AS build

CMD ["bash"]
