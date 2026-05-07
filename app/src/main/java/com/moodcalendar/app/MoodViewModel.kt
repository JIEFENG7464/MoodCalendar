package com.moodcalendar.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moodcalendar.app.data.MoodDatabase
import com.moodcalendar.app.data.MoodEntry
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MoodViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = MoodDatabase.getInstance(application).moodDao()

    val allEntries: StateFlow<Map<String, List<MoodEntry>>> = dao.getAllEntries()
        .map { list -> list.groupBy { it.date } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyMap()
        )

    fun saveEntry(entry: MoodEntry) {
        viewModelScope.launch {
            dao.upsert(entry)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            dao.deleteById(id)
        }
    }
}
