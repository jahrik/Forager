# 2026-09-27: emulator device check attempted in the cloud container, not achieved

The owner asked whether device checks could run now ("But if you can do it now then go ahead"), then "Use API 36 as API 37 is gone for now". The planner installed the emulator (37.1.11) and `system-images;android-36;google_apis;x86_64` (rev 7) in the cloud container and created AVD `f36` (pixel_7, `/data` 6G). **Nothing was verified on it.**

## What happened

- The container has no hardware virtualisation: no `/dev/kvm`, and `/proc/cpuinfo` shows no `vmx` or `svm` flags; `emulator -accel-check`: "KVM requires a CPU that supports vmx or svm". The emulator ran with `-accel off -no-window -gpu swiftshader_indirect`, pure software emulation.
- Boot completed after **2,381 s** (`sys.boot_completed=1`).
- A device-check agent then built `journal-redesign` at `3870e4d` (`:app:assembleDebug`, BUILD SUCCESSFUL, APK 80,987,549 bytes) and tried to install it. It never installed: Android's `system_server` was killed by its own watchdog **seven times between 20:34:06 and 21:03:52 UTC** ("WATCHDOG KILLING SYSTEM PROCESS: Blocked in handler on main thread ... for 60 to 67 s"), coming back 1 to 4 minutes after each kill and living 4 to 5 minutes. The first two kills came before any install attempt. The one install that started ran 1 m 47 s and ended "Broken pipe" at the next kill. The two readable stacks blocked in different places (telecom `MediaSession` creation; `Looper.showSlowLog`), consistent with the whole guest starving of CPU rather than one deadlock (inferred). Host load: qemu about 116% across 4 host CPUs, a coder's Gradle daemon about 38%.
- The planner stopped the emulator afterwards to return the CPU to the running coder.

## Not checked

F1 (the find picker's snap-back fix in the live map), its positive control, Back from a Records chip, and everything already device-only for the owner's S22 Ultra. All stay in the consolidated J7 device check the owner runs at the end of the Journal project.

## For the device check (from the check agent; applies on the phone too)

- **F1's pass condition has a trap.** "Pin at: stays at P after new fixes" is also what you see if the new fixes never reach the app. A pass must be paired with evidence the fixes arrived: the positive control (a fresh, untouched picker follows the new fix) or `dumpsys location` showing it.
- **Confirm the pan registered first.** Before the first touch, a new region rewrites "Pin at:" during composition; after a touch, only camera idle changes it (`CentrePinLocationPicker.kt:120-130`). So check that P differs from the starting point before reading what later fixes do; a swipe MapLibre did not report as a gesture would look like a failure.

## If the emulator is tried again

It needs a host with KVM, or at least a quiet host (no parallel Gradle) and an install that fits inside one framework lifetime. In this container it is not a usable test device.
