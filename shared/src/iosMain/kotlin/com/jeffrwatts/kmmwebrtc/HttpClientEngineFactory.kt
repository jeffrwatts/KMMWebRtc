package com.jeffrwatts.kmmwebrtc

import io.ktor.client.engine.*
import io.ktor.client.engine.ios.*

actual class HttpClientEngineFactory {
    actual fun createHttpClientEngineFactory(): HttpClientEngine {
        return Ios.create()
    }
}