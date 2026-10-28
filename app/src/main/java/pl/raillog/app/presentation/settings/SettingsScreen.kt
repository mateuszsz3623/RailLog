package pl.raillog.app.presentation.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import pl.raillog.app.BuildConfig
import pl.raillog.app.R
import pl.raillog.app.core.designsystem.AppThemeMode
import pl.raillog.app.core.designsystem.component.FormSectionHeader
import pl.raillog.app.core.designsystem.component.StatusBadge
import pl.raillog.app.core.designsystem.component.TechnicalCard
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    themeMode: AppThemeMode,
    onThemeModeChanged: (AppThemeMode) -> Unit
) {
    val context = LocalContext.current
    val isProcessing by viewModel.isProcessing.collectAsState()

    val autoBackupInfo by viewModel.autoBackupInfo.collectAsState()
    val planInfo by viewModel.planInfo.collectAsState()

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showUndoDialog by remember { mutableStateOf(false) }
    var showClearPlanDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }

    // File Picker for restoring backup
    val restoreFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val content = inputStream?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    pendingRestoreJson = content
                    showRestoreConfirmDialog = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Błąd odczytu pliku: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Import planu służb z pliku .ics (limit 2 MB, odczyt poza głównym wątkiem)
    val planFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        val bytes = context.contentResolver.openInputStream(uri)?.use { readAtMost(it, MAX_PLAN_BYTES + 1) }
                        when {
                            bytes == null -> Result.failure(java.io.IOException("Nie można otworzyć pliku"))
                            bytes.size > MAX_PLAN_BYTES -> Result.failure(java.io.IOException("Plik jest większy niż 2 MB"))
                            else -> Result.success(String(bytes, Charsets.UTF_8))
                        }
                    } catch (e: Exception) {
                        Result.failure(e)
                    }
                }
                val text = result.getOrNull()
                when {
                    text == null -> Toast.makeText(
                        context, "Błąd odczytu pliku: ${result.exceptionOrNull()?.localizedMessage}", Toast.LENGTH_LONG
                    ).show()
                    !text.contains("BEGIN:VCALENDAR", ignoreCase = true) -> Toast.makeText(
                        context, "To nie jest plik kalendarza (.ics)", Toast.LENGTH_LONG
                    ).show()
                    else -> viewModel.importPlan(text)
                }
            }
        }
    }

    // Zapis kopii JSON do pliku wskazanego przez użytkownika
    var pendingFileContent by remember { mutableStateOf<String?>(null) }
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        val content = pendingFileContent
        pendingFileContent = null
        if (uri != null && content != null) {
            scope.launch {
                val error = withContext(Dispatchers.IO) {
                    try {
                        context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(content.toByteArray(Charsets.UTF_8)) }
                            ?: throw java.io.IOException("Nie można otworzyć pliku")
                        null
                    } catch (e: Exception) {
                        e.localizedMessage ?: "nieznany błąd"
                    }
                }
                Toast.makeText(
                    context,
                    if (error == null) "Zapisano kopię do pliku ✓" else "Błąd zapisu pliku: $error",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.SaveToFile -> {
                    pendingFileContent = event.content
                    saveFileLauncher.launch(event.filename)
                }
                is SettingsEvent.ShareContent -> {
                    shareContent(context, event.content, event.mimeType, event.filename)
                }
                is SettingsEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Ustawienia i Dane",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: THEME SELECTION
            item {
                Spacer(modifier = Modifier.height(4.dp))
                FormSectionHeader(title = "MOTYW APLIKACJI")
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Palette, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Wybór stylu wizualnego",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))

                        ThemeOptionRow(
                            title = "Ciemny (Grafitowy)",
                            subtitle = "Głębokie grafity, wysoki kontrast, idealny w kabinie",
                            selected = themeMode == AppThemeMode.DARK,
                            onClick = { onThemeModeChanged(AppThemeMode.DARK) }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        ThemeOptionRow(
                            title = "Jasny (Technical Precision)",
                            subtitle = "Czysta stalowa biel, wysoka czytelność w pełnym słońcu",
                            selected = themeMode == AppThemeMode.LIGHT,
                            onClick = { onThemeModeChanged(AppThemeMode.LIGHT) }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        ThemeOptionRow(
                            title = "Zgodny z systemem Android",
                            subtitle = "Dopasowuje się automatycznie do trybu telefonu",
                            selected = themeMode == AppThemeMode.SYSTEM,
                            onClick = { onThemeModeChanged(AppThemeMode.SYSTEM) }
                        )
                    }
                }
            }

            // Section 2: EXPORT
            item {
                FormSectionHeader(title = "EKSPORT DANYCH")
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.TableChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Eksport do CSV (Excel)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Polskie kodowanie UTF-8 z BOM, separator średnik (;). Bezproblemowe otwarcie w programie Excel.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = viewModel::exportCsv,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("GENERUJ PLIK CSV", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Eksport do JSON",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Czysty format strukturalny dla integracji i zaawansowanych kopii.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = viewModel::exportJson,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("POBIERZ JSON", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Section 3: BACKUP & RESTORE
            item {
                FormSectionHeader(title = "KOPIA ZAPASOWA I PRZYWRACANIE")
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Kopia zapasowa (Pełny backup)",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Zapisz wszystkie swoje służby w jednym pliku, aby przenieść je na inny telefon.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = viewModel::exportJson,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("UTWÓRZ KOPIĘ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = { restoreFilePickerLauncher.launch(arrayOf("application/json", "text/*")) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PRZYWRÓĆ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = viewModel::saveJsonToFile,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ZAPISZ DO PLIKU", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        autoBackupInfo?.let { info ->
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = { showUndoDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("PRZYWRÓĆ DANE SPRZED OSTATNIEJ ZMIANY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = "Kopia z ${formatBackupTime(info.timestamp)} · ${info.dutiesCount} służb",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section: PLAN SŁUŻB (opcjonalnie)
            item {
                FormSectionHeader(title = "PLAN SŁUŻB (OPCJONALNIE)")
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Jeśli pracodawca udostępnia plan w pliku .ics, wczytaj go. Bez pliku aplikacja działa normalnie.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { planFilePickerLauncher.launch(arrayOf("*/*")) },
                            enabled = !isProcessing,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("IMPORTUJ PLAN (.ics)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        planInfo?.let { info ->
                            val fmt = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy")
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Wczytany plan: ${info.from.format(fmt)} – ${info.to.format(fmt)} · ${info.count} służb",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            TextButton(onClick = { showClearPlanDialog = true }) {
                                Text("WYCZYŚĆ PLAN", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Section 4: PRIVACY & ABOUT
            item {
                FormSectionHeader(title = "O APLIKACJI I PRYWATNOŚĆ")
                TechnicalCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(54.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_taktrail_logo),
                                    contentDescription = "Logo RailLog",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "RAILLOG",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = 1.2.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Dziennik służb maszynisty",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Prywatność",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bez kont, reklam i trackerów. Dane są na telefonie; kopia zapasowa Androida może je przechować na Twoim koncie Google. Podgląd pociągów łączy się z API PLK tylko, gdy o to poprosisz.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Wersja:",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            StatusBadge(text = "RailLog ${BuildConfig.VERSION_NAME}", color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Stworzone przez NIVEMI — Mateusz Szerszeń",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (BuildConfig.PLK_API_KEY.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Dane o pociągach: PKP Polskie Linie Kolejowe S.A.",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showClearPlanDialog) {
        AlertDialog(
            onDismissRequest = { showClearPlanDialog = false },
            title = { Text(text = "Wyczyścić plan służb?", fontWeight = FontWeight.Bold) },
            text = { Text("Usuniemy tylko wczytany plan. Zapisane służby zostają bez zmian.") },
            confirmButton = {
                Button(
                    onClick = {
                        showClearPlanDialog = false
                        viewModel.clearPlan()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("WYCZYŚĆ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearPlanDialog = false }) {
                    Text("ANULUJ")
                }
            }
        )
    }

    if (showUndoDialog) {
        AlertDialog(
            onDismissRequest = { showUndoDialog = false },
            icon = {
                Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = {
                Text(text = "Przywrócić dane sprzed zmiany?", fontWeight = FontWeight.Bold)
            },
            text = {
                val info = autoBackupInfo
                val backupLabel = info?.let { "${formatBackupTime(it.timestamp)} (${it.dutiesCount} służb)" } ?: "wybranej daty"
                Text("Obecne dane zostaną zastąpione kopią z $backupLabel. Obecny stan też zapiszemy jako kopię awaryjną.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showUndoDialog = false
                        viewModel.undoLastRestore()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("PRZYWRÓĆ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUndoDialog = false }) {
                    Text("ANULUJ")
                }
            }
        )
    }

    // Confirmation Dialog before Restoring Database
    if (showRestoreConfirmDialog && pendingRestoreJson != null) {
        AlertDialog(
            onDismissRequest = {
                showRestoreConfirmDialog = false
                pendingRestoreJson = null
            },
            icon = {
                Icon(imageVector = Icons.Default.Backup, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            title = {
                Text(text = "Przywrócić kopię zapasową?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("Wybierz sposób importu danych:\n\n• ZASTĄP CAŁOŚĆ: usunie obecną bazę i wgra wpisy z pliku.\n• SCAL: dopisze wpisy z pliku do Twojej obecnej bazy.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreBackup(pendingRestoreJson!!, overwrite = true)
                        showRestoreConfirmDialog = false
                        pendingRestoreJson = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ZASTĄP CAŁOŚĆ")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = {
                            viewModel.restoreBackup(pendingRestoreJson!!, overwrite = false)
                            showRestoreConfirmDialog = false
                            pendingRestoreJson = null
                        }
                    ) {
                        Text("SCAL WPISY")
                    }
                    TextButton(
                        onClick = {
                            showRestoreConfirmDialog = false
                            pendingRestoreJson = null
                        }
                    ) {
                        Text("ANULUJ")
                    }
                }
            }
        )
    }
}

private const val MAX_PLAN_BYTES = 2 * 1024 * 1024

/** Czyta strumień, ale nie więcej niż [limit] bajtów (żeby nie ładować ogromnych plików do pamięci). */
private fun readAtMost(input: java.io.InputStream, limit: Int): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (out.size() < limit) {
        val read = input.read(buffer, 0, minOf(buffer.size, limit - out.size()))
        if (read < 0) break
        out.write(buffer, 0, read)
    }
    return out.toByteArray()
}

private fun formatBackupTime(millis: Long): String =
    java.time.Instant.ofEpochMilli(millis)
        .atZone(java.time.ZoneId.systemDefault())
        .format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))

@Composable
private fun ThemeOptionRow(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun shareContent(context: Context, content: String, mimeType: String, filename: String) {
    try {
        val cacheDir = context.cacheDir
        val file = File(cacheDir, filename)
        file.writeText(content, Charsets.UTF_8)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Eksport RailLog - Dziennik Maszynisty")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Udostępnij lub zapisz plik"))
    } catch (e: Exception) {
        Toast.makeText(context, "Nie udało się udostępnić pliku: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
    }
}
