package com.jeffrwatts.kmmwebrtc.android

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeffrwatts.kmmwebrtc.Dog
import com.jeffrwatts.kmmwebrtc.DogModel
import kotlinx.coroutines.launch

class DogViewModel : ViewModel() {
    private val dogModel = DogModel()
    private var dogs = MutableLiveData<List<Dog>>()

    fun getDogs(): LiveData<List<Dog>> {
        loadDogs()
        return dogs
    }

    private fun loadDogs() {
        viewModelScope.launch {
            try {
                val dogsList = dogModel.getDogs()
                if (dogsList.isNullOrEmpty()) {
                    dogs.value = emptyList()
                } else {
                    dogs.value = dogsList
                }

            } catch (e: Exception) {
                Log.e("DogViewModel", "Exception", e)
            }
        }
    }
}