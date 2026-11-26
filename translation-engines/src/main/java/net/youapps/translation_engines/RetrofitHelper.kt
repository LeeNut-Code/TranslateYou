/*
 * Copyright (c) 2026 You Apps
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.youapps.translation_engines

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitHelper {
    val contentType = "application/json".toMediaTypeOrNull()!!

    inline fun <reified T> createInstance(baseUrl: String): T {
        return createInstance(baseUrl, HttpLoggingInterceptor.Level.BODY)
    }

    inline fun <reified T> createInstance(
        baseUrl: String,
        logLevel: HttpLoggingInterceptor.Level
    ): T {
        return createInstance(baseUrl, logLevel, 10, 10, 10, TimeUnit.SECONDS)
    }

    inline fun <reified T> createInstance(
        baseUrl: String,
        logLevel: HttpLoggingInterceptor.Level,
        connectTimeout: Long,
        readTimeout: Long,
        writeTimeout: Long,
        timeUnit: TimeUnit
    ): T {
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(connectTimeout, timeUnit)
            .readTimeout(readTimeout, timeUnit)
            .writeTimeout(writeTimeout, timeUnit)

        val logging = HttpLoggingInterceptor().apply {
            level = logLevel
        }
        httpClient.addInterceptor(logging) // <-- this is the important line!

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(JsonHelper.json.asConverterFactory(contentType))
            .client(httpClient.build())
            .build()
            .create(T::class.java)
    }

    inline fun <reified T> createApi(engine: TranslationEngine) = createInstance<T>(engine.getUrl())
}
