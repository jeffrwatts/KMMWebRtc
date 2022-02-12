package com.jeffrwatts.kmmwebrtc

import io.ktor.client.engine.*
import io.ktor.client.engine.ios.*

actual class HttpClientEngineFactory {
    actual fun createHttpClientEngine(): HttpClientEngine {
        return Ios.create()
    }
}