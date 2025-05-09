package me.nanova.subspace.exception

import android.content.Context
import android.content.Intent
import android.os.Process
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val applicationContext: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
    private val errorActivityClass: Class<*>
) : Thread.UncaughtExceptionHandler {

    companion object {
        private const val TAG = "GlobalExceptionHandler"
        const val EXTRA_ERROR_DETAILS =
            "me.nanova.subspace.ERROR_DETAILS"
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        Log.e(TAG, "Uncaught exception: ${exception.message}", exception)

        // 1. Log to Firebase Crashlytics
        try {
            FirebaseCrashlytics.getInstance().recordException(exception)
        } catch (e: Exception) {
            Log.e(TAG, "Error reporting to Firebase Crashlytics", e)
        }

        // 2. Attempt to launch a dedicated error activity with Compose UI
        try {
            val intent = Intent(applicationContext, errorActivityClass).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                putExtra(EXTRA_ERROR_DETAILS, exception.stackTraceToString())
            }
            applicationContext.startActivity(intent)

            // 3. Terminate the current process
            Process.killProcess(Process.myPid())
            exitProcess(10) // Exit with a non-zero status code (convention for error)

        } catch (e: Exception) {
            Log.e(TAG, "Error while trying to launch ErrorActivity after crash", e)
            // If launching the error activity fails, fall back to the default handler
            defaultHandler?.uncaughtException(thread, exception)
        }
    }
}
