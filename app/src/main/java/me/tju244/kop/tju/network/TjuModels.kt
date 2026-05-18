package me.tju244.kop.tju.network

data class TjuClassesBundle(
    val courses: List<TjuCourseDto> = emptyList(),
    val exams: List<TjuExamDto> = emptyList(),
    val gpa: TjuGpaDto? = null,
)

data class TjuCourseDto(
    val name: String = "",
    val classId: String = "",
    val courseId: String = "",
    val credit: String = "",
    val campus: String = "",
    val weeks: String = "",
    val teacherList: List<String> = emptyList(),
    val arrangeList: List<TjuArrangeDto> = emptyList(),
    val type: Int = 0,
) {
    val location: String
        get() = arrangeList.firstOrNull { it.location.isNotBlank() }?.location.orEmpty()

    val teachers: String
        get() = teacherList.ifEmpty { arrangeList.flatMap { it.teacherList } }.distinct().joinToString("、")
}

data class TjuArrangeDto(
    val name: String? = null,
    val location: String = "",
    val weekday: Int = 1,
    val weekList: List<Int> = emptyList(),
    val unitList: List<Int> = emptyList(),
    val teacherList: List<String> = emptyList(),
    val showMode: Int = 0,
    val isExperiment: Boolean = false,
)

data class TjuExamDto(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val date: String = "",
    val arrange: String = "",
    val location: String = "",
    val seat: String = "",
    val state: String = "",
    val ext: String = "",
)

data class TjuGpaDto(
    val total: TjuGpaTotalDto = TjuGpaTotalDto(),
    val courses: List<TjuGpaCourseDto> = emptyList(),
    val stats: List<TjuGpaStatDto> = emptyList(),
)

data class TjuGpaTotalDto(
    val score: Double = 0.0,
    val weighted: Double = 0.0,
    val gpa: Double = 0.0,
    val credit: Double = 0.0,
    val credits: Double = 0.0,
) {
    val displayScore: Double get() = if (weighted != 0.0) weighted else score
    val displayCredit: Double get() = if (credits != 0.0) credits else credit
}

data class TjuGpaCourseDto(
    val semester: String = "",
    val name: String = "",
    val classType: String = "",
    val score: Double = 0.0,
    val rawScore: String = "",
    val credit: Double = 0.0,
    val gpa: Double = 0.0,
)

data class TjuGpaStatDto(
    val term: String = "",
    val weighted: Double = 0.0,
    val gpa: Double = 0.0,
    val credits: Double = 0.0,
    val courses: List<TjuGpaCourseDto> = emptyList(),
)

class TjuApiException(message: String) : Exception(message)

