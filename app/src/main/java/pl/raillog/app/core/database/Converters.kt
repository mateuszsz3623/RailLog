package pl.raillog.app.core.database

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.raillog.app.domain.model.DutyLeg
import java.time.LocalDate

class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? {
        return value?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String {
        return if (list == null) "[]" else json.encodeToString(list)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromDutyLegList(list: List<DutyLeg>?): String {
        return if (list == null) "[]" else json.encodeToString(list)
    }

    @TypeConverter
    fun toDutyLegList(value: String?): List<DutyLeg> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
