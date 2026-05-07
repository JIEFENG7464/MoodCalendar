package com.moodcalendar.app.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val (enabled, hour, minute) = ReminderHelper.loadState(context)
            if (enabled) {
                ReminderHelper.schedule(context, hour, minute)
            }
        }
    }
}
