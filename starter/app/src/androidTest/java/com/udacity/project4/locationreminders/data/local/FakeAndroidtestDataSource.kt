package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeAndroidTestRepository : ReminderDataSource {

    var remindersServiceData: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return when (shouldReturnError) {
            true -> Result.Error("Error was set for testing")
            else -> Result.Success(remindersServiceData.values.toList())
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersServiceData[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val reminder = remindersServiceData[id]
        return if (reminder == null) {
            Result.Error("Reminder not found.")
        } else {
            Result.Success(reminder)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersServiceData.clear()
    }
}