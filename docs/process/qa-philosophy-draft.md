# Forager's QA philosophy: a draft for the owner to rule on

**Status: draft.** Nothing here binds anyone until the owner rules on it.

Written 2026-10-16 by Forager's UX auditor, under dispatch 2026-10-16-03 (RECORD -1085), from main at `402489ae`. No QA philosophy existed before this one: none was found on main, pre-main, dev or records-after-173.

Each principle is marked one of two ways:

- **The owner's words**: quoted, with where they were said.
- **Proposed by the auditor**: new, with the reason and the established practice behind it.

The numbered questions at the end are the choices only the owner can make. Sources are in Appendix A.

## 1. What quality means for Forager

Forager is a field instrument. It is used in the woods, often one-handed, offline, in poor light and on a low battery, by someone whose attention is on the ground. Quality there means three things, in this order:

1. **It can be trusted.** What it shows is true, including when it doesn't know. It doesn't crash, lose records, or send anyone the wrong way.
2. **It asks nothing of the user.** It does the sensible thing by itself, reads at a glance, and never leaves anyone wondering whether it broke.
3. **It feels finished.** Smooth, consistent and polished, from the first screen of a task to the last.

The owner set the bar for main on 2026-10-16 (RECORD -1085):

> "Main is the sacred live production branch where all Play builds come from. So anything going into main from this point (not retroactive on already running coders), has to be tested, debugged, and approved for public use."

This document proposes what that means in practice.

## 2. The principles

### The owner's words

**Don't make me think.** CLAUDE.md, "UX defaults" (2026-09-30).
> "users hate to think. So the biggest principle to consider when designing, is to not make users think."

Test: count what the change asks the user to notice, decide or do. A state the user could misread as broken is an item.

**Report results honestly.** CLAUDE.md, "Errors and failure paths".
> "Partial or failed results are reported as such, never presented as success." "An unsupported feature or capability returns an explicit "unsupported," never a fabricated plausible value."

Test: for each way the change can fail, what does the screen say?

**Honest, then one step beyond.** The search bar's "Search a location" ruling, 2026-09-28 (docs/plans/journal-redesign.md, built under RECORD -34).
> "while bare accuracy is an easy thing to settle on, since it fulfills the honesty part, we need to go one step beyond that and give the user a spark or motivation to action based on that honesty."

Test: every empty, missing or failed state says what the user can do next.

**Every failure has a fallback.** RECORD -519.
> "If network isn't available I want to be confident I'm not sending people to get stranded. [...] My goal is that for each failure, there is another method available that can help fill the gaps".

Test: a change to position, navigation or alerts names each failure it can meet and what fills the gap. One with no fallback is listed as a gap.

**Nothing fully obstructs the map.** CLAUDE.md, "UX defaults"; RECORD -57, -58.
> "My idea is that nothing should fully obstruct the map view. All map chrome gets 80% opacity as a result."

Test: anything drawn over the map is at 80%, and a real finger's press or pan in the empty space around it reaches the map.

**Back retraces the way in.** RECORD -383 (with -255 and -384).
> "back out one step at a time, going exactly the way they came. That's how Forager defines back. Otherwise returning to map is a home function".

Test: each new screen is walked in, then backed out, as a step path.

**The camera screen never rotates.** RECORD -980, -981.
> "The camera is to never, ever, rotate like this. This is a cardinal sin".

Test: at every angle, on the phones and the tablet, the camera's layout stays put and only its icons turn.

**What the user set stays set.** CLAUDE.md, "UX defaults"; RECORD -1005.
> "remember their decision, the intent is to use it and having to switch it on every time can be annoying".

Test: set something, leave, come back. A reset is an item unless the owner made an exception for that case.

**Data reads at a glance.** RECORD -653, -943.
> "raw data is useful and honest, but put plainly in a UI amounts to clutter to a user, no matter how useful it is." "Distance by trail is the only honest framing. And keeping it to one number is less UI clutter".

Test: a new readout leads with one plain figure or a visual, clearly labelled.

**Polish is even, and smooth.** RECORD -650, -653.
> "A polished journal, and then a raw data dump as if we gave up at the last moment." "strike a fine balance between speed/utility and visual polish".

Test: the last screen of a task is as finished as the first, and on-screen changes move smoothly without slowing the task.

**Measured against what people already know.** RECORD -708.
> "That's a hit on quality when a mainstream maps app does it much better, and people are used to that."

Test: compare the change with how established apps do the same thing. A weaker version of a familiar feature is an item.

**Forager never identifies a mushroom.** docs/plans/mushroom-log.md ("Decided explicitly by the project owner"); RECORD -963; the owner's answer to Google Play, -1059.
> "It never identifies mushrooms or says what is safe to eat."

Test: no text, ordering or default reads as the app naming a species or judging whether it is safe to eat.

**No developer tools in what the public gets.** RECORD -926.
> "remove the walk logger and diagnostics from the app completely before releasing to testers. That's to remain with me only for dev purposes", then "Remove every trace".

Test: nothing from the dev branch's tools arrives in main.

### Proposed by the auditor

**A1. It starts, and doesn't crash, on a real phone.**
Why: on 2026-10-08 main crashed at launch on every phone while every automated test was green, because those tests never run the phone's own check of the app's code (RECORD -739). Prior art: Android's core app quality guidelines (no crashes or freezes); the other major mobile platform's review guidelines, section 2.1 "App Completeness" (test on a device; builds that crash are rejected); Google Play's crash and freeze thresholds, above which Play may show the app to fewer people.
Test: a cold start on each test phone with an empty crash log, then each changed path walked once.

**A2. An update never costs the user their records.**
Why: a debug build quietly wipes its records if an update is missing a data-upgrade step, and the Play build doesn't (the database code's own note). Only an upgrade over a real install shows what users will get. Prior art: Android's core app quality guidelines (state is restored after a relaunch; no accidental data loss).
Test: install the candidate over the current Play version holding test records, photos and settings. All of them survive.

**A3. Usable by people who see, reach or hear differently.**
Why: no ruling on accessibility was found, but the owner has asked for legibility ("Not too dark since it can make it harder to read", -37) and for left-handed users (-711). Prior art: WCAG 2.1 level AA; Android's core app quality accessibility items.
Test: the checks in Appendix B, on the screens the change touches.

**A4. It survives interruption and the field.**
Why: a walk is interrupted by calls, a locked screen, a pocket, gloves and sun. Prior art: Android's core app quality guidelines (switch away and back, sleep and wake, lock and unlock, and rotate three times, each without losing state); Forager's field-instrument design rules, which are inferred from its purpose and not yet measured in the field.
Test: each changed screen is interrupted mid-task in those ways and resumes where it was.

**A5. Nothing unfinished reaches the public.**
Why: the owner removed text because it "seems like a placeholder and users might get confused" (-695). Prior art: the other platform's section 2.1 ("placeholder text, empty websites, and other temporary content should be scrubbed"); Nielsen's heuristic "Aesthetic and minimalist design".
Test: no placeholder text, no dead-end screen, no control that does nothing.

**A6. Known bugs are listed and judged before release, never found first by users.**
Why: halting a staged rollout on Google Play stops new installs, but phones that already updated keep the update. Prior art: the Scrum Guide's Definition of Done (work that doesn't meet it "cannot be released").
Test: the promotion carries a list of every known bug it ships with, each marked new or already live, with what the user would meet.

**A7. What Forager says in public stays true.**
Why: the owner's answer to Google Play says every change "is checked on two phones and a tablet and on real field walks" (-1059). On 2026-10-15 the privacy policy still said backups were "not built yet", two weeks after they shipped (pre-production tidying inventory, section 6; corrected in the repository the same day, not yet published then).
Test: the privacy policy, the store text and the app's own words are read against what the promoted build does.

**A8. A change states its battery cost.**
Why: screen time, GPS and sensors are design choices, and battery work itself is deferred (-520). Prior art: Android's core app quality guidelines (no needless background work, keeping GPS on among their examples; support for the phone's power saving); Google Play's wake-lock threshold.
Test: a change that keeps the screen, GPS or a sensor on longer says by how much. See question 18.

## 3. "Tested, debugged, and approved for public use"

What the auditor reads for each promotion from pre-main into main.

**CI on pre-main.** The push run on pre-main at the exact commit being promoted is green: build, version check and the full test suite. The auditor also reads the change for tests skipped, deleted or weakened, and for growth in CI's list of allowed skips (CLAUDE.md, "Testing").

**What CI can't show.** It builds only the debug version. It never runs the phone's own code check (-739). Its test harness sees no system bars at the screen edges (CLAUDE.md, "Known pitfalls"). Its simulated taps reach a control even when something covers it (CLAUDE.md, "Testing"). A green run is needed, and is not enough.

**Device checks of a plain build.** Since -930 main has no developer tools, so every build of main is plain. Each check is written before it runs, with steps, a pass condition and the evidence for each step.

- **The S22 Ultra test phone** (no SIM, so GPS only), every promotion: the launch check, then each changed behaviour as a step path.
- **The S26 Ultra**, the owner's phone, every promotion.
- **The tablet**, when a screen changes, and always for the camera, because newer Android ignores the portrait request on wide screens (-980).
- **The upgrade** over the current Play version (A2).

**Only a phone can show:**

- whether the app starts at all (-739);
- layouts against real system bars, cut-outs and the navigation bar;
- whether a finger reaches the map or a control through whatever is drawn above it;
- the camera's fixed layout, on a phone and on the tablet;
- GPS, compass, height, alerts and recording with the screen off;
- legibility outdoors and at night;
- a screen reader and the largest font size;
- smoothness, heat and battery drain;
- an upgrade over a real install.

**Known bugs.** The list in A6, judged against question 19.

**Accessibility.** The checks in Appendix B.

**Copy.** Every new or changed word on screen is honest, gives a next step wherever something is empty or failed, is plain, contains no placeholder, and doesn't read as identification. Public text is read against A7.

## 4. How an audit runs and what it reports

1. The planner asks for an audit of a named pre-main commit, with the changes since the last promotion.
2. The auditor reads the evidence in section 3 and walks each changed behaviour as a step path against the principles.
3. It reports a verdict, its items, and what it couldn't check.

**The verdict:**

- **Pass**: nothing found that a user would meet.
- **Pass with items**: items listed, none of which, in the auditor's reading, risks a user's records, safety or trust. The owner ships or holds each one.
- **Fail**: at least one item that would crash the app, lose records, mislead about position, direction or time, read as identification, break one of the owner's rulings, or ship something unfinished.

**Each item says** where it is (the screen and the step path), what the user would experience, why it matters (the principle), and the evidence, or "not seen on a phone".

**The verdict advises; the owner decides.** The owner, verbatim: "Advises, you decide" (-1085). Nothing reaches main without the owner's approval.

**An urgent fix for a bug in the live app:** "Same gate, front of queue" (-1085). The same evidence, scaled to what the fix touches, ahead of everything else waiting.

## 5. What the auditor does not do

- It doesn't write app code, and it doesn't fix what it finds.
- It doesn't merge, and it doesn't decide on the owner's behalf.
- It doesn't silence, skip or weaken a test.
- It doesn't turn the owner's words into rules of its own, and it doesn't settle a conflict between two rulings. It asks.
- It doesn't put the owner's screenshots or device data on GitHub.

## 6. Questions for the owner

Each has a recommendation. Answer by number.

1. **Your words.** Are the thirteen quoted principles in section 2 the right set, each quoted correctly? *Recommended: confirm, and name any ruling that's missing.*
2. **A1, starts and doesn't crash on a real phone.** Adopt? *Recommended: adopt.*
3. **A2, an update never costs records.** Adopt? *Recommended: adopt.*
4. **A3, usable by people who see, reach or hear differently.** Adopt? *Recommended: adopt, with the bar in question 14.*
5. **A4, survives interruption and the field.** Adopt? *Recommended: adopt.*
6. **A5, nothing unfinished reaches the public.** Adopt? *Recommended: adopt.*
7. **A6, known bugs listed before release.** Adopt? *Recommended: adopt.*
8. **A7, what Forager says in public stays true.** Adopt? *Recommended: adopt.*
9. **A8, a change states its battery cost.** Adopt? *Recommended: adopt, with question 18.*
10. **The verdicts.** Are the three definitions in section 4 right, especially what counts as a fail? *Recommended: adopt as written.*
11. **Which build the phone checks use.** A debug build quietly wipes records when an upgrade step is missing; the Play build doesn't. Options: (a) CI's debug build only; (b) CI's build, plus your signed Play build for the upgrade check before upload; (c) also add a Play-type build to CI, which is a separate change to CI. *Recommended: (b) now; (c) with the release tidying list, where it is already proposed.*
12. **Field walks.** All Play builds come from main, so testers only meet a change after it is in main. Should a change to recording, navigation, alerts or position need a walk on a CI build of pre-main before promotion? *Recommended: yes, for those features only.*
13. **Tester builds.** On 2026-10-12 a tester build went out before the owed phone checks: "Ship to testers now" (-923). Under -1085 all Play builds come from main and everything going into main is tested. Does the gate now cover closed-testing builds too? *Recommended: yes.*
14. **The accessibility bar.** Full WCAG 2.1 AA now, or the checks in Appendix B now and full AA later? *Recommended: Appendix B now.*
15. **The camera and the accessibility standard.** WCAG 2.1 asks that a screen not be locked to one orientation unless that is essential (1.3.4, level AA). Your ruling keeps the camera's layout fixed, with its icons turning (-981). *Recommended: record the camera as your stated exception, so the auditor never flags it. It still works at any angle, because its icons turn.*
16. **Map chrome and contrast.** Text on a fill at 80% over the map changes contrast with whatever is beneath it. *Recommended: the auditor measures it over the lightest and darkest maps it can sit on and reports any shortfall as an item. The 80% rule stays yours.*
17. **Honest text that reads like a placeholder.** In -695 honest text was removed because it looked like a placeholder. In "Search a location" the honesty stayed and a next step was added. Which should the auditor recommend when it meets one? *Recommended: keep the honest message and add a next step; removal stays your call each time.*
18. **Battery while battery work is deferred (-520).** *Recommended: the auditor reports a change's battery cost as an item, and doesn't fail a promotion on battery alone until battery work is taken up.*
19. **Which known bugs may ship.** *Recommended: a bug may ship if it's listed, was already in the live app, and isn't a crash, lost records, a wrong position, direction or time, or anything that reads as identification. A new bug of those kinds is a fail.*
20. **Reverts.** When a live bug is fixed by undoing a change, does that need the full audit? *Recommended: a short one, confirming it matches the earlier approved state and starts on both phones, at the front of the queue.*
21. **Public text.** Does a change to the privacy policy or store text go through the audit even with no app change? *Recommended: yes, a copy-only audit against what the app does. Internal documents need none.*
22. **The platform's own backup.** Android's quality guidelines expect an app's data to survive the platform's backup and phone-to-phone transfer. Your 2026-09-09 ruling keeps that off for good, with Forager's own Backup instead (-116). *Recommended: the auditor treats Forager's Backup as meeting that guideline and never flags the switch-off.*
23. **The auditor's own checks.** May the auditor run its own read-only walk-through on the S22 with a CI build, on the planner's go? Forecast reviews already recompute results with their own code (D18). *Recommended: yes, on the S22 only; never on your S26 or the tablet, and never over a running recording.*
24. **This draft.** *Recommended: you answer 1 to 23, the auditor revises, and the ruled version goes into pre-main as docs/process/qa-philosophy.md on your word.*

## Appendix A. Sources

**Owner's rulings, RECORD.md on records-after-173:**

- -34: the "Search a location" change; the owner's words are in docs/plans/journal-redesign.md, section "Search bar copy" (main, line 584).
- -57, -58: nothing fully obstructs the map.
- -255, -383, -384: Back. The owner's definition is in -383. -255 records an instance (Back closes the bubble, then the fan).
- -519, -520: every failure has a fallback; battery work deferred.
- -650, -653, -654, -655: the three UI sweeps (motion, data friendliness, engineering hygiene) and the owner's choices on them, including "Log all, tell user where it matters".
- -695: honest text removed as placeholder-like.
- -708: Satellite removed; "a mainstream maps app does it much better".
- -711: the icon bar remembered "so left handed users don't need to change it".
- -739 to -745: the launch crash with every test green; the launch check script.
- -916: a beta tester's feedback. -917, -923, -924, -926, -930: the production release, the tester build, the owed checks, and the removal of developer tools from main.
- -943: "Distance by trail is the only honest framing".
- -963: the no-identification rule unchanged.
- -980, -981: the camera never rotates.
- -1005: remember the user's choice.
- -1059: the answers to Google Play, drafted by the planner and submitted by the owner.
- -1085: this role and the gate on main.
- -116: the owner's 2026-09-09 ruling that the platform's backup stays off.
- -37: "Not too dark since it can make it harder to read."

**Files:** CLAUDE.md (all); docs/process/planner-profile.md; docs/audits/2026-09-26-pre-main-working-trunk.md; docs/plans/journal-redesign.md; docs/plans/mushroom-log.md; docs/beta/README.md; prompts/preserved/2026-10-15-08-pre-production-tidying-inventory.md (records-after-173); the field-instrument design rules and the mobile design-principles reference (the owner's synced skills); .github/workflows/ci.yml; scripts/s22-launch-check.sh; app/src/main/AndroidManifest.xml; the database code's note on its debug-only wipe (app/src/main/java/com/zynergylabs/forager/app/data/local/ForagerDatabase.kt, lines 168-185 and 230-240).

**Prior art, read on the web on 2026-10-16:**

- Android's core app quality guidelines.
- Android's adaptive app quality guidelines (tablets and large screens).
- Google Play's Android vitals thresholds: crashes 1.09% of users overall and 8% on any one phone model; freezes 0.47% overall and 8% per model; excessive partial wake locks 5%.
- Google Play's pre-launch report and staged rollouts.
- WCAG 2.1 (W3C); W3C's note that it has no separate mobile guidelines, and its mobile guidance documents.
- Jakob Nielsen's ten usability heuristics (with Rolf Molich, 1990; refined 1994).
- Steve Krug, *Don't Make Me Think*, the source of the owner's professor's phrase.
- The other major mobile platform's app review guidelines, section 2.1, "App Completeness".
- The Scrum Guide (2020), "Definition of Done".

## Appendix B. Accessibility checks (proposed, for question 14)

On each screen the change touches:

1. Text contrast at least 4.5 to 1, or 3 to 1 for large text; controls and the parts of icons needed to understand them at least 3 to 1 (WCAG 1.4.3 and 1.4.11; Android's contrast item).
2. Touch targets at least 48 dp (Android's own minimum; WCAG 2.1's 44-pixel target is at its higher AAA level).
3. Every control has a label a screen reader speaks, and that label contains the words shown on screen (WCAG 4.1.2 and 2.5.3; Android's content-description item).
4. Text still fits and works at the phone's largest font size (WCAG 1.4.4).
5. Anything done with a pinch, two fingers or a drawn path also has a one-finger way (WCAG 2.5.1).
6. Messages such as "saved" or "off track" are announced to a screen reader without moving its focus (WCAG 4.1.3).
7. Works in portrait and landscape (WCAG 1.3.4), with the camera as question 15 decides.

## Appendix C. Notes for the planner

- **CI:** `.github/workflows/ci.yml` runs `assembleDebug` and `testDebugUnitTest` on pushes to main, pre-main and dev (`:13`, `:116`, `:184`). It has an exact-match skip allowlist (`:284-297`). No release-variant build runs on CI (tidying inventory, top-5 item 4).
- **The launch check** (`scripts/s22-launch-check.sh`) installs with `install -r`, has the phone verify the code, cold-starts the app and reads the crash log. It is not on CI, because CI has no phone.
- **Debug and release differ on records:** `ForagerDatabase.create` adds the destructive fallback only when `BuildConfig.DEBUG`. The code's note says the asymmetry is deliberate and not to be "fixed".
- **The dispatch cited -255 for "Back retraces the way in".** -255 is an instance (bubble first, then fan); the owner's definition is in -383, with the step path in -384. Both are cited above.
- **"Remove every trace" is -926, not -930.** -930's "Absolutely zero trace" was about privacy notes in a document, not about the tools.
