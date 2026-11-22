package me.nanova.subspace.exception

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.os.Process
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import kotlin.system.exitProcess

class GlobalExceptionHandler(
    private val applicationContext: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?,
    private val errorActivityClass: Class<*>
) : Thread.UncaughtExceptionHandler {

    companion object {
        private const val TAG = "GlobalExceptionHandler"
        const val EXTRA_ERROR_DETAILS = "me.nanova.subspace.ERROR_DETAILS"
        const val EXTRA_ERROR_SUMMARY = "me.nanova.subspace.ERROR_SUMMARY"
        const val EXTRA_ERROR_THREAD = "me.nanova.subspace.ERROR_THREAD"

        @Volatile
        private var isHandlingException = false
    }

    override fun uncaughtException(thread: Thread, exception: Throwable) {
        val isMainThread = thread == Looper.getMainLooper().thread
        if (!isMainThread && isNonFatalBackgroundException(exception)) {
            Log.w(
                TAG,
                "Ignored non-fatal background exception on thread ${thread.name}: ${exception.message}",
                exception
            )
            return
        }

        // Check if already handling a fatal exception.
        // If so, delegate to the default handler to avoid a loop.
        if (isHandlingException) {
            Log.e(TAG, "Re-entrant call to uncaughtException. Delegating to default handler.", exception)
            defaultHandler?.uncaughtException(thread, exception)
            return
        }
        isHandlingException = true

        Log.e(
            TAG,
            "Fatal uncaught exception on thread ${thread.name}: ${exception.message}",
            exception
        )

        // 1. Log to Firebase Crashlytics
        try {
            FirebaseCrashlytics.getInstance().recordException(exception)
        } catch (e: Exception) {
            Log.e(TAG, "Error reporting to Firebase Crashlytics", e)
        }

        // 2. Attempt to launch the dedicated crash recovery activity
        try {
            val rootCause = generateSequence(exception) { it.cause }.last()
            val summary = buildString {
                append(rootCause::class.java.simpleName.ifBlank { "Exception" })
                val msg = rootCause.localizedMessage ?: exception.localizedMessage
                if (!msg.isNullOrBlank()) {
                    append(": ")
                    append(msg)
                }
            }

            val intent = Intent(applicationContext, errorActivityClass).apply {
                addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK or
                            Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
                )
                putExtra(EXTRA_ERROR_SUMMARY, summary)
                putExtra(EXTRA_ERROR_THREAD, thread.name)
                putExtra(EXTRA_ERROR_DETAILS, exception.stackTraceToString())
            }
            applicationContext.startActivity(intent)

            // 3. Terminate the current crashed process
            Process.killProcess(Process.myPid())
            exitProcess(10)
        } catch (e: Exception) {
            Log.e(TAG, "Error while trying to launch ErrorActivity after crash", e)
            defaultHandler?.uncaughtException(thread, exception)
        }
    }

    private fun isNonFatalBackgroundException(throwable: Throwable): Boolean {
        val causes = generateSequence(throwable) { it.cause }
        return causes.any { cause ->
            cause is CancellationException ||
                    cause is IOException ||
                    cause is HttpException
        }
    }
}
