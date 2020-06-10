package com.oruyanke.vtbs

import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineDatabase

data class Group(val name: String, val desc: List<LocalizedText>)

data class Voice(val name: String, val url: String, val group: String, val desc: List<LocalizedText>)

fun Group.toResponseWith(voices: List<VoiceResponse>) =
    GroupResponse(name, desc.toLocalizedMap(), voices)

fun Voice.toResponse() =
    VoiceResponse(name, url, group, desc.toLocalizedMap())

data class LocalizedText(val lang: String, val text: String) {
    companion object {
        @JvmStatic
        fun zh(text: String) = LocalizedText("zh", text)

        @JvmStatic
        fun en(text: String) = LocalizedText("en", text)

        @JvmStatic
        fun ja(text: String) = LocalizedText("ja", text)
    }
}

fun List<LocalizedText>.toLocalizedMap(): Map<String, String> =
    this.map { Pair(it.lang, it.text) }
        .toMap()

fun CoroutineClient.forVtuber(name: String) = this.getDatabase("vtuber-$name")

suspend fun CoroutineClient.vtuberNames() =
    this.listDatabaseNames()
        .filter { it.startsWith("vtuber-") }

fun CoroutineDatabase.groups() = this.getCollection<Group>("groups")

fun CoroutineDatabase.voices() = this.getCollection<Voice>("voices")
