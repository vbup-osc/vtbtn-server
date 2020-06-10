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
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.util.pipeline.PipelineContext
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.CoroutineClient

fun Route.userRoutes() {
    val mongo: CoroutineClient by inject()

    route("/vtubers") {
        get("/") {
            errorAware {
                val navigator = mongo.vtuberNames()
                    .map { Pair(it, "${call.request.path()}/$it") }
                    .toMap()
                call.respond(navigator)
            }
        }

        get("/{vtb}") {
            val vtb = param("vtb")
            val groups = mongo.forVtuber(vtb).groups().find().toList()
            call.respond(
                mapOf(
                    "name" to vtb,
                    "groups" to groups
                )
            )
        }

        get("/{vtb}/{group}") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")
                val db = mongo.forVtuber(vtb)
                val groupInfo = db.groups().find("name = $group").first()
                    ?: throw IllegalArgumentException("group '$group' not found")
                val voices = db.voices().find("group = $group").toList()

                call.respond(
                    mapOf(
                        "group" to groupInfo,
                        "voices" to voices
                    )
                )
            }
        }

        post<AddGroupRequest>("/{vtb}/add-group") { request ->
            errorAware {
                val vtb = param("vtb")
                val group = GroupInfo(name = request.name, desc = request.desc)
                mongo.forVtuber(vtb).groups().insertOne(group)
                call.respond(HttpStatusCode.OK)
            }
        }

        post<AddVoiceRequest>("/{vtb}/{group}/add-voice") { request ->
            errorAware {
                val vtb = param("vtb")
                val group = param("group")
                val voice = VoiceInfo(
                    name = request.name,
                    url = request.url,
                    group = group,
                    desc = request.desc
                )
                mongo.forVtuber(vtb).voices().insertOne(voice)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private suspend fun <R> PipelineContext<*, ApplicationCall>.errorAware(block: suspend () -> R): R? {
    return try {
        block()
    } catch (e: Exception) {
        call.respondText(
            """{"code": 1, "msg":"$e"}""",
            ContentType.parse("application/json"),
            HttpStatusCode.InternalServerError
        )
        null
    }
}

private suspend fun ApplicationCall.respondError(e: Exception) {
    respondText(
        """{"code": 1, "msg":"$e"}""",
        ContentType.parse("application/json"),
        HttpStatusCode.InternalServerError
    )
}

private fun PipelineContext<*, ApplicationCall>.param(name: String) =
    call.parameters[name] ?: throw IllegalArgumentException("Missing '$name'")

