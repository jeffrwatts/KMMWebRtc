package com.jeffrwatts.kmmwebrtc

import io.ktor.client.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*

class DogApi {
    companion object {
        private const val DOGS_ENDPOINT = "https://dog.ceo/api/breeds/list/all"
    }

    private val httpClient = HttpClient (HttpClientEngineFactory().createHttpClientEngine()) {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }
    }

    suspend fun getDogs(): DogsResult {
        return httpClient.get(DOGS_ENDPOINT)
    }
}