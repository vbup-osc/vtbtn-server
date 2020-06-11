package com.oruyanke.vtbs

import io.ktor.http.HttpStatusCode

open class ResponseException(val code: HttpStatusCode, msg: String) : java.lang.RuntimeException(msg)

class BadRequestException(msg: String) : ResponseException(HttpStatusCode.BadRequest, msg)
