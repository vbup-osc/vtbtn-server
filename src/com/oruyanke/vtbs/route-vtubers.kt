package com.oruyanke.vtbs

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.path
import io.ktor.response.respond
import io.ktor.routing.*
import org.koin.ktor.ext.inject
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.litote.kmongo.updateOne

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

        patch<PatchVoiceRequest>("/{vtb}/{group}/{name}") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")
                val name = param("name")

                mongo.forVtuber(vtb).voices().bulkWrite(
                    it.toBulkWriteOperations(group, name)
                )
                call.respond(HttpStatusCode.OK)
            }
        }

        patch<PatchGroupRequest>("/{vtb}/{group}") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")

                mongo.forVtuber(vtb).groups().bulkWrite(
                    it.toBulkWriteOperations(group)
                )

                if (it.name != null) {
                    mongo.forVtuber(vtb).voices().updateMany(
                        Voice::group eq group,
                        setValue(Voice::group, it.name)
                    )
                }
                call.respond(HttpStatusCode.OK)
            }
        }

        delete("/{vtb}/{group}/{name}") {
            errorAware {
                val vtb = param("vtb")
                val name = param("name")

                mongo.forVtuber(vtb).voices().deleteOneById(name)
                call.respond(HttpStatusCode.OK)
            }
        }

        delete("/{vtb}/{group}") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")

                mongo.forVtuber(vtb).groups().deleteOneById(group)
                mongo.forVtuber(vtb).voices().deleteMany(Voice::group eq group)
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

private data class PatchVoiceRequest(
    val name: String?,
    val group: String?,
    val url: String?,
    val desc: Map<String, String>?
)

private data class PatchGroupRequest(
    val name: String?,
    val desc: Map<String, String>?
)

private fun PatchVoiceRequest.toBulkWriteOperations(group: String, name: String) =
    mapOf(
        Voice::name to this.name,
        Voice::group to this.group,
        Voice::url to this.url,
        Voice::desc to this.desc
    )
        .filterValues { it != null }
        .map {
            updateOne<Voice>(
                and(Voice::name eq name, Voice::group eq group),
                setValue(it.key, it.value)
            )
        }

private fun PatchGroupRequest.toBulkWriteOperations(group: String) =
    mapOf(
        Group::name to this.name,
        Group::desc to this.desc
    )
        .filterValues { it != null }
        .map {
            updateOne<Group>(
                Group::name eq group,
                setValue(it.key, it.value)
            )
        }

private fun Map<String, String>.toLocalizedTexts() =
    this.map { LocalizedText(it.key, it.value) }
        .toList()

private fun Group.toResponseWith(voices: List<VoiceResponse>) =
    GroupResponse(name, desc.toLocalizedMap(), voices)

private fun Voice.toResponse() =
    VoiceResponse(name, url, group, desc.toLocalizedMap())

