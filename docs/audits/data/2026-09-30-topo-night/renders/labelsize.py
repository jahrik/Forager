#!/usr/bin/env python3
"""Height in tile pixels of label letters, per tile zoom: connected components of near-neutral dark
pixels (black text), bounding-box height, components of 6-24 px height and width <= 24 px (a letter, not a
line or a coastline). Reports the median and 90th percentile letter height per tile zoom."""
import glob, os, sys
from PIL import Image
here = os.path.dirname(os.path.abspath(__file__)); tdir = os.path.join(here, "..", "tiles")
def heights(path):
    im = Image.open(path).convert("RGB"); w, h = im.size; px = im.load()
    mask = [[(max(px[x, y]) - min(px[x, y]) < 20 and sum(px[x, y]) / 3 < 64) for x in range(w)] for y in range(h)]
    seen = [[False] * w for _ in range(h)]; out = []
    for y in range(h):
        for x in range(w):
            if mask[y][x] and not seen[y][x]:
                st = [(x, y)]; seen[y][x] = True; x0 = x1 = x; y0 = y1 = y; n = 0
                while st:
                    cx, cy = st.pop(); n += 1
                    x0 = min(x0, cx); x1 = max(x1, cx); y0 = min(y0, cy); y1 = max(y1, cy)
                    for nx, ny in ((cx+1, cy), (cx-1, cy), (cx, cy+1), (cx, cy-1)):
                        if 0 <= nx < w and 0 <= ny < h and mask[ny][nx] and not seen[ny][nx]:
                            seen[ny][nx] = True; st.append((nx, ny))
                bh, bw = y1 - y0 + 1, x1 - x0 + 1
                if 6 <= bh <= 24 and bw <= 24 and n >= 10: out.append(bh)
    return out
if __name__ == "__main__":
    for z in range(8, 15):
        hs = []
        for p in sorted(glob.glob(os.path.join(tdir, f"{z}_*.png"))): hs += heights(p)
        hs.sort()
        if hs: print(f"z{z}: {len(hs)} letters, median {hs[len(hs)//2]} px, p90 {hs[int(len(hs)*0.9)]} px")
