package com.jeffrwatts.kmmwebrtc.android

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jeffrwatts.kmmwebrtc.Dog
import com.jeffrwatts.kmmwebrtc.DogModel
import com.jeffrwatts.kmmwebrtc.FirebaseSignalingChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DogViewModel : ViewModel() {
    private val dogModel = DogModel()
    private val firebaseSignalingChannel = FirebaseSignalingChannel()
    private var dogs = MutableLiveData<List<Dog>>()

    fun getDogsFromApi(): LiveData<List<Dog>> {
        viewModelScope.launch {
            try {
                setDogsValue(dogModel.getDogs())
            } catch (e: Exception) {
                Log.e("DogViewModel", "Exception", e)
            }
        }
        return dogs
    }

    fun getDogsFromFirebase(): LiveData<List<Dog>> {
        viewModelScope.launch {
            try {
                setDogsValue(firebaseSignalingChannel.getDogs())
            } catch (e: Exception) {
                Log.e("DogViewModel", "Exception", e)
            }
        }
        return dogs
    }

    fun observeDog(dog: Dog): Flow<String> {
        return firebaseSignalingChannel.observeDog(dog)
    }

    private fun setDogsValue (dogsList: List<Dog>) {
        if (dogsList.isNullOrEmpty()) {
            dogs.value = emptyList()
        } else {
            dogs.value = dogsList
        }
    }
}