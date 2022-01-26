package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.UiSelector
import androidx.test.uiautomator.Until
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


private const val SELECT_LOCATION_PACKAGE = "com.udacity.project4.locationreminders.savereminder.selectreminderlocation"
private const val LAUNCH_TIMEOUT = 3000L

@RunWith(AndroidJUnit4::class)
@SdkSuppress(minSdkVersion = 18)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
        AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    //UIdevice instance for UI automator testing
    private lateinit var device: UiDevice

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

        // Initialize UiDevice instance
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
    }


    @After
    fun resetRepository() {
        //clear the repository to avoid the test pollution
        runBlocking {
            repository.deleteAllReminders()
        }
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


//    Done: add End to End testing to the app

    @Test
    fun addReminder() = runBlocking {

        //Given remindersActivity is started with no reminders
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        //when the add reminder fab is clicked to enter reminder details in next screens
        onView(withId(R.id.addReminderFAB)).perform(click())

        //reminder details to be entered
        val title = "Test Title"
        val description = "Test description"

        //enter the details in saveReminderFragment
        onView(withId(R.id.reminderTitle)).perform(typeText(title), closeSoftKeyboard())
        onView(withId(R.id.reminderDescription)).perform(typeText(description), closeSoftKeyboard())

        //select the location by clicking o location text
        onView(withId(R.id.selectLocation)).perform(click())


        //wait until select location fragment appears
        device.wait(Until.hasObject(By.pkg(SELECT_LOCATION_PACKAGE).depth(0)), LAUNCH_TIMEOUT)

        onView(withContentDescription("Google Map")).perform(longClick())


        //Click yes button on dialog once poi is selected
        val yesButton = device.findObject(UiSelector().text("YES").className("android.widget.Button"))
        // Simulate a user-click on the OK button, if found.
        if (yesButton.exists() && yesButton.isEnabled) {
            yesButton.click()
        }

        //once returned to saveReminderFragment click the save button
        onView(withId(R.id.saveReminder)).perform(click())

        //check if the title and description are vissible
        onView(withText(title)).check(matches(isDisplayed()))
        onView(withText(description)).check(matches(isDisplayed()))

        // Make sure the activity is closed before resetting the db:
        activityScenario.close()


    }

    @Test
    fun nav_test() = runBlocking {

        //Given a example reminder
        val reminder1 = ReminderDTO("Groceries", "Buy groceries when here", "surabhi",
                null, null)

        repository.saveReminder(reminder1)

        //When remindersActivity is started
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.selectLocation)).perform(click())

        onView(withContentDescription("Navigate up")).perform(click())
        onView(withId(R.id.reminderTitle)).check(matches(isDisplayed()))

        onView(withContentDescription("Navigate up")).perform(click())
        onView(withId(R.id.reminderssRecyclerView)).check(matches(isDisplayed()))

        activityScenario.close()
    }

}
