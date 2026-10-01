package com.zynergylabs.forager.app.ui.log

import org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin

/**
 * Installs [PhotoDecodeTestEnvironment]'s default before every Robolectric test, so no test class
 * has to ask for it (dispatch 2026-09-28-351). Robolectric finds it through
 * `META-INF/services/org.robolectric.pluginapi.TestEnvironmentLifecyclePlugin` in this source
 * set's resources. Chosen over a test-only `Application` in `robolectric.properties`: that would
 * replace `ForagerApplication`, which tests boot for real, and `ForagerApplication` is final. It
 * runs per test, so a test's opt-in cannot leak into the next. Whether it runs in the sandbox that
 * loads the app's classes (and not a second copy of them) is what `PhotoDecodeThreadTest` shows.
 */
class PhotoDecodeTestLifecycle : TestEnvironmentLifecyclePlugin {
    override fun onSetupApplicationState() {
        PhotoDecodeTestEnvironment.installDefault()
    }
}
