package pl.raillog.app.core.util

import pl.raillog.app.domain.model.Duty

object CsvExporter {

    /**
     * Generates CSV content formatted specifically for Polish Excel:
     * - UTF-8 BOM (\uFEFF) so Excel opens Polish diacritics (ą, ę, ł, etc.) correctly
     * - Semicolon (;) separator used by default in Polish locale Excel
     * - Escaping of quotes and newlines
     */
    fun exportToCsv(duties: List<Duty>): String {
        val sb = StringBuilder()
        
        // UTF-8 Byte Order Mark (BOM)
        sb.append("\uFEFF")

        // CSV Header
        sb.append("Data;Rozpoczęcie;Zakończenie;Czas służby;Pojazd;Przebieg trasy;Pociągi;Szczegóły odcinków;Nadgodziny (min);Nadgodziny sformatowane;Cała służba nadgodziny;Uwagi;Dodatkowe notatki\n")

        for (duty in duties) {
            val dateStr = duty.date.toString()
            val start = escapeCsv(duty.startTime ?: "")
            val end = escapeCsv(duty.endTime ?: "")
            val duration = escapeCsv(duty.displayDuration ?: "")
            val vehicle = escapeCsv(duty.displayVehicle)
            val route = escapeCsv(duty.displayRoute)
            val trains = escapeCsv(duty.displayTrains)
            val legsDetail = escapeCsv(
                duty.legs.joinToString(" | ") { leg ->
                    val extra = listOfNotNull(
                        leg.trainNumber.takeIf { it.isNotBlank() }?.let { "Poc. $it" },
                        leg.displayVehicle.takeIf { it != "—" }
                    ).joinToString(", ")
                    if (extra.isNotEmpty()) "${leg.routeText} [$extra]" else leg.routeText
                }
            )
            val overtimeMin = duty.overtimeMinutes.toString()
            val overtimeFmt = escapeCsv(duty.displayOvertime.removePrefix("+"))
            val wholeDuty = if (duty.isWholeDutyOvertime) "TAK" else "NIE"
            val notes = escapeCsv(duty.notes ?: "")
            val additionalNotes = escapeCsv(duty.additionalNotes ?: "")

            sb.append("$dateStr;$start;$end;$duration;$vehicle;$route;$trains;$legsDetail;$overtimeMin;$overtimeFmt;$wholeDuty;$notes;$additionalNotes\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var res = value.replace("\"", "\"\"")
        if (res.contains(";") || res.contains("\n") || res.contains("\"") || res.contains(",")) {
            res = "\"$res\""
        }
        return res
    }
}
