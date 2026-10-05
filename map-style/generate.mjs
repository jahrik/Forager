// Builds forager-light.json from Protomaps' public-domain light style (pinned in package.json) and
// Forager's own changes in overrides/forager-colours.json (dispatch 2026-09-28-485). Run: npm run generate
import fs from "node:fs";
import { layers, namedFlavor } from "@protomaps/basemaps";

const here = (path) => new URL(path, import.meta.url);

// The tiles the preview draws: the old Worker's TileJSON, the same archive (Protomaps Basemap 4.15.2)
// Forager's offline maps come from, and open to any origin. Nothing is changed by reading it.
const TILEJSON_URL = "https://forager-pmtiles.brandonlee1-894.workers.dev/us.json";
// The fonts and icons, served by serve.py from assets/ (fetched by fetch-assets.sh).
const LOCAL = "http://localhost:8765";

const overrides = JSON.parse(fs.readFileSync(here("./overrides/forager-colours.json"), "utf8"));
const flavor = structuredClone(namedFlavor("light"));

for (const [key, value] of Object.entries(overrides.colours ?? {})) {
  if (!(key in flavor)) throw new Error(`overrides: "${key}" is not one of the style's colour names (see README.md)`);
  if (typeof flavor[key] === "object") {
    for (const [inner, colour] of Object.entries(value)) {
      if (!(inner in flavor[key])) throw new Error(`overrides: "${key}.${inner}" is not one of the style's colour names`);
      flavor[key][inner] = colour;
    }
  } else {
    flavor[key] = value;
  }
}

// A colour name the pinned version never uses changes nothing (in 5.7.2: park_a, wood_a, scrub_a,
// military, tunnel_link, link_casing, bridges_link_casing, bridges_link, pois.red, pois.turquoise).
// Each change is tried on its own against the plain light style, and one that changes nothing is
// reported, so an edit never silently does nothing.
const plain = JSON.stringify(layers("protomaps", namedFlavor("light"), { lang: "en" }));
for (const [key, value] of Object.entries(overrides.colours ?? {})) {
  const single = structuredClone(namedFlavor("light"));
  if (typeof single[key] === "object") Object.assign(single[key], value);
  else single[key] = value;
  if (JSON.stringify(layers("protomaps", single, { lang: "en" })) === plain) {
    console.warn(`note: "${key}" changes nothing in this version of the style; see README.md`);
  }
}

const allLayers = layers("protomaps", flavor, { lang: "en" });
const hidden = new Set(overrides.hide ?? []);
for (const id of hidden) {
  if (!allLayers.some((layer) => layer.id === id)) throw new Error(`overrides: there is no layer "${id}" to hide (see README.md)`);
}
for (const layer of allLayers) {
  if (hidden.has(layer.id)) layer.layout = { ...(layer.layout ?? {}), visibility: "none" };
}

const style = {
  version: 8,
  name: "Forager light",
  metadata: { "forager:generated-from": "@protomaps/basemaps 5.7.2, flavor light, with overrides/forager-colours.json" },
  sources: {
    protomaps: {
      type: "vector",
      url: TILEJSON_URL,
      attribution: '<a href="https://github.com/protomaps/basemaps">Protomaps</a> © <a href="https://osm.org/copyright">OpenStreetMap</a>',
    },
  },
  glyphs: `${LOCAL}/assets/fonts/{fontstack}/{range}.pbf`,
  sprite: `${LOCAL}/assets/sprites/v4/light`,
  layers: allLayers,
};

fs.writeFileSync(here("./forager-light.json"), JSON.stringify(style, null, 2) + "\n");
// For Maputnik's hosted editor, opened from a file: the same style with the fonts and icons from
// Protomaps' public copy, since a public web page may not be allowed to read from localhost. That copy
// follows basemaps-assets' latest commit, not the pinned one: fine for editing, not for shipping.
const forMaputnik = {
  ...style,
  name: "Forager light (for Maputnik)",
  glyphs: "https://protomaps.github.io/basemaps-assets/fonts/{fontstack}/{range}.pbf",
  sprite: "https://protomaps.github.io/basemaps-assets/sprites/v4/light",
};
fs.writeFileSync(here("./forager-light.maputnik.json"), JSON.stringify(forMaputnik, null, 2) + "\n");
console.log(`forager-light.json: ${allLayers.length} layers, ${hidden.size} hidden, ${Object.keys(overrides.colours ?? {}).length} colours changed`);
