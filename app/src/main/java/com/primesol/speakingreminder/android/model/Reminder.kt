package com.primesol.speakingreminder.android.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "reminder")
class Reminder {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    @ColumnInfo(name = "title")
    var title: String = ""
    @ColumnInfo(name = "date")
    var date: String = ""
    @ColumnInfo(name = "time")
    var time: String = ""
    @ColumnInfo(name = "audio")
    var audio: String = ""
    @ColumnInfo(name = "createdAt")
    var createdAt: String = ""

    companion object{
        @Ignore
        val REMINDER_ID = "reminderId"
    }
}