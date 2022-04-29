package com.example.ryangallagher

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.Navigation
import com.example.ryangallagher.databinding.SearchFragmentBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SearchFragment : Fragment(), ActivityCompat.OnRequestPermissionsResultCallback {

    private lateinit var binding: SearchFragmentBinding
    @Inject lateinit var viewModel: SearchViewModel

    private val CHANNEL_ID = "channel_id_example"
    private val notificationId = 1111
    private lateinit var serviceIntent: Intent

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    @Suppress("PrivatePropertyName")
    private val REQUEST_LOCATION_PERMISSION = 100
    private lateinit var locationRequest: LocationRequest
    var locationLon: String? = null
    var locationLat: String? = null
    private var notificationFlag: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SearchFragmentBinding.inflate(layoutInflater)


        createNotificationChannel()
        serviceIntent = Intent(requireActivity().applicationContext, NotificationService::class.java)
        requireActivity().registerReceiver(updateNotification, IntentFilter(NotificationService.TIMER_UPDATED))

        binding.notificationButton.text = getString(R.string.turn_on_notifications)
        binding.notificationButton.setOnClickListener {                                                      //Notification Button OnClickListener
            if(checkNotificationServicePermission() && checkLocationPermission()) {
                getLocation()
                if(!notificationFlag) {
                    notificationFlag = true
                    serviceIntent.putExtra(NotificationService.ELAPSED_TIME, 0)
                    requireActivity().startService(serviceIntent)
                    sendNotification()
                    binding.notificationButton.text = getString(R.string.turn_off_notifications)
                }
                else {
                    notificationFlag = false
                    requireActivity().stopService(serviceIntent)
                    with(NotificationManagerCompat.from(requireContext())) {
                        cancel(notificationId)
                    }
                    binding.notificationButton.text = getString(R.string.turn_on_notifications)
                }
            }
            else {
                requestLocationPermission()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create()

        binding.locationButton.setOnClickListener {
            showLocationPreview()
            getLastLocation()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult
                for (location in locationResult.locations) {
                    locationLon = location.longitude.toString()
                    locationLat = location.latitude.toString()
                    viewModel.updateLatLon(locationLat, locationLon)
                    viewModel.locationBtnClicked().invokeOnCompletion {
                        if(it == null) {
                            navigateToCurrentConditions()
                            //createnotification() instead of navigate^ pass it location
                        }
                    }
                    return
                }
            }
        }
        //getLastLocation()
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = "Search"

        viewModel.enableButton.observe(viewLifecycleOwner) { enable ->
            binding.searchBtn.isEnabled = enable
        }

        viewModel.showErrorDialog.observe(viewLifecycleOwner) { showError ->
            if(showError) {
                ErrorDialogFragment().show(childFragmentManager, ErrorDialogFragment.TAG)
            }
        }

        binding.zipCode.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                p0?.toString()?.let { viewModel.updateZipCode(it) }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        binding.searchBtn.setOnClickListener {
            viewModel.submitButtonClicked()
            if(!(viewModel.showErrorDialog.value!!)) {
                navigateToCurrentConditions()
            }
            else {
                viewModel.setErrorDialogToFalse()
            }
        }
    }

    private fun navigateToCurrentConditions() {
        val currentConditionsArg = SearchFragmentDirections.searchToCurrentConditions(viewModel.getZipCode(), viewModel.currentConditions.value,
            viewModel.getLon().toString(), viewModel.getLat().toString())
        Navigation.findNavController(binding.root).navigate(currentConditionsArg)
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (checkLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkNotificationServicePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.FOREGROUND_SERVICE) == PackageManager.PERMISSION_GRANTED
    }

    private fun showLocationPreview() {
        requestLocationPermission()
    }

    private fun showLocationPermissionRationale() {
        AlertDialog.Builder(context)
            .setMessage(R.string.location_permission_rational)
            .setNeutralButton(R.string.ok) {_, _ ->
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                    REQUEST_LOCATION_PERMISSION
                )
            }
            .create()
            .show()
    }

    private fun requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)) {
            showLocationPermissionRationale()
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == REQUEST_LOCATION_PERMISSION) {
            if(grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
                Log.d("Debug", "Permission has been granted")
            }
            else {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    private fun getLocation() {
        try {
            viewModel.notificationBtnClicked(locationLat!!, locationLon!!)
        } catch (e : NullPointerException) {
            locationLat = "44.7319"
            locationLon = "93.2177"
            viewModel.notificationBtnClicked(locationLat!!, locationLon!!)
        }
    }

    private fun getLastLocation() {
        locationRequest.interval = 0L
        locationRequest.fastestInterval = 0L
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if(ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        //  fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            fusedLocationClient.lastLocation.addOnSuccessListener { task ->
                val location: Location? = task

                if (location != null) {
                    locationLat = location.latitude.toString()
                    locationLon = location.longitude.toString()
                    viewModel.locationBtnClicked()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Example name"
            val descriptionText = "Notification description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendNotification() {
        val intent = Intent(requireActivity(), MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            requireActivity(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(requireActivity(), CHANNEL_ID)
            .setSmallIcon(R.drawable.sun)
            .setContentTitle("content title")
            .setContentText("ContextText description")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(requireActivity())) {
            notify(notificationId, builder.build())
        }
    }

    private val updateNotification: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("StringFormatInvalid")
        override fun onReceive(p0: Context?, p1: Intent?) {
            getLocation()
            var builder = NotificationCompat.Builder(requireContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.notificationTitle, viewModel.currentConditions.value?.name))
                .setContentText(getString(R.string.notificationText, viewModel.currentConditions.value?.main?.temp?.toInt()))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            with(NotificationManagerCompat.from(requireContext())) {
                notify(notificationId, builder.build())
            }
        }
    }
}