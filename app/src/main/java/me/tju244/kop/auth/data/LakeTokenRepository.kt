package me.tju244.kop.auth.data

import me.tju244.kop.lake.network.LakeAuthApi

class LakeTokenRepository(
    private val lakeAuthApi: LakeAuthApi,
    private val sessionStore: SessionStore,
) {
    private var lastExchangeAt: Long = 0L

    suspend fun ensureLakeToken(): Result<String> = runCatching {
        val cached = sessionStore.currentLakeToken()
        if (cached.isNotBlank()) {
            val now = System.currentTimeMillis()
            if (now - lastExchangeAt < 10 * 60 * 1000L) {
                return@runCatching cached
            }
            val token = sessionStore.currentToken()
            if (token.isNotBlank()) {
                val resp = lakeAuthApi.exchangeToken(token)
                val freshToken = resp.data?.token.orEmpty()
                if (resp.code == 200 && freshToken.isNotBlank()) {
                    sessionStore.saveLakeToken(freshToken)
                    sessionStore.saveLakeIdentity(
                        uid = resp.data?.uid,
                        avatar = resp.data?.user?.avatar.orEmpty(),
                        nickname = resp.data?.user?.nickname.orEmpty().ifBlank { resp.data?.user?.username.orEmpty() },
                        username = resp.data?.user?.username.orEmpty(),
                    )
                    lastExchangeAt = now
                    return@runCatching freshToken
                }
            }
            val storedUsername = sessionStore.currentLakeUsername()
            if (storedUsername.isBlank()) {
                if (token.isNotBlank()) {
                    val resp = lakeAuthApi.exchangeToken(token)
                    if (resp.code == 200) {
                        sessionStore.saveLakeIdentity(
                            uid = resp.data?.uid,
                            avatar = resp.data?.user?.avatar.orEmpty(),
                            nickname = resp.data?.user?.nickname.orEmpty(),
                            username = resp.data?.user?.username.orEmpty(),
                        )
                        lastExchangeAt = now
                    }
                }
            }
            return@runCatching cached
        }

        val token = sessionStore.currentToken()
        require(token.isNotBlank()) { "主登录 token 为空" }

        val resp = lakeAuthApi.exchangeToken(token)
        val lakeToken = resp.data?.token.orEmpty()
        if (resp.code != 200 || lakeToken.isBlank()) {
            error(resp.msg ?: "获取湖底 token 失败")
        }
        sessionStore.saveLakeToken(lakeToken)
        sessionStore.saveLakeIdentity(
            uid = resp.data?.uid,
            avatar = resp.data?.user?.avatar.orEmpty(),
            nickname = resp.data?.user?.nickname.orEmpty().ifBlank { resp.data?.user?.username.orEmpty() },
            username = resp.data?.user?.username.orEmpty(),
        )
        lastExchangeAt = System.currentTimeMillis()
        lakeToken
    }

    suspend fun currentLakeUid(): Long? = sessionStore.currentLakeUid()

    suspend fun currentLakeAvatar(): String = sessionStore.currentLakeAvatar()

    suspend fun currentLakeNickname(): String = sessionStore.currentLakeNickname()

    suspend fun currentLakeUsername(): String = sessionStore.currentLakeUsername()

    suspend fun authUserNumber(): String = sessionStore.currentUserNumber()

    suspend fun clearLakeToken() {
        sessionStore.saveLakeToken("")
        lastExchangeAt = 0L
    }
}

