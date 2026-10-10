package pl.raillog.app

import android.app.Application
import pl.raillog.app.core.database.RailLogDatabase
import pl.raillog.app.data.repository.DutyRepositoryImpl
import pl.raillog.app.domain.repository.DutyRepository

class RailLogApplication : Application() {

    lateinit var database: RailLogDatabase
        private set

    lateinit var dutyRepository: DutyRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = RailLogDatabase.getDatabase(this)
        dutyRepository = DutyRepositoryImpl(database.dutyDao())
    }

    companion object {
        lateinit var instance: RailLogApplication
            private set
    }
}
