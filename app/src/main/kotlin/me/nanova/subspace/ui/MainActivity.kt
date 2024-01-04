@file:OptIn(ExperimentalMaterial3WindowSizeClassApi::class)

package me.nanova.subspace.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import me.nanova.subspace.ui.theme.Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Theme {
                val windowSize = calculateWindowSizeClass(this)

                Layout(
                    windowSize = windowSize
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    Theme {
        Layout(
            windowSize = WindowSizeClass.calculateFromSize(DpSize(400.dp, 900.dp)),
        )
    }
}
