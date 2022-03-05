# Location Reminder

A Todo list app with location reminders that remind the user to do something when he reaches a specific location. The app will require the user to create an account and login to set and access reminders.


## Project Details
    1. Login screen to ask users to login using an email address or a Google account.  Upon successful login, navigate the user to the Reminders screen.   If there is no account, the app should navigate to a Register screen.
    2.  Reminders screen displays the reminders retrieved from local storage. If there are no reminders, display a   "No Data"  indicator.  If there are any errors, display an error message.
    3. Select location screen shows a map with the user's current location and asks the user to select a point of interest to create a reminder.
    4. Save Reminder screen to add a reminder when a user reaches the selected location.  Each reminder includes
        a. title
        b. description
        c. selected location
    5. Reminder data ise saved to local storage.
    6. For each reminder, a geofencing request is added in the background that fires up a notification when the user enters the geofencing area.
    7. Test classes for the ViewModels, Coroutines and LiveData objects.
    8.  FakeDataSource to replace the Data Layer and testing the app in isolation.
    9. Espresso and Mockito to test each screen of the app:
    
    
 
# Screenshots

<img src="/starter/Welcome screen.jpg" width="200" align="center" border="30"> 


<img src="/starter/Login screen.jpg" width="200" align="center" border="30"> 


<img src="/starter/reminderList screen.jpg" width="200" align="center" border="30"> 


<img src="/starter/select locationScreen.jpg" width="200" align="center" border="30"> 


<img src="/starter/Permissions.jpg" width="200" align="center" border="30"> 


<img src="/starter/MapsScreen.jpg" width="200" align="center" border="30"> 




       

## Built With

* [Koin](https://github.com/InsertKoinIO/koin) - A pragmatic lightweight dependency injection framework for Kotlin.
* [FirebaseUI Authentication](https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md) - FirebaseUI provides a drop-in auth solution that handles the UI flows for signing
* [JobIntentService](https://developer.android.com/reference/androidx/core/app/JobIntentService) - Run background service from the background application, Compatible with >= Android O.



## Skills Learned

  1. **Google Maps** API
  2. **Geofencing** API
  3. Requesting and checking for **User permissions**
  4. **Dependency Injection** using Koin
  5. **Firebase Authentication** for managing the user login information
  6. Local and instrumented tests using AndroidX testing library
  7. UI testing with **Espresso** and **UiAutomator**
  8. Unit, Integration and End-End testing approaches and techniques
