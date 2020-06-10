package com.oruyanke.vtbs

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.util.pipeline.PipelineContext

fun Application.setupRouter() {
    routing {
        get(path("/greetings")) {
            call.respond("hello, world!")
        }

        get(path("/")) {
            // TODO: return available vtbs
        }

        get(path("/{vtb}")) {
            // TODO: return all voices of category {vtb}
        }

        get(path("/{vtb}/{group}")) {
            // TODO: return all voices belong to {group}
        }
    }
}

private suspend fun <R> PipelineContext<*, ApplicationCall>.errorAware(block: suspend () -> R): R? {
    return try {
        block()
    } catch (e: Exception) {
        call.respondText(
            """{"error":"$e"}""",
            ContentType.parse("application/json"),
            HttpStatusCode.InternalServerError
        )
        null
    }
}

private fun PipelineContext<*, ApplicationCall>.param(name: String) =
    call.parameters[name] ?: throw IllegalArgumentException("Missing '$name'")

private fun path(path: String): String = "${ServerConfig.endpoint}$path"
