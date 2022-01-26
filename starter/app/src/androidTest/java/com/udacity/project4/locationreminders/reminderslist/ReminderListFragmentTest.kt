package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeAndroidTestRepository
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

//    Done: test the navigation of the fragments.
//    Done: test the displayed data on the UI.
//    Done: add testing for the error messages.

    private lateinit var testRepository: ReminderDataSource

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun initKoin() {

        //Stop the original application Koin
        stopKoin()

        val testModule = module {
            viewModel {
                RemindersListViewModel(
                        getApplicationContext(),
                        get() as ReminderDataSource
                )
            }

            single {
                FakeAndroidTestRepository() as ReminderDataSource
            }
        }

        //start the koin with testModule
        startKoin {
            androidContext(getApplicationContext())
            modules(listOf(testModule))
        }

        testRepository = get()
    }

    /**
     * Idling resources tell Espresso that the app is idle or busy. This is needed when operations
     * are not scheduled in the main Looper (for example when executed on a different thread).
     */
    @Before
    fun registerIdlingResources() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }


    /**
     * Unregister your Idling Resource so it can be garbage collected and does not leak any memory.
     */
    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }


    @Test
    fun addingReminder_showsUpInScreen() {

        //Given a test Reminder is added in the repository
        runBlocking {
            testRepository.saveReminder(ReminderDTO("testTitle1", "testDescription",
                    "testLocation", null, null))
        }

        //When fragment is launched
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        //Then textViews on the screen shows the correct data
        onView(withId(R.id.title)).check(matches(withText("testTitle1")))
        onView(withId(R.id.description)).check(matches(withText("testDescription")))

    }

    @Test
    fun clickingFab_navigatesToSaveFragment() {

        //Given a fragment scenario and a mock nav controller
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        val mockController = mock(NavController::class.java)

        //set mockController on fragment
        fragmentScenario.onFragment { Navigation.setViewNavController(it.view!!, mockController) }

        //When Fab is clicked
        onView(withId(R.id.addReminderFAB)).perform(click())

        //Then navigates to SaveReminderFragment
        verify(mockController).navigate(ReminderListFragmentDirections.toSaveReminder())

    }

    @Test
    fun zeroReminders_showsNoDataIndicator() {
        //Given an empty repository

        //When reminderListsFragment is launched
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        //Then noDataIndicator is displayed
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))

    }
}