package com.oruyanke.vtbs

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import org.junit.Test
import org.litote.kmongo.json
import java.io.File
import kotlin.test.assertEquals

class ExternalJson {
    val groups: List<ExternalGroup> = mutableListOf()
}

class ExternalGroup {
    val group_name: String = ""
    val group_description: Map<String, String> = mutableMapOf()
    val voice_list: List<ExternalVoice> = mutableListOf()
}

class ExternalVoice {
    val name = ""
    val path = ""
    val description: Map<String, String> = mutableMapOf()
}

@ExperimentalStdlibApi
class PostTest {
    @Test
    fun testPost() {
        val file = "/Users/yangjinghua/mea-voices-test.json"
        val mapper = ObjectMapper()
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        val json = mapper.readValue(File(file), ExternalJson::class.java)

        json.groups
            .map { Group(name = it.group_name, desc = it.group_description.toLocalizedTexts()) }
            .forEach { addGroup(it) }
        json.groups
            .flatMap { group ->
                group.voice_list.map {
                    Voice(it.name, it.path, group.group_name, it.description.toLocalizedTexts())
                }
            }
            .forEach { addVoice(it) }
    }

    private fun addVoice(voice: Voice) {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/vtubers/mea/${voice.group}/add-voice") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    mapOf(
                        "name" to voice.name,
                        "url" to voice.url,
                        "desc" to voice.desc.toLocalizedMap()
                    ).json
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    private fun addGroup(group: Group) {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Post, "/vtubers/mea/add-group") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody(
                    mapOf(
                        "name" to group.name,
                        "desc" to group.desc.toLocalizedMap()
                    ).json
                )
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
