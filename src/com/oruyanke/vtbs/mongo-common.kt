package com.oruyanke.vtbs

import org.litote.kmongo.coroutine.CoroutineClient

fun CoroutineClient.forVtuber(name: String) = this.getDatabase("vtuber_$name")

suspend fun CoroutineClient.vtuberNames() =
    this.listDatabaseNames()
        .filter { it.startsWith("vtuber_") }
        .map { it.removePrefix("vtuber_") }
