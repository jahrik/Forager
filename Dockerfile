# Build environment for Forager: JDK 21 and the Android SDK, so a contributor needs only
# podman or docker. Mirrors CI (.github/workflows/ci.yml: ubuntu-24.04, Temurin 21) and installs
# the SDK with the repo's own scripts/setup-android-sdk.sh, so the two cannot drift apart.
#
# This image holds the toolchain only. The source is mounted at /workspace at run time (see
# README.md, "Building in a container"), because the build reads its version from the git history.

# Tag for readability, digest for reproducibility.
FROM eclipse-temurin:21-jdk-noble@sha256:743b453cb8639d815a8251e128f7d8e88840c684e43fdbde1b1e79788437b5cf

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
CMD ["bash"]
