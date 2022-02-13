package com.jeffrwatts.kmmwebrtc

import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*

actual class HttpClientEngineFactory {
    actual fun createHttpClientEngineFactory(): HttpClientEngine {
        return OkHttp.create()
    }
}