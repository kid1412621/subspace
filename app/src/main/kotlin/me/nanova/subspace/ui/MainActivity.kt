package me.nanova.subspace.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import me.nanova.subspace.domain.model.ThemeMode
import me.nanova.subspace.ui.page.AccountPage
import me.nanova.subspace.ui.page.HomePage
import me.nanova.subspace.ui.page.SettingsPage
import me.nanova.subspace.ui.page.TorrentDetailsPage
import me.nanova.subspace.ui.theme.Theme
import me.nanova.subspace.ui.vm.SettingsViewModel

val LocalWindowWidthSizeClass = compositionLocalOf { WindowWidthSizeClass.Compact }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var incomingTorrent by mutableStateOf<Uri?>(null)
    private var openHomeForImport by mutableStateOf(false)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        receiveTorrent(intent)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("incomingTorrent", incomingTorrent?.toString())
        super.onSaveInstanceState(outState)
    }

    private fun receiveTorrent(intent: Intent) {
        if (intent.action != Intent.ACTION_VIEW) return
        val uri = intent.data?.normalizeScheme() ?: return
        when (uri.scheme) {
            "magnet" -> Unit
            "http", "https", "content" -> if (intent.type != "application/x-bittorrent") return
            else -> return
        }
        incomingTorrent = uri
        openHomeForImport = true
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            receiveTorrent(intent)
        } else {
            incomingTorrent = savedInstanceState.getString("incomingTorrent")?.let(Uri::parse)
        }

        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val settings by settingsViewModel.settings.collectAsState()
            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            val windowSizeClass = calculateWindowSizeClass(this)
            CompositionLocalProvider(LocalWindowWidthSizeClass provides windowSizeClass.widthSizeClass) {
                Theme(darkTheme = darkTheme) {
                    val navigationState = rememberNavigationState(startRoute = HomeRoute)
                    LaunchedEffect(openHomeForImport) {
                        if (openHomeForImport) {
                            Navigator(navigationState).goHome()
                            openHomeForImport = false
                        }
                    }
                    val pendingTorrent = incomingTorrent
                    AppContainer(
                        navigationState = navigationState,
                        incomingTorrent = pendingTorrent,
                        onIncomingTorrentHandled = {
                            if (incomingTorrent == pendingTorrent) incomingTorrent = null
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun AppContainer(
    modifier: Modifier = Modifier,
    navigationState: NavigationState = rememberNavigationState(startRoute = HomeRoute),
    incomingTorrent: Uri? = null,
    onIncomingTorrentHandled: () -> Unit = {},
) {
    val navigator = remember(navigationState) { Navigator(navigationState) }
    var refreshAfterDelete by rememberSaveable { mutableStateOf(false) }
    val spatialSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntOffset>()
    val effectsSpec = MaterialTheme.motionScheme.defaultEffectsSpec<Float>()

    val entryProvider = entryProvider {
        entry<HomeRoute> {
            HomePage(
                navigator = navigator,
                incomingTorrent = incomingTorrent,
                onIncomingTorrentHandled = onIncomingTorrentHandled,
                refreshAfterDelete = refreshAfterDelete,
                onDeleteRefreshStarted = { refreshAfterDelete = false },
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
        entry<TorrentDetailsRoute> { key ->
            TorrentDetailsPage(
                hash = key.hash,
                title = key.title,
                navigator = navigator,
                onDeleted = { refreshAfterDelete = true },
            )
        }
        entry<SettingsRoute> {
            SettingsPage(
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
