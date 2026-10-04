#!/usr/bin/env bash
# One command for the preview: ./start.sh, then open http://localhost:8765/preview/ (dispatch 2026-09-28-485).
# Installs what is missing (the pinned packages, the fonts and icons), builds the style from your
# overrides, and serves this folder until Ctrl+C.
set -euo pipefail
cd "$(dirname "$0")"

# Node: on PATH, or the copy on the USB drive (Node 24.21.0, checksum-checked when it was fetched).
USB_NODE=/run/media/zynergy-labs/2ebd084f-5fdd-4730-8cef-96b267723190/map-style/node/node-v24.21.0-linux-x64/bin
if ! command -v node >/dev/null && [ -x "$USB_NODE/node" ]; then export PATH="$USB_NODE:$PATH"; fi
command -v node >/dev/null || { echo "Node is not installed, and the USB drive's copy is not there. See README.md."; exit 1; }

[ -d node_modules/@protomaps/basemaps ] || npm ci --no-audit --no-fund
[ -d assets/fonts ] || ./fetch-assets.sh
node generate.mjs
node tools/check-style.mjs
python3 serve.py
