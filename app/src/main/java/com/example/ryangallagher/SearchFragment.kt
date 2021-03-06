package com.example.ryangallagher

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.location.Location
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

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    @Suppress("PrivatePropertyName")
    private val REQUEST_LOCATION_PERMISSION = 100
    private lateinit var locationRequest: LocationRequest
    var locationLon: String? = null
    var locationLat: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SearchFragmentBinding.inflate(layoutInflater)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create()

        binding.locationButton.setOnClickListener {
            showLocationPreview()
            viewModel.locationBtnClicked()
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    locationLon = location.longitude.toString()
                    locationLat = location.latitude.toString()
                    viewModel.updateLatLon(locationLat, locationLon)
                    navigateToCurrentConditions()
                    return
                }
            }
        }
        getLastLocation()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().title = "Search"

        viewModel.enableButton.observe(viewLifecycleOwner) { enable ->
            binding.button.isEnabled = enable
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


        binding.button.setOnClickListener {
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
        if (checkPermission()) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun checkPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
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

    private fun getLastLocation() {
        locationRequest.interval = 0L
        locationRequest.fastestInterval = 0L
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        if(ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())

            fusedLocationClient.lastLocation.addOnSuccessListener(requireActivity()) { task ->
                val location: Location? = task

                if (location != null) {
                    locationLat = location.latitude.toString()
                    locationLon = location.longitude.toString()
                }
            }
        }
    }
}