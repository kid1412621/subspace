package me.nanova.subspace.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.data.NetworkRepo
import me.nanova.subspace.data.Repo
import me.nanova.subspace.domain.HttpClient
import me.nanova.subspace.domain.QtApiService
import me.nanova.subspace.ui.theme.Theme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Theme {
                AppWrapper()
            }
        }
    }
}

@Composable
fun AppWrapper(
    navController: NavHostController = rememberNavController()
) {
//    val settingViewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
    val settingViewModel: SettingsViewModel = SettingsViewModel()
    val homeViewModel: HomeViewModel
    val account = settingViewModel.accounts[AccountType.QT]

    val retrofitService: QtApiService by lazy {
        HttpClient(account).getRetrofit()
            .create(QtApiService::class.java)
    }
    val repo: Repo by lazy {
        NetworkRepo(retrofitService)
    }
    homeViewModel = HomeViewModel(repo)

    NavHost(
        navController = navController,
        startDestination = Routes.Blank.name,
//                    modifier = Modifier.padding(innerPadding)
    ) {
        composable(route = Routes.Settings.name) {
            Settings(
                viewModel = settingViewModel
            )
        }
        composable(route = Routes.Home.name) {
            Layout(
                homeViewModel = homeViewModel,
                navController = navController
            )
        }
        composable(route = Routes.Blank.name) {
            BlankPage(
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
