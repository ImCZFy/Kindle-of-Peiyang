package me.tju244.kop.core.network

import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object ApiFactory {
    fun create(
        baseUrl: String,
        fixedHeaders: Map<String, String> = emptyMap(),
        tokenProvider: (() -> String)? = null,
        cacheDir: File? = null,
    ): Retrofit {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logging)

        if (fixedHeaders.isNotEmpty()) {
            clientBuilder.addInterceptor(FixedHeadersInterceptor(fixedHeaders))
        }

        tokenProvider?.let { clientBuilder.addInterceptor(AuthTokenInterceptor(it)) }

        cacheDir?.let {
            val cacheFile = File(it, "okhttp_cache")
            clientBuilder.cache(Cache(cacheFile, 10L * 1024 * 1024))
        }

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

