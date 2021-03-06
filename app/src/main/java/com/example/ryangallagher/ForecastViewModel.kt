package com.example.ryangallagher

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class ForecastViewModel @Inject constructor(private val service: Api) : ViewModel() {

    private val _forecast = MutableLiveData<Forecast>()
    val forecast: LiveData<Forecast>
        get() = _forecast

    fun loadData(zipCodeArg: String?) = runBlocking {
        launch { _forecast.value = service.getForecast(zipCodeArg!!) }
    }

    fun loadData(latArg: String?, lonArg: String?) = runBlocking {
        launch {
            _forecast.value = service.getForecastLatLon(latArg!!, lonArg!!)
        }
    }

}

