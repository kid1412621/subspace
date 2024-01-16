package me.nanova.subspace.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.nanova.subspace.ui.theme.Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Theme {
                App()
            }
        }
    }
}

@Composable
fun App(
    navController: NavHostController = rememberNavController()
) {
    val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

    NavHost(
        navController = navController,
        startDestination = Routes.Home.name,
//                    modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = Routes.Settings.name) {
            Settings(
            )
        }
        composable(route = Routes.Home.name) {
            Layout(
                homeViewModel = homeViewModel,
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
