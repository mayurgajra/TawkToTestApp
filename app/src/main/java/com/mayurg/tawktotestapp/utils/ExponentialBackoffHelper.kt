package com.mayurg.tawktotestapp.utils

import kotlinx.coroutines.delay
import java.io.IOException

/**
 * Helps with the exponential backoff of retrofit API call
 *
 * @param times indicates how many times it should try to make the call
 * @param initialDelay is the delay to start with after first failure
 * @param maxDelay makes sure that our exponential delay does not go beyond this number
 * @param factor to multiply the delay with to keep increasing delay exponentially
 */
suspend fun <T> retryIO(
    times: Int = 3,
    initialDelay: Long = 100, // 0.1 second
    maxDelay: Long = 1000,    // 1 second
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}