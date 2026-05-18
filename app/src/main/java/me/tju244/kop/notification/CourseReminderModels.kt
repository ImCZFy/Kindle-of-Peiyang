package me.tju244.kop.notification

import me.tju244.kop.tju.network.TjuArrangeDto
import me.tju244.kop.tju.network.TjuCourseDto
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class CourseReminderPayload(
    val courseName: String,
    val location: String,
    val timeRange: String,
    val teacher: String,
    val startAtMillis: Long,
)

private val reminderClockFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

fun CourseReminderPayload.displayTimeText(): String {
    val normalized = timeRange.trim()
    if (normalized.isNotEmpty() && !normalized.contains("分钟")) return normalized
    return java.time.Instant.ofEpochMilli(startAtMillis)
        .atZone(ZoneId.systemDefault())
        .toLocalTime()
        .format(reminderClockFormatter)
}

private data class CourseCandidate(
    val courseName: String,
    val location: String,
    val teacher: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val units: List<Int>,
)

private val sectionTimes = mapOf(
    1 to (LocalTime.of(8, 30) to LocalTime.of(9, 15)),
    2 to (LocalTime.of(9, 20) to LocalTime.of(10, 5)),
    3 to (LocalTime.of(10, 25) to LocalTime.of(11, 10)),
    4 to (LocalTime.of(11, 15) to LocalTime.of(12, 0)),
    5 to (LocalTime.of(13, 30) to LocalTime.of(14, 15)),
    6 to (LocalTime.of(14, 20) to LocalTime.of(15, 5)),
    7 to (LocalTime.of(15, 25) to LocalTime.of(16, 10)),
    8 to (LocalTime.of(16, 15) to LocalTime.of(17, 0)),
    9 to (LocalTime.of(18, 30) to LocalTime.of(19, 15)),
    10 to (LocalTime.of(19, 20) to LocalTime.of(20, 5)),
    11 to (LocalTime.of(20, 10) to LocalTime.of(20, 55)),
    12 to (LocalTime.of(21, 0) to LocalTime.of(21, 45)),
)

fun nextCourseReminderPayload(
    courses: List<TjuCourseDto>,
    now: LocalDateTime = LocalDateTime.now(),
): CourseReminderPayload? {
    return courseReminderPayloads(courses = courses, now = now, daysAhead = 30).firstOrNull()
}

fun courseReminderPayloads(
    courses: List<TjuCourseDto>,
    now: LocalDateTime = LocalDateTime.now(),
    daysAhead: Int = 30,
): List<CourseReminderPayload> {
    val nowMillis = now.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val maxWeek = courses.maxTeachingWeekForReminder()
    val days = (0 until daysAhead.coerceAtLeast(1)).map { now.toLocalDate().plusDays(it.toLong()) }
    return days.flatMap { date ->
        val week = calculateTeachingWeekForReminder(maxWeek, date)
        val weekday = date.dayOfWeek.value
        courses.flatMap { course ->
            course.arrangeList.mapNotNull { arrange ->
                arrange.toDateCandidate(course, week, weekday, date)
            }
        }
    }.sortedBy { it.start }
        .fold(mutableListOf<CourseCandidate>()) { merged, candidate ->
            val last = merged.lastOrNull()
            if (
                last != null &&
                last.courseName == candidate.courseName &&
                last.location == candidate.location &&
                !candidate.start.isAfter(last.end.plusMinutes(30))
            ) {
                merged[merged.lastIndex] = last.copy(
                    end = maxOf(last.end, candidate.end),
                    units = (last.units + candidate.units).distinct().sorted(),
                )
            } else {
                merged.add(candidate)
            }
            merged
        }
        .filter { candidate ->
            val start = candidate.start.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            start > nowMillis + 30_000L
        }
        .map { it.toReminderPayload() }
}

fun calculateReminderTriggerMillis(payload: CourseReminderPayload): Long {
    return payload.startAtMillis - Duration.ofMinutes(20).toMillis()
}

private fun CourseCandidate.toReminderPayload(): CourseReminderPayload {
    val timeRange = if (units.isNotEmpty()) {
        val first = sectionTimes[units.minOrNull() ?: units.first()]?.first
        val last = sectionTimes[units.maxOrNull() ?: units.last()]?.second
        if (first != null && last != null) "%02d:%02d-%02d:%02d".format(first.hour, first.minute, last.hour, last.minute) else ""
    } else {
        ""
    }
    return CourseReminderPayload(
        courseName = courseName,
        location = location.ifBlank { "地点未安排" },
        timeRange = timeRange,
        teacher = teacher.ifBlank { "教师未公布" },
        startAtMillis = start.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
    )
}

private fun TjuArrangeDto.toDateCandidate(
    course: TjuCourseDto,
    week: Int,
    weekday: Int,
    date: LocalDate,
): CourseCandidate? {
    if (this.weekday != weekday || week !in weekList || unitList.isEmpty()) return null
    val firstTime = sectionTimes[unitList.minOrNull() ?: return null] ?: return null
    val lastTime = sectionTimes[unitList.maxOrNull() ?: return null] ?: firstTime
    return CourseCandidate(
        courseName = name.orEmpty().ifBlank { course.name },
        location = location.ifBlank { course.location },
        teacher = teacherList.joinToString("、").ifBlank { course.teachers },
        start = LocalDateTime.of(date, firstTime.first),
        end = LocalDateTime.of(date, lastTime.second),
        units = unitList,
    )
}

private fun List<TjuCourseDto>.maxTeachingWeekForReminder(): Int {
    val max = flatMap { course -> course.arrangeList.flatMap { it.weekList } }.maxOrNull()
    return (max ?: 18).coerceIn(1, 30)
}

private fun calculateTeachingWeekForReminder(maxWeek: Int, today: LocalDate): Int {
    val candidates = listOf(
        LocalDate.of(today.year - 1, 9, 1),
        LocalDate.of(today.year, 2, 17),
        LocalDate.of(today.year, 9, 1),
    ).map { start ->
        var monday = start
        while (monday.dayOfWeek.value != 1) monday = monday.plusDays(1)
        monday
    }.filter { !it.isAfter(today) }
    val termStart = candidates.maxOrNull() ?: today
    val week = ChronoUnit.DAYS.between(termStart, today).toInt() / 7 + 1
    return week.coerceIn(1, maxWeek.coerceAtLeast(1))
}

