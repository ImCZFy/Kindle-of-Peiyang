package me.tju244.kop.core.network

import okhttp3.Interceptor
import okhttp3.Response

class FixedHeadersInterceptor(
    private val headers: Map<String, String>,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder = chain.request().newBuilder()
        headers.forEach { (k, v) ->
            if (v.isNotBlank() && chain.request().header(k).isNullOrBlank()) {
                requestBuilder.addHeader(k, v)
            }
        }
        return chain.proceed(requestBuilder.build())
    }
}


