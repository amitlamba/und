package com.und.common.utils

import java.security.SecureRandom
import java.util.Random



/**
 * Created by shiv on 01/09/17.
 */
fun String?.isEmpty() = this == null || this.trim() == ""

fun randomString(length:Int):String {
    return RandomString(length).nextString()
}

class RandomString(length: Int) {

    private val random = SecureRandom()

    private val buf: CharArray

    init {
        if (length < 1)
            throw IllegalArgumentException("length < 1: " + length)
        buf = CharArray(length)
    }

    fun nextString(): String {
        for (idx in buf.indices)
            buf[idx] = symbols[random.nextInt(symbols.length)]
        return String(buf)
    }

    companion object {
        // string contains the set of characters allowed
        private val symbols = "ABCDEFGJKLMNPRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz"
    }

}



