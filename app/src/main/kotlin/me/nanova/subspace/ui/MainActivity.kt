package me.nanova.subspace.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import me.nanova.subspace.ui.page.AccountPage
import me.nanova.subspace.ui.page.HomePage
import me.nanova.subspace.ui.theme.Theme

val LocalWindowWidthSizeClass = compositionLocalOf { WindowWidthSizeClass.Compact }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            CompositionLocalProvider(LocalWindowWidthSizeClass provides windowSizeClass.widthSizeClass) {
                Theme {
                    AppContainer()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AppContainer(
    modifier: Modifier = Modifier,
    navigationState: NavigationState = rememberNavigationState(startRoute = HomeRoute),
) {
    val navigator = remember(navigationState) { Navigator(navigationState) }
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val entryProvider = entryProvider {
        entry<HomeRoute> {
            HomePage(
                navigator = navigator
            )
        }
        entry<AccountCreationRoute> {
            AccountPage(
                navigator = navigator
            )
        }
        entry<AccountDetailsRoute> { key ->
            AccountPage(
                id = key.accountId,
                navigator = navigator
            )
        }
    }

    NavDisplay(
        entries = navigationState.toEntries(entryProvider),
        onBack = { navigator.goBack() },
        modifier = modifier,
        transitionSpec = {
            (fadeIn(
                animationSpec = effectsSpec
            ) + slideIntoContainer(
                animationSpec = spatialSpec,
                towards = AnimatedContentTransitionScope.SlideDirection.Start
            )) togetherWith ExitTransition.KeepUntilTransitionsFinished
        },
        popTransitionSpec = {
            EnterTransition.None togetherWith slideOutOfContainer(
                animationSpec = spatialSpec,
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        },
        predictivePopTransitionSpec = {
            EnterTransition.None togetherWith slideOutOfContainer(
                animationSpec = spatialSpec,
                towards = AnimatedContentTransitionScope.SlideDirection.End
            )
        }
    )
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
