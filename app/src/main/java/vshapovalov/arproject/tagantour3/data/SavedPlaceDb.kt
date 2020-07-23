package vshapovalov.arproject.tagantour3.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(SavedPlace::class), version = 1)
abstract class SavedPlaceDb : RoomDatabase() {
    companion object {
        @Volatile private var instance: SavedPlaceDb? = null
        private val LOCK = Any()

        operator fun invoke(context: Context)= instance?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it}
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context,
            SavedPlaceDb::class.java, "savedPlaces.db")
            .allowMainThreadQueries()
            .build()

        /*private var INSTANCE: SavedPlaceDb? = null
        fun getDataBase(context: Context): SavedPlaceDb {
            if (INSTANCE == null) {
                INSTANCE = Room.databaseBuilder(context.applicationContext, SavedPlaceDb::class.java, "savedPlaces-db")
                    .allowMainThreadQueries().build()
            }
            return INSTANCE as SavedPlaceDb
        }*/
    }

    abstract fun daoSavedPlace(): DaoSavedPlace
}