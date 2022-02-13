package com.jeffrwatts.kmmwebrtc

import io.ktor.client.engine.*

expect class HttpClientEngineFactory() {
    fun createHttpClientEngineFactory(): HttpClientEngine
}