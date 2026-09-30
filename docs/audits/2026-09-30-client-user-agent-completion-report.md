# Completion report: dispatch 2026-09-28-331, client-user-agent

Coder session, branch `client-user-agent`, base `origin/journal-redesign` at `30548ae6` (recorded at start; it has -325 merged). Dispatch: `prompts/preserved/2026-09-30-25.md`. Worktree `/home/zynergy-labs/Zynergy/forager-wt/client-user-agent`.

## Premises (all read, at the base)
- **The three builders:** `INaturalistClient.kt:22`, `OpenMeteoClient.kt:22`, `OpenMeteoArchiveClient.kt:26` each build `OkHttpClient.Builder()` with 15 s connect and 15 s read timeouts and, only when `debug`, a BASIC `HttpLoggingInterceptor`. No other interceptor, no cache, no retry setting. Each `create(debug)` is called **once**, as a `private val` in `AppContainer.kt:147-149`; it is not built per call.
- **No other HTTP client in `main/`.** Re-ran `grep -rnE "HttpURLConnection|URL\(|openConnection|URLConnection|Retrofit|OkHttpClient"` over `app/src/main`: hits were only the three clients, `MapHttpClient.kt` and doc comments. Scope: this covers the `main/` source tree; it does not cover what third-party libraries (MapLibre, image or crash libraries) open on their own. MapLibre's traffic is the map client (-325).
- **`MapUserAgent` / `mapHttpClient` / `installMapHttpClient`:** `installMapHttpClient(userAgent = MapUserAgent.forThisApp())` is called from `ForagerApplication.installMapHttpClientAtStart` (`ForagerApplication.kt:87-89`); `mapHttpClient` replaces the header with an inline interceptor. `-325` inferred OkHttp's default as `okhttp/4.12.0`; I did not read it from source and did not need to.

## The choice in step 1: moved, not reused
`MapUserAgent` became `app/net/AppUserAgent` (`git mv`, so history follows). Reusing it as it was would have made `data/remote` import from `app/map`, a UI-adjacent package, for a value that is not about the map; the repo's architecture rule keeps data and domain code free of such bindings. Both the format and `CONTACT` still exist once. Only the package, the object name and its KDoc changed.
- `MapHttpClient.kt`: import and references renamed. Nothing else.
- `MapUserAgentTest` (stays in the map package, since it tests the map client): only the references `MapUserAgent` -> `AppUserAgent` and one import. No assertion changed. 6 tests, all pass.

## What landed
- `ba6d634e` tests first, pushed failing: the move; an `internal fun httpClient(debug)` on each of the three client objects (the builder extracted verbatim from `create`, so a test can send through the real built client); `ClientUserAgentTest` (5 tests).
- `53322532` the fix: `net/UserAgentInterceptor` (sets `header("User-Agent", ...)`, replacing any existing value, default argument `AppUserAgent.forThisApp()`), added first in each of the three builders. Timeouts and debug logging untouched.
- `327a3641` merge of `origin/journal-redesign` (`git pull --no-rebase`, no conflicts; `docs/audits/README.md` kept every row).
- Report and README row: the commit after the merge.

## Red, then green
- **Red** (commit `ba6d634e`, before the interceptor existed): `ClientUserAgentTest` 4 of 5 failed at the header assertion (`ClientUserAgentTest.kt:68`): expected `[Forager/1.0.2187+gba6d634e (Android 16; com.zynergylabs.forager.app; +https://zynergy-labs.com; ...)]` but was the library-style value the test sent. The timeouts test passed, as it should (guards "everything else stays as it is"). `MapUserAgentTest` 6/6 passed. Build log: 0 `e:` lines.
- **Green** (`53322532`): 5/5 and 6/6.

## Revert check
Saved `OpenMeteoClient.kt` to `/tmp/OpenMeteoClient.saved`, deleted its `addInterceptor(UserAgentInterceptor())` line and import, ran `ClientUserAgentTest`. Build log: 0 `e:` lines (it compiled and ran). Failures: **"the Open-Meteo forecast client ..."** and **"the debug clients ..."** (the latter sends through all three clients and stops at the forecast one); the iNaturalist and archive tests, and the timeouts test, passed. Both failures are ones this edit could produce and no other. Restored from the saved copy, not from git; `git status` clean afterwards and `grep -c UserAgentInterceptor OpenMeteoClient.kt` = 2.

## Suite counts (from the XML, `app/build/test-results/testDebugUnitTest`, read after the run at `53322532`)
395 result files, **3211 tests, 24 skipped, 0 failures, 0 errors.** XML copied before the revert run was not needed for the full run, which came after it; the revert run's XML was overwritten by the full run. `assembleDebug`: BUILD SUCCESSFUL, 0 `e:` lines. The skips are `@Ignore`s already in the base; I did not add or touch any.

## The four disclosures
**Confirmed vs inferred.** Confirmed by running: each client's built `OkHttpClient` sends exactly one User-Agent equal to `AppUserAgent.forThisApp()`, even when the request already carried another (tests, Robolectric). Inferred: that `Retrofit` passes that client's interceptors on every call (standard behaviour; no test went through Retrofit's own `create`).
**Could not determine.** What goes on the wire on a device (no device used). Whether any library opens connections outside `main/` source (see scope above).
**Premises that were wrong.** None. The dispatch said "`5f1263e3` or later"; the head was `30548ae6`, which I used.
**Decided beyond scope.** (1) The `httpClient(debug)` extraction in each client is a refactor the dispatch did not list; it is needed to test the real built client, and `create` calls it unchanged. (2) `mapHttpClient` keeps its own inline interceptor rather than reusing `UserAgentInterceptor`; unifying them is possible but touches working map code. (3) The interceptor is added before the debug logging interceptor. (4) **The full suite and `assembleDebug` ran before the final merge.** The merge brought in 216 files (topo-night, -310, none in the files this change touches: checked with `git diff --name-only 53322532 HEAD` over `remote/`, `net/`, `MapHttp*`, `MapUserAgent*`, which printed nothing), and I did not rebuild after it.

## Device-only
On the S22 (owner-run): capture one request to `api.inaturalist.org` and one to `api.open-meteo.com` (and one to `archive-api.open-meteo.com` if reachable) and read the `User-Agent` header. Pass: exactly one header per request, equal to `Forager/<versionName> (Android <release>; com.zynergylabs.forager.app; +https://zynergy-labs.com; support@zynergy-labs.com)`.
