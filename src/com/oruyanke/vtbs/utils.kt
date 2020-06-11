package com.oruyanke.vtbs

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.util.pipeline.PipelineContext

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
        """{"code": 1, "msg":"$msg"""",
        ContentType.Application.Json,
        code
    )

fun PipelineContext<*, ApplicationCall>.param(name: String) =
    call.parameters[name] ?: throw IllegalArgumentException("Missing '$name'")
