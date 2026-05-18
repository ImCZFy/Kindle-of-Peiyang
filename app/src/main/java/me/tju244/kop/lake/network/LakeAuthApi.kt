package me.tju244.kop.lake.network

import retrofit2.http.GET
import retrofit2.http.Query

interface LakeAuthApi {
    @GET("api/v1/f/auth/token")
    suspend fun exchangeToken(@Query("token") token: String): LakeBaseResp<LakeTokenData>
}

