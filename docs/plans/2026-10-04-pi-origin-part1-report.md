# The Raspberry Pi 5 as Forager's tile origin: Part 1

Dispatch `prompts/preserved/2026-10-04-01.md` (titled "Dispatch 2026-09-28-468"), with **Amendment 1**
(the Pi hidden behind the Worker, an Access-protected origin hostname) and **Amendment 2** (two
Cloudflare accounts; everything for this part on the `zynergy-labs.com` account). Run by a Claude
Code session on the Pi itself, model `claude-opus-5[1m]` (Opus 5, 1M context). Reasoning effort is
not exposed to the session and so is not stated.

Base `main` at `88a5b785`, which is the commit the dispatch states it assumes and the commit
`origin/main` carried when this branch was cut. `origin/main` has since advanced to `61f2c363`,
which includes both amendments and **three new rows in `docs/audits/README.md`** — a merge conflict
is expected there and must be resolved by keeping every row (`CLAUDE.md`, serialization point).

**This report contains no addresses, credentials, tokens, tunnel IDs or key material.** The Pi's
`wlan0` carries a globally-routable IPv6 address as well as a private LAN IPv4; both are omitted, as
the home IP is. The tunnel UUID, the Cloudflare zone certificate, the R2 credentials and the Access
service token appear nowhere here.

## Summary

Part 1's target is met. The archive is on the Pi's NVMe, verified byte-for-byte against R2. A pinned
`pmtiles serve` serves it on localhost only. A pinned `cloudflared` reaches Cloudflare outbound-only
over QUIC, with no inbound port opened and no firewall change. `origin.zynergy-labs.com` is live and
refused to everyone except the holder of one Access service token. Both services run as a non-root
system user with bounded logs and restart-on-failure. The app and the Worker were not touched.

Not yet done: the reboot test, SSH password authentication (deliberately left on), and automatic
security updates. All three are recorded under "What remains".

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
| Power state | `throttled=0x0` — no under-voltage, no cap, no throttling, none since boot | `vcgencmd get_throttled` |
| Uptime before this work | 18 days | `uptime` |
| Boot device | **NVMe.** `/` is `/dev/nvme0n1p2`; `BOOT_ORDER=0xf416` | `findmnt`, `lsblk`, `vcgencmd bootloader_config` |
| NVMe | FIKWOT FN501Pro 256GB, 238.5 G | `lsblk` |
| Free disk | `/` 234 G total, **170 G available** before the archive, ~162 G after | `df -h` |
| Second volume | `/dev/mmcblk0p1`, 119.4 G **SD card** at `/mnt/archive`, 111 G free, unused | `lsblk`, `df -h` |
| Network | **Wi-Fi.** `wlan0` up, default route via `wlan0`. `eth0` **down, no carrier** | `ip -br addr`, `ip route`, `/sys/class/net/*` |

**What already ran**, before this work (`ss -tulpn`, as root): `sshd` on `0.0.0.0:22` and `[::]:22`
(**not** localhost); `cupsd` on `127.0.0.1:631` and `[::1]:631` (localhost only); `avahi-daemon` on
`0.0.0.0:5353` and ephemeral UDP ports (**not** localhost); `NetworkManager`'s DHCPv6 client on a
link-local address. 32 enabled services, no failed units.

**This is a desktop image, not a server image** — `lightdm`, `cups`, `cups-browsed`, `bluetooth`,
`ModemManager`, `wayvnc-control`, `triggerhappy` and `avahi-daemon` are all enabled. The owner ruled
to leave them.

## 2. Security baseline as found

- **Claude Code runs as `bwann83`** (uid 1000), in `sudo` among other groups, with
  `sudo -n -l` reporting `(ALL) NOPASSWD: ALL` — unrestricted, password-free root. Recorded because
  it means nothing on the Pi mechanically enforces the owner's step-by-step consent rule; the gate
  is the session's compliance, not permissions.
- **SSH enabled and active.** Effective `sshd -T` as found: port 22, listening `0.0.0.0` and `[::]`,
  `pubkeyauthentication yes`, **`passwordauthentication yes`**, `kbdinteractiveauthentication no`,
  `permitemptypasswords no`, `permitrootlogin without-password`. `/etc/ssh/sshd_config.d/` is empty,
  so no drop-in overrides.
- **No `authorized_keys` for any account** — not `bwann83`, not `root`. Access was password-only.
- **Automatic security updates off.** `unattended-upgrades` not installed; no
  `/etc/apt/apt.conf.d/20auto-upgrades`. The `apt-daily` timers refresh metadata and apply nothing.
- **No firewall.** `ufw` and `iptables` absent; `nft` present with an **empty ruleset**.

## 3. Installed among the five named, as found

None of `cloudflared`, `go-pmtiles`/`pmtiles`, `rclone`, Java or Docker. Present: `curl` 7.88.1,
`git` 2.39.5, `nft`, `gpg`. Absent and worth noting: `jq`, `gh`, `dig`.

**Push capability.** Tested with `git push --dry-run`, so nothing was created on the remote:
HTTPS push **fails** (`could not read Username`; no credential helper, no `~/.git-credentials`, no
`gh`); SSH push **succeeds** as `slayer8366`. The clone was HTTPS and was switched to SSH.
`user.name`/`user.email` were unset and were taken from this repository's own history (1862 commits
as `slayer8366`), not assumed.

## 4. What was built

Versions pinned per `CLAUDE.md` (Building), resolved from upstream release APIs on 2026-10-03.

| Component | Pin | Provenance |
|---|---|---|
| `go-pmtiles` | **v1.31.2** | tarball over HTTPS; **upstream publishes no checksums or signatures** |
| `rclone` | **v1.75.1** | `.deb` verified against a **PGP-signed** `SHA256SUMS` |
| `cloudflared` | **2026.9.3** | `.deb` verified through Cloudflare's **GPG-signed apt index** |

### Integrity, step by step

- **go-pmtiles.** The release carries no `checksums.txt`, and this is deliberate: `.goreleaser.yml`
  at tag `v1.31.2` reads `checksum: disable: true`. The owner chose to proceed on HTTPS alone and
  record the hashes computed on the Pi, for comparison on any future reinstall:
  - tarball `go-pmtiles_1.31.2_Linux_arm64.tar.gz` — `f8bd47e7ea866863489cad588fbaf2f31f42e5821f7a03f009b3769f05801cb1`
  - binary `pmtiles` — `8cd0affde1ba5380b7cea6de0f94c674f88e4f586c77ae5820ea9652862691f4`
- **rclone.** `SHA256SUMS` is PGP clearsigned. `Good signature from "Nick Craig-Wood
  <nick@craig-wood.com>"`, DSA key `FBF737ECE9F8AB18604BD2AC93935E02FF3B54FA`. That fingerprint was
  cross-checked across **four independent channels** — rclone.org's signing page, the `key.rclone.org`
  TXT record via Cloudflare's resolver, the same via Google's resolver, and the key file at
  `craig-wood.com` — which is what substitutes for web-of-trust certification. The `.deb` hash
  `773f3a76615f91f7d4654183a537afddce3343c8d99ac1d74984f060f2ade2d9` matched and `sha256sum -c`
  returned `OK`.
- **cloudflared.** The **GitHub release publishes no checksums or signatures**. Cloudflare's apt
  repository carries the identical version with a GPG-signed `InRelease`, so the `.deb` was taken
  from the repo pool and verified through the chain: `InRelease` signature good (`CloudFlare Software
  Packaging 2025`, RSA `CC94B39C77AE7342A68B89628A682D308D4E5E73`) -> `Packages` index hash matched
  -> `.deb` hash `bcce0111878f13d26e66b1d2ea7f270c8bde4bd549e32ce74d32474521583ca3` matched.
  **No persistent apt source was added**, deliberately, so that automatic upgrades cannot later move
  `cloudflared` off its pin.
  Two honest caveats: the `InRelease` carries two signatures and only one verified, the other key not
  being in Cloudflare's published bundle (consistent with rotation); and **this fingerprint could not
  be cross-checked across independent channels** — key and packages share one origin — so it verifies
  integrity in transit, not provenance from an independent root. Weaker than the rclone chain.

### The archive

Copied read-only from R2 with `rclone copyto … --s3-no-check-bucket` (`copyto`, not `copy`, per
`server/pmtiles-worker/README.md:76-80`, which records `copy` landing the object nested).

| Check | Expected | Got |
|---|---|---|
| Size | 8,817,909,309 bytes | 8,817,909,309 — **match** |
| MD5 | `5fd65536e74301e0333ac42ee61acf54` | identical — **match** |
| Readable as PMTiles | — | spec v3, mvt, clustered — **yes** |

The R2 object's MD5 has no multipart suffix, so it is a true whole-object hash and end-to-end
verification was possible; the dispatch's "where available, its checksum" clause is satisfied rather
than reported unavailable. `pmtiles show` was run **in addition** to size and hash, because a matching
hash proves the bytes arrived but not that the installed binary can parse them. It reports max zoom
14 and bounds matching `README.md:60-62`; `planetiler:osm:osmosisreplicationtime 2026-08-19T04:00:00Z`,
matching the `20260819.pmtiles` build and the object's ModTime; and **Protomaps Basemap 4.15.2**, the
same version survey section 1 recorded the live Worker serving. The Pi's copy and production are the
same archive.

The transfer took **85 minutes at 1.32 MiB/s** over Wi-Fi. `Multi-thread Copied` in the log confirms
rclone was already using parallel streams, so single-threading is not the explanation. The file is at
`/srv/forager-tiles/us.pmtiles` on the NVMe (the owner's choice over the SD card at `/mnt/archive`).

### Services

Both run as **`forager-tiles`**, a system account with no password, no login shell
(`/usr/sbin/nologin`), no sudo. Both carry `NoNewPrivileges`, `PrivateTmp`, `PrivateDevices`,
`ProtectSystem=strict`, `ProtectHome`, `ProtectKernel*`, `RestrictAddressFamilies`,
`RestrictNamespaces`, `LockPersonality`, `ReadOnlyPaths=/srv/forager-tiles`, restart on failure, and
`LogRateLimitIntervalSec=30s` / `LogRateLimitBurst=1000`.

**`forager-tiles.service`** —
`pmtiles serve /srv/forager-tiles --interface 127.0.0.1 --port 8080 --cache-size 256
--public-url https://tiles.zynergy-labs.com`.

**`forager-tunnel.service`** — `cloudflared … tunnel run`, `Requires=forager-tiles.service`,
config at `/srv/forager-tiles/.cloudflared/config.yml` with `protocol: quic`, `no-autoupdate: true`,
and ingress routing `origin.zynergy-labs.com` to `http://127.0.0.1:8080` with a `http_status:404`
catch-all. `cloudflared tunnel ingress validate` returns `OK` and rule #0 matches the hostname.

**Journal bounded:** `SystemMaxUse=2G`, `SystemMaxFileSize=64M`, read back via
`systemd-analyze cat-config` rather than from the file. Usage was 966.7 MB before and after the
restart — the owner chose a cap above current usage so **no history was vacuumed**. The previous
effective cap was the 10%-of-filesystem default, about 23 GB.

### Two findings that changed the target's shape

Both read from go-pmtiles source at the pinned tag and then **confirmed against the installed binary**,
which is the check `CLAUDE.md` asks for — a source claim held up against something outside itself.

1. **`pmtiles serve` defaults to `--interface 0.0.0.0`** (`main.go:105`; the installed binary's
   `--help` prints `--interface="0.0.0.0"`). Localhost-only is an explicit flag, not a default. On a
   Pi with an empty nft ruleset, taking the default would have published the tile server to the LAN.
2. **`/us.json` returns HTTP 501 `"PUBLIC_URL must be set for TileJSON"`** without `--public-url`
   (`pmtiles/server.go:292`). The dispatch's stated verification could not have passed as written.
   With the flag set, `/us.json` returns 200 and its `tiles` array reads
   `https://tiles.zynergy-labs.com/us/{z}/{x}/{y}.mvt` — the public Worker name, per Amendment 1.

### The tunnel and its protection

Tunnel `forager-origin` created on the **zynergy-labs.com** account (Amendment 2), with one proxied
CNAME for `origin.zynergy-labs.com`. Credentials written by `cloudflared` at mode `0400`, owned by
`forager-tiles`. Four connections registered, `protocol=quic`, edge locations 2x pdx03, 1x sea10,
1x sea11.

A Cloudflare **Access application** on `origin.zynergy-labs.com` with a **Service Auth** policy
(not Allow) including service token `forager-worker`, created by the owner in the dashboard. The
token's two values were entered by the owner directly into `/etc/forager/access-token.env`, root-owned
`0600` — **the session never saw them**, and `forager-tiles` cannot read the file. Values were
validated by shape and by behaviour, never by being read.

**Ordering was deliberate:** the Access application was created *before* the tunnel service was
started, so `origin.zynergy-labs.com` was never a publicly reachable unauthenticated tile server.
Before the tunnel ran, an unauthenticated request returned **403 with `cf-access-domain:
origin.zynergy-labs.com`** — distinguishing "Access is protecting it" from the earlier **530**
("no origin available"), which 530 alone could not.

### Step 12, the paired check

| | Without token | With token |
|---|---|---|
| `/us.json` | **HTTP 403** | **HTTP 200**, `application/json`, 10,427 bytes |
| z5 tile `us/5/7/12.mvt` | **HTTP 403** | **HTTP 200**, `application/x-protobuf`, 63,032 bytes |

Refusals carry `cf-access-domain: origin.zynergy-labs.com` and `server: cloudflare`, so Access
rejects at the edge and the tile server is never reached. Both paths were tested, not just TileJSON.

**The tile is byte-identical to the one served locally.** The two SHA-256 hashes differed at first
because the tunnel-delivered copy arrived decompressed (63,032 bytes) while the localhost copy was
gzipped (45,472). After decompressing, both are
`0f3bbaeee00d83a1615385f8f2c582e58d93aaba543eeb55e56149f0f45ae58d` and `cmp` is clean. Recorded
because a size-only check would have read as a mismatch and a hash-only check as a failure; only
normalising the encoding gives the right answer.

The authenticated requests passed the token through a `curl` config on stdin, never as a command-line
argument, so the secret did not enter the process table or any log.

### Access control, as left

A separate unprivileged user **`planner`** (uid 1001, group `planner` only, not in `sudo`, no
password) holds the planner laptop's key at `/home/planner/.ssh/authorized_keys`, `0600` in a `0700`
directory. Key `ssh-ed25519`, fingerprint `SHA256:CF9q6JZd4bpTLf948IazCPcpZOP5mWvQOUQXGni1KDQ`,
confirmed by the planner against their own copy before installation.

The dispatch asks for **read-only** access; adding the key to `bwann83` would have granted
passwordless root, since that account has `NOPASSWD: ALL`. The owner chose the separate account.
Read-only was **verified by testing**, not assumed: as `planner`, reads of
`/etc/forager/access-token.env`, the Cloudflare zone certificate and `bwann83`'s GitHub key are all
denied. The planner confirmed login works with no password prompt, `sudo` refused, both services
visible, and `127.0.0.1:8080/us.json` returning 200.

## 5. Pre-reboot baseline

Captured before the reboot test, so that a post-reboot comparison has something to compare against:

| | |
|---|---|
| `forager-tiles` | enabled, active, listening `127.0.0.1:8080` |
| `forager-tunnel` | enabled, active, 12 connection registrations logged |
| Local `/us.json` | HTTP 200 |
| Through the tunnel, no token | HTTP 403 |
| Archive | 8,817,909,309 bytes |
| Uptime | 2 weeks 4 days |

That last row matters: this Pi had not rebooted in 18 days and has never booted with any of this
configuration present, so an unrelated pre-existing problem could surface at the reboot and look like
this work's fault. This report exists before the reboot partly so the two can be told apart from a
written record rather than from memory (`CLAUDE.md`, "Push before you tidy").

## 6. Observations worth carrying forward

- **cloudflared's own startup precheck reports `hard_fail=true`** — UDP and TCP connectivity to
  `region1`/`region2.v2.argotunnel.com` and to `api.cloudflare.com:443` all fail — **yet all four
  connections then register successfully over IPv6.** The prechecks appear to test IPv4 only, and
  this Pi's working path to Cloudflare is IPv6. The precheck's verdict is therefore wrong about the
  outcome. Anything later that depends on **IPv4** egress to Cloudflare may not work; the dispatch's
  UDP 7844 question resolves as "yes, over IPv6".
- **The Wi-Fi link is weak.** Measured during the archive copy: **-65 dBm**, RX bitrate collapsing
  from 45 to 6 MBit/s between samples seconds apart, **4,075 TX failures** of 246,242 packets (1.65%),
  about 12 Mbit/s effective. This is the **likely but not confirmed** cause of the 85-minute copy; a
  second transfer from an unrelated host would be needed to separate it from an ISP cap or R2-side
  throttling, and that test competes for the same link, so it was not run. Survey section 6 estimates
  ~250 GB/month uploaded at 10,000 users with peaks well above the 0.8 Mbps average — that figure
  should be weighed against a re-measurement after the Pi is moved closer to the router. **Ethernet
  was rejected as unavailable, not on the merits.**
- **A unit-file failure worth recording.** The tunnel's first unit used `Type=notify`; `cloudflared`
  sends no readiness notification, so systemd waited, timed out and killed a tunnel that had been
  connecting normally. Diagnosed before changing anything, from `Failed with result 'timeout'`
  alongside healthy `Initial protocol quic` startup logs, with the teardown following systemd's
  give-up rather than causing it. `Type=simple` plus `TimeoutStartSec=60` fixed it.

## 7. Handback for the planner

1. **Two Cloudflare accounts** — found by this session before step 10 and now settled by Amendment 2
   and records -480/-481. Everything this part created is on the **zynergy-labs.com** account. The
   R2 read token on the Pi still belongs to the **old** account's bucket and was left as is; the
   archive copy does not depend on which account serves it later.
2. **Workers VPC was researched and is not used.** Cloudflare's docs state Workers VPC supports both
   tunnel types but *"we recommend creating a remotely-managed tunnel through the dashboard"*;
   it requires `cloudflared` **2025.7.0+** (satisfied), **QUIC** transport (configured and confirmed
   in the connection logs), and outbound **UDP 7844**. It is **in beta** and **free on all Workers
   plans**, which confirms survey section 6's claim. The owner ruled locally-managed, no VPC.
   Structurally the two differ: a public-hostname tunnel versus a registered VPC Service with no
   public name — that is very likely *why* the dashboard-managed tunnel is recommended, since VPC
   registration lives on Cloudflare's side while locally-managed ingress lives in a file on the Pi.
3. **A Worker Custom Domain question the next part must answer.** With the map service moving to the
   zynergy-labs account (Amendment 2), this may be moot — but it should be checked rather than
   assumed. Cloudflare's docs say a Custom Domain cannot be created *"on a zone you do not own"* and
   require an active zone, but **do not explicitly address the cross-account case**. The mechanism
   suggests same-account is required, since attaching a Custom Domain creates a DNS record in that
   zone from the Worker's account. **Unverified** — this session had no Cloudflare read access.
4. **The Worker will need the Access service token's two headers** (`CF-Access-Client-Id`,
   `CF-Access-Client-Secret`) to reach `origin.zynergy-labs.com`. They are on the Pi at
   `/etc/forager/access-token.env`, root-only, and must be set as Worker secrets in the next part —
   never committed.
5. **`cloudflared` has no apt source**, by design. Automatic upgrades cannot move it off its pin; a
   deliberate step is needed to update it.
6. **`docs/audits/README.md` will conflict.** `origin/main` gained three rows while this branch was
   open. Merge, never rebase, and keep every row.

## Disclosure

### Confirmed

Every row of sections 1, 2 and 3 from the command named beside it. Both push probes. Both go-pmtiles
findings, at source and against the installed binary. All three version pins and all three integrity
chains. The archive's size, MD5 and parseability. Every service state, bind address, permission and
ownership quoted. The step 12 results including the byte-identity of the tile after normalising
compression. The tunnel's four QUIC registrations. The Access 403/200 pair. The planner account's
inability to read the three sensitive files. The base commit, the drift to `61f2c363`, and both
amendments.

### Inferred, not verified

That the weak Wi-Fi link is the cause of the slow copy rather than an ISP cap or R2 throttling. That
the router does not forward 22 or 5353 — not probed. That `--public-url` set to the public name is
what downstream TileJSON consumers need — read from source, not exercised by a real client. That a
Worker on another account can present this Access service token successfully: a service token is two
HTTP headers and the client's account should not matter, but the documentation does not say so.

### Could not determine

- **The power supply's rating.** `throttled=0x0` is the only power evidence and it is good news, but
  there is no `max_current_a` device-tree node and no PMIC line in `dmesg`, so survey section 6's
  `5 V 5 A supply` claim, marked `[C]`, **cannot be confirmed from the Pi** and should be read as
  unverified.
- **Whether `cloudflared` 2026.9.3 exposes a `--protocol` flag.** It appears in no help output;
  the string `quic` is in the binary and `protocol: quic` in the config file works, confirmed by
  `Initial protocol quic` and `protocol=quic` in the registration logs. Behaviour was verified
  instead of the flag's documented default.
- **Whether a Worker Custom Domain works cross-account.** See handback item 3.
- **Whether both services survive a reboot.** Not yet tested; see What remains.

### Premises that were wrong

- **`pmtiles serve` binds `0.0.0.0` by default**, so "localhost only" is an explicit flag.
- **`/us.json` 501s without `--public-url`**, so the dispatch's check could not pass as written.
- **The Pi is on Wi-Fi, not Ethernet.** `eth0` is down with no carrier.
- **`zynergy-labs.com` and the `forager-maps` bucket are on different Cloudflare accounts** — not
  anticipated by the dispatch or Amendment 1; settled by Amendment 2.
- **go-pmtiles publishes no checksums** (deliberately, `checksum: disable: true`), and
  **cloudflared's GitHub release publishes none either** — the plan had promised verification against
  a `checksums.txt` that does not exist.
- **A Cloudflare Access service token secret is not 64 hex characters.** This session expected that
  format and the real value did not match it; the authoritative test was behavioural — Access
  accepted the token — and the format expectation was simply wrong.
- **`cloudflared` does not support `Type=notify`.**
- **The dispatch's title and filename disagree** — `2026-10-04-01.md`, written 2026-10-04, titled
  "Dispatch 2026-09-28-468".
- **Adding the planner's key to `authorized_keys` would not have been read-only access**, since the
  obvious account for it has `NOPASSWD: ALL`.

### Decided beyond scope

Nothing was decided beyond scope by the session. Every change was put to the owner first with what it
changed and how to undo it, and several were reordered or redirected on the owner's word: the archive
onto the NVMe, the journal cap raised to keep existing history, the rclone config moved rather than
copied, the separate `planner` account, and step 16 deferred.

Three items in this part are **the owner's additions** to the dispatch rather than the session's:
automatic security updates, the planner's key, and disabling SSH password authentication.

## What remains

- **The reboot test.** `enabled` is a claim about configuration, not evidence that the services come
  back. Approved; the report is being pushed first so the record survives the reboot.
- **SSH password authentication is still ON, deliberately.** `bwann83` and `root` have **no**
  `authorized_keys`, so disabling it would leave `planner` — unprivileged — as the only remote entry
  and the owner reachable only by keyboard and monitor, on a Pi whose `eth0` is down. The owner chose
  to defer the decision until after the reboot passes.
- **Automatic security updates** are not yet enabled. Approved with Debian's security-only default,
  **no automatic reboot**, and no mail configuration (there is no MTA, so failures would be silent in
  `/var/log/unattended-upgrades/`).

## Rollback

| Step | Undo |
|---|---|
| go-pmtiles | `rm /usr/local/bin/pmtiles` |
| rclone | `apt-get purge rclone` |
| R2 config | delete `/srv/forager-tiles/.config/rclone/rclone.conf` |
| Archive | `rm -rf /srv/forager-tiles` (8.2 GiB, 85 minutes to re-copy) |
| Service user | `userdel forager-tiles`, restore ownership |
| Tile service | `systemctl disable --now forager-tiles`, remove unit, `daemon-reload` |
| Journal cap | restore `/etc/systemd/journald.conf.pre-pi-origin`, restart `systemd-journald` |
| cloudflared | `apt-get purge cloudflared` |
| Zone cert | delete `/srv/forager-tiles/.cloudflared/cert.pem` |
| Tunnel + DNS | delete the DNS record in the dashboard, then `cloudflared tunnel delete forager-origin` |
| Access app + token | delete both in the Zero Trust dashboard; remove `/etc/forager/` |
| Tunnel service | `systemctl disable --now forager-tunnel`, remove unit, `daemon-reload` |
| Planner account | `userdel -r planner` |

Backups kept before editing, as copies rather than git restores (`CLAUDE.md`, revert-runner pitfall):
`/etc/systemd/journald.conf.pre-pi-origin` and `/etc/systemd/system/forager-tiles.service.pre-step7`.

---

# Addendum, 2026-10-04: the reboot test, and three changes after it

Appended after the body above was pushed at `07030cb8`, deliberately before the reboot so the record
would survive it (`CLAUDE.md`, "Push before you tidy"). The Pi rebooted at 23:52 PDT; boot
`829add4d` succeeded the boot that had been running 18 days.

## The reboot test: the configuration survives, but the tunnel was down about three minutes

| Check | Baseline | After reboot |
|---|---|---|
| `forager-tiles` | enabled, active | **enabled, active** |
| `forager-tunnel` | enabled, active | **enabled, active**, after 29 restarts |
| Listener | `127.0.0.1:8080` | **`127.0.0.1:8080`** only |
| Local `/us.json` | 200 | **200** |
| Local z5 tile | 45,472 bytes | **45,472 bytes** |
| Through the tunnel, no token | 403 | **403** |
| Tunnel connections | 4 | **4** |
| Archive | 8,817,909,309 bytes | **identical**, owner `forager-tiles` |

Stability afterwards: zero registrations and zero terminations in a clean 60-second window, five
probes returning 403 in 0.14 to 0.55 s with one 5.2 s outlier. **`enabled` is now evidence rather
than a claim**, which is what the test existed to establish.

### What went wrong, and what it was not

`forager-tunnel` crash-looped **29 times** between 23:53:36 and 23:56:31, each cycle exiting on
`Failed to fetch features ... lookup cfd-features.argotunnel.com on [::1]:53: connection refused` —
Go falling back to `[::1]:53` because `/etc/resolv.conf` was not yet populated, and nothing listening
there. `Restart=always` recovered it once DNS worked.

**The cause is the Wi-Fi association, not these units, and that was checked rather than assumed.**
`NetworkManager-wait-online` **failed this boot** on its 60 s timeout but **succeeded on both
previous boots**, taking 36 s on the last. This boot `wlan0` cycled through `need-auth` repeatedly
and reached `config -> failed (reason 'no-secrets')` twice before connecting at about 23:56. The
three saved Wi-Fi profiles are all `wpa-psk` with no `permissions` and no `psk-flags`, so secrets are
system-wide and available at boot; `no-secrets` here follows repeated authentication failure, which
is consistent with the -65 dBm signal measured during the archive copy. These units reference only
`network-online.target` and cannot delay NetworkManager. `NetworkManager-wait-online` is left as a
**failed unit** on this boot.

**Why this matters beyond the test.** For a box whose job is to be a serving origin, this is the
substantive finding: after a power cut the Pi is unreachable for minutes, and if Wi-Fi authentication
failed outright it would not return without intervention. Survey section 6 lists a UPS as a later
item; this suggests the Wi-Fi link is the more pressing half. It also sharpens the deferred step 16:
disabling password authentication on a Pi that may take minutes to join the network, with no key for
`bwann83` and `eth0` down, narrows the recovery path considerably.

## Change 1: automatic security updates (step 14)

`unattended-upgrades` installed and enabled, with
`/etc/apt/apt.conf.d/20auto-upgrades` setting `Update-Package-Lists "1"` and `Unattended-Upgrade "1"`.

**A correction to what was described when this was approved.** It was put to the owner as Debian's
default being "security updates only". It is not: the shipped `Origins-Pattern` also carries
`origin=Debian,codename=${distro_codename},label=Debian`, which is general stable updates. Because
the owner's instruction was security-only, `/etc/apt/apt.conf.d/52forager-security-only` now narrows
it to the two `Debian-Security` patterns. A first attempt simply declared the narrower list and
**did not work** — apt.conf lists accumulate, so `label=Debian` survived; `#clear
Unattended-Upgrade::Origins-Pattern;` before the declaration was required. Verified by reading
`Allowed origins are:` back from `unattended-upgrade --dry-run --debug`, not from the file.

- **No automatic reboot** (`Automatic-Reboot` unset, defaults false), as instructed.
- **No mail configuration** — there is no MTA on this Pi, so failures are silent and land in
  `/var/log/unattended-upgrades/`.
- **The Raspberry Pi archive is not covered, so the kernel and firmware are not updated
  automatically.** `archive.raspberrypi.com` publishes as `origin=Raspberry Pi Foundation,
  label=Raspberry Pi Foundation`, matching neither allowed origin. Confirmed from behaviour, not
  inference: `unattended-upgrade --dry-run --debug` inspects each of these packages and then logs
  `adjusting candidate version:` back to the **installed** version, declining the available upgrade
  because its origin is not permitted. Three updates were already pending when this was written and
  will not be applied by any automatic run:

  | Package | Installed | Available |
  |---|---|---|
  | `linux-image-rpi-2712` | 1:6.12.96-1+rpt1 | 1:6.12.109-1+rpt1 |
  | `raspi-firmware` | 1:1.20260521-1~bookworm | 1:1.20260915-1~bookworm |
  | `rpi-eeprom` | 28.27-1 | 28.33-1 |

  **These need a deliberate manual update and a reboot**, which no timer on this Pi will do:
  `sudo apt update && sudo apt upgrade` pulls them, and a kernel, bootloader or EEPROM change only
  takes effect after a restart. `Automatic-Reboot` is off by design, so even if the origin were
  allowed the Pi would run the old kernel until someone rebooted it. That is a standing maintenance
  obligation for this box, not a one-off.

  Two consequences specific to this deployment. A kernel this far behind matters more than usual
  here because **`firmware-brcm80211`, the Wi-Fi driver firmware, also comes from this archive** —
  the same archive as the kernel — and the open problem on this Pi is Wi-Fi association at boot. A
  kernel or Wi-Fi firmware update is therefore a plausible, untested input to that problem and
  should be tried before concluding the link is purely a signal-strength matter. And the reboot
  these updates require is the same reboot that takes the tile origin offline for minutes, so it is
  a deliberate maintenance window rather than something to do casually.

- Other Raspberry Pi archive packages — `chromium` and similar — are likewise untouched.
- **The pins hold.** `cloudflared` has no apt source at all (deliberate, see the body).
  `pmtiles` is a bare binary and invisible to apt. **`rclone` needed a correction to an earlier
  claim**: Debian bookworm *does* carry `rclone`, so the statement that no rclone apt source exists
  was wrong. It is nonetheless safe, because the installed 1.75.1 is higher than the repository's
  1.60.1, so apt reports `Candidate: 1.75.1` and the package does not appear in the upgrade
  candidate list at all. A dry run upgrades nothing today.

## Change 2: the tunnel waits for working DNS

`/usr/local/sbin/forager-wait-dns` (root, `0755`) polls `getent hosts region1.v2.argotunnel.com`
every 2 s for up to 60 s, and is wired in as `ExecStartPre` on `forager-tunnel.service`.
`TimeoutStartSec` was raised from 60 s to 150 s in the same change — at 60 s systemd would have
killed the unit while the pre-check was still inside its own budget, which would have replaced one
failure mode with another.

The script exits 0 either way, so a persistent DNS failure still starts cloudflared and surfaces as
cloudflared's own error rather than being hidden. This does not fix the Wi-Fi; it stops a slow
association from producing 29 restart cycles that bury the real cause.

Verified: the script returns `DNS usable after 0s`, the unit restarts cleanly, 4 connections
register, and the pair still holds — **403 without the token, 200 with it** (10,427 bytes).

## Change 3: Wi-Fi profile priority

The active Wi-Fi profile (named by the owner in window; SSID withheld here) set to `connection.autoconnect-priority 100` with `autoconnect yes`. **No profile
was deleted and no other profile was modified**, as instructed.

**This is unlikely to fix the boot delay on its own, and should not be recorded as a fix.**
That profile was *already* the highest-priority one (1, against 0 for the other two) and was already the active connection. That weakens the working hypothesis that
NetworkManager was cycling through candidate profiles: it was not choosing between them so much as
failing to authenticate on a weak link. The change makes the preference emphatic and costs nothing;
the association problem itself remains open and is expected to be addressed by moving the Pi closer
to the router. **A second reboot after the move is the test that would settle it** — one boot is a
single data point, and the two preceding boots behaved differently.

Applied without disrupting the live link: NetworkManager stayed `connected` and the tunnel continued
to answer through the change.

## Still outstanding

- **Step 16, SSH password authentication, remains ON** at the owner's instruction, on hold until the
  Pi has been moved. `bwann83` and `root` still have no `authorized_keys`; `planner` is the only
  account with one.
- **The Wi-Fi association** at boot, per change 3.
- **`NetworkManager-wait-online` is a failed unit** on the current boot.
- **Kernel, bootloader and EEPROM updates are a manual, standing obligation** — not covered by
  unattended-upgrades, and three were already pending when this was written. See change 1. Worth
  trying against the Wi-Fi association problem before concluding it is signal strength alone.
- A **UPS**, and the Worker work, both already out of scope for this part.

## Rollback for the addendum

| Change | Undo |
|---|---|
| unattended-upgrades | `apt-get purge unattended-upgrades`, `rm /etc/apt/apt.conf.d/20auto-upgrades /etc/apt/apt.conf.d/52forager-security-only` |
| Security-only narrowing alone | `rm /etc/apt/apt.conf.d/52forager-security-only` |
| DNS wait | restore `/etc/systemd/system/forager-tunnel.service.pre-dnswait`, `rm /usr/local/sbin/forager-wait-dns`, `daemon-reload`, restart |
| Wi-Fi priority | `nmcli connection modify <profile> connection.autoconnect-priority 1` |
