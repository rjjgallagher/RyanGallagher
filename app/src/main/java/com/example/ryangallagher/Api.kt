package com.example.ryangallagher

import retrofit2.http.GET
import retrofit2.http.Query

interface Api {

    /* When we construct out URL, it is expecting Query parameters. Query parameters are key-value pairs that you can send
     *  to a http server. And it's everything after the question mark in the API call. The question mark is a special character that says
     *  "api.openweathermap.org/data/2.5/weather" is the resource location and "zip={zip code},{country code}&appid={API key}" are parameters.
     *  The parameters are delimited by the "&", pronounced "ampersand."
     *
     *  For example:
     *      api.openweathermap.org/data/2.5/weather?zip={zip code},{country code}&appid={API key}
     *                              question mark  ^                   ampersand ^
     */


    /* The arguments provided to this function will be appended to the URL at runtime as Query parameters.
     *  The end point, or the path location, is "weather".
     */
    @GET("weather")
    suspend fun getCurrentConditions(
        //with Kotlin, you can have default parameters, which we're utilizing here in this function.
        @Query("zip") zip: String,
        @Query("units") units: String = "imperial",
        @Query("appid") appId: String = "e23be7dabdbe9983722033759ffb9fc3",

    ): CurrentConditions

    /* The arguments provided to this function will be appended to the URL at runtime as Query parameters.
     *  The end point, or the path location, is "daily/forecast".
     */
    @GET("forecast/daily")
    suspend fun getForecast(
        //with Kotlin, you can have default parameters, which we're utilizing here in this function.
        @Query("zip") zip: String,
        @Query("units") units: String = "imperial",
        @Query("appid") appId: String = "e23be7dabdbe9983722033759ffb9fc3",
        @Query("cnt") count: String = "16",
    ): Forecast

    @GET("weather")
    suspend fun getCurrentConditionsLL(
        @Query("lat") lat: String?,
        @Query("lon") lon: String?,
        @Query("units") units: String = "imperial",
        @Query("appid") appId: String = "e23be7dabdbe9983722033759ffb9fc3",
    ): CurrentConditions

    @GET ("forecast/daily")
    suspend fun getForecastLatLon(
        @Query("lat") lat: String?,
        @Query("lon") lon: String?,
        @Query("units") units: String = "imperial",
        @Query("appid") appId: String = "e23be7dabdbe9983722033759ffb9fc3",
        @Query("cnt") count: String = "16",
    ): Forecast
}