package com.oruyanke.vtbs

import com.fasterxml.jackson.core.util.DefaultPrettyPrinter
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module
import org.koin.ktor.ext.koin
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

object ServerConfig {
    const val MONGODB_PORT = 27000
    const val ENV_ROOT_NAME = "VTBTN_SERVER_ROOT_NAME"
    const val ENV_ROOT_PASSWORD = "VTBTN_SERVER_ROOT_PASSWORD"

    var TESTING = false
}

@ExperimentalStdlibApi
@ExperimentalUnsignedTypes
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    ServerConfig.TESTING = testing
    installFeatures()
    runBlocking {
        installEnvironment()
    }

    routing {
        vtuberRoutes()
        statisticsRoutes()
        userRoutes()
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
        anyHost()
        allowCredentials = true
    }

    koin {
        modules(module)
    }
}

val module = module {
    single {
        KMongo.createClient(
            "mongodb://localhost:${ServerConfig.MONGODB_PORT}"
        ).coroutine
    }
}
