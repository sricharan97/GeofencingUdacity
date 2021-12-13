package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
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

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private lateinit var pointOfInterest: PointOfInterest

    //private val REQUEST_LOCATION_PERMISSION = 1
    private val runningQorLater = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q
    private val requestForegroundAndBackgroundPermissionResultCode = 33
    private val requestForegroundOnlyPermissionResultCode = 34
    private val locationPermissionIndex = 0
    private val backgroundLocationPermissionIndex = 1
    private val requestTurnDeviceLocationOn = 29

    private val debugTag = "SelectLocationFragment"

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
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
//        TODO: zoom to the user location after taking his permission
//        TODO: add style to the map
//        TODO: put a marker to location that the user selected


//        TODO: call this function after the user confirms on the selected location

        return binding.root
    }

    /**
     * Based on the outcome from
     * requesting permissions
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        // Check if location permissions are granted and if so enable the
        // location data layer.
        /**  if (requestCode == REQUEST_LOCATION_PERMISSION) {
        if (grantResults.size > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
        enableMyLocation()
        }
        }**/

        if (
                grantResults.isEmpty() ||
                grantResults[locationPermissionIndex] == PackageManager.PERMISSION_DENIED ||
                (requestCode == requestForegroundAndBackgroundPermissionResultCode &&
                        grantResults[backgroundLocationPermissionIndex] ==
                        PackageManager.PERMISSION_DENIED)) {
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
            checkDeviceLocationSettings()

        }
    }

    private fun onLocationSelected() {
        //        Done: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
        MaterialAlertDialogBuilder(requireContext()).setTitle(pointOfInterest.name)
                .setMessage(resources.getString(R.string.poi_selected))
                .setPositiveButton(resources.getString(R.string.Dialogue_positive)) { dialogInterface, i ->
                    _viewModel.latitude.value = pointOfInterest.latLng.latitude
                    _viewModel.longitude.value = pointOfInterest.latLng.longitude
                    _viewModel.reminderSelectedLocationStr.value = pointOfInterest.name
                    _viewModel.navigationCommand.value = NavigationCommand.Back
                }
                .setNegativeButton(resources.getString(R.string.Dialogue_negative)) { dialogInterface, i ->
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

        when (foregroundAndBackgroundLocationPermissionApproved()) {

            true -> {
                // You can use the API that requires the permission.
                map.isMyLocationEnabled = true
            }

            else -> {
                var permissionsArray = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

                val resultCode = when {
                    runningQorLater -> {
                        // this provides the result[BACKGROUND_LOCATION_PERMISSION_INDEX]
                        permissionsArray += Manifest.permission.ACCESS_BACKGROUND_LOCATION
                        requestForegroundAndBackgroundPermissionResultCode
                    }
                    else -> requestForegroundOnlyPermissionResultCode
                }
                // You can directly ask for the permission.
                requestPermissions(
                        permissionsArray,
                        resultCode)
            }
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //("Not yet implemented")
        map = googleMap
        map.moveCamera(CameraUpdateFactory.zoomIn())
        setPoiClick(map)
        setMapStyle(map)
        enableMyLocation()
    }

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

    /*
     *  Determines whether the app has the appropriate permissions across Android 10+ and all other
     *  Android versions.
     */
    @TargetApi(29)
    private fun foregroundAndBackgroundLocationPermissionApproved(): Boolean {
        val foregroundLocationApproved = (
                PackageManager.PERMISSION_GRANTED ==
                        ContextCompat.checkSelfPermission(requireContext(),
                                Manifest.permission.ACCESS_FINE_LOCATION))
        val backgroundPermissionApproved =
                if (runningQorLater) {
                    PackageManager.PERMISSION_GRANTED ==
                            ContextCompat.checkSelfPermission(
                                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            )
                } else {
                    true
                }
        return foregroundLocationApproved && backgroundPermissionApproved
    }

    /*
     *  Uses the Location Client to check the current state of location settings, and gives the user
     *  the opportunity to turn on location services within our app.
     */
    private fun checkDeviceLocationSettings(resolve: Boolean = true) {

        val locationRequest = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_LOW_POWER
        }

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val settingsClient = LocationServices.getSettingsClient(requireContext())
        val locationSettingsResponseTask = settingsClient.checkLocationSettings(builder.build())
        locationSettingsResponseTask.addOnFailureListener { exception ->
            if (exception is ResolvableApiException && resolve) {
                try {
                    exception.startResolutionForResult(requireActivity(),
                            requestTurnDeviceLocationOn)
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
                enableMyLocation()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == requestTurnDeviceLocationOn) {
            checkDeviceLocationSettings(false)
        }
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        // TODO: Change the map type based on the user's selection.
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


