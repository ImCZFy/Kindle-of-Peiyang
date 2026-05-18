package me.tju244.kop.tju.network

import com.google.gson.JsonObject
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface TjuApi {
    @FormUrlEncoded
    @POST("get_classes")
    suspend fun getClasses(
        @Field("username") username: String,
        @Field("passwd") password: String,
    ): JsonObject
}

