# Forager's QA philosophy: a draft for the owner to rule on

**Status: draft.** Nothing here binds anyone until the owner rules on it.

Written 2026-10-16 by Forager's UX auditor, under dispatch 2026-10-16-03 (RECORD -1085), from main at `402489ae`. No QA philosophy existed before it: none was found on main, pre-main, dev or records-after-173.

Each principle is marked one of two ways:

- **The owner's words**: quoted exactly, with where they were said. "[...]" marks a cut.
- **Proposed by the auditor**: new. Its reason and prior art are in Appendix C.

The numbered questions in section 6 are the choices only the owner can make.

## 1. What quality means for Forager

Forager is a field instrument. It is used in the woods, often one-handed, offline, in poor light and on a low battery, by someone whose attention is on the ground. Quality there means three things, in this order:

1. **It can be trusted.** What it shows is true, including when it doesn't know. It doesn't crash, lose records, or send anyone the wrong way.
2. **It asks nothing of the user.** It does the sensible thing by itself, reads at a glance, and never leaves anyone wondering whether it broke.
3. **It feels finished.** Smooth and consistent, from the first screen of a task to the last.

The owner set the bar for main on 2026-10-16 (RECORD -1085): "Main is the sacred live production branch where all Play builds come from. So anything going into main from this point (not retroactive on already running coders), has to be tested, debugged, and approved for public use." The planner recorded the promotion steps in -1086: CI green on pre-main, phone checks of a plain pre-main build, this auditor's report, then the owner's approval. This draft proposes what each step means in practice.

## 2. The principles

### The owner's words

**Don't make me think.** "users hate to think. So the biggest principle to consider when designing, is to not make users think." (CLAUDE.md, "UX defaults")
*Test:* count what the user must notice, decide or do. A state they could misread as broken is an item.

**Report results honestly.** "Partial or failed results are reported as such, never presented as success." "An unsupported feature or capability returns an explicit "unsupported," never a fabricated plausible value." (CLAUDE.md, "Errors and failure paths")
*Test:* for each way the change can fail, what does the screen say?

**Honest, then one step beyond.** "while bare accuracy is an easy thing to settle on, since it fulfills the honesty part, we need to go one step beyond that and give the user a spark or motivation to action based on that honesty." (the "Search a location" ruling, 2026-09-28, in docs/plans/journal-redesign.md; built under RECORD -34)
*Test:* every empty, missing or failed state says what the user can do next.

**Every failure has a fallback.** "If network isn't available I want to be confident I'm not sending people to get stranded. [...] My goal is that for each failure, there is another method available that can help fill the gaps". (RECORD -519)
*Test:* a change to position, navigation or alerts names each failure it can meet and what fills the gap. One with no fallback is listed as a gap.

**Nothing fully obstructs the map.** "My idea is that nothing should fully obstruct the map view. All map chrome gets 80% opacity as a result." (CLAUDE.md, "UX defaults"; RECORD -57, -58)
*Test:* anything over the map is at 80%, and a real finger's press or pan beside it reaches the map.

**Back retraces the way in.** "back out one step at a time, going exactly the way they came. That's how Forager defines back. Otherwise returning to map is a home function". (RECORD -383; also -255, -384)
*Test:* each new screen is walked in, then backed out, as a step path.

**The camera screen never rotates.** "The camera is to never, ever, rotate like this. This is a cardinal sin". (RECORD -981; also -980)
*Test:* at every angle, on the phones and the tablet, the camera's layout stays put and only its icons turn.

**What the user set stays set.** "remember their decision, the intent is to use it and having to switch it on every time can be annoying". (RECORD -1005; CLAUDE.md, "UX defaults")
*Test:* set something, leave, come back. A reset is an item unless the owner made an exception for that case.

**Data reads at a glance.** "raw data is useful and honest, but put plainly in a UI amounts to clutter to a user, no matter how useful it is." (RECORD -653) "Distance by trail is the only honest framing. And keeping it to one number is less UI clutter". (RECORD -943)
*Test:* a new readout leads with one plain figure or a visual, clearly labelled.

**Polish is even, and smooth.** "A polished journal, and then a raw data dump as if we gave up at the last moment." "strike a fine balance between speed/utility and visual polish". (RECORD -653; also -650)
*Test:* the last screen of a task is as finished as the first, and changes on screen move smoothly without slowing the task.

**Measured against what people already know.** "That's a hit on quality when a mainstream maps app does it much better, and people are used to that." (RECORD -708)
*Test:* compare with how established apps do the same thing. A weaker version of a familiar feature is an item.

**Forager never identifies a mushroom.** "It never identifies mushrooms or says what is safe to eat." (the owner's answer to Google Play, drafted by the planner and submitted by the owner, RECORD -1059; the rule is in docs/plans/mushroom-log.md, "Decided explicitly by the project owner", and -963)
*Test:* no text, ordering or default reads as naming a species or judging whether it is safe to eat.

**No developer tools in what the public gets.** "remove the walk logger and diagnostics from the app completely before releasing to testers. That's to remain with me only for dev purposes", then "Remove every trace". (RECORD -926)
*Test:* nothing from the dev branch's tools arrives in main.

### Proposed by the auditor

**A1. It starts, and doesn't crash, on a real phone.** *Test:* a cold start on each test phone with an empty crash log, then each changed path walked once.

**A2. An update never costs the user their records.** *Test:* the candidate installed over the current Play version, holding test records, photos and settings; all survive.

**A3. Usable by people who see, reach or hear differently.** *Test:* the checks in Appendix B, on the screens the change touches.

**A4. It survives interruption and the field.** *Test:* each changed screen is locked, put to sleep, switched away from and rotated mid-task, and resumes where it was.

**A5. Nothing unfinished reaches the public.** *Test:* no placeholder text, no dead-end screen, no control that does nothing.

**A6. Known bugs are listed and judged before release, never found first by users.** *Test:* the promotion lists every known bug it ships with, new or already live, and what the user would meet.

**A7. What Forager says in public stays true.** *Test:* the privacy policy, the store text and the app's own words are read against what the promoted build does.

**A8. A change states its battery cost.** *Test:* a change that keeps the screen, GPS or a sensor on longer says by how much.

## 3. What "tested, debugged, and approved for public use" means

For each promotion from pre-main into main, the auditor reads:

- **CI on pre-main**, green at the exact commit being promoted: build, version check, full test suite. No test skipped, deleted or weakened, and no growth in CI's list of allowed skips (CLAUDE.md, "Testing").
- **Device checks of a plain build.** Since -930 main holds no developer tools, so every build of main is plain. Each check is written before it runs, with a pass condition and evidence for every step.
  - The S22 Ultra test phone (no SIM, so GPS only): every promotion that changes the app (-1086), starting with the launch check.
  - The S26 Ultra, the owner's phone: every promotion that changes the app.
  - The tablet: when a screen changes (-1086), including the camera, because newer Android ignores the portrait request on wide screens (-980).
  - An upgrade over the current Play version (A2).
- **Known bugs**: the list in A6.
- **Accessibility**: Appendix B.
- **Copy**: honest, with a next step wherever something is empty or failed, plain, no placeholder, nothing that reads as identification. Public text is read against A7.

**CI is needed, and not enough.** It builds only the debug version. It never runs the phone's own check of the app's code, which is how main crashed at launch with every test green (-739). Its test harness sees no system bars at the screen edges, and its simulated taps reach a control even when something covers it (CLAUDE.md, "Known pitfalls" and "Testing").

**Only a phone can show:** whether the app starts at all; layouts against real system bars and cut-outs; whether a finger reaches the map or a control through what is drawn above it; the camera's fixed layout on phone and tablet; GPS, compass, height, alerts and recording with the screen off; legibility outdoors and at night; a screen reader and the largest font size; smoothness, heat and battery; an upgrade over a real install.

**The auditor's own walk-through runs on a real phone.** That is the recommended way (-1094). Where a phone check isn't available, an emulator run is the allowed fallback. The emulator is a heavy job on this laptop: it runs only when no Gradle build or heavy forecast job is running, and only on the planner's go. Every result from it is marked "emulator, not a phone" in the audit report.

**What an emulator can't stand in for:** real GPS and compass in the field; the phone's other sensors, such as the barometer; real system bars and cut-outs; a real finger's touch; heat and battery; legibility outdoors; the phone maker's own camera behaviour; an upgrade over a real Play install. A check that needs one of these stays "not seen on a phone" in the report, whatever the emulator showed.

## 4. How an audit runs and what it reports

1. The planner names a pre-main commit and the changes since the last promotion.
2. The auditor reads the evidence in section 3 and walks each changed behaviour as a step path against the principles.
3. It reports a verdict, its items, and what it couldn't check.

**Verdicts:**

- **Pass**: nothing found that a user would meet.
- **Pass with items**: items listed, none of which, in the auditor's reading, risks a user's records, safety or trust. The owner ships or holds each one.
- **Fail**: at least one item that would crash the app, lose records, mislead about position, direction or time, read as identification, break one of the owner's rulings, or ship something unfinished.

**Each item says** where it is (the screen and step path), what the user would experience, why it matters (the principle), and the evidence, or "not seen on a phone". Evidence from an emulator is marked "emulator, not a phone".

**The owner decides.** "Advises, you decide" (-1085). Nothing reaches main without the owner's approval.

**An urgent fix for a live bug:** "Same gate, front of queue" (-1085). The same evidence, scaled to what the fix touches, ahead of everything else waiting. The fix starts from main and is carried into pre-main afterwards (-1086).

## 5. What the auditor does not do

- It doesn't write app code or fix what it finds.
- It doesn't merge, and doesn't decide on the owner's behalf.
- It doesn't silence, skip or weaken a test.
- It doesn't turn the owner's words into rules of its own, or settle a conflict between rulings. It asks.
- It doesn't put the owner's screenshots or device data on GitHub.

## 6. Questions for the owner

Answer by number. Each has a recommendation.

1. **Your words.** Are the thirteen in section 2 the right set, each quoted correctly? *Recommended: confirm, and name any ruling that's missing.*
2. **Adopt A1** (starts, doesn't crash)? *Recommended: yes.*
3. **Adopt A2** (updates keep records)? *Recommended: yes.*
4. **Adopt A3** (accessible)? *Recommended: yes, at the bar in 14.*
5. **Adopt A4** (survives interruption)? *Recommended: yes.*
6. **Adopt A5** (nothing unfinished)? *Recommended: yes.*
7. **Adopt A6** (known bugs listed)? *Recommended: yes.*
8. **Adopt A7** (public words stay true)? *Recommended: yes.*
9. **Adopt A8** (battery cost stated)? *Recommended: yes, as in 18.*
10. **Verdicts.** Are section 4's three definitions right, especially what counts as a fail? *Recommended: adopt as written.*
11. **Which build the phone checks use.** A debug build quietly wipes records when an upgrade step is missing; the Play build doesn't. (a) CI's debug build only; (b) that, plus your signed Play build for the upgrade check before upload; (c) also a Play-type build on CI, a separate change to CI. *Recommended: (b) now, (c) with the release tidying, where it is already proposed.*
12. **Field walks.** Testers meet a change only after it is in main. Should changes to recording, navigation, alerts or position need a walk on a pre-main build first? *Recommended: yes, for those features only.*
13. **Tester builds.** On 2026-10-12 a tester build went out before the owed phone checks ("Ship to testers now", -923). Under -1085 all Play builds come from main, so the auditor reads the gate as covering tester builds too. Is that right? *Recommended: yes.*
14. **Accessibility bar.** Full WCAG 2.1 AA now, or Appendix B now and full AA later? *Recommended: Appendix B now.*
15. **The camera.** WCAG 2.1 asks that a screen not be locked to one orientation unless that is essential (1.3.4, level AA). Your ruling fixes the camera's layout (-981). *Recommended: record the camera as your stated exception. Its icons turn, so it works at any angle.*
16. **Map chrome and contrast.** Text on an 80% fill changes contrast with the map beneath. *Recommended: the auditor measures it over the lightest and darkest maps and reports any shortfall; the 80% rule stays yours.*
17. **Honest text that looks like a placeholder.** In -695 it was removed. In "Search a location" it stayed, with a next step added. Which should the auditor recommend? *Recommended: keep it and add a next step; removal stays your call each time.*
18. **Battery, while battery work is deferred (-520).** *Recommended: the auditor reports the cost as an item, and never fails a promotion on battery alone until that work is taken up.*
19. **Which known bugs may ship.** *Recommended: a bug that is listed, already in the live app, and not a crash, lost records, a wrong position, direction or time, or anything that reads as identification. A new bug of those kinds is a fail.*
20. **Reverts.** When a live bug is fixed by undoing a change? *Recommended: a short audit, at the front of the queue: it matches the earlier approved state and starts on both phones.*
21. **Promotions with no app change.** Documents also reach main only by promotion, and those have no phone check (-1086). What should the auditor's report cover? *Recommended: public text (privacy policy, store text) is read against what the app does; anything else gets a one-line "no change a user would meet".*
22. **The platform's own backup.** Android's quality guidelines expect an app's data to survive the platform's backup and phone-to-phone transfer. Your 2026-09-09 ruling keeps that off for good, with Forager's own Backup instead (-116). *Recommended: the auditor treats Forager's Backup as meeting the guideline's purpose and never flags the switch-off.*
23. **The auditor's own checks. Answered (-1094):** "For the philosophy, running on a device is recommended. Also add an option for an emulator run it device check itself isn't available". The auditor's walk-through runs on a real phone. An emulator run is the fallback when no phone check is available, on the planner's go and never beside a Gradle build or heavy forecast job (section 3).
24. **This draft.** *Recommended: you answer 1 to 22, the auditor revises, and the ruled version goes into pre-main as docs/process/qa-philosophy.md on your word.*

## Appendix A. Sources

**The owner's rulings** (RECORD.md on records-after-173):

- -34: "Search a location"; the owner's words are in docs/plans/journal-redesign.md, "Search bar copy" (line 584 on main).
- -37: "Not too dark since it can make it harder to read."
- -57, -58: nothing fully obstructs the map.
- -116: the 2026-09-09 ruling that the platform's backup stays off.
- -255, -383, -384: Back. The definition is in -383; -255 is an instance (bubble first, then fan).
- -519, -520: every failure has a fallback; battery work deferred.
- -650, -653 to -655: the three UI sweeps (motion, data friendliness, engineering hygiene) and the owner's choices, including "Log all, tell user where it matters".
- -695: honest text removed as placeholder-like.
- -708: "a mainstream maps app does it much better".
- -711: the icon bar remembered "so left handed users don't need to change it".
- -739 to -745: the launch crash with every test green, and the launch check.
- -916, -917, -923, -924, -926, -930: tester feedback, the production release, the tester build, the owed checks, and developer tools out of main.
- -943, -963, -980, -981, -1005: honest framing; no identification; the camera; remembered choices.
- -1059: the answers to Google Play, drafted by the planner and submitted by the owner.
- -1085: this role and the gate on main.
- -1094: the auditor's own checks on a real phone, with an emulator as the fallback.
- -1086, -1087: the promotion steps and branch roles, recorded after the dispatch was written (read at records-after-173 `2e52449e`).

**Files:** CLAUDE.md (all); docs/process/planner-profile.md; docs/audits/2026-09-26-pre-main-working-trunk.md; docs/plans/journal-redesign.md; docs/plans/mushroom-log.md; docs/beta/README.md; prompts/preserved/2026-10-15-08-pre-production-tidying-inventory.md (records-after-173); the field-instrument design rules and the mobile design-principles reference (the owner's synced skills); .github/workflows/ci.yml; scripts/s22-launch-check.sh; app/src/main/AndroidManifest.xml; app/src/main/java/com/zynergylabs/forager/app/data/local/ForagerDatabase.kt.

**Prior art**, read on the web on 2026-10-16:

- Android's core app quality guidelines, and its adaptive app quality guidelines for tablets.
- Google Play's Android vitals thresholds, pre-launch report and staged rollouts.
- WCAG 2.1 (W3C), and W3C's statement that it has no separate mobile guidelines.
- Jakob Nielsen's ten usability heuristics (with Rolf Molich, 1990; refined 1994).
- Steve Krug, *Don't Make Me Think*, whose title is the owner's professor's phrase.
- The other major mobile platform's app review guidelines, section 2.1, "App Completeness".
- The Scrum Guide (2020), "Definition of Done".

## Appendix B. Accessibility checks (proposed, for question 14)

On each screen the change touches:

1. Text contrast at least 4.5 to 1, or 3 to 1 for large text. Controls, and the parts of icons needed to understand them, at least 3 to 1 (WCAG 1.4.3, 1.4.11; Android's contrast item).
2. Touch targets at least 48 dp (Android's own minimum; WCAG 2.1's 44-pixel target is at its higher AAA level).
3. Every control has a label a screen reader speaks, containing the words shown on screen (WCAG 4.1.2, 2.5.3; Android's content-description item).
4. Text still fits and works at the phone's largest font size (WCAG 1.4.4).
5. Anything done with a pinch, two fingers or a drawn path also has a one-finger way (WCAG 2.5.1).
6. Messages such as "saved" or "off track" reach a screen reader without moving its focus (WCAG 4.1.3).
7. Works in portrait and landscape (WCAG 1.3.4), with the camera as question 15 decides.

## Appendix C. Why each proposal, and its prior art

**A1.** On 2026-10-08 main crashed at launch on every phone while every automated test was green, because those tests never run the phone's own check of the app's code (RECORD -739). Prior art: Android's core app quality guidelines (no crashes or freezes); the other platform's section 2.1 (test on a device; builds that crash are rejected); Google Play's thresholds, above which Play may show the app to fewer people (crashes for 1.09% of users overall or 8% on one phone model; freezes 0.47% overall or 8% per model).

**A2.** A debug build quietly wipes its records if an update is missing a data-upgrade step; the Play build doesn't (the database code's own note, `ForagerDatabase.kt:168-185`, applied at `:230-240`). Only an upgrade over a real install shows what users will get. Prior art: Android's core app quality guidelines (state restored after a relaunch; no accidental data loss).

**A3.** No ruling on accessibility was found, but the owner has asked for legibility (-37) and for left-handed users (-711). Prior art: WCAG 2.1 level AA; Android's core app quality accessibility items (48 dp targets, contrast, labels).

**A4.** A walk is interrupted by calls, a locked screen, a pocket, gloves and sun. Prior art: Android's core app quality guidelines (switch away and back, sleep and wake, lock and unlock, rotate three times, each without losing state); Forager's field-instrument design rules, which are inferred from its purpose and not yet measured in the field.

**A5.** The owner removed text that "seems like a placeholder and users might get confused" (-695). Prior art: the other platform's section 2.1 ("placeholder text, empty websites, and other temporary content should be scrubbed"); Nielsen's "Aesthetic and minimalist design".

**A6.** Halting a staged rollout on Google Play stops new installs, but phones that already updated keep the update. Prior art: the Scrum Guide's Definition of Done (work that doesn't meet it "cannot be released").

**A7.** The owner's answer to Google Play says every change "is checked on two phones and a tablet and on real field walks" (-1059). On 2026-10-15 the privacy policy still said backups were "not built yet", two weeks after they shipped (tidying inventory, section 6; corrected in the repository that day by PR #229).

**A8.** Screen time, GPS and sensors are design choices (the field-instrument design rules), and battery work is deferred (-520). Prior art: Android's core app quality guidelines (no needless background work, with keeping GPS on among their examples; support for the phone's power saving); Google Play's 5% threshold for excessive wake locks.

## Appendix D. Notes for the planner

- **CI:** `.github/workflows/ci.yml` runs `assembleDebug` and `testDebugUnitTest` on pushes to main, pre-main and dev (`:13`, `:116`, `:184`), with an exact-match skip allowlist (`:284-297`). No release-variant build runs on CI (tidying inventory, top-5 item 4).
- **The launch check**, `scripts/s22-launch-check.sh`, installs with `install -r`, has ART verify the code, cold-starts the app and reads the crash buffer. It is not on CI, which has no phone.
- **Debug and release differ on records:** `ForagerDatabase.create` adds the destructive fallback only under `BuildConfig.DEBUG`; its note says the asymmetry is deliberate.
- **The dispatch cited -255 for "Back retraces the way in".** -255 is an instance; the owner's definition is -383, with the step path in -384.
- **"Remove every trace" is -926.** -930's "Absolutely zero trace" was about privacy notes in a document, not the tools.
