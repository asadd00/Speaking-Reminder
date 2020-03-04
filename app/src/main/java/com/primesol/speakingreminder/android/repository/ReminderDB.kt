package com.primesol.speakingreminder.android.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.primesol.speakingreminder.android.model.Reminder


@Database(entities = [Reminder::class], exportSchema = false, version = 2)
abstract class ReminderDB: RoomDatabase() {
    companion object{
        private val DB_NAME = "ReminderDB"
        private var instance: ReminderDB? = null
        fun getInstance(context: Context): ReminderDB{
            if(instance == null){
                instance = Room
                    .databaseBuilder(context.applicationContext, ReminderDB::class.java, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .build()
            }

            return instance!!
        }
    }
    abstract fun reminderDao(): ReminderDao?
}