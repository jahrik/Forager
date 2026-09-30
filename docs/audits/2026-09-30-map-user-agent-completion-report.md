# Completion report: dispatch 2026-09-28-325, map-user-agent

Coder session, branch `map-user-agent`, base `origin/journal-redesign` at `a0a0c333` (newer than the
`912396c9` the dispatch named; `912396c9` is an ancestor). Dispatch: `prompts/preserved/2026-09-30-24.md`.

## Premises
- **"Nothing in `main/` sets a User-Agent for map tiles": confirmed.** `git grep -n -i "user-agent\|userAgent\|HttpRequestUtil\|OkHttpClient" -- app/src/main` hit only `INaturalistClient.kt:22`, `OpenMeteoClient.kt:22`, `OpenMeteoArchiveClient.kt:26` (each builds a plain `OkHttpClient` for Retrofit: 15 s connect and read timeouts, a BASIC logging interceptor in debug). No hit sets a header.
- **The contact is supplied.** The base commit `a0a0c333` carries the planner's continuation 2026-09-28-326 (`RECORD.md`), so the constant is `+https://zynergy-labs.com; support@zynergy-labs.com`, not `CONTACT_PENDING`. No value was invented. The `https://` scheme was the planner's addition.

## The MapLibre mechanism (13.5.0; `javap -p -c -constants` on `classes.jar` from the pinned `.aar`)
- **Where the library sets its User-Agent:** `HttpRequestImpl.executeRequest` does `Request.Builder.addHeader("User-Agent", userAgentString)` (bytecode offsets 94 to 99). `userAgentString` is `private static final`, built in `HttpRequestImpl.<clinit>` from `HttpIdentifier.getIdentifier()`, the literal `MapLibre Android/13.5.0`, a hash, `Build.VERSION.SDK_INT` and `Build.SUPPORTED_ABIS[0]`. It cannot be configured.
- **The hook:** `HttpRequestUtil.setOkHttpClient(okhttp3.Call$Factory)` sets `HttpRequestImpl.client`; `getHttpClient()` reads that static on every request and only builds a default when it is null. So an interceptor on the installed client sees MapLibre's header already added, and `header("User-Agent", ...)` replaces it.
- **Ordering:** `HttpRequestUtil.setOkHttpClient` loads `HttpRequestImpl`, whose `<clinit>` calls `HttpIdentifier.getIdentifier()`, which reads `MapLibre.getApplicationContext()`. So the install runs after `MapLibre.getInstance` (`ForagerApplication.kt:57`, directly after `initializeMapLibreAtStart()`). The install need not precede the first request, since the client is read per request, but `onCreate` precedes all of them anyway.
- **Offline region downloads share the client: INFERRED, not observed.** `NativeHttpRequest` (the native file source's only Java HTTP entry) calls `MapLibre.getModuleProvider().createHttpRequest()`, which yields `HttpRequestImpl`; I found no other Java HTTP path in `classes.jar`. Not checked on a device.
- **Kept from MapLibre's default client (`getOrCreateDefaultClient`/`getDispatcher`):** a plain `OkHttpClient` with a `Dispatcher` at 20 requests per host (10 below API 21; minSdk is 26). It sets no timeouts and no cache, and neither does the replacement.

## Other clients' User-Agents (report only; unchanged)
`INaturalistClient`, `OpenMeteoClient` and `OpenMeteoArchiveClient` set no User-Agent, so each sends OkHttp's default (`okhttp/4.12.0`; from knowing OkHttp's bridge interceptor, not read from source here). No other HTTP clients found in `app/src/main`, by the grep above plus `HttpURLConnection`, `URL(`, `openConnection` and `Retrofit.Builder`. The planner takes any gaps to the owner.

## What landed
- `39698452` tests first (pushed uncompiled, so failing at compile, not at an assertion).
- `dc94b94b` implementation: `map/MapUserAgent.kt` (the one builder: `build(...)`, `forThisApp()` from `BuildConfig.VERSION_NAME`, `Build.VERSION.RELEASE`, `BuildConfig.APPLICATION_ID`, and `CONTACT`), `map/MapHttpClient.kt` (`mapHttpClient`, `installMapHttpClient`), `ForagerApplication.installMapHttpClientAtStart()` (failure logged at WARN, leaving MapLibre on its default client).
- `c024a01d` fix to my test's fake response (it had no body). The first run of the two header tests failed for that reason, not the feature.
- `b0a78983` merge of `origin/journal-redesign` (`git pull --no-rebase`).
- Unchanged: tile URLs, caching, basemaps, the other clients.
- New library: none. `MockWebServer` is not a dependency, so the header test sends a request already carrying MapLibre's `User-Agent` through the real client with a recording interceptor last in the chain.

## Tests (`MapUserAgentTest`, 6)
Shape matches exactly; the app's string takes the version and id from `BuildConfig` and the release from the device; the contact is not the placeholder; **the client replaces the library's own header rather than adding a second** (asserts `headers("User-Agent")` is exactly one value, the app's); the dispatcher limit is 20; and starting `ForagerApplication` under Robolectric installs a client into `HttpRequestImpl.client` (read by reflection) that sends the app's string.

## Revert check (saved copy, not git)
`ForagerApplication.kt` saved to `/tmp/mua/`, the `installMapHttpClientAtStart()` call line deleted, `MapUserAgentTest` run. Build log: 0 `e:` or compile-error lines. XML timestamp `2026-09-30T22:39:46Z` (fresh). Result: 6 tests, 1 failure, the install test, message "ForagerApplication.onCreate installed no HTTP client into MapLibre", which is this edit's own. File restored from the saved copy; `git diff --stat` clean and the call present (2 occurrences of the method name). The first run of the install test, before the fake-body fix, also got past its not-null check, which is consistent.

## Suite and build
- Full `:app:testDebugUnitTest` on the merged tree: 394 classes, 3206 tests, 24 skipped, 0 failures, 0 errors (summed from XML saved to `/tmp/mua/xml/` before any revert run). No album flake or `DiagnosticsPanelTest` failure showed in this run.
- `:app:assembleDebug`: BUILD SUCCESSFUL, 0 `e:` lines.

## Four disclosures
- **Confirmed vs inferred:** confirmed by `javap` and tests: where MapLibre sets its header, the hook, the per-request read, the ordering constraint, and that the app installs a client that replaces the header. Inferred: offline downloads share the client; the other clients send `okhttp/4.12.0`.
- **Could not determine:** what reaches a tile server on the wire from a real device; whether OSM's servers accept this string (policy text not re-read here).
- **Premises that were wrong:** the dispatch said to keep the contact as `CONTACT_PENDING` until supplied; the base already carried the supply (-326), so I used it. The base was `a0a0c333`, not `912396c9`.
- **Decided beyond scope:** the 20-per-host dispatcher is copied from MapLibre's default so the replacement does not change its concurrency; a failed install is logged and leaves MapLibre's default header.

## Device-only
Capture one tile request's headers from the S22 and confirm `User-Agent: Forager/<versionName> (Android <release>; com.zynergylabs.forager.app; +https://zynergy-labs.com; support@zynergy-labs.com)`: route the phone through a proxy (for example mitmproxy with a trusted CA, or a debug-only interceptor log line), open the Street basemap, and read the request to `tile.openstreetmap.org`. Then start an offline region download and confirm its requests carry the same header (this settles the inference above).
