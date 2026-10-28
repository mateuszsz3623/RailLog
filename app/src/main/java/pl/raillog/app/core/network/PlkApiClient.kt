package pl.raillog.app.core.network

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeParseException

data class PlkReply(val code: Int, val body: String, val hourlyRemaining: Int?)

/** Warstwa HTTP – w testach podstawiana atrapą. Rzuca [IOException] przy braku połączenia. */
interface PlkHttp {
    suspend fun get(url: String): PlkReply
}

sealed class PlkResult<out T> {
    data class Success<T>(val value: T, val hourlyRemaining: Int? = null) : PlkResult<T>()
    data class Failure(val message: String) : PlkResult<Nothing>()
}

class UrlConnectionPlkHttp(private val apiKey: String) : PlkHttp {
    override suspend fun get(url: String): PlkReply = withContext(Dispatchers.IO) {
        require(url.startsWith("https://")) { "Dozwolone tylko HTTPS" }
        val connection = URL(url).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = TIMEOUT_MS
            connection.readTimeout = TIMEOUT_MS
            connection.setRequestProperty("X-API-Key", apiKey)
            connection.setRequestProperty("Accept", "application/json")
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            PlkReply(code, body, connection.getHeaderField("X-RateLimit-Hourly-Remaining")?.trim()?.toIntOrNull())
        } finally {
            connection.disconnect()
        }
    }

    private companion object {
        const val TIMEOUT_MS = 15_000
    }
}

/** Klient API PLK (Otwarte Dane Kolejowe): rozkład IC (z cache dziennym) i dane o pociągu na żywo. */
class PlkApiClient(
    private val http: PlkHttp,
    cacheRoot: File,
    private val today: () -> LocalDate = { LocalDate.now() }
) {
    private val cacheDir = File(cacheRoot, "plk")

    suspend fun scheduleJson(date: LocalDate): PlkResult<String> {
        val cacheFile = File(cacheDir, "schedules_${date}_IC.json")
        val cached = withContext(Dispatchers.IO) {
            if (cacheFile.isFile) runCatching { cacheFile.readText(Charsets.UTF_8) }.getOrNull() else null
        }
        if (!cached.isNullOrBlank()) return PlkResult.Success(cached)

        val result = request("/api/v1/schedules?dateFrom=$date&dateTo=$date&carriersInclude=IC")
        if (result is PlkResult.Success) {
            withContext(Dispatchers.IO) {
                runCatching {
                    cacheDir.mkdirs()
                    cacheFile.writeText(result.value, Charsets.UTF_8)
                    cleanOldCache()
                }
            }
        }
        return result
    }

    suspend fun liveTrainJson(scheduleId: Int, orderId: Long, date: LocalDate): PlkResult<String> =
        request("/api/v1/operations/train/$scheduleId/$orderId/$date?withPlanned=true")

    /** Usuwa pliki rozkładu starsze niż 3 dni. */
    private fun cleanOldCache() {
        val limit = today().minusDays(3)
        cacheDir.listFiles()?.forEach { file ->
            val datePart = file.name.removePrefix("schedules_").substringBefore("_IC")
            val fileDate = try {
                LocalDate.parse(datePart)
            } catch (e: DateTimeParseException) {
                null
            }
            if (fileDate != null && fileDate.isBefore(limit)) file.delete()
        }
    }

    private suspend fun request(path: String): PlkResult<String> {
        return try {
            val reply = http.get(BASE_URL + path)
            when {
                reply.code in 200..299 -> PlkResult.Success(reply.body, reply.hourlyRemaining)
                reply.code == 401 || reply.code == 403 -> PlkResult.Failure("Klucz API nieprawidłowy")
                reply.code == 429 -> PlkResult.Failure("Wyczerpany limit zapytań (100/h) — spróbuj później")
                else -> PlkResult.Failure("Błąd serwera PLK (${reply.code})")
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            PlkResult.Failure("Brak połączenia z internetem")
        } catch (e: Exception) {
            PlkResult.Failure("Błąd połączenia: ${e.localizedMessage ?: e.javaClass.simpleName}")
        }
    }

    companion object {
        const val BASE_URL = "https://pdp-api.plk-sa.pl"
    }
}
