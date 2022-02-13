package com.jeffrwatts.kmmwebrtc

import kotlinx.serialization.Serializable

@Serializable
data class DogsResult(
    val message: Map<String, List<String>>,
    var status: String
)