package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.BuildConfig
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var pointOfInterest: PointOfInterest
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val requestForegroundOnlyPermissionResultCode = 34
    private val locationPermissionIndex = 0

    //private val backgroundLocationPermissionIndex = 1
    private val requestTurnDeviceLocationOn = 29

    private val debugTag = "SelectLocationFragment"

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
                DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

//        done: add the map setup implementation
        val mapFragment = childFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

//        Done: zoom to the user location after taking his permission
//        Done: add style to the map
//        Done: put a marker to location that the user selected
//        Done: call this function after the user confirms on the selected location

        return binding.root
    }

    /**
     * Set map style, Poi click listener etc once the map is ready to be used
     */
    override fun onMapReady(googleMap: GoogleMap) {
        //("Not yet implemented")
        map = googleMap
        setMapStyle(map)
        setMapLongClick(map)
        setPoiClick(map)
        enableMyLocation()
    }


    //Set custom map style
    private fun setMapStyle(map: GoogleMap) {
        try {
            val success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)

            )
            if (!success) {
                Log.e(debugTag, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(debugTag, "Can't find style. Error: ", e)
        }
    }

    //setup POI click listener and call onLocationSelected() function
    private fun setPoiClick(map: GoogleMap) {
        map.setOnPoiClickListener { poi ->
            pointOfInterest = poi
            val poiMarker = map.addMarker(MarkerOptions()
                    .position(poi.latLng)
                    .title(poi.name))
            poiMarker.showInfoWindow()

            onLocationSelected()

        }
    }

    //setup Long click listener for testing support
    private fun setMapLongClick(map: GoogleMap) {
        map.setOnMapLongClickListener { latLng ->
            val snippet = String.format(
                    Locale.getDefault(),
                    "Lat: %1$.5f, Long: %2$.5f",
                    latLng.latitude,
                    latLng.longitude
            )
            val longClickMarker = map.addMarker(MarkerOptions().position(latLng)
                    .title(getString(R.string.dropped_pin))
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
            )
            longClickMarker.showInfoWindow()
            MaterialAlertDialogBuilder(requireContext()).setTitle("LocationSelectedForTesting")
                    .setMessage(resources.getString(R.string.poi_selected))
                    .setPositiveButton(resources.getString(R.string.Dialogue_positive)) { _, _ ->
                        _viewModel.latitude.value = latLng.latitude
                        _viewModel.longitude.value = latLng.longitude
                        _viewModel.reminderSelectedLocationStr.value = "testLocation"
                        _viewModel.navigationCommand.value = NavigationCommand.Back
                    }
                    .setNegativeButton(resources.getString(R.string.Dialogue_negative)) { dialogInterface, _ ->
                        dialogInterface.cancel()
                    }.show()

        }


    }

    /**
     * Pass in the selectedPOI attributes to the viewModel based on
     * response from the user off the presented Dialogue
     */
    private fun onLocationSelected() {
        //        Done: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        MaterialAlertDialogBuilder(requireContext()).setTitle(pointOfInterest.name)
                .setMessage(resources.getString(R.string.poi_selected))
                .setPositiveButton(resources.getString(R.string.Dialogue_positive)) { _, _ ->
                    _viewModel.latitude.value = pointOfInterest.latLng.latitude
                    _viewModel.longitude.value = pointOfInterest.latLng.longitude
                    _viewModel.reminderSelectedLocationStr.value = pointOfInterest.name
                    _viewModel.navigationCommand.value = NavigationCommand.Back
                }
                .setNegativeButton(resources.getString(R.string.Dialogue_negative)) { dialogInterface, _ ->
                    dialogInterface.cancel()
                }.show()

    }

    /**
     * Enables location depending up on
     * the permissions granted or requests for
     * the permission if not granted
     */
    @SuppressLint("MissingPermission", "InlinedApi")
    private fun enableMyLocation() {

        when (foregroundLocationPermissionApproved()) {

            true -> {
                // You can use the API that requires the permission.
                map.isMyLocationEnabled = true
                checkDeviceLocationSettings()
            }

            else -> {
                // You can directly ask for the permission.
                requestPermissions(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        requestForegroundOnlyPermissionResultCode)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun locationEnabled() {

        // map.isMyLocationEnabled = true
        val zoomLevel = 15f
        //Get the user's last known location and zoom the camera.
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.apply {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude, longitude), zoomLevel))
            }
        }
    }

    //Checks if the app already has foreground location access
    private fun foregroundLocationPermissionApproved(): Boolean {

        return (
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION)
                )
    }




    /**
     * Based on the outcome from
     * requesting permissions
     */
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == requestForegroundOnlyPermissionResultCode) {
            if (grantResults.isEmpty() ||
                    grantResults[locationPermissionIndex] == PackageManager.PERMISSION_DENIED) {
                Log.d(debugTag, "Permissions were denied")
                // Permission denied.
                Snackbar.make(
                        binding.rootMapLayout,
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
                map.isMyLocationEnabled = true
                checkDeviceLocationSettings()

            }
        }
    }


    /*
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    startIntentSenderForResult(exception.resolution.intentSender,
                            requestTurnDeviceLocationOn, null, 0, 0, 0, null)
                    //exception.startResolutionForResult(requireActivity(),
                    //       requestTurnDeviceLocationOn)
                } catch (sendEx: IntentSender.SendIntentException) {
                    Log.d(debugTag, "Error getting location settings resolution: " + sendEx.message)
                }
            } else {
                Snackbar.make(
                        binding.rootMapLayout,
                        R.string.location_required_error, Snackbar.LENGTH_INDEFINITE
                ).setAction(android.R.string.ok) {
                    checkDeviceLocationSettings()
                }.show()
            }
        }

        locationSettingsResponseTask.addOnCompleteListener {
            if (it.isSuccessful) {
                locationEnabled()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(debugTag, "Inside onActivityResult")
        if (requestCode == requestTurnDeviceLocationOn) {
            checkDeviceLocationSettings(false)
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // Done: Change the map type based on the user's selection.
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

}


