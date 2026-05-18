package me.tju244.kop.core.network

import okhttp3.Interceptor
import okhttp3.Response

class AuthTokenInterceptor(
    private val tokenProvider: () -> String,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenProvider()
        val req = if (token.isNotBlank()) {
            chain.request().newBuilder().addHeader("token", token).build()
        } else {
            chain.request()
        }
        return chain.proceed(req)
    }
}

