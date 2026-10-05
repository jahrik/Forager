# The Raspberry Pi 5 as Forager's tile origin: Part 2, the Worker

Follows [part 1](2026-10-04-pi-origin-part1-report.md), handback item 4. Run by a Claude Code session
on the Pi, on `pi-origin` from `6c54797c`.

**This report contains no credentials, tokens or key material.**

## The owner's rulings, verbatim

The owner set the Access token's two values as Worker secrets ("Secrets set. Use API tokens"). Asked
what that meant, whether to fall back, and which account, the owner answered: "1a, Pi first with R2
fallback, same Worker same process. Pick it up and go." That means:

- **1a.** The Worker presents the Access service token to reach the Pi. A Cloudflare API token for
  this session was the other reading, and it was rejected.
- **The Pi first, R2 as fallback.** The rejected alternatives were Pi-only, and keeping R2 primary
  with the Pi unused.
- **The same Worker** (`forager-pmtiles`), on the account it is on now. Moving it to the
  zynergy-labs account is not part of this job.

## What changed

`server/pmtiles-worker/src/index.ts`, new function `fetchFromPi`, plus one branch in `fetch`:

- A **z/x/y tile request** goes to `${PI_ORIGIN_URL}/{name}/{z}/{x}/{y}.{ext}` first, carrying
  `CF-Access-Client-Id` and `CF-Access-Client-Secret`.
- **A 200 or 204 from the Pi is served as the answer.** Anything else falls through to the R2 path,
  which is unchanged. That includes a 404, any 3xx (redirects are not followed), a 403, a tunnel
  5xx, a network error, and a timeout after **4 s**. The 4 s is an operating limit chosen for the
  weak Wi-Fi link, not a measured figure.
- **TileJSON (`/us.json`), the z15 overflow and the tile-type check stay on R2.** The Pi's own
  TileJSON names the Pi's public URL, which clients must not be given.
- **Every fallback is logged**, except a 404, which is the expected answer above z14 and is not a
  fault. Logs never include tile coordinates, for privacy, and observability stays off, so these
  logs reach only a live `wrangler tail`.
- **Every response carries `X-Forager-Origin: pi` or `r2`.** Which origin answered can be read from
  outside without logs.
- **Missing configuration falls back to R2 and is logged:** an unset `PI_ORIGIN_URL`, or either
  secret missing.
- **Only plain archive names (`[A-Za-z0-9_-]+`) are forwarded to the Pi.** The tile-path pattern also
  admits `/` and `.`, and the token rides on every request.

`server/pmtiles-worker/wrangler.toml` gains `PI_ORIGIN_URL = "https://origin.zynergy-labs.com"`.
The secrets are not in the file.

## Disclosure

### Confirmed

- One Worker exists, `forager-pmtiles`, last modified 2026-10-05 05:02 UTC, through the Cloudflare
  connector.
- `origin.zynergy-labs.com` returns 403 without the token and 200 with it. This is from part 1, not
  re-run here.

### Not verified

- **The secret names.** The code reads `CF_ACCESS_CLIENT_ID` and `CF_ACCESS_CLIENT_SECRET`. The
  connector does not show secret names, and the owner did not state them. If they differ, every
  request falls back to R2 and logs "Access token secrets not set". Users are not affected, but the
  Pi goes unused. Either rename the secrets or tell the session the names.
- **That the code compiles.** Node is not installed on the Pi, so `npm run typecheck` was not run.
  `npm run build` was not run either, because the owner has not said "build". Workers Builds will
  compile it on its next build.
- **That a Worker on the old account can present a zynergy-labs Access token.** This is part 1's
  inference, still untested.
- **The Pi's 404 above z14, and its `Content-Type`.** Both are inferred from go-pmtiles, not observed
  through the Worker.
- **Which branch Workers Builds deploys from.** The README names a branch that no longer exists on
  the remote. The survey infers `main` (`2026-10-03-own-tiles-survey.md:27`). Pushing `pi-origin`
  is not expected to change production, but that is also unverified.

### Not done

Nothing is deployed. Going live needs this branch merged to whatever branch deploys, and that is the
owner's call. After a deploy, the check is: request one z5 tile through the Worker and read
`X-Forager-Origin: pi`, then stop `forager-tunnel` on the Pi and read `r2` on a tile not already in
the edge cache.
