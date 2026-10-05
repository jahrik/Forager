# Dispatch 2026-10-04-10: put the Pi-first Worker live and confirm it serves from the Pi

**Prep is read-only. Report back and ask before any non-read action. Do not build until the owner says "build".**

Written on 2026-10-04 by the Pi session that wrote the change, at the owner's word "Dispatch to a
coder when ready". The file name carries a suffix so it cannot collide with a planner's `-10` on
`main`. It is not a `RECORD.md` number.

**Base assumed:** `pi-origin` at the commit carrying this file, cut from `main` at `88a5b785`. When
this was written, `origin/main` was at `d4bd00bf`. Both are premises: check them against the remote
before acting.

## What exists, and how to check it

- `server/pmtiles-worker/src/index.ts`, `fetchFromPi`: z/x/y tile requests go to `PI_ORIGIN_URL`
  first, carrying the Access service token. Only a 200 or 204 is served from the Pi. Anything else
  falls back to the unchanged R2 path, including a timeout after 4 s. Every response carries
  `X-Forager-Origin: pi|r2`. The design, and what is still unverified, are in
  `docs/plans/2026-10-04-pi-origin-part2-report.md`.
- `wrangler.toml` sets `PI_ORIGIN_URL = "https://origin.zynergy-labs.com"`.
- **The owner confirmed** that the secrets on `forager-pmtiles` are named `CF_ACCESS_CLIENT_ID` and
  `CF_ACCESS_CLIENT_SECRET`. Never read, print or commit their values.
- **Node** is at `/opt/forager-build/node-v22.23.3-linux-arm64/bin`. It is not on `PATH`.

## Prep, read-only, then report by message

1. **Base.** Check `pi-origin` and `main` against the remote. Say what `main` has gained since
   `88a5b785`. Expect a conflict in `docs/audits/README.md`. Resolve it by merging, never by
   rebasing, and keep every row.
2. **Which branch Workers Builds deploys from.** The Worker README names
   `claude/pmtiles-cloudflare-worker`, and that branch no longer exists on the remote. The survey
   infers `main` (`docs/plans/2026-10-03-own-tiles-survey.md:27`). Confirm it from Cloudflare if you
   can (dashboard or connector). If you cannot, say so. Do not assume either.
3. **What a merge would ship.** List every change under `server/pmtiles-worker/` that the deploy
   branch would gain, not only this one.
4. **The typecheck**, with the Node above on `PATH`: run `tsc --noEmit` and report its exit code.
   This is a check, not a build.
5. **The plan for going live**, in plain words for the owner, with the choices laid out:
   - how the change reaches the deploy branch (a PR to `main` is expected);
   - the checks below, in order;
   - the rollback.

Stop there and ask.

## After the owner's go: the live checks, in order

1. **Request one z5 tile** through the Worker that has not been requested before, since the edge
   cache keeps the header of whoever answered first. Pass: HTTP 200 and `X-Forager-Origin: pi`.
2. **Request `/us.json`.** Pass: `maxzoom` is 15 and the tile URLs name the Worker's hostname,
   never `origin.zynergy-labs.com`.
3. **Request one uncached z15 tile.** Pass: `X-Forager-Origin: r2`. The Pi's archive stops at z14,
   so this goes through R2's overflow path.
4. **Fallback.** This needs its own go, because stopping a service is a non-read action. Stop
   `forager-tunnel` on the Pi and request one uncached z5 tile. Pass: 200 and `r2`, answered within
   about 4 s. Start the tunnel again and confirm it is `active` before going on.
5. Optional, if `wrangler tail` access exists: the check 4 request logs `pi-origin: HTTP 5xx` or a
   timeout, with no coordinates in the line.

If check 1 reads `r2`, stop and report. Do not change any code. The likely causes are the
cross-account token, which is untested, and secrets that did not reach the deployed version.
The fallback logs would tell them apart.

## Rollback

Deleting either secret (or unsetting `PI_ORIGIN_URL`) sends every tile back to R2, logged, with no
deploy. Reverting the merge commit removes the code. Both need the owner's go.

## Do not touch

The app. The R2 bucket. The Pi's services, except the approved stop and start in check 4. The
secret values. The overflow path. TileJSON.

## Asking

The owner, verbatim: "If something is ambiguous, or if they find a better way that wasn't
mentioned, or if something doesn't work how we want it, for any reason the coder has a question,
they're encouraged to stop and ask and not assume".

## Report

Append the results to `docs/plans/2026-10-04-pi-origin-part2-report.md` under a new heading,
together with its index row and the disclosure sections. Use no addresses, tokens or key material.
Hand back and stop.
