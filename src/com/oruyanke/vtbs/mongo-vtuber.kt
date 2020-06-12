package com.oruyanke.vtbs

import com.mongodb.MongoWriteException
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.upsert

data class Group(
    @BsonId val name: String,
    val desc: List<LocalizedText>
)

data class Voice(
    @BsonId val name: String,
    val url: String,
    val group: String,
    val desc: List<LocalizedText>
)

data class Statistic(
    val date: String,
    val name: String,
    val time: Int,
    val group: String
)

fun Group.toResponseWith(voices: List<VoiceResponse>) =
    GroupResponse(name, desc.toLocalizedMap(), voices)

fun Voice.toResponse() =
    VoiceResponse(name, url, group, desc.toLocalizedMap())

fun Statistic.toResponse() =
    StatisticResponse(date, name, time, group)

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

fun CoroutineClient.forVtuber(name: String) = this.getDatabase("vtuber_$name")

suspend fun CoroutineClient.vtuberNames() =
    this.listDatabaseNames()
        .filter { it.startsWith("vtuber_") }
        .map { it.removePrefix("vtuber_") }

fun CoroutineDatabase.groups() = this.getCollection<Group>("groups")

fun CoroutineDatabase.voices() = this.getCollection<Voice>("voices")

fun CoroutineDatabase.statistics() = this.getCollection<Statistic>("statistics")

private suspend inline fun <reified T : Any> CoroutineCollection<T>.insertOrBadRequest(
    data: T,
    message: () -> String
) = try {
    this.insertOne(data)
} catch (e: MongoWriteException) {
    when (e.error.code) {
        MongoErrorCodes.KEY_DUPLICATE ->
            throw BadRequestException(message())
        else -> throw e
    }
}

suspend fun CoroutineCollection<Group>.addGroup(group: Group) =
    insertOrBadRequest(group) { "Group '${group.name}' already exists" }

suspend fun CoroutineCollection<Group>.byName(name: String) =
    findOneById(name)
        ?: throw BadRequestException("Group '$name' not found")

suspend fun CoroutineCollection<Statistic>.plusOne(statistic: PlusOneRequest) =
    updateOne(
    org.litote.kmongo.and(
        Statistic::date eq statistic.date,
        Statistic::group eq statistic.group,
        Statistic::name eq statistic.name
    ),
    inc(Statistic::time, 1),
    upsert()
)


suspend fun CoroutineCollection<Voice>.addVoice(voice: Voice) =
    insertOrBadRequest(voice) { "Voice '${voice.name}' already exists in group '${voice.group}'" }

fun CoroutineCollection<Voice>.byGroup(group: String) =
    find(Voice::group eq group)

private object MongoErrorCodes {
    const val KEY_DUPLICATE = 11000
}
