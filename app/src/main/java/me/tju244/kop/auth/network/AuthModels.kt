package me.tju244.kop.auth.network

import com.google.gson.annotations.SerializedName

data class AuthCommonResult(
    val token: String = "",
    @SerializedName(value = "userNumber", alternate = ["user_number", "usernumber", "idNumber", "id_number"])
    val userNumber: String = "",
    val nickname: String = "",
)

data class ApiEnvelope<T>(
    val error_code: Int? = null,
    val message: String? = null,
    val result: T? = null,
)

data class SemesterResult(
    val semesterStartTimestamp: Long? = null,
    val semesterName: String? = null,
    val semesterStartAt: String? = null,
)

