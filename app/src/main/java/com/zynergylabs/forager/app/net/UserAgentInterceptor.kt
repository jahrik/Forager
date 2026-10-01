package com.zynergylabs.forager.app.net

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Sets the request's `User-Agent` to [userAgent], replacing any value already there (OkHttp's own
 * default is added by its bridge interceptor only when the header is absent, and `header` rather
 * than `addHeader` keeps the result to exactly one). Shared by the iNaturalist and Open-Meteo
 * clients so each sends the app's identifier ([AppUserAgent]) rather than the library's.
 */
internal class UserAgentInterceptor(private val userAgent: String = AppUserAgent.forThisApp()) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(chain.request().newBuilder().header("User-Agent", userAgent).build())
}
