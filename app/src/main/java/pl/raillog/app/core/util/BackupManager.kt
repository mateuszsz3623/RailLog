package pl.raillog.app.core.util

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import pl.raillog.app.core.database.entity.DutyEntity

@Serializable
data class BackupPayload(
    val appName: String = "RailLog",
    val schemaVersion: Int = 3,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val dutiesCount: Int,
    val duties: List<DutyEntity>
)

object BackupManager {

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Serializes all duties into a complete JSON backup payload.
     */
    fun createBackupJson(duties: List<DutyEntity>): String {
        val payload = BackupPayload(
            schemaVersion = 3,
            dutiesCount = duties.size,
            duties = duties
        )
        return json.encodeToString(payload)
    }

    /**
     * Parses and validates a JSON backup payload.
     * Backwards-compatible with v1 backups due to ignoreUnknownKeys and default values in DutyEntity.
     */
    fun parseBackupJson(jsonString: String): Result<BackupPayload> {
        return try {
            val payload = json.decodeFromString<BackupPayload>(jsonString)
            Result.success(payload)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
