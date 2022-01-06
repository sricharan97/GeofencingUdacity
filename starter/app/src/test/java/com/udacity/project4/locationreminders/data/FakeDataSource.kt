package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource

class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) : ReminderDataSource {

//    TODO: Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        //Done("Return the reminders")
        reminders?.let { return Result.Success(ArrayList(it)) }
        return Result.Error("reminders not found")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        //Done("save the reminder")
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        //Done("return the reminder with the id")
        reminders?.let {
            val reminderItem = it.find { item -> item.id == id }
            if (reminders!!.contains(reminderItem)) {
                return Result.Success(reminderItem!!)
            }
            return Result.Error("Could not find the given reminder Item")
        }
        return Result.Error("Present reminderList is empty")
    }

    override suspend fun deleteAllReminders() {
        //Done("delete all the reminders")
        reminders?.clear()
    }


}