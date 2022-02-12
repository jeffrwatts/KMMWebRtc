package com.jeffrwatts.kmmwebrtc

class DogModel {
    private val dogApi = DogApi()

    @Throws(Exception::class) suspend fun getDogs(): List<Dog> {
        val dogsResult = dogApi.getDogs()
        val dogsList = dogsResult.message.keys.sorted().toList()
        if (dogsList.isNullOrEmpty()) {
            return emptyList()
        }
        return dogsList.map { Dog(it) }
    }
}