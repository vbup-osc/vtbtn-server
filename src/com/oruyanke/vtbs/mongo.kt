package com.oruyanke.vtbs

import org.litote.kmongo.coroutine.CoroutineClient

fun CoroutineClient.vtubersDB() =
    this.getDatabase(ServerConfig.DB_NAME)

