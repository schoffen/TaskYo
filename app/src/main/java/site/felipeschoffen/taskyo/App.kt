package site.felipeschoffen.taskyo

import android.app.Application
import site.felipeschoffen.taskyo.database.AppDatabase

class App : Application() {

    lateinit var db: AppDatabase

    override fun onCreate() {
        super.onCreate()
        db = AppDatabase.getDatabase(this)
    }
}