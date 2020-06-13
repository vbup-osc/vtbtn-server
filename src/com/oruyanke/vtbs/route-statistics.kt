package com.oruyanke.vtbs

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.koin.ktor.ext.inject
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.eq


fun Route.statisticsRoutes() {
    val mongo: CoroutineClient by inject()

    route("/statistics") {
        get("/{vtb}") {
            errorAware {
                val vtb = param("vtb")
                val startTime = queryTimeOrEpoch("from")
                val endTime = queryTimeOrNow("to")

                val sum = mongo.forVtuber(vtb).statistics().rangeClickTime(
                    startTime,
                    endTime
                )

                call.respond(
                    mapOf(
                        "vtuber" to vtb,
                        "from" to startTime.toHumanReadable(),
                        "to" to endTime.toHumanReadable(),
                        "click" to sum
                    )
                )
            }
        }

        get("/{vtb}/{group}") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")
                val startTime = queryTimeOrEpoch("from")
                val endTime = queryTimeOrNow("to")

                val sum = mongo.forVtuber(vtb).statistics().rangeClickTime(
                    startTime,
                    endTime,
                    Statistic::group eq group
                )

                call.respond(
                    mapOf(
                        "vtuber" to vtb,
                        "group" to group,
                        "from" to startTime.toHumanReadable(),
                        "to" to endTime.toHumanReadable(),
                        "click" to sum
                    )
                )
            }
        }

        get("/{vtb}/{group}/{voice}") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")
                val voiceName = param("voice")
                val startTime = queryTimeOrEpoch("from")
                val endTime = queryTimeOrNow("to")

                val sum = mongo.forVtuber(vtb).statistics().rangeClickTime(
                    startTime,
                    endTime,
                    Statistic::name eq voiceName,
                    Statistic::group eq group
                )

                call.respond(
                    mapOf(
                        "vtuber" to vtb,
                        "name" to voiceName,
                        "group" to group,
                        "from" to startTime.toHumanReadable(),
                        "to" to endTime.toHumanReadable(),
                        "click" to sum
                    )
                )
            }
        }

        post<PlusOneRequest>("/{vtb}/click") {
            errorAware {
                val vtb = param("vtb")
                mongo.forVtuber(vtb).statistics().click(it.group, it.name)
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
