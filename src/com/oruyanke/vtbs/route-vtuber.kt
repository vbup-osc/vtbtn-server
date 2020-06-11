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
import org.litote.kmongo.eq

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

                val groupInfo = db.groups()
                    .find(Group::name eq group)
                    .first()
                    ?: throw IllegalArgumentException("group '$group' not found")

                val voices = db.voices()
                    .find(Voice::group eq group)
                    .toList()
                    .map { it.toResponse() }

                call.respond(groupInfo.toResponseWith(voices))
            }
        }

        post<AddGroupRequest>("/{vtb}/add-group") {
            errorAware {
                val vtb = param("vtb")
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
