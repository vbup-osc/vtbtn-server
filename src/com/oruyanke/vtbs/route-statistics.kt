package com.oruyanke.vtbs

import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.koin.ktor.ext.inject
import org.litote.kmongo.and
import org.litote.kmongo.coroutine.CoroutineClient
import org.litote.kmongo.eq
import org.litote.kmongo.inc
import org.litote.kmongo.upsert
import java.time.LocalDate


fun Route.statisticsRoutes() {
    val mongo: CoroutineClient by inject()

    route("/statistics") {
        get("/{vtb}") {
            errorAware {
                val vtb = param("vtb")
                val sum = mongo.forVtuber(vtb).statistics().totalClickTime()

                call.respond(
                    mapOf(
                        "vtuber" to vtb,
                        "click" to sum
                    )
                )
            }
        }

        get("/{vtb}/{group}") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")
                val sum = mongo.forVtuber(vtb).statistics().groupClickTime(group)

                call.respond(
                    mapOf(
                        "vtuber" to vtb,
                        "group" to group,
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
                mongo.forVtuber(vtb).statistics().updateOne(
                    and(
                        Statistic::date eq LocalDate.now(),
                        Statistic::name eq it.name,
                        Statistic::group eq it.group
                    ),
                    inc(Statistic::time, 1),
                    upsert()
                )
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
