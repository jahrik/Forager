# An editable Forager map style, with a preview (dispatch 2026-09-28-485)

**Status: built and pushed on `map-style-scaffold`, not merged.** Nothing in the app, the offline style, the Worker, either Cloudflare account or the Pi changed. No phone.

**Date:** 2026-10-04 (UTC).
**Dispatch:** `prompts/preserved/2026-10-04-04.md` (from `origin/records-after-163`).
**Base:** `origin/main` at `61f2c363` (PR #164), checked against the remote.
**The owner's words,** to this session directly: "Yes, start -485".

Large files are on the owner's USB drive under `map-style/`: Node 24.21.0, the installed packages, the fonts and icons, and the screenshot tooling. Screenshots and scripts are in `~/Zynergy/device-evidence/2026-10-04-map-style/`.

## In plain terms

1. **`map-style/` holds Forager's map design.** It starts as Protomaps' light map, whose design is public domain. The owner's changes are kept in one small file, `overrides/forager-colours.json`.
2. **`./start.sh` builds it and serves a preview** at `http://localhost:8765/preview/`, drawn on the map data Forager already serves.
3. **Colours change by name;** layers hide by name.
4. **Edits by eye happen in Maputnik,** the free browser editor, by opening a file. What the owner changes there is listed by `npm run diff` and carried into the overrides.
5. **The preview is the web engine, not the phone's,** and the page says so: labels in particular may draw differently on MapLibre Android 13.5.0.

## Verified before building (reported by message first; the planner accepted it)

1. **`@protomaps/basemaps`:**
   - Version 5.7.2 (2026-03-10), the newest Styles release, with no dependencies.
   - `LICENSE.md`, re-read: code BSD-3-Clause; "The visual design … created by Geraldine Sarmiento for Protomaps LLC and released under a Creative Commons 0 (CC0) license"; some icons derive from Mapzen's MIT tangrams icons.
   - The old Worker's `/us.json`, read live, is "Protomaps Basemap" 4.15.2, the newest Tiles release, with zooms 0 to 15.
   - The flavours are plain colour objects, and `layers(source, flavor, {lang})` builds the layers.
   - The npm package carries no licence file, so the repository's `LICENSE.md` was copied.
2. **Fonts and icons (`protomaps/basemaps-assets`):**
   - There are no tags, so they are pinned at commit `028c18f7` (2025-10-31).
   - Noto Sans Regular, Medium and Italic glyphs, under `fonts/OFL.txt`.
   - Sprites `v4`. These have no licence file of their own; the README says they derive from MIT tangrams icons, so that text was copied from `tangrams/icons`.
3. **Maputnik:**
   - v3.0.0 and v3.1.0 have no downloadable build; the last `dist.zip` was v2.1.1.
   - The hosted editor opens `?style=<url>` with a CORS fetch (`src/libs/urlopen.ts`), and its Open dialog takes a file (`ModalOpen.tsx`).
4. **Tiles:** the old Worker's `/us.json` answered with `access-control-allow-origin: *`, `cf-cache-status: HIT` (one GET). The Pi was not used.
5. **Night mode:**
   - `NightColour.kt:74-78` recolours background, fill and line layers only. A labelled style's text and icons would not change at night as the app stands.
   - Protomaps' dark flavour is a reference only.

## What was built (`map-style/`)

| File | What it does |
|---|---|
| `package.json`, `package-lock.json` | Pins `@protomaps/basemaps` 5.7.2 and `maplibre-gl` 6.12.0; npm checks each package's sha512 from the lockfile |
| `overrides/forager-colours.json` | The owner's changes: `colours` by name, `hide` by layer name. Empty, so the style equals Protomaps' light |
| `generate.mjs` | Builds `forager-light.json` and `forager-light.maputnik.json` |
| `fetch-assets.sh` | The fonts and icons at the pinned commit; the archive's sha256 is checked |
| `serve.py` | Python's static server on 127.0.0.1:8765, with CORS and the private-network header |
| `preview/index.html` | The preview: MapLibre GL JS 6.12.0, the view kept in the address bar, a reload link, and the note that the phone may differ |
| `start.sh` | One command: install what is missing, build, check, serve |
| `tools/check-style.mjs` | The checks below |
| `tools/diff-style.mjs` | Lists what an edited style changes, layer by layer |
| `licences/` | Noto OFL, tangrams MIT, Protomaps BSD-3 and CC0, MapLibre GL JS BSD-3 |
| `README.md` | The owner's guide |

**`generate.mjs` in more detail:**
- It rejects a colour name or layer name the style doesn't have, saying which.
- It warns when a colour name changes nothing. That list was found by trying each name: in 5.7.2, `park_a`, `wood_a`, `scrub_a`, `military`, `tunnel_link`, `link_casing`, `bridges_link_casing`, `bridges_link`, `pois.red` and `pois.turquoise`.
- **Why two styles:** the Maputnik copy takes its fonts and icons from Protomaps' public copy (`protomaps.github.io`), which follows their latest commit rather than ours. Only the font and icon addresses differ; the layers are identical (checked).

**Not committed, and why:**
- `node_modules`, `assets` (14 MB of fonts) and the screenshots are gitignored; `start.sh` and `fetch-assets.sh` recreate the first two.
- Here they are symlinks to the USB drive, since laptop space is short.
- `forager-light.json` is committed, so a change shows up as a readable diff.

## Evidence

**The generated style:** 71 layers (1 background, 15 fill, 41 line, 14 symbol), from `npm run check`:
- **Fonts:** Noto Sans Italic, Regular and Medium, each with all 256 glyph ranges present locally.
- **Icons:** every icon name the style can ask for was produced by evaluating its `icon-image` expressions for the inputs they receive: each POI kind in the layer's filter, shield networks with 1 to 5 characters, capital or not, and three zooms. **53 of the 55 names are in the 53-icon sprite.** The two missing:
  - `townhall`: a real gap upstream. Protomaps' POI layer accepts town halls, but its sprite has no icon for them, so MapLibre draws the label without one.
  - `generic_shield-0char`: only for a road shield with no text. Whether the tiles ever carry one was not checked.
- **Licences:** all four files present.

**Screenshots** (`screenshots/`), from headless Firefox 157 through `puppeteer-core` 25.12.0, each taken after the map reported idle:
- **A town:** downtown Portland at zoom 15.5. Streets, buildings, the river, labels and POI icons are drawn from the local fonts and sprite.
- **A national forest with trails:** Ramona Falls, Mount Hood National Forest, at zoom 14. **Trails are thin white lines on the green**, faint, which is a design question for the owner. A stream label is drawn.
- **A coastline:** Cannon Beach at zoom 13: the shore, beach, town, Highway 101's shield, and POI icons.

Two tooling notes:
- Playwright 1.56.1 refused to install a browser on Ubuntu 26.04 ("does not support … ubuntu26.04-x64"), so the system Firefox was driven instead.
- Firefox is a snap, and needed its profile folder inside `~/snap/firefox/common/`.

**Maputnik:**
- **The `?style=http://localhost:8765/forager-light.json` link did not work** in Firefox 157 (headless). The editor read and removed the parameter, but no request reached the local server and none was reported failed. Inferred: the browser keeps public sites from reading localhost. Not tried in Chrome.
- **The file route worked** (`screenshots/maputnik-file-route.png`): Open, upload `forager-light.maputnik.json`, and the editor listed the layers and drew Cannon Beach with labels and icons on the Worker's tiles.
- **`npm run diff`:** against a test edit (water recoloured, POIs hidden) it listed exactly those two changes; against the Maputnik copy, zero.

**`start.sh`,** run with a bare PATH (`/usr/bin:/bin`): it found Node on the USB drive, built, checked, and served the preview and the style (HTTP 200 each).

## The preview against the phone

**The preview is MapLibre GL JS 6.12.0; the app is MapLibre Android 13.5.0.** They share the style specification but not the renderer. **Labels in particular are unproven on the phone:** the survey left open whether a labelled vector style renders online on 13.5.0, and nothing here tests that. The preview page says so in its own note.

The night recolour is the app's, not the style's. As it stands it would leave a labelled style's text and icons in their day colours.

## Disclosure

**Confirmed vs inferred.**
- Confirmed, by the reads, the scripts' output and the screenshots: versions, licences, the schema match, Worker CORS, the layer count, fonts and icons present, the preview drawing, Maputnik's file route, the diff tool, `start.sh`.
- Inferred: why the `?style=` link failed; that `generic_shield-0char` is never asked for.

**Could not determine.**
- Whether Chrome lets the hosted Maputnik read localhost.
- Whether the tiles carry roads with an empty shield text.
- How any of this draws on the phone.

**Premises that were wrong.**
1. **The report's location.** The dispatch placed it at `docs/plans/2026-10-04-map-style-scaffold-report.md`, against this session's standing rule not to touch `docs/plans/`. The planner moved it here.
2. **Node and npm** were not on the laptop. Node 24.21.0 LTS was fetched (checksum against nodejs.org's SHASUMS256) instead of the 20.x that Protomaps' own `.nvmrc` names, since Node 20 is past end of life.
3. **"The repo's `.nvmrc` (the Worker's)"** in the planner's acceptance: Forager has no `.nvmrc`. The one I cited was Protomaps'.
4. **"The hosted editor loading a style from a local URL"** as a route: it did not work in Firefox 157. The file route is the one the guide leads with.

**Decided beyond scope.**
1. **The colour-name warning, the layer and colour checks, `start.sh`, and the diff tool,** so an edit never silently does nothing and the owner needs one command.
2. **A second, Maputnik-ready copy of the style.**
3. **Fonts, icons and installed packages live on the USB drive** behind symlinks. The laptop was below 2,048 MB free during this work: 1,946 MB after the first install, about 1,983 MB after the moves. Most of the drop is not this work's, and nothing outside it was deleted.
