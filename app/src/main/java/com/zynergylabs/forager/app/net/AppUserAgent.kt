package com.zynergylabs.forager.app.net

import android.os.Build
import com.zynergylabs.forager.app.BuildConfig

/**
 * The one place the app's User-Agent is built, for every outbound request: the map's tiles, styles
 * and glyphs (dispatch 2026-09-28-325; owner, "Yes set a proper app identifier") and the iNaturalist
 * and Open-Meteo clients (dispatch 2026-09-28-331; owner, "same identifier for inaturalist and
 * open-meteo"). OpenStreetMap's tile usage policy blocks library-default User-Agents and asks apps
 * to identify themselves, and the Street basemap depends on its servers. Moved here from the map
 * package (where it was `MapUserAgent`) so the data layer does not import from the map. The shape
 * is `Forager/<versionName> (Android <release>; <applicationId>; +<contact>)`.
 */
internal object AppUserAgent {
    /**
     * Where a server's operator can reach the app's owner: the website and support address the
     * owner gave (RECORD.md, 2026-09-28-326). The planner added the `https://` scheme, the form
     * OSM's policy example uses.
     */
    const val CONTACT = "+https://zynergy-labs.com; support@zynergy-labs.com"

    fun build(versionName: String, androidRelease: String, applicationId: String): String =
        "Forager/$versionName (Android $androidRelease; $applicationId; $CONTACT)"

    fun forThisApp(): String = build(BuildConfig.VERSION_NAME, Build.VERSION.RELEASE, BuildConfig.APPLICATION_ID)
}
