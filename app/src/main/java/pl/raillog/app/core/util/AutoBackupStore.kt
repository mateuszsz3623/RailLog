package pl.raillog.app.core.util

import java.io.File
import java.io.IOException

/**
 * Przechowuje kilka ostatnich kopii awaryjnych (JSON) tworzonych przed nadpisaniem bazy.
 * Pliki: auto_<millis>.json w katalogu [dir]; zostaje [maxFiles] najnowszych.
 */
class AutoBackupStore(
    private val dir: File,
    private val maxFiles: Int = 3,
    private val clock: () -> Long = System::currentTimeMillis
) {

    @Throws(IOException::class)
    fun save(json: String): File {
        if (!dir.isDirectory && !dir.mkdirs()) {
            throw IOException("Nie można utworzyć katalogu kopii awaryjnych: ${dir.path}")
        }
        val target = File(dir, PREFIX + clock() + SUFFIX)
        val tmp = File(dir, target.name + ".tmp")
        tmp.writeText(json, Charsets.UTF_8)
        if (!tmp.renameTo(target)) {
            tmp.delete()
            throw IOException("Nie udało się zapisać kopii awaryjnej: ${target.name}")
        }
        files().drop(maxFiles).forEach { it.delete() }
        return target
    }

    fun latest(): File? = files().firstOrNull()

    fun timestampOf(file: File): Long =
        file.name.removePrefix(PREFIX).removeSuffix(SUFFIX).toLongOrNull() ?: 0L

    fun delete(file: File) {
        file.delete()
    }

    /** Pliki kopii od najnowszego. */
    private fun files(): List<File> =
        dir.listFiles { f -> f.isFile && f.name.startsWith(PREFIX) && f.name.endsWith(SUFFIX) }
            ?.sortedByDescending { timestampOf(it) }
            ?: emptyList()

    private companion object {
        const val PREFIX = "auto_"
        const val SUFFIX = ".json"
    }
}
