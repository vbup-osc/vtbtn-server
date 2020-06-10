package com.oruyanke.vtbs

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.get
import io.ktor.routing.routing

fun Application.setupRouter() {
    routing {
        get("/greetings") {
            call.respond("hello, world!")
        }
    }
}
