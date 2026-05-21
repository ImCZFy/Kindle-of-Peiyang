package me.tju244.kop.tju.data

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import me.tju244.kop.auth.data.SessionStore
import me.tju244.kop.tju.network.TjuApi
import me.tju244.kop.tju.network.TjuApiException
import me.tju244.kop.tju.network.TjuArrangeDto
import me.tju244.kop.tju.network.TjuClassesBundle
import me.tju244.kop.tju.network.TjuCourseDto
import me.tju244.kop.tju.network.TjuExamDto
import me.tju244.kop.tju.network.TjuGpaCourseDto
import me.tju244.kop.tju.network.TjuGpaDto
import me.tju244.kop.tju.network.TjuGpaStatDto
import me.tju244.kop.tju.network.TjuGpaTotalDto
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.tju244.kop.auth.data.AuthRepository

class TjuAuthRepository(
    private val api: TjuApi,
    private val sessionStore: SessionStore,
    private val authRepository: AuthRepository,
) {
    private val gson = Gson()
    private val cookieJar = InMemoryCookieJar()
    private val spiderClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(false)
        .followSslRedirects(false)
        .build()
    private val classesClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    suspend fun storedCredentials(): Pair<String, String> {
        return sessionStore.currentTjuUsername() to sessionStore.currentTjuPassword()
    }

    suspend fun cachedBundle(): TjuClassesBundle? {
        val cache = sessionStore.currentTjuClassesCache()
        if (cache.isBlank()) return null
        return runCatching {
            gson.fromJson(cache, TjuClassesBundle::class.java)
        }.getOrNull()
    }

    suspend fun loginAndFetch(username: String, password: String): TjuClassesBundle {
        val normalizedUsername = username.trim()
        if (normalizedUsername.isBlank() || password.isBlank()) {
            throw TjuApiException("请填写教务网账号和密码")
        }
        debug("开始绑定教务网账号：${normalizedUsername.maskForLog()}")
        val bundle = fetch(normalizedUsername, password)
        sessionStore.saveTjuCredentials(normalizedUsername, password)
        sessionStore.saveTjuClassesCache(gson.toJson(bundle))
        debug("绑定完成并写入本地缓存")
        return bundle
    }

    suspend fun refreshStored(): TjuClassesBundle {
        val (username, password) = storedCredentials()
        if (username.isBlank() || password.isBlank()) {
            throw TjuApiException("请先登录教务网")
        }
        debug("开始刷新教务缓存：${username.maskForLog()}")
        return fetch(username, password).also { bundle ->
            sessionStore.saveTjuClassesCache(gson.toJson(bundle))
            debug("刷新完成：课程 ${bundle.courses.size}，成绩 ${bundle.gpa?.courses.orEmpty().size}，考试 ${bundle.exams.size}")
        }
    }

    suspend fun logout() {
        sessionStore.clearTjuCredentials()
    }

    private suspend fun fetch(username: String, password: String): TjuClassesBundle {
        refreshSemesterInfo()
        debug("请求 TJU 教务聚合接口")
        val root = api.getClasses(username, password)
        val code = root.intOrNull("code") ?: root.intOrNull("error_code") ?: 200
        val dataElement = root.get("data")
        debug("聚合接口返回 code=$code，dataType=${dataElement?.javaClass?.simpleName ?: "null"}")
        if (code != 200) {
            debug("聚合接口失败，尝试 classes.tju.edu.cn 直抓兜底")
            val directAcademicResult = runCatching { fetchDirectAcademicData(username, password) }
            directAcademicResult.onFailure { debug("直抓兜底失败：${it.message ?: it::class.java.simpleName}") }
            val directAcademicData = directAcademicResult.getOrNull()
            if (directAcademicData != null) {
                return TjuClassesBundle(
                    courses = emptyList(),
                    exams = directAcademicData.exams,
                    gpa = directAcademicData.gpa,
                )
            }
            throw TjuApiException(dataElement?.asStringOrNull() ?: root.stringOrNull("message") ?: "教务网登录失败")
        }
        val data = dataElement?.asJsonObjectOrNull() ?: throw TjuApiException("教务网返回数据为空")
        val backendCourses = parseCourses(data.getAsJsonObjectOrNull("courses"))
        val backendExams = data.getAsJsonArrayOrNull("exams").jsonElements().mapNotNull { it.asObjectOrNull()?.toExam() }
        val backendGpa = data.getAsJsonObjectOrNull("gpa")?.toGpa()
        debug("聚合接口解析：课程 ${backendCourses.size}，成绩 ${backendGpa?.courses.orEmpty().size}，考试 ${backendExams.size}")
        val directAcademicResult = runCatching { fetchDirectAcademicData(username, password) }
        directAcademicResult.onFailure { debug("直抓失败：${it.message ?: it::class.java.simpleName}") }
        val directAcademicData = directAcademicResult.getOrNull()
        val resolvedGpa = directAcademicData?.gpa?.takeIf { it.hasDisplayableGrades() } ?: backendGpa
        debug(
            "最终采用数据：成绩 ${resolvedGpa?.courses.orEmpty().size}，考试 ${
                directAcademicData?.exams?.ifEmpty { backendExams }?.size ?: backendExams.size
            }",
        )
        if (resolvedGpa?.hasDisplayableGrades() != true && directAcademicData?.gpaError != null) {
            throw TjuApiException("成绩获取失败：${directAcademicData.gpaError}")
        }
        return TjuClassesBundle(
            courses = backendCourses,
            exams = directAcademicData?.exams?.ifEmpty { backendExams } ?: backendExams,
            gpa = resolvedGpa,
        )
    }

    private suspend fun fetchDirectAcademicData(username: String, password: String): DirectAcademicData = withContext(Dispatchers.IO) {
        debug("直抓：开始 SSO 登录与教务身份确认")
        val identity = loginSso(username, password)
        debug("直抓：身份确认 isMaster=${identity.isMaster}，hasMinor=${identity.hasMinor}")
        val gpaResult = runCatching { fetchDirectGpaAfterLogin(isMaster = identity.isMaster) }
        gpaResult.onSuccess { debug("直抓成绩成功：课程 ${it.courses.size}，学期 ${it.stats.size}") }
            .onFailure { debug("直抓成绩失败：${it.message ?: it::class.java.simpleName}") }
        val examResult = runCatching { fetchDirectExamsAfterLogin() }
        examResult.onSuccess { debug("直抓考试成功：${it.size} 场") }
            .onFailure { debug("直抓考试失败：${it.message ?: it::class.java.simpleName}") }
        DirectAcademicData(
            exams = examResult.getOrDefault(emptyList()),
            gpa = gpaResult.getOrNull(),
            gpaError = gpaResult.exceptionOrNull()?.message,
        )
    }

    private fun fetchDirectExamsAfterLogin(): List<TjuExamDto> {
        val request = Request.Builder()
            .url("https://classes.tju.edu.cn/eams/stdExamTable!examTable.action")
            .get()
            .build()
        val html = classesClient.newCall(request).execute().use { response ->
            debug("考试页 HTTP ${response.code}，url=${response.request.url.encodedPath}")
            if (!response.isSuccessful) throw TjuApiException("考试安排请求失败：${response.code}")
            response.body?.string().orEmpty()
        }
        debug("考试页 HTML 长度=${html.length}，containsGrid=${html.contains("gridtable")}")
        if (html.contains("统一认证系统")) throw TjuApiException("办公网绑定失效，请重新绑定")
        return html.parseExamTable().also { exams ->
            debug("考试页解析：${exams.size} 场，未安排 ${exams.count { it.date.isBlank() || it.location.isBlank() }} 场")
        }
    }

    private fun fetchDirectGpaAfterLogin(isMaster: Boolean): TjuGpaDto {
        val projectId = if (isMaster) "22" else "1"
        debug("成绩页：切换 projectId=$projectId")
        val switchRequest = Request.Builder()
            .url("https://classes.tju.edu.cn/eams/courseTableForStd!index.action?projectId=$projectId")
            .get()
            .build()
        classesClient.newCall(switchRequest).execute().use { response ->
            debug("成绩页切换 HTTP ${response.code}，url=${response.request.url.encodedPath}")
            if (!response.isSuccessful && response.code != 302) {
                throw TjuApiException("成绩系统切换失败：${response.code}")
            }
        }
        val request = Request.Builder()
            .url("https://classes.tju.edu.cn/eams/teach/grade/course/person!historyCourseGrade.action?projectType=MAJOR")
            .get()
            .build()
        val html = classesClient.newCall(request).execute().use { response ->
            debug("成绩页 HTTP ${response.code}，url=${response.request.url.encodedPath}")
            if (!response.isSuccessful) throw TjuApiException("成绩请求失败：${response.code}")
            response.body?.string().orEmpty()
        }
        debug("成绩页 HTML 长度=${html.length}，containsGrid=${html.contains("gridtable")}，containsSummary=${html.contains("在校汇总")}")
        return html.parseDirectGpa(isMaster)
    }

    private fun loginSso(username: String, password: String): ClassesIdentity {
        debug("SSO：请求登录页")
        val loginPageRequest = Request.Builder()
            .url("https://sso.tju.edu.cn/cas/login")
            .get()
            .build()
        val loginPage = spiderClient.newCall(loginPageRequest).execute().use { response ->
            debug("SSO 登录页 HTTP ${response.code}")
            if (response.code == 302) return@use ""
            response.body?.string().orEmpty()
        }
        if (loginPage.isNotBlank()) {
            debug("SSO：需要提交账号密码与验证码")
            val execution = loginPage.findFirst("""name="execution"\s+value="(\w+)"""")
                ?: throw TjuApiException("无法获取办公网登录参数")
            val lt = loginPage.findFirst("""name="lt"\s+value="([\w-]+)"""")
                ?: throw TjuApiException("无法获取办公网登录参数")
            val rsa = fetchRsa(username + password + lt)
            val code = fetchCaptchaCode()
            val loginBody = FormBody.Builder()
                .add("code", code)
                .add("ul", username.length.toString())
                .add("pl", password.length.toString())
                .add("lt", lt)
                .add("rsa", rsa)
                .add("execution", execution)
                .add("_eventId", "submit")
                .build()
            val loginRequest = Request.Builder()
                .url("https://sso.tju.edu.cn/cas/login")
                .post(loginBody)
                .build()
            val loginResult = spiderClient.newCall(loginRequest).execute().use { response ->
                debug("SSO 提交 HTTP ${response.code}")
                response.code to response.body?.string().orEmpty()
            }
            if (loginResult.first != 302 && !loginResult.second.contains("var remind_strong_pwd = 'true'")) {
                throw TjuApiException("办公网账号密码或验证码校验失败")
            }
        } else {
            debug("SSO：已有会话，跳过账号密码提交")
        }
        return completeClassesIdentity()
    }

    private fun fetchRsa(value: String): String {
        val body = FormBody.Builder().add("val", value).build()
        val request = Request.Builder()
            .url("https://learning.twt.edu.cn/enc")
            .post(body)
            .build()
        val json = spiderClient.newCall(request).execute().use { response ->
            debug("SSO 加密参数 HTTP ${response.code}")
            if (!response.isSuccessful) throw TjuApiException("办公网加密参数获取失败")
            response.body?.string().orEmpty()
        }
        return gson.fromJson(json, JsonObject::class.java)
            ?.get("data")
            ?.asStringOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: throw TjuApiException("办公网加密参数为空")
    }

    private fun fetchCaptchaCode(): String {
        val captchaRequest = Request.Builder()
            .url("https://sso.tju.edu.cn/cas/code")
            .get()
            .build()
        val bytes = spiderClient.newCall(captchaRequest).execute().use { response ->
            debug("SSO 验证码图片 HTTP ${response.code}")
            if (!response.isSuccessful) throw TjuApiException("验证码获取失败")
            response.body?.bytes() ?: ByteArray(0)
        }
        val imageBody = bytes.toRequestBody("image/jpg".toMediaType())
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("image", "123.png", imageBody)
            .build()
        val request = Request.Builder()
            .url("https://learning.twt.edu.cn/ocr")
            .post(body)
            .build()
        val json = spiderClient.newCall(request).execute().use { response ->
            debug("SSO 验证码识别 HTTP ${response.code}")
            if (!response.isSuccessful) throw TjuApiException("验证码识别失败")
            response.body?.string().orEmpty()
        }
        return gson.fromJson(json, JsonObject::class.java)
            ?.get("data")
            ?.asStringOrNull()
            ?.takeIf { it.isNotBlank() }
            ?: throw TjuApiException("验证码识别为空")
    }

    private fun completeClassesIdentity(): ClassesIdentity {
        var url = "https://classes.tju.edu.cn/eams/dataQuery.action"
        var redirect = false
        var settled = false
        repeat(8) step@{
            if (settled) return@step
            val requestBuilder = Request.Builder().url(url)
            val request = if (redirect) {
                requestBuilder.get().build()
            } else {
                requestBuilder.post(FormBody.Builder().add("entityId", "").build()).build()
            }
            val response = spiderClient.newCall(request).execute()
            response.use {
                debug("身份跳转 HTTP ${it.code}，path=${it.request.url.encodedPath}")
                if (it.code == 302) {
                    url = it.header("location") ?: return@step
                    redirect = true
                } else {
                    settled = true
                    return@step
                }
            }
        }
        val identityRequest = Request.Builder()
            .url(url)
            .post(FormBody.Builder().add("entityId", "").build())
            .build()
        val identityHtml = spiderClient.newCall(identityRequest).execute().use { response ->
            debug("身份页 HTTP ${response.code}，path=${response.request.url.encodedPath}")
            response.body?.string().orEmpty()
        }
        debug("身份页 HTML 长度=${identityHtml.length}，本科=${identityHtml.contains("本科")}，研究=${identityHtml.contains("研究") || identityHtml.contains("研究生")}")
        return ClassesIdentity(
            isMaster = identityHtml.contains(" 研究") || identityHtml.contains("研究生"),
            hasMinor = identityHtml.contains("辅修"),
        )
    }

    private fun parseCourses(courses: JsonObject?): List<TjuCourseDto> {
        if (courses == null) return emptyList()
        val major = courses.getAsJsonArrayOrNull("major").jsonElements()
        val minor = courses.getAsJsonArrayOrNull("minor").jsonElements()
        return (major + minor).mapNotNull { it.asObjectOrNull()?.toCourse() }
    }

    private fun JsonObject.toCourse(): TjuCourseDto {
        return TjuCourseDto(
            name = stringOrNull("name").orEmpty(),
            classId = stringOrNull("classId").orEmpty(),
            courseId = stringOrNull("courseId").orEmpty(),
            credit = stringOrNull("credit").orEmpty(),
            campus = stringOrNull("campus").orEmpty(),
            weeks = stringOrNull("weeks").orEmpty(),
            teacherList = stringArray("teacherList"),
            arrangeList = getAsJsonArrayOrNull("arrangeList").jsonElements().mapNotNull { it.asObjectOrNull()?.toArrange() },
            type = intOrNull("type") ?: 0,
        )
    }

    private fun JsonObject.toArrange(): TjuArrangeDto {
        return TjuArrangeDto(
            name = stringOrNull("name"),
            location = stringOrNull("location").orEmpty(),
            weekday = intOrNull("weekday") ?: 1,
            weekList = intArray("weekList"),
            unitList = intArray("unitList"),
            teacherList = stringArray("teacherList"),
            showMode = intOrNull("showMode") ?: 0,
            isExperiment = booleanOrNull("isExperiment") ?: false,
        )
    }

    private fun JsonObject.toExam(): TjuExamDto {
        return TjuExamDto(
            id = stringOrNull("id").orEmpty(),
            name = stringOrNull("name").orEmpty(),
            type = stringOrNull("type").orEmpty(),
            date = stringOrNull("date").orEmpty(),
            arrange = stringOrNull("arrange").orEmpty(),
            location = stringOrNull("location").orEmpty(),
            seat = stringOrNull("seat").orEmpty(),
            state = stringOrNull("state").orEmpty(),
            ext = stringOrNull("ext").orEmpty(),
        )
    }

    private fun JsonObject.toGpa(): TjuGpaDto {
        val total = getAsJsonObjectOrNull("total")
        val courses = getAsJsonArrayOrNull("courses").jsonElements().mapNotNull { it.asObjectOrNull()?.toGpaCourse() }
        val parsedStats = getAsJsonArrayOrNull("stats").jsonElements().mapNotNull { it.asObjectOrNull()?.toGpaStat() }
            .filter { it.hasDisplayableGrades() }
        val stats = parsedStats.ifEmpty { buildStats(courses) }
        val parsedTotal = TjuGpaTotalDto(
            score = total?.doubleOrNull("score") ?: 0.0,
            weighted = total?.doubleOrNull("weighted") ?: 0.0,
            gpa = total?.doubleOrNull("gpa") ?: 0.0,
            credit = total?.doubleOrNull("credit") ?: 0.0,
            credits = total?.doubleOrNull("credits") ?: total?.doubleOrNull("credit") ?: 0.0,
        )
        return TjuGpaDto(
            total = parsedTotal.takeUnless { it.weighted == 0.0 && it.score == 0.0 && it.gpa == 0.0 && it.displayCredit == 0.0 }
                ?: stats.toTotal(),
            courses = courses,
            stats = stats,
        )
    }

    private fun JsonObject.toGpaCourse(): TjuGpaCourseDto {
        val score = doubleOrNull("score") ?: 0.0
        val rawScore = stringOrNull("rawScore")
            ?: stringOrNull("raw_score")
            ?: stringOrNull("rawscore")
            ?: stringOrNull("score")
            ?: score.takeIf { it != 0.0 }?.toString()
            ?: ""
        return TjuGpaCourseDto(
            semester = stringOrNull("semester")?.replace("\\s+".toRegex(), " ").orEmpty(),
            name = stringOrNull("name").orEmpty(),
            classType = stringOrNull("classType") ?: stringOrNull("class_type").orEmpty(),
            score = rawScore.toDoubleOrNull() ?: score,
            rawScore = rawScore,
            credit = doubleOrNull("credit") ?: 0.0,
            gpa = doubleOrNull("gpa") ?: 0.0,
        )
    }

    private fun JsonObject.toGpaStat(): TjuGpaStatDto {
        val courses = getAsJsonArrayOrNull("courses").jsonElements().mapNotNull { it.asObjectOrNull()?.toGpaCourse() }
        return TjuGpaStatDto(
            term = (stringOrNull("term") ?: stringOrNull("semester").orEmpty()).toTermLabel(),
            weighted = doubleOrNull("weighted") ?: doubleOrNull("score") ?: 0.0,
            gpa = doubleOrNull("gpa") ?: 0.0,
            credits = doubleOrNull("credits") ?: doubleOrNull("credit") ?: 0.0,
            courses = courses,
        )
    }

    private fun buildStats(courses: List<TjuGpaCourseDto>): List<TjuGpaStatDto> {
        return courses.groupBy { it.semester }.mapNotNull { (semester, list) ->
            val filtered = list.filter { it.credit > 0.0 && it.score > 0.0 && !it.name.contains("重修") }
            val credits = filtered.sumOf { it.credit }
            if (credits == 0.0 && list.none { it.rawScore.isNotBlank() && it.score > 0.0 }) return@mapNotNull null
            val weighted = if (credits == 0.0) 0.0 else filtered.sumOf { it.credit * it.score } / credits
            val gpa = if (credits == 0.0) 0.0 else filtered.sumOf { it.credit * it.gpa } / credits
            TjuGpaStatDto(
                term = semester.toTermLabel(),
                weighted = weighted.round2(),
                gpa = gpa.round2(),
                credits = credits.round2(),
                courses = list,
            )
        }.sortedByDescending { it.term.toTermSortKey() }
    }

    private fun String.toTermLabel(): String {
        val compact = replace("\\s+".toRegex(), "")
        Regex("""([12])H(\d{2})""").find(compact)?.let { match ->
            val term = match.groupValues[1]
            val endYear = 2000 + match.groupValues[2].toInt()
            return "${endYear - 1}-${endYear} 学年第 ${term} 学期"
        }
        Regex("""(20\d{2})-?(20\d{2})([12])""").find(compact)?.let { match ->
            return "${match.groupValues[1]}-${match.groupValues[2]} 学年第 ${match.groupValues[3]} 学期"
        }
        val years = Regex("""20\d{2}""").findAll(compact).map { it.value }.toList()
        val term = compact.lastOrNull { it == '1' || it == '2' } ?: return this
        if (years.size >= 2) return "${years.first()}-${years.last()} 学年第 ${term} 学期"
        return this
    }

    private fun String.toTermSortKey(): Int {
        Regex("""([12])H(\d{2})""").find(this)?.let { match ->
            return (2000 + match.groupValues[2].toInt()) * 10 + match.groupValues[1].toInt()
        }
        Regex("""(20\d{2})-(20\d{2}).*第\s*([12])\s*学期""").find(this)?.let { match ->
            return match.groupValues[2].toInt() * 10 + match.groupValues[3].toInt()
        }
        Regex("""(20\d{2})-?(20\d{2})([12])""").find(replace("\\s+".toRegex(), ""))?.let { match ->
            return match.groupValues[2].toInt() * 10 + match.groupValues[3].toInt()
        }
        return 0
    }

    private fun TjuGpaStatDto.hasDisplayableGrades(): Boolean =
        credits > 0.0 || weighted > 0.0 || gpa > 0.0 || courses.any { it.score > 0.0 || it.rawScore.isNotBlank() }

    private fun TjuGpaDto.hasDisplayableGrades(): Boolean =
        courses.any { it.score > 0.0 || it.rawScore.isNotBlank() } ||
            stats.any { it.hasDisplayableGrades() } ||
            total.displayScore > 0.0 ||
            total.gpa > 0.0 ||
            total.displayCredit > 0.0

    private fun Double.round2(): Double = kotlin.math.round(this * 100.0) / 100.0

    private fun List<TjuGpaStatDto>.toTotal(): TjuGpaTotalDto {
        val credits = sumOf { it.credits }
        val weighted = if (credits == 0.0) 0.0 else sumOf { it.weighted * it.credits } / credits
        val gpa = if (credits == 0.0) 0.0 else sumOf { it.gpa * it.credits } / credits
        return TjuGpaTotalDto(weighted = weighted.round2(), gpa = gpa.round2(), credits = credits.round2())
    }

    private fun String.parseExamTable(): List<TjuExamDto> {
        val html = normalizeClassesHtmlForParsing()
        val tbody = Regex("<tbody[\\s\\S]*?</tbody>", RegexOption.IGNORE_CASE).find(html)?.value.orEmpty()
        if (!tbody.contains("<td", ignoreCase = true)) return emptyList()
        return Regex("<tr[\\s\\S]*?</tr>", RegexOption.IGNORE_CASE).findAll(tbody).mapNotNull { row ->
            val cells = Regex("<td[^>]*>([\\s\\S]*?)</td>", RegexOption.IGNORE_CASE).findAll(row.value)
                .map { it.groupValues[1].cleanHtmlCell() }
                .toList()
            if (cells.size < 2 || cells.all { it.isBlank() }) return@mapNotNull null
            val ext = if (cells.getOrNull(8) == "正常") "" else cells.getOrNull(9).orEmpty()
            TjuExamDto(
                id = cells.getOrNull(0).orEmpty(),
                name = cells.getOrNull(1).orEmpty(),
                type = cells.getOrNull(2).orEmpty(),
                date = cells.getOrNull(3).orEmpty(),
                arrange = cells.getOrNull(5).orEmpty(),
                location = cells.getOrNull(6).orEmpty(),
                seat = cells.getOrNull(7).orEmpty(),
                state = cells.getOrNull(8).orEmpty(),
                ext = ext,
            )
        }.toList()
    }

    private fun String.parseDirectGpa(isMaster: Boolean): TjuGpaDto {
        val html = normalizeClassesHtmlForParsing()
        if (!html.contains("在校汇总") || html.contains("本次会话已经被过期")) {
            throw TjuApiException("成绩页面会话失效，请重新绑定")
        }
        if (html.contains("就差一个评教的距离啦")) {
            throw TjuApiException("存在未评教课程，请先前往教务网评教")
        }

        val totalTitle = if (isMaster) "在校汇总" else "总计"
        val totalCells = html.findGpaSummaryCells(totalTitle)
        debug("成绩汇总：title=$totalTitle，cells=${totalCells.size}")
        val total = TjuGpaTotalDto(
            weighted = totalCells.getOrNull(3) ?: 0.0,
            gpa = totalCells.getOrNull(2) ?: 0.0,
            credits = totalCells.getOrNull(1) ?: 0.0,
        )

        val tables = Regex(
            """<table[^>]*class\s*=\s*["'][^"']*\bgridtable\b[^"']*["'][^>]*>[\s\S]*?</table>""",
            RegexOption.IGNORE_CASE,
        )
            .findAll(html)
            .map { it.value }
            .toList()
            .ifEmpty {
                Regex("""(?<=gridtable)[\s\S]*?(?=</table)""", RegexOption.IGNORE_CASE)
                    .findAll(html)
                    .map { it.value }
                    .toList()
            }
        debug("成绩表格：gridtable 数量=${tables.size}")
        val courseTable = tables.firstOrNull { table ->
            table.contains("学年学期") && table.contains("课程名称") && table.contains("学分") && table.contains("绩点")
        } ?: tables.getOrNull(1) ?: tables.lastOrNull()
            ?: throw TjuApiException("成绩表格为空")
        val gridHead = Regex("""(?<=gridhead)[\s\S]*(?=</thead)""", RegexOption.IGNORE_CASE)
            .find(courseTable)
            ?.value
            ?: courseTable.substringBefore("</thead>", courseTable)
        val headers = Regex("""<th[^>]*>([\s\S][^<]*?)</th>""", RegexOption.IGNORE_CASE)
            .findAll(gridHead)
            .map { it.groupValues[1].cleanHtmlCell() }
            .toList()
        debug("成绩表头：${headers.joinToString("/")}")
        val indexMap = mutableMapOf<String, Int>()
        headers.forEachIndexed { index, title ->
            when (title) {
                "学年学期" -> indexMap["semester"] = index
                "课程代码" -> indexMap["code"] = index
                "课程序号" -> indexMap["no"] = index
                "课程名称" -> indexMap["name"] = index
                "课程类别", "课程性质" -> indexMap["type"] = index
                "考试情况" -> indexMap["condition"] = index
                "学分" -> indexMap["credit"] = index
                "总评成绩", "最终", "成绩" -> indexMap["score"] = index
                "绩点" -> indexMap["gpa"] = index
            }
        }
        val body = Regex("""(?<=<tbody>)[\s\S]*(?=</tbody>)""", RegexOption.IGNORE_CASE)
            .find(courseTable)
            ?.value
            ?: Regex("""<tbody[^>]*>([\s\S]*?)</tbody>""", RegexOption.IGNORE_CASE)
                .find(courseTable)
                ?.groupValues
                ?.getOrNull(1)
                .orEmpty()
        val courses = Regex("""(?<=<tr)[\s\S]*?(?=</tr)""", RegexOption.IGNORE_CASE)
            .findAll(body)
            .mapNotNull { row ->
                if (row.value.contains("重修")) return@mapNotNull null
                val originalStyleCells = Regex("""<td[ =":a-zA-Z]*?>([\s\S]*?)(?=<)""")
                    .findAll(row.value)
                    .map { it.groupValues[1].cleanHtmlCell().replace("\\s+".toRegex(), "") }
                    .toList()
                val fullCells = Regex("""<td[^>]*>([\s\S]*?)</td>""", RegexOption.IGNORE_CASE)
                    .findAll(row.value)
                    .map { it.groupValues[1].cleanHtmlCell().replace("\\s+".toRegex(), "") }
                    .toList()
                val cells = if (originalStyleCells.size >= headers.size && originalStyleCells.any { it.isNotBlank() }) {
                    originalStyleCells.mapIndexed { index, value -> value.ifBlank { fullCells.getOrNull(index).orEmpty() } }
                } else {
                    fullCells
                }
                cells.toDirectGpaCourse(indexMap)
            }
            .toList()
        debug("成绩课程解析：${courses.size} 条，含分数 ${courses.count { it.rawScore.isNotBlank() }} 条")
        val stats = buildStats(courses)
        debug("成绩学期统计：${stats.size} 个学期")
        return TjuGpaDto(
            total = total.takeUnless { it.displayScore == 0.0 && it.gpa == 0.0 && it.displayCredit == 0.0 }
                ?: stats.toTotal(),
            courses = courses,
            stats = stats,
        )
    }

    private fun String.findGpaSummaryCells(title: String): List<Double> {
        val row = Regex("""<tr[^>]*>[\s\S]*?<th[^>]*>\s*$title\s*</th>[\s\S]*?</tr>""", RegexOption.IGNORE_CASE)
            .find(this)
            ?.value
            .orEmpty()
        return Regex("""<th[^>]*>([\s\S]*?)</th>""", RegexOption.IGNORE_CASE)
            .findAll(row)
            .map { it.groupValues[1].cleanHtmlCell().toDoubleOrNull() }
            .filterNotNull()
            .toList()
    }

    private fun List<String>.toDirectGpaCourse(indexMap: Map<String, Int>): TjuGpaCourseDto? {
        val rawScore = getByIndexName(indexMap, "score")
            .ifBlank { getOrNull(10).orEmpty() }
            .ifBlank { getOrNull(9).orEmpty() }
        if (rawScore.isBlank() || rawScore == "缓考" || rawScore == "--") return null
        return TjuGpaCourseDto(
            semester = getByIndexName(indexMap, "semester").ifBlank { getOrNull(0).orEmpty() },
            name = getByIndexName(indexMap, "name").ifBlank { getOrNull(3).orEmpty() },
            classType = getByIndexName(indexMap, "type").ifBlank { getOrNull(4).orEmpty() },
            score = rawScore.toDoubleOrNull() ?: 0.0,
            rawScore = rawScore,
            credit = getByIndexName(indexMap, "credit").ifBlank { getOrNull(5).orEmpty() }.toDoubleOrNull() ?: 0.0,
            gpa = getByIndexName(indexMap, "gpa").ifBlank { lastOrNull().orEmpty() }.toDoubleOrNull() ?: 0.0,
        )
    }

    private fun List<String>.getByIndexName(indexMap: Map<String, Int>, name: String): String {
        return indexMap[name]?.let { getOrNull(it) }.orEmpty()
    }

    private fun String.cleanHtmlCell(): String {
        return replace(Regex("<font[^>]*>([\\s\\S]*?)</font>", RegexOption.IGNORE_CASE), "$1")
            .replace(Regex("<[^>]+>"), "")
            .replace("&nbsp;", " ")
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }

    private fun String.normalizeClassesHtmlForParsing(): String {
        if (!contains("""class="line-content"""") || !contains("&lt;")) return this
        return Regex("""<td class="line-content">([\s\S]*?)</td></tr>""", RegexOption.IGNORE_CASE)
            .findAll(this)
            .joinToString("\n") { match ->
                match.groupValues[1]
                    .replace(Regex("<[^>]+>"), "")
                    .decodeHtmlEntities()
            }
    }

    private fun String.decodeHtmlEntities(): String =
        replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&nbsp;", " ")

    private fun String.findFirst(pattern: String): String? {
        return Regex(pattern, RegexOption.IGNORE_CASE).find(this)?.groupValues?.getOrNull(1)
    }

    private fun JsonObject.getAsJsonObjectOrNull(name: String): JsonObject? = get(name)?.asJsonObjectOrNull()
    private fun JsonObject.getAsJsonArrayOrNull(name: String): JsonArray? = get(name)?.takeIf { it.isJsonArray }?.asJsonArray
    private fun JsonElement.asObjectOrNull(): JsonObject? = takeIf { it.isJsonObject }?.asJsonObject
    private fun JsonElement.asJsonObjectOrNull(): JsonObject? = takeIf { it.isJsonObject }?.asJsonObject
    private fun JsonElement.asStringOrNull(): String? = takeIf { it.isJsonPrimitive }?.asString
    private fun JsonObject.stringOrNull(name: String): String? = get(name)?.asStringOrNull()
    private fun JsonObject.intOrNull(name: String): Int? = get(name)?.asStringOrNull()?.toIntOrNull()
    private fun JsonObject.doubleOrNull(name: String): Double? = get(name)?.asStringOrNull()?.toDoubleOrNull()
    private fun JsonObject.booleanOrNull(name: String): Boolean? = get(name)?.asStringOrNull()?.toBooleanStrictOrNull()
    private fun JsonObject.stringArray(name: String): List<String> = getAsJsonArrayOrNull(name).jsonElements().mapNotNull { it.asStringOrNull() }
    private fun JsonObject.intArray(name: String): List<Int> = getAsJsonArrayOrNull(name).jsonElements().mapNotNull { it.asStringOrNull()?.toIntOrNull() }
    private fun JsonArray?.jsonElements(): List<JsonElement> = this?.toList() ?: emptyList()

    private fun debug(message: String) {
        TjuDebugLog.add(message)
    }

    private suspend fun refreshSemesterInfo() {
        authRepository.semester()
            .onSuccess { semester ->
                debug("学期信息：${semester.semesterName.orEmpty()}，开学 ${semester.semesterStartAt.orEmpty()}")
            }
            .onFailure { error ->
                debug("学期信息刷新失败：${error.message ?: error::class.java.simpleName}")
            }
    }

    private fun String.maskForLog(): String {
        if (length <= 2) return "***"
        return take(2) + "***" + takeLast(1)
    }
}

private data class DirectAcademicData(
    val exams: List<TjuExamDto>,
    val gpa: TjuGpaDto?,
    val gpaError: String? = null,
)

private data class ClassesIdentity(
    val isMaster: Boolean,
    val hasMinor: Boolean,
)

private class InMemoryCookieJar : CookieJar {
    private val cookies = mutableMapOf<String, MutableList<Cookie>>()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        this.cookies.getOrPut(url.host) { mutableListOf() }.apply {
            removeAll { existing -> cookies.any { it.name == existing.name } }
            addAll(cookies)
        }
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookies.filterKeys { host -> url.host == host || url.host.endsWith(".$host") }
            .values
            .flatten()
    }
}
