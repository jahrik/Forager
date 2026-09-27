# 2026-09-27: owner's device result, the find-location picker and the Journal

The owner ran a CI debug build from PR #140 on their phone (the S22 Ultra named for the device check) and reported, verbatim:

> Finds location picker now works as expected. Journal looks amazing

**Which build.** The app's Diagnostics row read "Build 1173 · 1.0.1173+ga1eafc4c" (owner's screenshot). CI builds a pull request's merge commit, so `a1eafc4c` is a merge of the PR head into `pre-main` (`352b708`); that merge ref has since been replaced and cannot be fetched. The build number is the commit count of the merge that was built, and `git rev-list --count 352b708 <head>` + 1 gives 1173 only for head **`e36ff86`** (J5's closing commit, terminal `2026-09-27-67`); the method checks against the current merge ref, whose count for head `463468f` is 1174 as predicted. `e36ff86`'s app code is identical to `27e5c70` (J5's final code). So the build contains J1-J5 and the picker fix stage, and none of J5c.

**What it establishes.** The find picker's snap-back fix (F1, terminal `2026-09-27-64`) behaves as intended on a real device: the pin stays where the owner pans it. This is the first device evidence that `MapRenderMode.onUserCameraGesture` is wired in the live map, which Robolectric could not show and the cloud emulator attempt could not reach (`2026-09-27-emulator-check-attempt.md`).

**What it does not separately establish.** The emulator check's caution applies: "the pin stays" is also what would be seen if new location fixes never arrived after the pan. The owner's report does not say whether fixes were arriving (location was on and the blue dot showed in earlier observations). Recorded as an owner-observed pass on the plain behaviour; the fix-delivery control stays on the J7 checklist.

**Everything else** from J1-J5 and the fix stage remains unverified on a device item by item, and goes to the consolidated J7 check; "Journal looks amazing" is the owner's overall impression, not a pass on individual items.
