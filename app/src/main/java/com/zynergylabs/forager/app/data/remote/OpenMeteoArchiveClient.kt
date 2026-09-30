package com.zynergylabs.forager.app.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.zynergylabs.forager.app.net.UserAgentInterceptor
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Builds the [OpenMeteoArchiveApi] instance. The only place that constructs Retrofit/OkHttp for
 * it — a separate client from [OpenMeteoClient] because the historical archive API is served from
 * a different host (`archive-api.open-meteo.com`, not `api.open-meteo.com`).
 */
object OpenMeteoArchiveClient {

    private const val BASE_URL = "https://archive-api.open-meteo.com/"

    /** The built client, separate from [create] so a test can send a request through the real one. */
    internal fun httpClient(debug: Boolean): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(UserAgentInterceptor())
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .apply {
                if (debug) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BASIC
                        },
                    )
                }
            }
            .build()

    fun create(debug: Boolean): OpenMeteoArchiveApi {
        val json = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }

        val okHttpClient = httpClient(debug)

        val contentType = "application/json".toMediaType()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(OpenMeteoArchiveApi::class.java)
    }
}
