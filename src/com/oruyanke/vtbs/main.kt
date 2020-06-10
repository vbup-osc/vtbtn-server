package com.oruyanke.vtbs

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

object ServerConfig {
    var testing = false
}

@ExperimentalStdlibApi
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    ServerConfig.testing = testing
    installFeatures()
    setupRouter()
}

fun Application.installFeatures() = run {
    install(ContentNegotiation) {
        jackson {
            setDefaultPrettyPrinter(DefaultPrettyPrinter())
        }
    }
}
