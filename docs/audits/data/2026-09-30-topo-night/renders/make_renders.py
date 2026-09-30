#!/usr/bin/env python3
"""Builds every render and sheet in this directory. Run: python3 make_renders.py"""
import json, os, sys, time
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import render as R
import measure as m

V1 = m.V1
# Night hillshade colours (premultiplied inside hillshade_over): dark shadow, a subtle warm-grey highlight, a neutral accent.
HS = dict(exaggeration=0.8, shadow=R.premult("#000000", 1.0), highlight=R.premult("#9FA892", 0.85), accent=R.premult("#14160F", 1.0))
HS_TEXT = "hillshade standard, exaggeration 0.8, shadow #000000, highlight #9FA892 a0.85, accent #14160F a1.0, light 335"
stats = {}
def save(name, im, note):
    im.save(os.path.join(R.HERE, name + ".jpg"), quality=93); stats[name] = dict(mean_lightness=round(R.lightness(im), 3), note=note); return im

def topo_v1(mz, size=256):
    im, n, d = R.raster_view(R.TOPO_DIRS, mz, size); return R.apply_paint(im, V1), n, d
def osm_v1(mz):
    im, n, d = R.raster_view(R.OSM_DIRS, mz, 256); return R.apply_paint(im, V1), n, d

sheets = {}
for mz in (7, 8.5, 9.4):
    tag = f"mz{mz}"; panels = []
    a, n, d = topo_v1(mz);            panels.append((f"{tag} today: topo V1, 256 px source (tile z{n}, {d:.0f} dp/tile)", save(f"{tag}_0_today_topo_v1", a, "today")))
    a, n, d = topo_v1(mz, 128);       panels.append((f"{tag} (a) topo V1, 128 px source (tile z{n}, {d:.0f} dp/tile)", save(f"{tag}_a_topo_v1_tilesize128", a, "tileSize 128")))
    if mz != 8.5:
        a, n, d = topo_v1(mz, 64);    panels.append((f"{tag} (a) topo V1, 64 px source (tile z{n}, {d:.0f} dp/tile)", save(f"{tag}_a_topo_v1_tilesize64", a, "tileSize 64")))
    if mz == 7:
        a, n, d = R.raster_view(R.TOPO_DIRS, mz, 32, v=96); a = R.apply_paint(a, V1)
        panels.append((f"{tag} (a) topo V1, 32 px source (tile z{n}, {d:.0f} dp/tile), 96 dp crop", save(f"{tag}_a_topo_v1_tilesize32_crop96dp", a, "tileSize 32, 96 dp crop")))
    b, n, d = osm_v1(mz);             panels.append((f"{tag} (b) OSM Standard V1 (tile z{n}, {d:.0f} dp/tile)", save(f"{tag}_b_osm_v1", b, "OSM under topo")))
    c, n, d = R.hillshade_over(b, mz, **HS)
    panels.append((f"{tag} (c) OSM V1 + hillshade (DEM z{n}, {d:.0f} dp/tile)", save(f"{tag}_c_osm_v1_hillshade", c, HS_TEXT)))
    sheets[tag] = panels; R.sheet(panels, os.path.join(R.HERE, f"sheet_{tag}.png"), scale=0.5)
    print(tag, "done", flush=True)

# the zoomed-in reference and (c) above 9.5
ref, n, d = topo_v1(11); save("mz11_ref_topo_v1", ref, "zoomed-in reference")
refhs, n2, d2 = R.hillshade_over(ref, 11, **HS); save("mz11_c_topo_v1_hillshade", refhs, HS_TEXT)
R.sheet([("mz11 reference: topo V1 (tile z12)", ref), (f"mz11 (c) topo V1 + hillshade (DEM z{n2})", refhs)], os.path.join(R.HERE, "sheet_mz11.png"), scale=0.5)

# the switch at 9.5 under (b): OSM V1 at 9.4, topo V1 at 9.6
o94, n, d = osm_v1(9.4); t96, n2, d2 = topo_v1(9.6); t94, _, _ = topo_v1(9.4)
save("switch_9.4_osm_v1", o94, "below 9.5 under (b)"); save("switch_9.6_topo_v1", t96, "at/above 9.5")
R.sheet([(f"9.4 (b) OSM V1 (tile z{n})", o94), (f"9.6 topo V1 (tile z{n2})", t96), ("9.4 today: topo V1 (for comparison)", t94)], os.path.join(R.HERE, "sheet_switch_9.5.png"), scale=0.5)
json.dump(stats, open(os.path.join(R.HERE, "render-stats.json"), "w"), indent=1)
print("all done")
