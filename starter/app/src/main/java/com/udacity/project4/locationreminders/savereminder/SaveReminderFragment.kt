package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

@Suppress("SpellCheckingInspection")
class SaveReminderFragment : BaseFragment() {


    companion object {
        internal const val ACTION_GEOFENCE_EVENT = "SaveReminderFragment.saveGeofence.action.ACTION_GEOFENCE_EVENT"
        internal const val GEOFENCE_RADIUS_IN_METERS = 1000f
    }

    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var reminderItem: ReminderDataItem
    private val runningQorLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val requestBackgroundPermissionResultCode = 33

    // A PendingIntent for the Broadcast Receiver that handles geofence transitions.
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        intent.action = ACTION_GEOFENCE_EVENT
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)

        binding.viewModel = _viewModel

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                    NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            val title = _viewModel.reminderTitle.value
            val description = _viewModel.reminderDescription.value
            val location = _viewModel.reminderSelectedLocationStr.value
            val latitude = _viewModel.latitude.value
            val longitude = _viewModel.longitude.value
            reminderItem = ReminderDataItem(title, description, location, latitude, longitude)

//            TODO: use the user entered reminder details to:
//             1) add a geofencing request
//             2) save the reminder to the local db
            if (backgroundLocationPermissionApproved()) {
                startGeofence(reminderItem)

            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                    requestBackgroundPermissionResultCode
                )
            }


        }
    }

    /*
    *  Determines whether the app has the appropriate permissions across Android 10+ and all other
    *  Android versions.
    */
    @TargetApi(29)
    private fun backgroundLocationPermissionApproved(): Boolean {

        return if (runningQorLater) {
            PackageManager.PERMISSION_GRANTED ==
                    ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
        } else {
            true
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == requestBackgroundPermissionResultCode) {
            if (grantResults.isEmpty() ||
                    grantResults[0] == PackageManager.PERMISSION_DENIED) {

                // Permission denied.
                Snackbar.make(
                        binding.rootSave,
                        R.string.permission_denied_explanation, Snackbar.LENGTH_INDEFINITE
                )
                        .setAction(R.string.settings) {
                            // Displays App settings screen.
                            startActivity(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            })
                        }.show()

            } else {
                startGeofence(reminderItem)


            }
        }
    }

    //Start Geofence request
    @SuppressLint("MissingPermission")
    private fun startGeofence(reminderDataItem: ReminderDataItem) {

        reminderDataItem.latitude?.let {
            reminderDataItem.longitude?.let {  // Build the Geofence Object
                val geofence = Geofence.Builder()
                        // Set the request ID of the geofence. This is a string to identify this
                        // geofence
                        .setRequestId(reminderDataItem.id)
                        // Set the circular region of this geofence
                        .setCircularRegion(reminderDataItem.latitude!!, reminderDataItem.longitude!!,
                                GEOFENCE_RADIUS_IN_METERS)
                        // Set the expiration duration of the geofence
                        .setExpirationDuration(Geofence.NEVER_EXPIRE)
                        // Set the transition types of interest.
                        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                        // Create the geofence.
                        .build()

                // Build the geofence request
                val geofencingRequest = GeofencingRequest.Builder()
                        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
                        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
                        // is already inside that geofence.
                        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)


                        // Add the geofences to be monitored by geofencing service.
                        .addGeofence(geofence)
                        .build()

                geofencingClient.removeGeofences(geofencePendingIntent)?.run {
                    addOnCompleteListener {
                        geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                            addOnSuccessListener {
                                //Geofence is added
                                Toast.makeText(context, R.string.geofence_entered, Toast.LENGTH_SHORT).show()
                                _viewModel.validateAndSaveReminder(reminderDataItem)
                            }
                            addOnFailureListener {
                                //failure adding geofence
                                Toast.makeText(context, R.string.error_adding_geofence, Toast.LENGTH_SHORT).show()

                            }
                        }
                    }
                }
            }
        }


    }


    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()

    }
}
