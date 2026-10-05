// Lists what an edited style (for example one saved from Maputnik) changes against forager-light.json,
// layer by layer, so the change can be carried into overrides/forager-colours.json (dispatch
// 2026-09-28-485). Run: npm run diff -- path/to/edited.json
import fs from "node:fs";

const [editedPath] = process.argv.slice(2);
if (!editedPath) {
  console.error("usage: npm run diff -- path/to/edited.json");
  process.exit(2);
}
const base = JSON.parse(fs.readFileSync(new URL("../forager-light.json", import.meta.url), "utf8"));
const edited = JSON.parse(fs.readFileSync(editedPath, "utf8"));
const byId = (style) => new Map(style.layers.map((layer) => [layer.id, layer]));
const before = byId(base);
const after = byId(edited);
let changes = 0;

for (const [id, layer] of after) {
  const old = before.get(id);
  if (!old) {
    console.log(`added layer "${id}" (${layer.type})`);
    changes++;
    continue;
  }
  for (const part of ["paint", "layout"]) {
    const keys = new Set([...Object.keys(old[part] ?? {}), ...Object.keys(layer[part] ?? {})]);
    for (const key of keys) {
      const a = JSON.stringify(old[part]?.[key]);
      const b = JSON.stringify(layer[part]?.[key]);
      if (a !== b) {
        console.log(`layer "${id}" ${part} ${key}: ${a ?? "(none)"} -> ${b ?? "(none)"}`);
        changes++;
      }
    }
  }
  for (const key of ["filter", "minzoom", "maxzoom"]) {
    if (JSON.stringify(old[key]) !== JSON.stringify(layer[key])) {
      console.log(`layer "${id}" ${key} changed`);
      changes++;
    }
  }
}
for (const id of before.keys()) {
  if (!after.has(id)) {
    console.log(`removed layer "${id}"`);
    changes++;
  }
}
console.log(`${changes} change(s)`);
