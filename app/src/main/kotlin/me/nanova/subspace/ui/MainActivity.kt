package me.nanova.subspace.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.nanova.subspace.ui.page.HomePage
import me.nanova.subspace.ui.page.Settings
import me.nanova.subspace.ui.theme.Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            Theme {
                AppContainer(
                    Modifier
                        .safeDrawingPadding()
                        .windowInsetsPadding(WindowInsets.safeContent)
                )
            }
        }
    }
}

@Composable
fun AppContainer(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {

    NavHost(
        navController = navController,
        startDestination = Routes.Home.name,
//                    modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = Routes.Settings.name) {
            Settings(
                navController = navController
            )
        }
        composable(route = Routes.Home.name) {
            HomePage(
                navController = navController
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    Theme {
//        Layout(
//            homeViewModel = HomeViewModel().apply {  }
//        )
    }
}
