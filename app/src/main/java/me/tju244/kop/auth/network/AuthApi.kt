package me.tju244.kop.auth.network

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface AuthApi {
    @FormUrlEncoded
    @POST("auth/common")
    suspend fun loginByPassword(
        @Field("account") account: String,
        @Field("password") password: String,
    ): ApiEnvelope<AuthCommonResult>

    @FormUrlEncoded
    @POST("auth/phone/msg")
    suspend fun requestLoginCode(
        @Field("phone") phone: String,
    ): ApiEnvelope<Any>

    @FormUrlEncoded
    @POST("auth/phone")
    suspend fun loginByCode(
        @Field("phone") phone: String,
        @Field("code") code: String,
    ): ApiEnvelope<AuthCommonResult>

    @POST("password/reset/msg")
    suspend fun requestResetCode(
        @Query("phone") phone: String,
    ): ApiEnvelope<Any>

    @FormUrlEncoded
    @POST("password/reset/verify")
    suspend fun verifyResetCode(
        @Field("phone") phone: String,
        @Field("code") code: String,
    ): ApiEnvelope<Any>

    @FormUrlEncoded
    @POST("password/reset")
    suspend fun resetPasswordByPhone(
        @Field("phone") phone: String,
        @Field("password") password: String,
    ): ApiEnvelope<Any>

    @POST("auth/updateToken")
    suspend fun updateToken(): ApiEnvelope<String>

    @GET("semester")
    suspend fun getSemester(): ApiEnvelope<SemesterResult>
}

