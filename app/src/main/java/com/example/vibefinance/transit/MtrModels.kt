package com.example.vibefinance.transit

data class MtrStation(
    val id: Int,
    val code: String,
    val nameZh: String,
    val nameEn: String,
    val lines: List<String>,
    val latitude: Double,
    val longitude: Double
)

enum class MtrPassengerType(val titleZh: String, val titleEn: String) {
    ADULT("成人 (Adult)", "Adult"),
    STUDENT("學生 (Student)", "Student"),
    CHILD("小童 (Child)", "Child"),
    JOYYOU("長者/樂悠咭 (JoyYou)", "JoyYou / Senior");

    companion object {
        fun fromString(value: String?): MtrPassengerType {
            return entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: ADULT
        }
    }
}

data class TimestampedBreadcrumb(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val stationId: Int? = null,
    val stationName: String? = null,
    val accuracyMeters: Float? = null
)

data class MtrTransitRoute(
    val originStation: MtrStation,
    val destinationStation: MtrStation,
    val fare: Double,
    val passengerType: MtrPassengerType,
    val confidence: Float = 1.0f
) {
    val routeDisplay: String
        get() = "${originStation.nameZh} ➔ ${destinationStation.nameZh}"
}
