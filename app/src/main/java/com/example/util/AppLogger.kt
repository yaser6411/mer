package com.example.util

import android.util.Log
import com.example.BuildConfig

/**
 * Enterprise logging wrapper.
 * Ensures debug logs are active during development but completely silenced in production release builds.
 */
object AppLogger {
    private const val TAG = "MercuryApp"

    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    fun i(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, message)
        }
    }

    fun w(message: String, throwable: Throwable? = null, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) {
                Log.w(tag, message, throwable)
            } else {
                Log.w(tag, message)
            }
        }
    }

    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        // Errors are logged or forwarded to crash reporting
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}
