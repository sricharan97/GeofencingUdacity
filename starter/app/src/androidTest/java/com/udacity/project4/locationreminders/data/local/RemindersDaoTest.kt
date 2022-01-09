package com.udacity.project4.locationreminders.data.local

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
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
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    //    TODO: Add testing implementation to the RemindersDao.kt
    //subject under test
    private lateinit var remindersDao: RemindersDao

    //create inMemory database for testing Dao
    private lateinit var testDatabase: RemindersDatabase

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDatabase = Room.inMemoryDatabaseBuilder(context, RemindersDatabase::class.java).build()
        remindersDao = testDatabase.reminderDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        testDatabase.close()
    }

    @Test
    fun getReminders_savingTwoItems_returnsTwoItems() = runBlockingTest {
        //Given a list of two reminders
        val reminder1 = ReminderDTO("test1", "sampleReminder1", "testLocation1",
                null, null)
        val reminderId1 = reminder1.id
        val reminder2 = ReminderDTO("test2", "sampleReminder2", "testLocation2",
                null, null)
        val reminderId2 = reminder2.id

        //When saving the reminders through Dao
        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)

        //Then getReminders from Dao retrieves correct reminders
        assertThat(remindersDao.getReminderById(reminderId1)?.id, `is`(reminder1.id))
        assertThat(remindersDao.getReminderById(reminderId2)?.id, `is`(reminder2.id))
        assertThat(remindersDao.getReminders().size, `is`(2))

    }

    @Test
    fun deleteAllReminders_savingTwoItems_returnsEmptyList() = runBlockingTest {
        //Given a list of two reminders
        val reminder1 = ReminderDTO("test1", "sampleReminder1", "testLocation1",
                null, null)
        val reminderId1 = reminder1.id
        val reminder2 = ReminderDTO("test2", "sampleReminder2", "testLocation2",
                null, null)
        val reminderId2 = reminder2.id

        //When saving the reminders through Dao
        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)

        //Then getReminders from Dao retrieves correct reminders
        assertThat(remindersDao.getReminders().size, `is`(2))

        //When deleteAllreminders is called
        remindersDao.deleteAllReminders()

        //Then returns empty list
        assertThat(remindersDao.getReminders().size, `is`(0))

    }

}