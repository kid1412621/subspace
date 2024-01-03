package me.nanova.subspace.theme

import android.os.Build
//import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.darkColorScheme
//import androidx.compose.material3.dynamicDarkColorScheme
//import androidx.compose.material3.dynamicLightColorScheme
//import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Material 3 color schemes
private val replyDarkColorScheme = darkColorScheme(

)

private val replyLightColorScheme = lightColorScheme(

)

@Composable
fun Theme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val replyColorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
        darkTheme -> replyDarkColorScheme
        else -> replyLightColorScheme
    }

    MaterialTheme(
        colorScheme = replyColorScheme,
//        typography = replyTypography,
        shapes = shapes,
        content = content
    )
}
