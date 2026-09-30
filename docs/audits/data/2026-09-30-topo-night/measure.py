#!/usr/bin/env python3
"""Per-zoom lightness of the sampled OpenTopoMap tiles, before and after candidate night paints.

Dispatch 2026-09-28-310. Pure Pillow, no numpy. Models MapLibre Native 13.5.0's raster shader
(shaders/raster.fragment.glsl + raster_layer_tweaker.cpp, as cited in
docs/audits/2026-09-27-marker-swatch-board.md section 2) on gamma-encoded 0..1 channels.
Lightness here = the pixel's channel mean (r+g+b)/3/255, averaged over the tile pixels, because
that is the quantity V1 flips about.
"""
import glob, math, os, sys
from collections import defaultdict
from PIL import Image

HERE = os.path.dirname(os.path.abspath(__file__))

def spin_weights(deg):
    s, c = math.sin(math.radians(deg)), math.cos(math.radians(deg))
    r3 = math.sqrt(3)
    return ((2*c+1)/3, (-r3*s - c + 1)/3, (r3*s - c + 1)/3)

def sat_factor(s): return 1 - 1/(1.001 - s) if s > 0 else -s
def con_factor(c): return 1/(1 - c) if c > 0 else 1 + c

def shade(rgb, mn=0.0, mx=1.0, hue=0.0, sat=0.0, con=0.0):
    r, g, b = rgb
    wx, wy, wz = spin_weights(hue)
    sr = r*wx + g*wy + b*wz          # dot(rgb, w.xyz)
    sg = r*wz + g*wx + b*wy          # dot(rgb, w.zxy)
    sb = r*wy + g*wz + b*wx          # dot(rgb, w.yzx)
    avg = (r+g+b)/3
    f = sat_factor(sat)
    out = []
    for v in (sr, sg, sb):
        v += (avg - v)*f
        v = (v - 0.5)*con_factor(con) + 0.5
        v = mn + (mx - mn)*v
        out.append(min(1.0, max(0.0, v)))
    return tuple(out)

V1 = dict(mn=1.0, mx=0.0, hue=180.0)
DAY = dict()

def tiles_by_zoom():
    by = defaultdict(list)
    for p in sorted(glob.glob(os.path.join(HERE, "tiles", "*.png"))):
        z, x, y = os.path.basename(p)[:-4].split("_")
        by[int(z)].append(p)
    return by

def colours(path):
    im = Image.open(path).convert("RGB")
    return [(n, (c[0]/255, c[1]/255, c[2]/255)) for n, c in im.getcolors(65536)]

def mean_lightness(paths, paint):
    tot = n = 0.0
    for p in paths:
        for cnt, rgb in colours(p):
            o = shade(rgb, **paint)
            tot += cnt*sum(o)/3
            n += cnt
    return tot/n

if __name__ == "__main__" and len(sys.argv) == 1:
    by = tiles_by_zoom()
    print("tile z | map zoom | tiles | day L | V1 L")
    for z in sorted(by):
        ps = by[z]
        print(f"{z:2d} | {z-1:2d} | {len(ps)} | {mean_lightness(ps, DAY):.3f} | {mean_lightness(ps, V1):.3f}")


# --- per-zoom report (run: python3 measure.py report [dir]) ---------------------------------------
def _classify(c):
    r, g, b = c
    mu = (r + g + b) / 3
    if max(c) - min(c) < 0.08 and mu < 0.25:
        return "label"
    if r >= 0.85 and g >= 0.6 and b <= 0.45:
        return "road"
    return "ground"

def _stats(paths, paint):
    """mean, median, label mean, road mean of 8-bit-rounded channel-mean lightness."""
    allp, acc = [], {"label": [0.0, 0], "road": [0.0, 0]}
    for p in paths:
        for n, c in colours(p):
            o = sum(round(v * 255) for v in shade(c, **paint)) / (3 * 255)
            allp.append((o, n))
            k = _classify(c)
            if k in acc:
                acc[k][0] += o * n
                acc[k][1] += n
    allp.sort()
    tot = sum(n for _, n in allp)
    mean = sum(v * n for v, n in allp) / tot
    seen = 0
    for v, n in allp:
        seen += n
        if seen >= tot / 2:
            med = v
            break
    return mean, med, acc["label"][0] / max(1, acc["label"][1]), acc["road"][0] / max(1, acc["road"][1])

PAINTS = [
    ("day", DAY),
    ("V1 (today)", V1),
    ("A: min 0.5, max 0, hue 180 (built below map zoom 9.5)", dict(mn=0.5, mx=0.0, hue=180.0)),
    ("B: no inversion, max 0.3 (not built)", dict(mn=0.0, mx=0.3)),
]

def report(by):
    lines = []
    for name, paint in PAINTS:
        lines.append(f"\n### {name}\n")
        lines.append("| tile z | map zoom shown | mean | ground (median) | labels | roads |")
        lines.append("|---|---|---|---|---|---|")
        for z in sorted(by):
            mean, med, lab, road = _stats(by[z], paint)
            lines.append(f"| {z} | {z-1.5:g} to <{z-0.5:g} | {mean:.3f} | {med:.3f} | {lab:.3f} | {road:.3f} |")
    return "\n".join(lines)

if __name__ == "__main__" and len(sys.argv) > 1 and sys.argv[1] == "report":
    print(report(tiles_by_zoom()))
