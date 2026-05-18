package me.tju244.kop.auth.data

import me.tju244.kop.auth.network.AuthApi
import me.tju244.kop.auth.network.AuthCommonResult
import me.tju244.kop.auth.network.SemesterResult

class AuthRepository(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
) {
    suspend fun login(account: String, password: String): Result<AuthCommonResult> = runCatching {
        val res = authApi.loginByPassword(account, password)
        val token = res.result?.token.orEmpty()
        if (res.error_code != 0 || token.isBlank()) {
            error(res.message ?: "登录失败")
        }
        sessionStore.saveToken(token)
        sessionStore.saveAccount(account.trim())
        val userNumber = res.result?.userNumber.orEmpty().takeIf { it.isStudentNumber() }.orEmpty()
        val fallbackNumber = account.trim().takeIf { it.isStudentNumber() }.orEmpty()
        (userNumber.ifBlank { fallbackNumber }).takeIf { it.isNotBlank() }?.let { sessionStore.saveUserNumber(it) }
        res.result!!
    }

    suspend fun updateToken(): Result<String> = runCatching {
        val res = authApi.updateToken()
        val token = res.result.orEmpty()
        if (res.error_code != 0 || token.isBlank()) {
            error(res.message ?: "更新 token 失败")
        }
        sessionStore.saveToken(token)
        token
    }

    suspend fun semester(): Result<SemesterResult> = runCatching {
        val res = authApi.getSemester()
        if (res.error_code != 0 || res.result == null) {
            error(res.message ?: "获取学期信息失败")
        }
        res.result
    }

    suspend fun requestLoginCode(phone: String): Result<Unit> = runCatching {
        val res = authApi.requestLoginCode(phone = phone)
        if (res.error_code != 0) {
            error(res.message ?: "验证码发送失败")
        }
    }

    suspend fun loginByCode(phone: String, code: String): Result<AuthCommonResult> = runCatching {
        val res = authApi.loginByCode(phone = phone, code = code)
        val token = res.result?.token.orEmpty()
        if (res.error_code != 0 || token.isBlank()) {
            error(res.message ?: "验证码登录失败")
        }
        sessionStore.saveToken(token)
        sessionStore.saveAccount(phone.trim())
        val userNumber = res.result?.userNumber.orEmpty().takeIf { it.isStudentNumber() }.orEmpty()
        if (userNumber.isNotBlank()) sessionStore.saveUserNumber(userNumber)
        res.result!!
    }

    suspend fun requestResetCode(phone: String): Result<Unit> = runCatching {
        val res = authApi.requestResetCode(phone = phone)
        if (res.error_code != 0) {
            error(res.message ?: "验证码发送失败")
        }
    }

    suspend fun verifyResetCode(phone: String, code: String): Result<Unit> = runCatching {
        val res = authApi.verifyResetCode(phone = phone, code = code)
        if (res.error_code != 0) {
            error(res.message ?: "验证码校验失败")
        }
    }

    suspend fun resetPasswordByPhone(phone: String, password: String): Result<Unit> = runCatching {
        val res = authApi.resetPasswordByPhone(phone = phone, password = password)
        if (res.error_code != 0) {
            error(res.message ?: "重置密码失败")
        }
    }

    suspend fun logout() {
        sessionStore.clear()
    }

    private fun String.isStudentNumber(): Boolean {
        val value = trim()
        return value.matches(Regex("""\d{8,12}""")) &&
            !value.matches(Regex("""1[3-9]\d{9}"""))
    }
}

