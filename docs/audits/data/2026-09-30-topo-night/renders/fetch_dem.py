#!/usr/bin/env python3
"""AWS Open Data Terrain Tiles (Terrarium PNG), the few tiles the hillshade renders need (dem-needed.txt), one request
per 1.5 s with an identifying User-Agent. https://s3.amazonaws.com/elevation-tiles-prod/terrarium/{z}/{x}/{y}.png"""
import os, time, urllib.request
here = os.path.dirname(os.path.abspath(__file__))
UA = "Forager-topo-night-measurement/1.0 (armyofwann@gmail.com; one-off DEM tiles for a hillshade render comparison)"
for line in open(os.path.join(here, "dem-needed.txt")).read().split("\n"):
    z, x, y = map(int, line.split()); p = os.path.join(here, "dem-tiles", f"{z}_{x}_{y}.png")
    if os.path.exists(p): continue
    req = urllib.request.Request(f"https://s3.amazonaws.com/elevation-tiles-prod/terrarium/{z}/{x}/{y}.png", headers={"User-Agent": UA})
    data = urllib.request.urlopen(req, timeout=30).read(); open(p, "wb").write(data); print(z, x, y, len(data), flush=True); time.sleep(1.5)
