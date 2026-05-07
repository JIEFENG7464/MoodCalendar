package com.moodcalendar.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String,
    val emoji: String,
    val note: String,
    val createdAt: Long = System.currentTimeMillis()
)
