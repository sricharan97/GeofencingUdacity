package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.utils.MainCoroutineRule
import com.udacity.project4.locationreminders.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat

import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(maxSdk = Build.VERSION_CODES.P)
class SaveReminderViewModelTest {


    //Done: provide testing to the SaveReminderView and its live data objects

    //Subject under test
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    //use fakeDataSource to be injected into reminderListViewModel
    private lateinit var reminderDataSource: FakeDataSource

    // Set the main coroutines dispatcher for unit testing.
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    //rule needed for testing liveData to make the architecture components
    // background jobs running in a single thread
    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setupViewModel() {
        stopKoin()
        //Given FakeDatasource and corresponding reminderListViewmodel object
        reminderDataSource = FakeDataSource()
        saveReminderViewModel = SaveReminderViewModel(ApplicationProvider.getApplicationContext(),
                reminderDataSource)

    }


    @Test
    fun validateAndSaveReminder_emptyTitle_showsSnackbar() {

        //Given the reminderDataItem have an emptyTitle
        val reminder1 = ReminderDataItem(null, "location with Empty title", "test location1",
                null, null)

        //When the above reminder is asked to be saved
        saveReminderViewModel.validateAndSaveReminder(reminder1)

        //Then snackbar with corresponding title missing error shows up
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))


    }

    @Test
    fun validateAndSaveReminder_emptyLocation_showsSnackbar() {

        //Given the reminderDataItem have no location
        val reminder2 = ReminderDataItem("testTitle", "test location2", null,
                null,
                null)

        //When the reminder is asked to be saved
        saveReminderViewModel.validateAndSaveReminder(reminder2)

        //Then snackbar with corresponding location missing error shows up
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))

    }

    @Test
    fun validateAndSaveReminder_validValues_navigatesBackAndShowsToast() {

        //Given the reminderDataItem has all the valid data
        val reminder3 = ReminderDataItem("validTitle", "ValidTitleDescription", "test location3",
                null, null)

        //When the reminder is asked to be saved
        saveReminderViewModel.validateAndSaveReminder(reminder3)

        //Then toast shows up with right message and navigates back
        assertThat(saveReminderViewModel.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
        assertThat(saveReminderViewModel.navigationCommand.getOrAwaitValue(), notNullValue())


    }


}