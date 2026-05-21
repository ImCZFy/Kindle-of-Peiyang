package me.tju244.kop

import android.app.Application
import me.tju244.kop.auth.data.AuthRepository
import me.tju244.kop.auth.data.SessionStore
import me.tju244.kop.auth.network.AuthApi
import me.tju244.kop.core.network.ApiFactory
import me.tju244.kop.core.network.Env
import me.tju244.kop.tju.data.TjuAuthRepository
import me.tju244.kop.tju.data.EntryQrRepository
import me.tju244.kop.tju.network.TjuApi
import kotlinx.coroutines.runBlocking

class RebuildApplication : Application() {
    lateinit var sessionStore: SessionStore
        private set

    lateinit var authRepository: AuthRepository
        private set

    lateinit var tjuAuthRepository: TjuAuthRepository
        private set

    lateinit var entryQrRepository: EntryQrRepository
        private set

    override fun onCreate() {
        super.onCreate()
        sessionStore = SessionStore(this)

        val authApi = ApiFactory.create(
            baseUrl = Env.API,
            fixedHeaders = mapOf(
                "DOMAIN" to Env.AUTH_DOMAIN,
                "ticket" to Env.AUTH_TICKET,
            ),
            tokenProvider = { runBlocking { sessionStore.currentToken() } },
        ).create(AuthApi::class.java)
        authRepository = AuthRepository(authApi, sessionStore)

        val tjuApi = ApiFactory.create("https://learning.twt.edu.cn/", cacheDir = cacheDir).create(TjuApi::class.java)
        tjuAuthRepository = TjuAuthRepository(tjuApi, sessionStore, authRepository)
        entryQrRepository = EntryQrRepository(sessionStore)
    }
}

