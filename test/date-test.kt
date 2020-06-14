package com.oruyanke.vtbs

import java.time.LocalDate
import java.time.format.DateTimeFormatter


fun main() {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val date = LocalDate.parse("2020-01-01", formatter)

}

