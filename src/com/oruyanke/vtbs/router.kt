package com.oruyanke.vtbs

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.CoroutineClient

data class Greeting(
    val text: Map<String, String> =
        listOf(
            LocalizedText.zh("你好"),
            LocalizedText.en("hello"),
            LocalizedText.jp("こんにちは")
        ).toMap()
)

fun Route.userRoutes() {
    val mongo: CoroutineClient by inject()
    val db = mongo.vtubersDB()

    route("/vtubers") {
        get("/") {
            val navigator = db.listCollectionNames()
                .map { Pair(it, "${call.request.path()}/$it") }
                .toMap()
            call.respond(navigator)
        }

        get("/{vtb}") {
            val vtb = param("vtb")
            val groups = db.getCollection<VoiceGroup>(vtb).find().toList()
            call.respond(Vtuber(name = vtb, voiceGroups = groups))
        }

        get("/{vtb}/{group}") {
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

