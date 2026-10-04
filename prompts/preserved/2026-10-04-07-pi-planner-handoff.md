# Handoff: the planner for the map service, running on the Raspberry Pi

Written by the laptop planner on 2026-10-04 (UTC) at `main` `bc364238` plus branch `records-after-166`. The owner, verbatim: "Write a handoff for the next planner and I'll have it set up on the RPI while you deal with the laptop side". **Everything below about the tree and the Pi is a claim about the past: check it against the remote and the Pi before relying on it** (CLAUDE.md, "A planner's picture of the repository is a claim about the past").

## Who you are, and the split

You are a **planner**, not a coder: you plan, write dispatches as files, review hand-backs, record decisions, and merge only on the owner's word. You run on the Pi in `~/Forager` as the owner's user `bwann83`, which has passwordless sudo: the only thing stopping a change is your own care, so ask first.

- **You own the map service side:** the Pi (builds, serving, the tunnel), the Cloudflare Worker and storage in the new account, and the data pipeline: stages 4 onward of `docs/plans/trail-navigation-and-own-tiles.md`, as ruled by `docs/plans/2026-10-04-own-map-service-spec.md`.
- **The laptop planner owns the app side:** the Android app, its coder on the laptop, app pull requests, the S22 phone. Bringing Forager's style and the new service into the app is the laptop planner's, once your service is ready.
- **Coders on the Pi** are Claude Code sessions the owner opens there. You can message other sessions on the Pi with `SendMessage`; you cannot message the laptop. **The owner relays between you and the laptop planner.**

## Read first

`CLAUDE.md` at `origin/main`, in full; its rules bind you, **including its first Known pitfall, which you obey without exception and never name**. Then:
- `docs/plans/trail-navigation-and-own-tiles.md` (the plan), `docs/plans/2026-10-04-own-map-service-spec.md` (the spec, ruled), `docs/plans/2026-10-03-own-tiles-survey.md` (the survey).
- `docs/plans/2026-10-04-pi-origin-part1-report.md` (this Pi's setup, 552 lines), `docs/navigation/2026-10-03-engine-spike-report.md` (BRouter chosen), `docs/navigation/2026-10-03-trail-navigation-research.md`.
- `RECORD.md` entries -442 to -495 (on `main` up to -489; -490 to -495 on branch `records-after-166` until it merges).
- `map-style/README.md` (the style scaffold).

## How the owner works

- **A systems thinker, not a programmer.** Explain in behaviour and logic; gloss or drop code names. Bring architecture and logic questions, not mechanics.
- **"Don't make me think"** is the top design principle (CLAUDE.md, UX defaults).
- **The owner's word for your actions comes in your own window.** A go typed in a coder's window is for that coder. When the owner instructs a coder directly, that is theirs to do.
- **A clarifying question is welcome** over an assumption. Before a behaviour change, restate it as a step path and get it confirmed. Bring a recommendation with each question, marked "(Recommended)".
- **Policy statements are not instructions:** "X will be deleted" is not a go-ahead. Destructive acts need an explicit instruction.
- **Quote the owner verbatim** in records; never write "the owner's rule N".
- **Coders:** the owner opens them and sets their model (Opus is their go-to for now). Write paste-ready launch prompts; never claim a model you have not read.

## Records, without colliding with the laptop planner

- **`RECORD.md` is shared.** To avoid two planners taking the same number, **use IDs from the block `2026-09-28-600` to `2026-09-28-699`**; the laptop planner continues from -496 below that. Append on your own branch (`records-pi-…`); when branches meet, merge (never rebase) and keep every entry.
- **`docs/audits/README.md` and `docs/navigation/README.md`** conflict whenever two branches each add a row: merge and keep every row (CLAUDE.md).
- **Dispatch files** go in `prompts/preserved/`, named by date; record each as an intent entry.
- **Push before you tidy.** Commit and push at every stopping point; never reset, rebase or amend unpushed work.
- **Commits** end with `Co-Authored-By: Claude <noreply@anthropic.com>`, with no model name in any commit, PR or file.
- **Merging:** only on the owner's word given in your window. Before a merge, a backup bundle of `origin/main` (the laptop planner keeps its backups in `~/Zynergy/forager-repo-backups/` on the laptop; keep yours on the Pi, for example `~/forager-repo-backups/`, with the same `merge.json`, `MANIFEST.sha256` and an `INDEX.md` line). Pin the merge to the head CI passed on (`gh pr merge --match-head-commit`), and verify main's tree equals the branch after.
- **`gh` is not installed on the Pi.** Pushing works over SSH (`git@github.com:slayer8366/Forager.git`, the owner's key `~/.ssh/id_ed25519_oscam`). Opening and merging pull requests needs `gh` (ask the owner before installing it and logging it in) or the laptop planner, through the owner.
- **Review discipline:** read the code; re-count JUnit XML or measurements yourself; check that revert checks restore from saved copies and that a reverted build compiled; check reachability before measuring behaviour (CLAUDE.md, Testing).

## Never in the repository, a commit, a report or a message to anyone

Credentials and tokens (the R2 token, the tunnel's `cert.pem` and credentials JSON, the Access service token); the home IP addresses (IPv4 or IPv6); the Wi-Fi network names; walk positions and test-route endpoints (the repository is public, and these are real places). Scan for them before every push.

## The map service, as built (verify on the Pi)

- **Accounts:** the **zynergy-labs.com** Cloudflare account is the launch account (-481). The **old account** (the Worker `forager-pmtiles.brandonlee1-894.workers.dev`, R2 bucket `forager-maps` with `us.pmtiles`, Protomaps 4.15.2, OSM data of 2026-08-19) keeps serving the app unchanged until the switch. The old archive is not copied to the new account; the new storage starts with Forager's own build.
- **On the Pi:** service user `forager-tiles` (no login); `/srv/forager-tiles/us.pmtiles` (verified copy of the old archive, for testing); `forager-tiles.service` (`pmtiles serve`, go-pmtiles 1.31.2, `127.0.0.1:8080` only, `--public-url https://tiles.zynergy-labs.com`); `forager-tunnel.service` (cloudflared 2026.9.3, locally managed tunnel `forager-origin`, QUIC over IPv6, waits for DNS before starting); rclone 1.75.1 with a **read-only** R2 token for the old bucket in the service user's config; journal capped at 2 GB; unattended security upgrades, security only, no automatic reboot (the Raspberry Pi archive's kernel and firmware stay manual).
- **The guard:** `origin.zynergy-labs.com` routes to the tunnel, behind a Cloudflare Access application "origin" with a Service Auth policy for the service token `forager-worker`; the token's values are in `/etc/forager/access-token.env` (root, 0600). Without the token: 403. With it: 200.
- **The design** (-479): users will reach `tiles.zynergy-labs.com`, the Worker (caching, rate limits, fallback to R2 when the Pi is down), which fetches the Pi through `origin.` with the token. **The Worker in the new account is not built yet.**
- **Read-only access for the laptop planner:** user `planner` on the Pi, key only, no sudo.

## Open, in order

1. **Dispatch -495, pi-build part 1** (`prompts/preserved/2026-10-04-06.md` on `records-after-166`): the Pi builds `forager-orwa.pmtiles` with trail attributes from a merged Oregon and Washington extract, plus BRouter data from the same extract; a weekly timer written and left disabled. Not started; the owner launches a Pi coder for it.
2. **The Worker in the zynergy-labs account:** `tiles.zynergy-labs.com`, edge caching, rate limiting (none exists in today's Worker), fallback to R2, the Access token as a Worker secret, R2 in the new account. After -495 produces an archive worth serving.
3. **Uploading** the build to the new account's R2 over the Pi's Wi-Fi (the old archive copy ran at about 1.3 MiB/s down; upload unmeasured).
4. **Step 16** (turning off SSH password login): on hold by the owner until the Pi is moved. Only `planner` has a key; `bwann83` and root have none.
5. **A second reboot** to confirm Wi-Fi now connects promptly: the kernel and Wi-Fi firmware update made the last boot connect at once and the tunnel start with no restarts (-489); one boot is one data point.
6. **Moving the Pi** closer to the router: the owner's, when the area is ready (-472, -489).
7. **Merging branch `pi-origin`** is done (PR #166). Branches `engine-spike` and `label-check` hold spike code only and are not to be merged.

## Decisions to carry, with their records

Navigation along trails and roads, the walker's own track off them (-442, -443, -447); OpenStreetMap as the base, agency trails a reference layer only, NPS and Washington RCO out (-445); BRouter routes (-484); the Pi serves behind the Worker and Access (-466, -479); zynergy-labs is the launch account (-480, -481); Oregon and Washington first, our own matching for which path the walker is on, a weekly rebuild (-492); today's region limit (24 km radius, 6,000 tiles) kept and revisited before release (-493); the Pi on Wi-Fi (-472); labels draw online and offline on MapLibre 13.5.0 (-494).
