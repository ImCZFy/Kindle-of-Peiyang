package me.tju244.kop.tju.data

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import me.tju244.kop.auth.data.SessionStore
import me.tju244.kop.core.network.Env
import okhttp3.OkHttpClient
import okhttp3.Request
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StudyRoomRepository(
    private val sessionStore: SessionStore,
    private val client: OkHttpClient = OkHttpClient(),
    private val gson: Gson = Gson(),
) {
    suspend fun campuses(): List<StudyCampus> = getList("/campus")

    suspend fun buildings(campusId: Int): List<StudyBuilding> =
        getList<StudyBuilding>("/campus/$campusId/building").map {
            it.copy(name = it.name.trimLeadingZeroes())
        }

    suspend fun rooms(buildingId: Int, session: Int, date: Date = Date()): List<StudyRoom> {
        val currentSession = if (session == CURRENT_SESSION) currentSessionIndex() else session
        val path = if (currentSession == ALL_SESSIONS) {
            "/building/$buildingId/room"
        } else {
            val dateText = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(date)
            "/building/$buildingId/room/session/$currentSession/date/$dateText"
        }
        return getList<StudyRoom>(path).map {
            it.copy(name = it.name.trimLeadingZeroes())
        }
    }

    suspend fun schedule(roomId: Int): List<StudyRoomOccupy> = getList("/room/$roomId/schedule")

    private suspend inline fun <reified T> getList(path: String): List<T> {
        val token = sessionStore.currentToken()
        if (token.isBlank()) throw IllegalStateException("请先登录北洋之炬账号")
        val request = Request.Builder()
            .url(BASE_URL.trimEnd('/') + path)
            .header("DOMAIN", Env.AUTH_DOMAIN)
            .header("ticket", Env.AUTH_TICKET)
            .header("token", token)
            .header("Accept", "application/json")
            .build()
        val body = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IllegalStateException("教室查询请求失败：${response.code}")
            response.body?.string().orEmpty()
        }
        val root = JsonParser.parseString(body).asJsonObject
        val code = root.get("code")?.asInt ?: -1
        if (code != 10000) {
            val message = root.readMessage() ?: "接口返回 $code"
            throw IllegalStateException(message)
        }
        val data: JsonElement = root.get("data") ?: return emptyList()
        if (!data.isJsonArray) return emptyList()
        val type = object : TypeToken<List<T>>() {}.type
        return gson.fromJson(data, type)
    }

    private fun com.google.gson.JsonObject.readMessage(): String? =
        listOf("message", "msg", "data").firstNotNullOfOrNull { key ->
            get(key)?.takeIf { !it.isJsonNull }?.asString?.takeIf { it.isNotBlank() }
        }

    private fun String.trimLeadingZeroes(): String = replace(Regex("^0+"), "")

    companion object {
        private const val BASE_URL = "https://selfstudy.twt.edu.cn/"
        const val CURRENT_SESSION = 0
        const val ALL_SESSIONS = -1

        fun currentSessionIndex(date: Date = Date()): Int {
            val minutes = SimpleDateFormat("HH:mm", Locale.CHINA).format(date).split(":").let {
                it[0].toInt() * 60 + it[1].toInt()
            }
            val periods = listOf(
                7 * 60 to 9 * 60 + 15,
                9 * 60 + 16 to 10 * 60 + 5,
                10 * 60 + 6 to 11 * 60 + 10,
                11 * 60 + 11 to 12 * 60,
                12 * 60 + 1 to 14 * 60 + 15,
                14 * 60 + 16 to 15 * 60 + 5,
                15 * 60 + 6 to 16 * 60 + 10,
                16 * 60 + 11 to 17 * 60,
                17 * 60 + 1 to 19 * 60 + 15,
                19 * 60 + 16 to 20 * 60 + 5,
                20 * 60 + 6 to 20 * 60 + 55,
                20 * 60 + 56 to 21 * 60 + 45,
            )
            return periods.indexOfFirst { minutes > it.first && minutes < it.second }.let {
                if (it == -1) ALL_SESSIONS else it + 1
            }
        }
    }
}

data class StudyCampus(
    val id: Int,
    val name: String,
)

data class StudyBuilding(
    val id: Int,
    val name: String,
    @SerializedName("campus_id")
    val campusId: Int,
)

data class StudyRoom(
    val id: Int,
    val name: String,
    @SerializedName("building_id")
    val buildingId: String,
    @SerializedName("free")
    val isFree: Boolean,
)

data class StudyRoomOccupy(
    val date: String,
    @SerializedName("session_index")
    val sessionIndex: Int,
)

