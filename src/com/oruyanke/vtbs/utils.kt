package com.oruyanke.vtbs

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.util.pipeline.ContextDsl
import io.ktor.util.pipeline.PipelineContext
import org.litote.kmongo.coroutine.CoroutineClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ContextDsl
@JvmName("patchTyped")
inline fun <reified R : Any> Route.patch(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(R) -> Unit
): Route {
    return route(path, HttpMethod.Patch) {
        handle {
            body(call.receive())
        }
    }
}

suspend fun <R> PipelineContext<*, ApplicationCall>.errorAware(block: suspend () -> R): R? {
    return try {
        block()
    } catch (e: ResponseException) {
        call.respondError(e.code, e.localizedMessage)
        null
    } catch (e: Exception) {
        call.respondError(HttpStatusCode.InternalServerError, e.toString())
        null
    }
}

suspend fun ApplicationCall.respondError(code: HttpStatusCode, msg: String) =
    this.respondText(
        """{"code": 1, "msg":"$msg"}""",
        ContentType.Application.Json,
        code
    )

fun PipelineContext<*, ApplicationCall>.param(name: String) =
    call.parameters[name] ?: throw IllegalArgumentException("Missing '$name'")

fun PipelineContext<*, ApplicationCall>.queryTime(name: String): LocalDate? =
    call.request.queryParameters[name]?.let {
        LocalDate.parse(it, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    }

fun PipelineContext<*, ApplicationCall>.queryTimeOrEpoch(name: String): LocalDate =
    queryTime(name) ?: LocalDate.EPOCH

fun PipelineContext<*, ApplicationCall>.queryTimeOrNow(name: String): LocalDate =
    queryTime(name) ?: LocalDate.now()

fun PipelineContext<*, ApplicationCall>.sessionId(): String =
    call.request.cookies[UserConfig.SESSION_ID] ?: throw AuthRequiredException()

suspend fun PipelineContext<*, ApplicationCall>.sessionUser(mongo: CoroutineClient): User {
    val sessionId = sessionId()
    val db = mongo.userDB()
    val session = db.sessions().getSession(sessionId)
    return db.users().bySession(session)
}

fun LocalDate.toHumanReadable(): String =
    this.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
