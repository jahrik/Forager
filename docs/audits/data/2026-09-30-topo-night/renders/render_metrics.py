#!/usr/bin/env python3
"""Numbers for the README: per config tile level, dp per tile, letter size in dp, tile count for a phone viewport,
and each render's mean and median lightness (channel mean)."""
import math, os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import tilemath
from PIL import Image
LETTER_PX = 10          # median label letter height in a 256 px OpenTopoMap tile, z8-z14: 8-11 px (labelsize.py)
W, H = 412, 892         # dp, a phone-sized map viewport (the S22 Ultra's is about this)
def tiles_for(d): return (W / d + 1) * (H / d + 1)
print("| map zoom | source tileSize | tile level | dp per tile | letter height (dp) | tiles for a 412 x 892 dp view | x today |")
print("|---|---|---|---|---|---|---|")
for mz in (7, 8.5, 9.4, 11):
    base = tiles_for(tilemath.shown_dp(mz, 256))
    for size in (256, 128, 64, 32):
        n = tilemath.level(mz, size); d = tilemath.shown_dp(mz, size)
        print(f"| {mz} | {size} | {n} | {d:.1f} | {LETTER_PX * d / 256:.1f} | {tiles_for(d):.0f} | {tiles_for(d) / base:.1f}x |")
print()
print("smallest tileSize whose level is >= 11 (the dark regime), per map zoom: S <= 2^(mz - 1.5)")
for mz in (5, 6, 7, 8, 8.5, 9, 9.4):
    S = 2 ** (mz - 1.5); n = tilemath.level(mz, int(S)); d = tilemath.shown_dp(mz, int(S))
    print(f"  mz {mz}: tileSize <= {S:.0f} -> level {n}, {d:.1f} dp/tile, letters {LETTER_PX * d / 256:.1f} dp, tiles {tiles_for(d):.0f}")
print()
here = os.path.dirname(os.path.abspath(__file__))
print("| render | mean | median (ground) |")
print("|---|---|---|")
for f in sorted(os.listdir(here)):
    if f.endswith(".jpg") and not f.startswith("sheet_"):
        px = [sum(p) / 765 for p in Image.open(os.path.join(here, f)).convert("RGB").getdata()]; px.sort()
        print(f"| {f[:-4]} | {sum(px) / len(px):.3f} | {px[len(px) // 2]:.3f} |")
