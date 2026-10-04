# The Raspberry Pi 5 as Forager's tile origin: Part 1, the report

Dispatch `prompts/preserved/2026-10-04-01.md` (its title reads "Dispatch 2026-09-28-468";
see Premises below). Part 1 is report-only: establish what the Pi is, what runs on it, what is
installed, and the plan for the target — changing nothing. Run by a Claude Code session on the Pi
itself, model `claude-opus-5[1m]` (Opus 5, 1M context).

Written against `main` at `88a5b785`, which is the base the dispatch states it assumes and the
commit `origin/main` carried when this was written — verified, no drift (`CLAUDE.md`, "A planner's
picture of the repository is a claim about the past").

No addresses, credentials, tokens or key material appear in this document. The Pi's `wlan0` holds a
globally-routable IPv6 address as well as a private LAN IPv4; both are omitted, as the home IP is.

## 1. The Pi

| Fact | Value | Source |
|---|---|---|
| Model | Raspberry Pi 5 Model B Rev 1.1 | `/proc/device-tree/model` |
| OS | Debian GNU/Linux 12 (bookworm) | `/etc/os-release` |
| Kernel | `6.12.96+rpt-rpi-2712`, `aarch64` | `uname -a` |
| 64-bit | Yes — `arm64` userland, `LONG_BIT=64` | `dpkg --print-architecture`, `getconf LONG_BIT` |
| Firmware | `086b83e3` (release), 2026/05/26 | `vcgencmd version` |
| RAM | 7.9 GiB total, 6.7 GiB available; swap 199 MiB | `free -h`, `/etc/dphys-swapfile` |
| CPU | 4x Cortex-A76, max 2400 MHz, observed 2.400 GHz | `lscpu`, `vcgencmd measure_clock arm` |
| Temperature at idle | 47.7 C, load average 0.00 / 0.04 / 0.21 | `vcgencmd measure_temp`, `uptime` |
| Power state | `throttled=0x0` — no under-voltage, no frequency cap, no throttling, none since boot | `vcgencmd get_throttled` |
| Uptime | 18 days, 2:54 | `uptime` |
| Boot device | NVMe. `/` is `/dev/nvme0n1p2`; `BOOT_ORDER=0xf416` | `findmnt`, `lsblk`, `vcgencmd bootloader_config` |
| NVMe | FIKWOT FN501Pro 256GB, 238.5 G | `lsblk` |
| Free disk | `/` 234 G total, 52 G used, **170 G available** (24% used) | `df -h` |
| Second volume | `/dev/mmcblk0p1`, 119.4 G **SD card** at `/mnt/archive`, 111 G free, 24 K used | `lsblk`, `df -h` |
| Network | **Wi-Fi.** `wlan0` up, default route via `wlan0` (DHCP). `eth0` is down, no carrier (`speed -1`, `carrier 0`) | `ip -br addr`, `ip route`, `/sys/class/net/*` |

**What already runs.** Listening sockets (`ss -tulpn`, as root):

| Proto | Bind | Process | Localhost only? |
|---|---|---|---|
| tcp | `0.0.0.0:22`, `[::]:22` | `sshd` | **No** |
| tcp | `127.0.0.1:631`, `[::1]:631` | `cupsd` | Yes |
| udp | `0.0.0.0:5353`, `*:5353`, + ephemeral 49458/51723 | `avahi-daemon` | **No** |
| udp | link-local `:546` | `NetworkManager` (DHCPv6 client) | Link-local |

32 enabled services, no failed units. This is a **desktop image, not a server image**: `lightdm`,
`cups`, `cups-browsed`, `bluetooth`, `ModemManager`, `wayvnc-control`, `triggerhappy` and
`avahi-daemon` are all enabled. No tile-, `pmtiles`- or `cloudflared`-related unit exists.

## 2. Security baseline

- **Claude Code runs as `bwann83`** (uid 1000; groups include `sudo`, `adm`, `video`, `gpio`).
  `sudo -n -l` reports `(ALL) NOPASSWD: ALL` — the session has unrestricted password-free root.
  Recorded because it means nothing on the Pi mechanically enforces the owner's step-by-step
  consent rule; the gate is the session's compliance, not permissions.
- **SSH enabled and active** (`ssh.service` enabled + active, `ssh.socket` disabled). Effective
  `sshd -T`: port 22, listening `0.0.0.0` and `[::]`, `pubkeyauthentication yes`,
  **`passwordauthentication yes`**, `kbdinteractiveauthentication no`, `permitemptypasswords no`,
  `permitrootlogin without-password` (root key-only).
- **No `~/.ssh/authorized_keys`.** Access to `bwann83` is therefore **by password only** today.
  The planner's requested read-only key access from the laptop does not exist yet.
- **Automatic security updates are off.** `unattended-upgrades` is not installed
  (`dpkg-query: no packages found`) and `/etc/apt/apt.conf.d/20auto-upgrades` does not exist.
  `apt-daily.timer` and `apt-daily-upgrade.timer` fire, but with neither the package nor the
  enabling config they refresh metadata and apply nothing. Nothing patches this Pi automatically.
- **No firewall.** `ufw` and `iptables` not installed; `nft` present with an **empty ruleset**.
  Inbound filtering is whatever the home router does, which was not probed.
- **Off-localhost listeners:** `sshd` on 22 and `avahi-daemon` on 5353, per the table above.

## 3. Installed among the five named

| Tool | State |
|---|---|
| `cloudflared` | **Not installed.** No `~/.cloudflared`, no `/etc/cloudflared`, no Cloudflare apt source. |
| `go-pmtiles` / `pmtiles` | **Not installed.** |
| `rclone` | **Not installed.** No `~/.config/rclone`. Debian bookworm's candidate is `1.60.1+dfsg-2+b5`. |
| Java | **Not installed.** No `java`, no `javac`, no `openjdk-*`. |
| Docker | **Not installed.** No `docker`, `docker-compose`, `podman`. |

Present: `curl` 7.88.1, `git` 2.39.5, `nft`. Absent and worth noting: `jq`, `gh`.

**Push capability**, tested because the dispatch's record depends on it. Both probes were
`git push --dry-run`, so nothing was created on the remote:

- **HTTPS push fails** — `fatal: could not read Username for 'https://github.com'`. No credential
  helper, no `~/.git-credentials`, no `gh`.
- **SSH push succeeds** — `* [new branch]` against `git@github.com:slayer8366/Forager.git`. The key
  at `~/.ssh/id_ed25519_oscam` authenticates as `slayer8366` with write access.

So this Pi can push, over SSH only. `user.name` and `user.email` were both unset; the identity used
for this commit was taken from the repository's own history (1862 commits as
`slayer8366`), not assumed.

## 4. The plan, with versions pinned and each step's rollback

Pins resolved from the upstream release APIs on 2026-10-03:

| Component | Pin | Asset |
|---|---|---|
| `cloudflared` | **2026.9.3** (published 2026-09-24) | `cloudflared-linux-arm64.deb` |
| `go-pmtiles` | **v1.31.2** (published 2026-07-22) | `go-pmtiles_1.31.2_Linux_arm64.tar.gz` |
| `rclone` | **v1.75.1** (published 2026-09-04) | `rclone-v1.75.1-linux-arm64.deb`, upstream rather than Debian's 1.60.1 |

### Two findings that change the target's shape

Both read from go-pmtiles' own source at the pinned tag `v1.31.2`, not inferred from documentation.

1. **`pmtiles serve` defaults to `--interface 0.0.0.0`** (`main.go:105`). "Localhost only" is not
   the default; it needs an explicit `--interface 127.0.0.1`. Left at the default on this Pi — which
   has no firewall — the tile server would be exposed to the Wi-Fi LAN the moment it started.
2. **`/us.json` returns HTTP 501 `"PUBLIC_URL must be set for TileJSON"`** unless the server is
   started with `--public-url` (`pmtiles/server.go:292`). The dispatch's stated check — curl the
   public hostname for `/us.json` — therefore cannot pass as written unless that flag is set at
   launch. `/us/metadata` needs no such flag. A public hostname is not a secret, so the pin is safe
   in a unit file.

Also confirmed from source: `serve` takes a directory and resolves `<name>.pmtiles` within it
(`server.go:257`), routing `/<name>.json`, `/<name>/metadata` and `/<name>/{z}/{x}/{y}.{ext}`
(`server.go:441-442`, `parseTilePath`). The file must be named exactly `us.pmtiles` to yield
`/us.json` and `/us/5/{x}/{y}.mvt`.

### Steps

Each is asked in the owner's window first, one at a time, with what it changes and how to undo it.

1. **Install `go-pmtiles` 1.31.2** — pinned tarball, checksum verified against the release's
   `checksums.txt`, single binary to `/usr/local/bin/pmtiles`. *Undo:* delete the binary.
2. **Install `rclone` v1.75.1** from the pinned `.deb`. *Undo:* `apt-get purge rclone`.
3. **Configure the R2 remote.** The owner enters the credentials themselves via `rclone config` in
   their own terminal; the session supplies the exact values to enter and never receives the key.
   Written only to the service user's `~/.config/rclone/rclone.conf`, mode `0600`. Never in the
   repository, a commit or a report. Shape per `server/pmtiles-worker/README.md:70-73` — provider
   Cloudflare, the account's R2 endpoint. *Undo:* delete that file.
4. **Copy the archive, read-only against R2**, onto the **NVMe** (owner's choice; see Decisions).
   `rclone copyto r2:forager-maps/us.pmtiles <dir>/us.pmtiles --s3-no-check-bucket` — `copyto`, not
   `copy`, per `README.md:76-80`, which records `copy` landing the object nested at
   `us.pmtiles/us.pmtiles`. Verified against the source's size and, where available, its checksum
   (`rclone lsjson --hash` versus local `sha256sum`), both reported. The bucket's contents stay
   read-only. *Undo:* delete the local file.
5. **Create a non-root service user** (`--system`, no login shell, no sudo) owning the archive
   directory and both services. *Undo:* `userdel`, remove its home.
6. **`pmtiles serve` as a systemd unit** — `--interface 127.0.0.1 --port <port>
   --public-url https://<hostname>`, `Restart=on-failure`, `WantedBy=multi-user.target`, with
   `NoNewPrivileges`, `ProtectSystem=strict`, `PrivateTmp` and a read-only archive path, plus a
   unit-level log bound. Localhost-only binding **verified with `ss -tulpn`** before proceeding,
   given finding 1. *Undo:* `systemctl disable --now`, delete the unit, `daemon-reload`.
7. **Bound the journal.** `journalctl --disk-usage` already reports **966.7 MB** and
   `/etc/systemd/journald.conf` carries no non-default settings, so the effective cap is the
   10%-of-filesystem default — about 23 GB on a 234 GB root. Set `SystemMaxUse=` explicitly.
   *Undo:* revert the line, restart `systemd-journald`.
8. **Install `cloudflared` 2026.9.3** from the pinned arm64 `.deb`. *Undo:* `apt-get purge cloudflared`.
9. **`cloudflared tunnel login`** — the owner's browser. The session hands over the URL; a
   cert lands in the service user's `~/.cloudflared`. No Cloudflare resource created yet.
   *Undo:* delete the cert file.
10. **Create the tunnel and its one DNS record** on the hostname the owner names. The only changes
    to the Cloudflare account this part makes. *Undo:* delete the DNS record, then
    `cloudflared tunnel delete <name>`.
11. **The tunnel as a systemd service** under the same non-root user, ingress restricted to the one
    localhost tile port, `Restart=always`, log bound. **Outbound only — no inbound port opened and
    no firewall change requested.** *Undo:* disable, delete the unit, `daemon-reload`.
12. **Verify from the Pi** with `curl` to the public hostname: `/us.json` (per finding 2) and one z5
    tile, expecting `200` and `application/x-protobuf`. Responses reported verbatim with headers.
    The app and the Worker are **not** pointed at it — out of scope for this part.
13. **This report**, its row in `docs/audits/README.md`, committed on `pi-origin` from
    `origin/main` and pushed. Done first, per `CLAUDE.md`'s "Push before you tidy".

### Added to this part by the owner, beyond the dispatch

Each still gated on the owner's word, in this order:

14. **Enable unattended security upgrades** — install `unattended-upgrades`, write
    `20auto-upgrades`. *Undo:* purge the package, remove the config.
15. **Add the planner laptop's public key** to `authorized_keys` (the owner pastes the public key).
    *Undo:* remove the line.
16. **Disable SSH password authentication — only after key login is confirmed working.** Ordering
    matters: done before confirmation, a failed key could lock the owner out of their own Pi.
    *Undo:* restore `PasswordAuthentication yes`, reload `sshd`.

## Decisions the owner made on this report

1. Hostname `tiles.<owner's domain>` — **the literal domain is still outstanding**; see Could not
   determine.
2. R2 credentials entered by the owner directly at step 3, never pasted to the session.
3. The archive goes on the **NVMe** (170 GB free), not the SD card at `/mnt/archive`.
4. Push over SSH, identity `slayer8366`.
5. The three security steps above added to this part.
6. **Stay on Wi-Fi** for now.
7. **Leave the desktop services** in place.

## Disclosure

### Confirmed

Every row of sections 1, 2 and 3, each from the command named beside it. Both push probes. Both
go-pmtiles source findings, read at tag `v1.31.2`. All three version pins, from the upstream release
APIs. The base commit matching `88a5b785`. The archive's description at
`server/pmtiles-worker/README.md:52-54` and the `copyto` guidance at `:76-80`. The commit identity,
from this repository's history.

### Inferred, not verified

That the router does not forward 22 or 5353 — not probed. That `--public-url` set to the public
hostname is what downstream TileJSON consumers need: read from source, not exercised. That 170 GB
free on the NVMe is ample for an 8.8 GB archive plus future refreshes — arithmetic, not a measured
refresh cycle.

### Could not determine

- **The power supply's rating.** `throttled=0x0` is the only power evidence available, and it is
  good news: no under-voltage across 18 days of uptime. But there is no `max_current_a` device-tree
  node and no PMIC or power line in `dmesg`, so the survey's `5 V 5 A supply` claim
  (`docs/plans/2026-10-03-own-tiles-survey.md` section 6, marked `[C]`) **cannot be confirmed from
  the Pi** and should be read as unverified.
- **The archive's true size and checksum.** 8.8 GB is the README's figure, not a measurement. It is
  verifiable only at step 3/4, once the R2 remote exists.
- **The literal hostname.** See the first wrong premise below.

### Premises that were wrong

- **`pmtiles serve` binds `0.0.0.0` by default**, so the dispatch's "localhost only" is an explicit
  flag rather than a property of the tool — and this Pi has no firewall to catch the mistake.
- **`/us.json` 501s without `--public-url`**, so the dispatch's verification step cannot pass as
  written.
- **The Pi is on Wi-Fi, not Ethernet.** `eth0` is down with no carrier. The dispatch asks which,
  and the survey's section 6 reasons about the Pi as a serving origin without settling it. The owner
  has since ruled: stay on Wi-Fi for now.
- **The hostname supplied was the placeholder `tiles.YOURDOMAIN`**, not a domain. Steps 6 and 9-12
  cannot be executed until the literal domain is given; steps 1-5, 7, 8 and 14-16 do not depend on
  it and can proceed.
- **The dispatch's own label is inconsistent** with its filename: the file is
  `prompts/preserved/2026-10-04-01.md`, its text says written 2026-10-04, and its title reads
  "Dispatch 2026-09-28-468". Not blocking; recorded so the index row names the right thing.
- **This is a desktop image, not a server image.** Nothing in the dispatch anticipates `lightdm`,
  `cups`, `wayvnc-control`, `bluetooth`, `ModemManager` or `triggerhappy` running on the serving
  origin. The owner has ruled to leave them.

### Decided beyond scope

Nothing. At the time this report was written no package had been installed, no service or user
created, no Cloudflare call made, and no file written outside the repository clone. The three
security steps (14-16) are **the owner's additions to this part**, not the session's, and remain
individually gated.

The only changes made on the Pi to produce this report: cloning the repository into the
pre-existing empty `/home/bwann83/Forager` (authorised), switching `origin` to SSH, and setting
`user.name` and `user.email` (authorised).
