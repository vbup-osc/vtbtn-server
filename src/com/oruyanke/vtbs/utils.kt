package com.oruyanke.vtbs

fun Boolean.runIf(block: () -> Unit) {
    if (this) {
        block()
    }
}
