package com.oruyanke.vtbs

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.text.SimpleDateFormat
import java.util.*


fun Route.statisticsRoutes() {
    val mongo: CoroutineClient by inject()

    route("/statistics") {
        get("/{vtb}") {
            call.respond("Hello, world")

        }
        get("/{vtb}/{group}") {
            val vtb = param("vtb")
            val group = param("group")

            val db = mongo.forVtuber(vtb)
            val groupInfo = db.statistics()
        }
        get("/{vtb}/{name}") {
            errorAware {
                val vtb = param("vtb")
                val voiceName = param("name")

                val db = mongo.forVtuber(vtb)
                val voiceInfo = db.statistics()
                call.respond("Hello")
            }
        }

        post("/{vtb}/{name}") {
            errorAware {
                val date = SimpleDateFormat("yyyy/M/dd").format(Date()).toString()
                val vtb = param("vtb")
                val name = param("name")
                mongo.forVtuber(vtb).statistics().updateOne(
                        org.litote.kmongo.and(
                                Statistic::date eq date,
                                Statistic::name eq name
                        ),
                        setValue(Statistic::time, 1)
                )

            }
        }

    }
}
