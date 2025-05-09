package me.nanova.subspace

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import me.nanova.subspace.exception.GlobalExceptionHandler
import me.nanova.subspace.ui.ErrorActivity

@HiltAndroidApp
class App : Application() {
    private var originalDefaultUncaughtExceptionHandler: Thread.UncaughtExceptionHandler? = null

    override fun onCreate() {
        super.onCreate()

        originalDefaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler(
            GlobalExceptionHandler(
                this,
                originalDefaultUncaughtExceptionHandler,
                ErrorActivity::class.java
            )
        )
    }
}



