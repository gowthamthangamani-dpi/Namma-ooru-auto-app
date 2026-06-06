package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.CommuteDao
import com.example.data.dao.RideDao
import com.example.data.model.BookedRide
import com.example.data.model.FrequentCommute

@Database(entities = [FrequentCommute::class, BookedRide::class], version = 3, exportSchema = false)
abstract class AutoDatabase : RoomDatabase() {
    abstract fun commuteDao(): CommuteDao
    abstract fun rideDao(): RideDao

    companion object {
        @Volatile
        private var INSTANCE: AutoDatabase? = null

        fun getDatabase(context: Context): AutoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AutoDatabase::class.java,
                    "namma_ooru_auto_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
