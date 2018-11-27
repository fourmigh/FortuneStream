package org.caojun.fortunestream.room

import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.content.Context

/**
 * Created by CaoJun on 2017/9/25.
 */
@Database(
        entities = [(Fortune::class), (Account::class), (Date::class)],
        version = 1,
        exportSchema = false
)
abstract class FortuneDatabase: RoomDatabase() {

    abstract fun getFortuneDao(): FortuneDao
    abstract fun getAccountDao(): AccountDao
    abstract fun getDateDao(): DateDao

    companion object {
        private var INSTANCE: FortuneDatabase? = null

        @JvmStatic fun getDatabase(context: Context): FortuneDatabase {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context, FortuneDatabase::class.java, "fortune_database").build()
            }
            return INSTANCE!!
        }

        @JvmStatic fun destroyInstance() {
            INSTANCE = null
        }
    }
}