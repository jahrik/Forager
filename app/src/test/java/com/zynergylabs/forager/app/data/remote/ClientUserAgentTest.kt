package com.zynergylabs.forager.app.data.remote

import com.zynergylabs.forager.app.net.AppUserAgent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Every request the iNaturalist and Open-Meteo clients make carries exactly one User-Agent, the
 * app's (dispatch 2026-09-28-331). Each test sends a request through the real client the class
 * builds; the last interceptor in the chain records what would have gone on the wire. No
 * MockWebServer is a dependency of this project, and none was added (same approach as
 * `MapUserAgentTest`). The request carries a library-style value first, as MapLibre's does, so a
 * client that only adds a header, rather than replacing one, is caught by the count.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ClientUserAgentTest {

    @Test
    fun `the iNaturalist client sends exactly one User-Agent, the app's`() {
        assertOneAppUserAgent(INaturalistClient.httpClient(debug = false), "https://api.inaturalist.org/v1/observations")
    }

    @Test
    fun `the Open-Meteo forecast client sends exactly one User-Agent, the app's`() {
        assertOneAppUserAgent(OpenMeteoClient.httpClient(debug = false), "https://api.open-meteo.com/v1/forecast")
    }

    @Test
    fun `the Open-Meteo archive client sends exactly one User-Agent, the app's`() {
        assertOneAppUserAgent(OpenMeteoArchiveClient.httpClient(debug = false), "https://archive-api.open-meteo.com/v1/archive")
    }

    @Test
    fun `the debug clients, which add a logging interceptor, send exactly one User-Agent too`() {
        assertOneAppUserAgent(INaturalistClient.httpClient(debug = true), "https://api.inaturalist.org/v1/observations")
        assertOneAppUserAgent(OpenMeteoClient.httpClient(debug = true), "https://api.open-meteo.com/v1/forecast")
        assertOneAppUserAgent(OpenMeteoArchiveClient.httpClient(debug = true), "https://archive-api.open-meteo.com/v1/archive")
    }

    @Test
    fun `the clients keep their 15 second connect and read timeouts`() {
        listOf(INaturalistClient.httpClient(false), OpenMeteoClient.httpClient(false), OpenMeteoArchiveClient.httpClient(false)).forEach {
            assertEquals(15_000, it.connectTimeoutMillis)
            assertEquals(15_000, it.readTimeoutMillis)
        }
    }

    private fun assertOneAppUserAgent(client: OkHttpClient, url: String) {
        lateinit var seen: Request
        val recording = Interceptor { chain ->
            seen = chain.request()
            Response.Builder().request(seen).protocol(Protocol.HTTP_1_1).code(200).message("OK").body("".toResponseBody(null)).build()
        }
        client.newBuilder().addInterceptor(recording).build()
            .newCall(Request.Builder().url(url).addHeader("User-Agent", "library-default/0").build())
            .execute().close()

        assertEquals(listOf(AppUserAgent.forThisApp()), seen.headers("User-Agent"))
    }
}
