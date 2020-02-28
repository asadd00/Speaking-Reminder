package com.primesol.speakingreminder.android.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "reminder")
class Reminder: Serializable {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
    @ColumnInfo(name = "title")
    var title: String = ""
    @ColumnInfo(name = "dateTime")
    var dateTime: String = ""
    @ColumnInfo(name = "audio")
    var audio: String = ""
    @ColumnInfo(name = "createdAt")
    var createdAt: String = ""

    companion object{
        @Ignore
        val REMINDER = "reminder"
        @Ignore
        val REMINDER_ID = "reminderId"
    }
}