#!/usr/bin/env bash
# Fetches the fonts and icons the style uses into assets/ (not committed), at a pinned version, and
# checks them. Run from this folder: ./fetch-assets.sh
#
# Source: protomaps/basemaps-assets at commit 028c18f7 (2025-10-31; the repository has no tags).
# GitHub publishes no checksum for a commit's archive, so the sha256 below is the one recorded when
# this script was written (dispatch 2026-09-28-485): trust on first use. If GitHub ever re-compresses
# the archive the check fails loudly, and the files must be compared before the new sum is accepted.
set -euo pipefail
cd "$(dirname "$0")"

COMMIT=028c18f713baecad011301ff7a69acc39bcc2ae7
SHA256=57e40e8c512bd8042d0a3a251f19d0d1c8523ad963c666c3c6643bada4dc92d0
URL="https://codeload.github.com/protomaps/basemaps-assets/tar.gz/$COMMIT"

mkdir -p assets
archive="assets/basemaps-assets-$COMMIT.tar.gz"
if [ ! -f "$archive" ]; then
  curl -sSfL -o "$archive.part" "$URL"
  mv "$archive.part" "$archive"
fi
echo "$SHA256  $archive" | sha256sum -c -

rm -rf assets/fonts assets/sprites
tar -xzf "$archive" -C assets --strip-components=1 \
  "basemaps-assets-$COMMIT/fonts/Noto Sans Regular" \
  "basemaps-assets-$COMMIT/fonts/Noto Sans Medium" \
  "basemaps-assets-$COMMIT/fonts/Noto Sans Italic" \
  "basemaps-assets-$COMMIT/sprites/v4"
echo "fonts: $(ls assets/fonts | tr '\n' ',' ); sprites: $(ls assets/sprites/v4 | wc -l) files"
