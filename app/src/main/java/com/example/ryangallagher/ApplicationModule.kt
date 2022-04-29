package com.example.ryangallagher

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ServiceComponent
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory


/*
 * The annotations tell the compiler to create classes for you and do these bindings for you.
 * What we've got is this object that Hilt knows about and there is a function that provides our API
 * service for us (provideApiService()). We've denoted that it provides the Api service by the pair being
 * the Provides annotation and the return type of the function being "Api". Hilt uses reflection to determine
 * what this function returns and is then able to provide it as an argument to our MainViewModel constructor
 * because it has the same type. Anytime there is an Inject annotation, it will look for either a class
 * with an inject constructor if its the same type, or a provides function.
 */

@Module
@InstallIn(ActivityComponent::class, ServiceComponent::class)
class ApplicationModule {
    @Provides
    fun provideApiService(): Api {
        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        return retrofit.create(Api::class.java)
    }
}