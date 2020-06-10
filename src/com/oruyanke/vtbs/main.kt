package com.oruyanke.vtbs

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import org.koin.dsl.module
import org.koin.ktor.ext.koin
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

object ServerConfig {
    var TESTING = false
}

@ExperimentalStdlibApi
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    ServerConfig.TESTING = testing
    installFeatures()

    routing {
        vtuberRoutes()
    }
}

fun Application.installFeatures() = run {
    install(ContentNegotiation) {
        jackson {
            setDefaultPrettyPrinter(DefaultPrettyPrinter())
        }
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        allowCredentials = true
    }

    koin {
        modules(module)
    }
}

val module = module {
    single { KMongo.createClient("mongodb://localhost:27017").coroutine }
}
