package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(maxSdk = Build.VERSION_CODES.P)
class RemindersListViewModelTest {

    //Subject under test
    private lateinit var reminderListViewModel: RemindersListViewModel

    //use fakeDataSource to be injected into reminderListViewModel
    private lateinit var reminderDataSource: FakeDataSource

    //TODO: provide testing to the RemindersListViewModel and its live data objects
    //rule needed for testing liveData to make the architecture components
    // background jobs running in a single thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        //Given FakeDatasource and corresponding reminderListViewmodel object
        reminderDataSource = FakeDataSource()
        reminderListViewModel = RemindersListViewModel(ApplicationProvider.getApplicationContext(),
                reminderDataSource)
    }

    @Test
    fun loadReminders_emptyReminders_showsNoData() {
        //when the FakeDataSource is empty
        reminderListViewModel.loadReminders()

        //Then showNoDataValue liveData value returns true
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(true))

    }

    @Test
    fun loadReminders_nonEmptyReminders_returnsAndShowsData() = runBlockingTest {

        //when reminderList is non empty
        val reminder1 = ReminderDTO("Groceries", "Buy groceries when here", "surabhi",
                null, null)
        val reminder2 = ReminderDTO("Vegetables", "Buy vegetables in the market", "market",
                null, null)
        reminderDataSource.saveReminder(reminder1)
        reminderDataSource.saveReminder(reminder2)
        reminderListViewModel.loadReminders()

        //Then remindersList live Data value is non null list
        assertThat(reminderListViewModel.remindersList.getOrAwaitValue(), notNullValue())

        //Then showNoDataValue liveData returns false
        assertThat(reminderListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }


}