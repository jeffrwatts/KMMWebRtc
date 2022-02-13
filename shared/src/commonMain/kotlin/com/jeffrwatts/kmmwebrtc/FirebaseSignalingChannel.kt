package com.jeffrwatts.kmmwebrtc

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.map

class FirebaseSignalingChannel {
    suspend fun getDogs (): List<Dog> {
        return buildList<Dog> {
            Firebase.firestore.collection("dogs").get().documents.forEach {
                add(Dog(it.id))
            }
        }
    }

    fun observeDog(dog: Dog) : Flow<String> {
        return Firebase.firestore.collection("dogs").document(dog.name).snapshots
            .filterNot { it.metadata.isFromCache }
            .filter { it.contains("breed") }
            .map {
                it.get("breed")
            }
    }
}