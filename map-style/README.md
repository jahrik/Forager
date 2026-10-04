# Forager's map style

This folder is where Forager's own map design lives. It starts as Protomaps' light map, which is free
to change and reuse (its design is public domain), and your changes are kept in one small file on top
of it. Nothing here changes the app until a later step builds it in.

## See the map

1. In a terminal: `cd map-style` and then `./start.sh`.
2. Open **http://localhost:8765/preview/** in your browser.
3. Move around as on any web map. The address bar remembers where you are, so you can bookmark a place.
4. Stop with Ctrl+C in the terminal.

**The preview is the web version of the map engine. The phone uses the Android version, which can draw
some things differently, labels in particular.** Treat the preview as a draft; the phone is the final
word.

## Change a colour or hide something

Your changes go in **`overrides/forager-colours.json`**. It starts empty, which means "exactly
Protomaps' light map".

Change colours by name, with web colour codes:

```json
{
  "about": "...",
  "colours": {
    "water": "#5bb8d6",
    "park_a": "#c9e4c5",
    "wood_a": "#cfe0c3"
  },
  "hide": ["pois"]
}
```

Then press Ctrl+C, run `./start.sh` again, and reload the preview page (or click "Reload the style"
on it). A name the style doesn't know stops the build with a message saying which one.

**The colour names** (72, plus two groups):

- **Land:** background, earth, glacier, sand, beach, scrub_a, scrub_b, wood_a, wood_b, park_a, park_b, pedestrian, zoo, military, aerodrome, runway, hospital, industrial, school, pier.
- **Water:** water.
- **Roads:** highway, major, minor_a, minor_b, minor_service, link, other, with their casings (outlines): highway_casing_early, highway_casing_late, major_casing_early, major_casing_late, minor_casing, minor_service_casing, link_casing. Also railway, buildings and boundaries.
- **Bridges and tunnels:** bridges_* and tunnel_* versions of the road colours.
- **Labels:** roads_label_minor, roads_label_major, ocean_label, city_label, subplace_label, state_label, country_label, address_label, most with a matching *_halo (the outline that keeps text readable).
- **Groups:** pois and landcover, each a group of a few colours.

**Ten names change nothing** in this version, because the style never uses them: park_a, wood_a, scrub_a, military, tunnel_link, link_casing, bridges_link_casing, bridges_link, and pois.red and pois.turquoise. Parks, woods and scrub use their "_b" colours. If you set one of these ten, the build says so rather than quietly ignoring it.

**Layer names to hide** are the ones listed in Maputnik (below), for example `pois` (the icons for
shops, cafés and so on), `buildings`, `address_label`.

## Edit by eye in Maputnik

Maputnik is a free visual editor for map styles that runs in the browser.

1. Run `./start.sh` (it also writes `forager-light.maputnik.json`).
2. Open **https://maplibre.org/maputnik/**, click **Open**, and drop **`forager-light.maputnik.json`**
   into the upload box.
3. Click a layer on the left, change its colour or width, and watch the map. **Save** downloads your
   edited style as a file.
4. **Send that file back.** A coder runs `npm run diff -- <your file>`, which lists exactly what you
   changed, and carries it into the overrides file. That way your change survives the next build.

Why a file and not a link: Maputnik can also open a style from a web address
(`?style=http://localhost:8765/forager-light.json`). Tried in Firefox 157 on 2026-10-04, the editor
never reached this computer's local server, likely because the browser keeps public websites away from
it. The file route worked. The Maputnik copy takes its fonts and icons from Protomaps' public web copy
instead of this computer, for the same reason.

## What's in this folder

| File | What it is |
|---|---|
| `overrides/forager-colours.json` | **Your changes.** The only file you need to edit by hand. |
| `forager-light.json` | The built style, for the preview. Rebuilt by `./start.sh`; don't edit it by hand. |
| `forager-light.maputnik.json` | The same style for Maputnik's editor. |
| `generate.mjs` | Builds the two styles from Protomaps' light map and your overrides. |
| `preview/index.html` | The preview page. |
| `start.sh`, `serve.py`, `fetch-assets.sh` | Install, build and serve. |
| `tools/` | Checks (every font and icon the style names is present) and the diff for Maputnik edits. |
| `licences/` | The licences for everything this uses. |

## Versions and licences

Every version is pinned, so the map looks the same whenever it is built:
- **The style:** `@protomaps/basemaps` 5.7.2. Code BSD-3-Clause; design CC0 (public domain), by Geraldine Sarmiento for Protomaps.
- **Fonts and icons:** `protomaps/basemaps-assets` at commit 028c18f7. The fonts are Noto Sans (SIL Open Font License). The icons derive from Mapzen's MIT-licensed tangrams icons.
- **The preview:** MapLibre GL JS 6.12.0 (BSD-3-Clause).
- **Node:** 24.21.0.

The map data comes from OpenStreetMap (© OpenStreetMap contributors, ODbL), served by Forager's tile
Worker. The preview only reads it.
