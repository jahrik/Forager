#!/usr/bin/env python3
"""Extra OpenTopoMap tiles for the renders (planner request after the owner rejected the built paint).
Only tiles tilemath.needed() says a render needs and tiles/ lacks: 128 px source at map zoom 8.5 (3 tiles),
64 px source at map zoom 7 and 9.4 (16 + 7). One request per 1.5 s, identifying User-Agent."""
import os, sys, time, urllib.request
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import tilemath
here = os.path.dirname(os.path.abspath(__file__)); tdir = os.path.join(here, "tiles-extra"); os.makedirs(tdir, exist_ok=True)
base = os.path.join(here, "..", "tiles")
UA = "Forager-topo-night-measurement/1.0 (armyofwann@gmail.com; one-off 26 extra tiles for a render comparison)"
want = set()
for mz, size in ((8.5, 128), (7, 64), (9.4, 64)):
    n, d, tl = tilemath.needed(mz, size)
    for x, y in tl:
        if not os.path.exists(os.path.join(base, f"{n}_{x}_{y}.png")):
            want.add((n, x, y))
for n, x, y in sorted(want):
    path = os.path.join(tdir, f"{n}_{x}_{y}.png")
    if os.path.exists(path): continue
    req = urllib.request.Request(f"https://a.tile.opentopomap.org/{n}/{x}/{y}.png", headers={"User-Agent": UA})
    with urllib.request.urlopen(req, timeout=30) as r: data = r.read()
    open(path, "wb").write(data); print(n, x, y, len(data), flush=True); time.sleep(1.5)
print(len(want), "tiles wanted")
