package com.oruyanke.vtbs

import io.ktor.application.Application
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logging

fun Boolean.runIf(block: () -> Unit) {
    if (this) {
        block()
    }
}

fun Application.newHttpClient() = HttpClient {
    install(Auth) {
    }
    install(JsonFeature) {
        serializer = JacksonSerializer()
    }
    install(Logging) {
        level = LogLevel.HEADERS
    }
}
