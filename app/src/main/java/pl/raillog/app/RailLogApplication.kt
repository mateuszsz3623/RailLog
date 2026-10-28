package pl.raillog.app

import android.app.Application
import pl.raillog.app.core.database.RailLogDatabase
import pl.raillog.app.core.network.PlkApiClient
import pl.raillog.app.core.network.UrlConnectionPlkHttp
import pl.raillog.app.core.util.AutoBackupStore
import pl.raillog.app.data.repository.DutyRepositoryImpl
import pl.raillog.app.data.repository.PlanRepositoryImpl
import pl.raillog.app.domain.repository.DutyRepository
import pl.raillog.app.domain.repository.PlanRepository
import java.io.File

class RailLogApplication : Application() {

    lateinit var database: RailLogDatabase
        private set

    lateinit var dutyRepository: DutyRepository
        private set

    lateinit var planRepository: PlanRepository
        private set

    /** Null, gdy w buildzie nie ma klucza API PLK – wtedy podgląd pociągów jest wyłączony. */
    var plkClient: PlkApiClient? = null
        private set

    override fun onCreate() {
        super.onCreate()
        database = RailLogDatabase.getDatabase(this)
        dutyRepository = DutyRepositoryImpl(database.dutyDao(), AutoBackupStore(File(filesDir, "auto_backup")))
        planRepository = PlanRepositoryImpl(database.plannedDutyDao())
        if (BuildConfig.PLK_API_KEY.isNotEmpty()) {
            plkClient = PlkApiClient(UrlConnectionPlkHttp(BuildConfig.PLK_API_KEY), cacheDir)
        }
    }
}
