package com.oruyanke.vtbs

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.CoroutineClient

fun Route.vtuberRoutes() {
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
            val db = mongo.forVtuber(vtb)

            val voices = db.voices().find().toList()
                .map { it.toResponse() }
                .groupBy { it.group }
                .toMap()

            val groups = db.groups().find().toList()
                .map { it.toResponseWith(voices[it.name] ?: listOf()) }
                .toList()

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

                val groupInfo = db.groups().byName(group)
                val voices = db.voices()
                    .byGroup(group)
                    .toList()
                    .map { it.toResponse() }

                call.respond(groupInfo.toResponseWith(voices))
            }
        }

        post<AddGroupRequest>("/{vtb}/add-group") {
            errorAware {
                val vtb = param("vtb")
                sessionUser(mongo).mustBeTheAdminOf(vtb)

                mongo.forVtuber(vtb).groups().addGroup(
                    Group(
                        name = it.name,
                        desc = it.desc.toLocalizedTexts()
                    )
                )
                call.respond(HttpStatusCode.OK)
            }
        }

        post<AddVoiceRequest>("/{vtb}/{group}/add-voice") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")
                sessionUser(mongo).mustBeTheAdminOf(vtb)

                mongo.forVtuber(vtb).voices().addVoice(
                    Voice(
                        name = it.name,
                        url = it.url,
                        group = group,
                        desc = it.desc.toLocalizedTexts()
                    )
                )
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}

private data class GroupResponse(
    val name: String,
    val desc: Map<String, String>,
    val voices: List<VoiceResponse>
)

private data class VoiceResponse(
    val name: String,
    val url: String,
    val group: String,
    val desc: Map<String, String>
)

private data class AddVoiceRequest(
    val name: String,
    val url: String,
    val desc: Map<String, String>
)

private data class AddGroupRequest(
    val name: String,
    val desc: Map<String, String>
)

private fun Map<String, String>.toLocalizedTexts() =
    this.map { LocalizedText(it.key, it.value) }
        .toList()

private fun Group.toResponseWith(voices: List<VoiceResponse>) =
    GroupResponse(name, desc.toLocalizedMap(), voices)

private fun Voice.toResponse() =
    VoiceResponse(name, url, group, desc.toLocalizedMap())

