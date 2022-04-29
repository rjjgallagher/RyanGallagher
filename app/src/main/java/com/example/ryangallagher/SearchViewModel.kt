package com.example.ryangallagher

import android.app.AlertDialog
import android.media.metrics.Event
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import retrofit2.HttpException
import javax.inject.Inject

class SearchViewModel @Inject constructor(private val api: Api) : ViewModel() {
    private var _enableButton = MutableLiveData(false)
    private val _showErrorDialog = MutableLiveData(false)
    private var zipCode: String? = null
    private val _currentConditions = MutableLiveData<CurrentConditions>()
    private var lat: String? = null
    private var lon: String? = null


    val enableButton: LiveData<Boolean>
        get() = _enableButton

    val currentConditions: LiveData<CurrentConditions>
        get() = _currentConditions

    val showErrorDialog: LiveData<Boolean>
        get() = _showErrorDialog

    fun updateZipCode(zipCode: String) {
        if (zipCode != this.zipCode) {
            this.zipCode = zipCode
            _enableButton.value = isValidZipCode(zipCode)
        }
    }

    fun updateLatLon(lat: String?, lon: String?) {
        if (!this.lat.equals(lat) && !this.lon.equals(lon))
        this.lat = lat
        this.lon = lon
    }

    private fun isValidZipCode(zipCode: String): Boolean {
        return zipCode.length == 5 && zipCode.all {it.isDigit()}
    }

    fun getZipCode(): String? {
        return zipCode
    }

    fun submitButtonClicked() = runBlocking {
        launch {
            try {
                _currentConditions.value = api.getCurrentConditions(zipCode.toString())
            } catch (e: HttpException) {
                _showErrorDialog.value = true
            }
        }
    }

    fun locationBtnClicked() = runBlocking {
        launch {
            try {
                _currentConditions.value = api.getCurrentConditionsLL(lat.toString(), lon.toString())
            } catch (e: HttpException) {
                _showErrorDialog.value = true
            }
        }
    }

    fun notificationBtnClicked(latitude: String, longitude: String) = runBlocking {
        launch {
            try {
                _currentConditions.value = api.getCurrentConditionsLL(latitude, longitude)
            } catch (e: HttpException) {
                _showErrorDialog.value = true
            }
        }
    }

    fun setErrorDialogToFalse() {
        _showErrorDialog.value = false
    }

    fun getLat(): String? {
        return lat
    }

    fun getLon(): String? {
        return lon
    }
}