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
    @ColumnInfo(name = "status")
    var status: String = Status.STATUS_ACTIVE.name

    companion object{
        @Ignore
        val REMINDER = "reminder"
        @Ignore
        val REMINDER_ID = "reminderId"
        @Ignore
        val INTERVAL_DAILY = 1
        @Ignore
        val INTERVAL_WEEKLY = 2
        @Ignore
        val INTERVAL_MONTHLY = 3
        @Ignore
        val INTERVAL_YEARLY = 4
    }

    enum class Status{
        STATUS_ACTIVE, STATUS_INACTIVE, STATUS_PASSED
    }
}