package me.tju244.kop.tju.data

import me.tju244.kop.auth.data.SessionStore
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EntryQrRepository(
    private val sessionStore: SessionStore,
    private val client: OkHttpClient = OkHttpClient(),
) {
    suspend fun getQrContent(): String {
        val sid = sessionStore.currentUserNumber()
            .ifBlank { sessionStore.currentAccount().takeIf { it.isStudentNumber() }.orEmpty() }
            .ifBlank { sessionStore.currentTjuUsername().takeIf { it.isStudentNumber() }.orEmpty() }
        if (sid.isBlank()) throw IllegalStateException("请先重新登录北洋之炬账号以同步学号")
        val query = "method=getAccountQRcodeInfo&ID_NUMBER=$sid"
        val encryptedPath = query.tripleDesHex()
        val request = Request.Builder()
            .url("https://f.tju.edu.cn/tp_up/up/mobile/ifs/$encryptedPath")
            .header("Content-Type", "application/json;charset=UTF-8")
            .header("Accept", "application/json;charset=UTF-8")
            .header("Host", "f.tju.edu.cn")
            .header("Connection", "Keep-Alive")
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            .build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("入校码请求失败：${response.code}")
            response.body?.string().orEmpty()
        }
        return body.extractMessage()
            ?.replace("&amp;", "&")
            ?.replace("&lt;", "<")
            ?.replace("&gt;", ">")
            ?.replace("&quot;", "\"")
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("入校码返回为空：${body.sanitizeBodyPreview()}")
    }

    private fun String.isStudentNumber(): Boolean {
        val value = trim()
        return value.matches(Regex("""\d{8,12}""")) &&
            !value.matches(Regex("""1[3-9]\d{9}"""))
    }

    private fun String.extractMessage(): String? {
        val cdata = Regex("<(message|return|data)>\\s*<!\\[CDATA\\[([\\s\\S]*?)]]>\\s*</\\1>", RegexOption.IGNORE_CASE)
            .find(this)
            ?.groupValues
            ?.getOrNull(2)
        if (!cdata.isNullOrBlank()) return cdata
        Regex("<(message|return|data)>([\\s\\S]*?)</\\1>", RegexOption.IGNORE_CASE)
            .find(this)
            ?.groupValues
            ?.getOrNull(2)
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { return it }
        return Regex(""""(?:message|return|data)"\s*:\s*"([\s\S]*?)"""", RegexOption.IGNORE_CASE)
            .find(this)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
    }

    private fun String.sanitizeBodyPreview(): String =
        replace(Regex("\\s+"), " ").take(180)

    private fun String.tripleDesHex(): String {
        val cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding")
        val key = SecretKeySpec("neusofteducationplatform".toByteArray(Charsets.UTF_8), "DESede")
        val iv = IvParameterSpec("01234567".toByteArray(Charsets.UTF_8))
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        return cipher.doFinal(toByteArray(Charsets.UTF_8)).joinToString("") { "%02X".format(it) }
    }
}

