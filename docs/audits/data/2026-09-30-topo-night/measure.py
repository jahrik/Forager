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

if __name__ == "__main__":
    by = tiles_by_zoom()
    print("tile z | map zoom | tiles | day L | V1 L")
    for z in sorted(by):
        ps = by[z]
        print(f"{z:2d} | {z-1:2d} | {len(ps)} | {mean_lightness(ps, DAY):.3f} | {mean_lightness(ps, V1):.3f}")
