package com.oruyanke.vtbs

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlin.test.Test
import kotlin.test.assertEquals

@ExperimentalStdlibApi
class ApplicationTest {
    @Test
    fun testGreetings() {
        withTestApplication({ module(testing = true) }) {
            handleRequest(HttpMethod.Get, "/vtubers").apply {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("""
                    {"aqua":"/vtubers/aqua","fubuki":"/vtubers/fubuki"}
                """.trimIndent(), response.content)
            }
        }
    }
}
