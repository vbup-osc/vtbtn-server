package com.oruyanke.vtbs

import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import org.koin.ktor.ext.inject
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineClient
import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun Route.statisticsRoutes() {
    val mongo: CoroutineClient by inject()

    route("/statistics") {
        get("/{vtb}") {
            errorAware {
                val vtb = param("vtb")
                var sum = 0
                mongo.forVtuber(vtb).statistics().find()
                        .toList()
                        .forEach {
                            sum += it.time
                        }
                call.respond(
                        mapOf(
                                "vtuber" to vtb,
                                "clickTimes" to sum
                        )
                )
            }
        }

        get("/{vtb}/{group}") {
            errorAware {
                val vtb = param("vtb")
                val group = param("group")
                var sum = 0
                mongo.forVtuber(vtb).statistics().find(Statistic::group eq group)
                        .toList()
                        .forEach {
                            sum += it.time
                        }
                call.respond(
                        mapOf(
                                "vtuber" to vtb,
                                "group" to group,
                                "finalClickTimes" to sum)
                )


            }
        }

        get("/{vtb}/{group}/{voice}") {
            errorAware {
                val vtb = param("vtb")
                val voiceName = param("voice")
                var sum = 0
                mongo.forVtuber(vtb).statistics().find(Statistic::name eq voiceName)
                        .toList()
                        .forEach {
                            sum += it.time
                        }
                call.respond(
                        mapOf(
                                "vtuber" to vtb,
                                "name" to voiceName,
                                "finalClickTimes" to sum
                        )
                )
            }
        }

        get("/{vtb}/{group}/{voice}/{start}") {
            errorAware {
                val vtb = param("vtb")
                val voiceName = param("voice")
                val startTime = LocalDate.parse(param("start"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                var sum = 0
                mongo.forVtuber(vtb).statistics().find(Statistic::name eq voiceName, Statistic::date eq startTime)
                        .toList()
                        .forEach {
                            sum += it.time
                        }
                call.respond(
                        mapOf(
                                "vtuber" to vtb,
                                "name" to voiceName,
                                "requestDate" to param("start"),
                                "dayClickTimes" to sum
                        )
                )
            }
        }
        get("/{vtb}/{group}/{voice}/{start}/{end}") {
            errorAware {
                val vtb = param("vtb")
                val voiceName = param("voice")
                val startTime = LocalDate.parse(param("start"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val endTime = LocalDate.parse(param("end"), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                var sum = 0
                mongo.forVtuber(vtb).statistics().find(
                        Statistic::name eq voiceName,
                        Statistic::date gte startTime,
                        Statistic::date lte endTime
                )
                        .toList()
                        .forEach {
                            sum += it.time
                        }
                call.respond(
                        mapOf(
                                "vtuber" to vtb,
                                "name" to voiceName,
                                "start" to param("start"),
                                "end" to param("end"),
                                "sumClickTimes" to sum
                        )
                )
            }
        }


        post<PlusOneRequest>("/{vtb}") {
            errorAware {
                val vtb = param("vtb")
                mongo.forVtuber(vtb).statistics().updateOne(
                        and(
                                Statistic::date eq LocalDate.now(),
                                Statistic::name eq it.name,
                                Statistic::group eq it.group
                        ),
                        inc(Statistic::time, 1),
                        upsert())

            }
        }
    }
}
