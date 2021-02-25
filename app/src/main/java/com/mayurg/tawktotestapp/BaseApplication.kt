package com.mayurg.tawktotestapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Base application class used for dagger-hilt application context
 */
@HiltAndroidApp
open class BaseApplication : Application() {
}