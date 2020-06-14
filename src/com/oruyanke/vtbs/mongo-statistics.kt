package com.oruyanke.vtbs

import com.mongodb.client.model.Filters
import org.bson.conversions.Bson
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import java.time.LocalDate

data class Statistic(
    val date: LocalDate,
    val name: String,
    val time: Int,
    val group: String
)

fun CoroutineDatabase.statistics() = this.getCollection<Statistic>("statistics")

suspend fun CoroutineCollection<Statistic>.rangeClickTime(
    from: LocalDate,
    to: LocalDate,
    vararg filters: Bson = arrayOf(EMPTY_BSON)
) = find(
    Filters.and(
        Statistic::date gte from,
        Statistic::date lte to,
        *filters
    )
).toList().sumClick()

suspend fun CoroutineCollection<Statistic>.click(group: String, name: String) =
    updateOne(
        Filters.and(
            Statistic::date eq LocalDate.now(),
            Statistic::group eq group,
            Statistic::name eq name
        ),
        inc(Statistic::time, 1),
        upsert()
    )

fun List<Statistic>.sumClick() = map { it.time }.sum()
