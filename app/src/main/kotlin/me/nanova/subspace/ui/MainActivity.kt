package me.nanova.subspace.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import me.nanova.subspace.ui.theme.Theme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Theme {
                AppContainer()
            }
        }
    }
}

@Composable
fun AppContainer(
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
