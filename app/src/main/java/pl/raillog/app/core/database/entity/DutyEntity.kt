package pl.raillog.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import pl.raillog.app.domain.model.DutyLeg
import java.time.LocalDate

object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: LocalDate) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

@Serializable
@Entity(
    tableName = "duties",
    indices = [
        Index(value = ["date"]),
        Index(value = ["vehicleSeries"]),
        Index(value = ["createdAt"])
    ]
)
data class DutyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    
    val vehicleSeries: String? = null,
    val vehicleNumber: String? = null,
    
    // Optional shift start and end times in "HH:mm" format
    val startTime: String? = null,
    val endTime: String? = null,
    val dutyDurationMinutes: Int = 0,
    
    // Ordered segments of the duty (each segment has start, end, train number, optional vehicle)
    val legs: List<DutyLeg> = emptyList(),

    // Legacy fields kept for backward compatibility and fallback
    val routeSegments: List<String> = emptyList(),
    val trainNumbers: List<String> = emptyList(),
    
    // Overtime in minutes (e.g. 80 = 1h 20m)
    val overtimeMinutes: Int = 0,
    
    // Flag if whole shift was overtime
    val isWholeDutyOvertime: Boolean = false,
    
    val notes: String? = null,
    val additionalNotes: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
