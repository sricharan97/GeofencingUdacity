package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

//    TODO: Add testing implementation to the RemindersLocalRepository.kt

    //create inMemory database for testing Dao
    private lateinit var testDatabase: RemindersDatabase

    //subject under test
    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        testDatabase = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext<Context>(),
                RemindersDatabase::class.java).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(testDatabase.reminderDao(), Dispatchers.Main)

    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        testDatabase.close()
    }

    @Test
    fun getReminders_savingTwoItems_returnsTwoItems() = runBlocking {
        //Given a list of two reminders
        val reminder1 = ReminderDTO("test1", "sampleReminder1", "testLocation1",
                null, null)
        val reminderId1 = reminder1.id
        val reminder2 = ReminderDTO("test2", "sampleReminder2", "testLocation2",
                null, null)
        val reminderId2 = reminder2.id

        //When saving the reminders through repository
        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.saveReminder(reminder2)

        val result1 = remindersLocalRepository.getReminder(reminderId1)
        val result2 = remindersLocalRepository.getReminder(reminderId2)
        val results = remindersLocalRepository.getReminders()

        //Then getReminders from repository retrieves correct reminders and the operation is success
        assertThat(result1 is Result.Success, `is`(true))
        assertThat(result2 is Result.Success, `is`(true))
        assertThat(results is Result.Success, `is`(true))
        result1 as Result.Success
        result2 as Result.Success
        results as Result.Success
        assertThat(result1.data.title, `is`(reminder1.title))
        assertThat(result2.data.description, `is`(reminder2.description))
        assertThat(results.data.size, `is`(2))

    }

    @Test
    fun deleteAllReminders_savingTwoItems_returnsEmptyList() = runBlocking {
        //Given a list of two reminders
        val reminder1 = ReminderDTO("test1", "sampleReminder1", "testLocation1",
                null, null)
        val reminderId1 = reminder1.id
        val reminder2 = ReminderDTO("test2", "sampleReminder2", "testLocation2",
                null, null)
        val reminderId2 = reminder2.id

        //When saving the reminders through repository
        remindersLocalRepository.saveReminder(reminder1)
        remindersLocalRepository.saveReminder(reminder2)

        val result = remindersLocalRepository.getReminders()

        //Then getReminders from repository is success
        assertThat(result is Result.Success, `is`(true))

        //When deleteAllreminders is called
        remindersLocalRepository.deleteAllReminders()
        val deleteResult = remindersLocalRepository.getReminders()
        deleteResult as Result.Success

        //Then  empty list is returned
        assertThat(deleteResult.data.size, `is`(0))

    }


}