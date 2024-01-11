package me.nanova.subspace.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHost
import androidx.navigation.compose.rememberNavController
import me.nanova.subspace.ui.theme.Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Theme {
                val homeViewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory)

                Layout(
                    homeViewModel = homeViewModel,
                )
            }
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
@Composable
fun Navigation() {
    val navController = rememberNavController()
//    NavHost(navController = navController, startDestination = "home") {
//        composable("home") { Layout(navController) }
//        composable(
//            "details/{itemId}",
//            arguments = listOf(navArgument("itemId") { type = NavType.StringType })
//        ) { backStackEntry ->
//            DetailsScreen(itemId = backStackEntry.arguments?.getString("itemId") ?: "")
//        }
//    }
}