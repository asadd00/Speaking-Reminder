package com.primesol.speakingreminder.android.repository

import androidx.room.*
import com.primesol.speakingreminder.android.model.Reminder

@Dao
interface ReminderDao {
    @Query("Select * from reminder")
    fun getReminderList(): List<Reminder>

    @Insert
    fun insertReminder(reminder: Reminder): Long

    @Update
    fun updateReminder(reminder: Reminder)

    @Delete
    fun deleteReminder(reminder: Reminder)

    @Query("Select * from reminder Where id=:id")
    fun getReminder(id: String): Reminder

    @Query("Select * from reminder Where dateTime=:dateTime")
    fun getReminderWithDateTime(dateTime: String): Reminder
}