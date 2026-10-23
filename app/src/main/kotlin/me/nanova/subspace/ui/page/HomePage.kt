package me.nanova.subspace.ui.page


import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import kotlinx.coroutines.launch
import me.nanova.subspace.R
import me.nanova.subspace.ui.AccountCreationRoute
import me.nanova.subspace.ui.AccountDetailsRoute
import me.nanova.subspace.ui.LocalWindowWidthSizeClass
import me.nanova.subspace.ui.Navigator
import me.nanova.subspace.ui.component.AccountMenu
import me.nanova.subspace.ui.component.FilterMenu
import me.nanova.subspace.ui.component.SortMenu
import me.nanova.subspace.ui.component.TorrentList
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.theme.Theme
import me.nanova.subspace.ui.vm.HomeUiState
import me.nanova.subspace.ui.vm.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HomePage(
    homeViewModel: HomeViewModel = hiltViewModel(),
    navigator: Navigator,
) {
    val uiState by homeViewModel.homeUiState.collectAsState()
    val currentAccount by homeViewModel.currentAccount.collectAsState(initial = null)
    val accounts by homeViewModel.accounts.collectAsState(initial = emptyList())

    val snackBarHostState = remember { SnackbarHostState() }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            val result = snackBarHostState.showSnackbar(it)
            if (result == SnackbarResult.Dismissed) {
                uiState.error = null
            }
        }
    }


    val windowWidthSizeClass = LocalWindowWidthSizeClass.current
    val isExpanded = windowWidthSizeClass == WindowWidthSizeClass.Expanded

    val accountMenuContent = @Composable {
        AccountMenu(
            currentAccountId = currentAccount?.id,
            accounts = accounts,
            onAccountAdding = {
                navigator.navigate(AccountCreationRoute)
            },
            onAccountSelected = {
                homeViewModel.switchAccount(it)
                if (!isExpanded) {
                    scope.launch { drawerState.close() }
                }
            },
            onAccountEditing = {
                navigator.navigate(AccountDetailsRoute(it.id))
            },
            onAccountDeleting = {
                homeViewModel.deleteAccount(it)
            }
        )
    }

    val mainContent = @Composable {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        subtitleContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    ),
                    navigationIcon = {
                        if (!isExpanded) {
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                    }
                                },
                                shapes = IconButtonDefaults.shapes(),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Menu,
                                    contentDescription = "Toggle navigation drawer"
                                )
                            }
                        }
                    },
                    title = {
                        Text(
                            text = LocalContext.current.resources.getString(R.string.app_name),
                            style = MaterialTheme.typography.titleLargeEmphasized
                        )
                    },
                    subtitle = {
                        currentAccount?.let { account ->
                            Text(
                                text = account.name,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomBar(currentAccount != null, uiState, homeViewModel)
            },
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState)
            },
        ) { innerPadding ->
            Surface(
                modifier = Modifier.fillMaxSize(),
            ) {
                Log.d("ACCOUNT", currentAccount.toString())
                if (currentAccount == null) {
                    BlankAccount(
                        modifier = Modifier
                            .padding(innerPadding)
                            .consumeWindowInsets(innerPadding)
                            .fillMaxSize(),
                        isExpanded = isExpanded,
                        onGoSetting = { navigator.navigate(AccountCreationRoute) }
                    )
                } else {
                    TorrentList(
                        viewModel = homeViewModel,
                        contentPadding = innerPadding,
                    )
                }
            }
        }
    }

    if (isExpanded) {
        PermanentNavigationDrawer(
            drawerContent = {
                PermanentDrawerSheet(
                    drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    modifier = Modifier.width(320.dp)
                ) {
                    accountMenuContent()
                }
            },
            content = mainContent
        )
    } else {
        DismissibleNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DismissibleDrawerSheet(
                    drawerState = drawerState,
                    drawerContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    accountMenuContent()
                }
            },
            content = mainContent
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BottomBar(
    show: Boolean = false,
    uiState: HomeUiState,
    homeViewModel: HomeViewModel,
) {
    if (!show) return

    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val category by homeViewModel.categories.collectAsState(initial = emptyMap())
    val tags by homeViewModel.tags.collectAsState(initial = emptyList())

    if (showFilterSheet) {
        FilterMenu(
            uiState.filter,
            category,
            tags,
            onClose = { showFilterSheet = false },
            onFilter = { homeViewModel.updateFilter(it) }
        )
    }

    BottomAppBar(
        actions = {
            IconButton(
                onClick = { /* do something */ },
                enabled = false,
                shapes = IconButtonDefaults.shapes(),
            ) {
                Icon(Icons.Rounded.Search, contentDescription = "search")
            }
            FilledTonalIconToggleButton(
                checked = uiState.filter.hasFiltered(),
                onCheckedChange = { showFilterSheet = true },
                shapes = IconButtonDefaults.toggleableShapes(),
            ) {
                Icon(
                    Icons.Rounded.FilterList,
                    contentDescription = "filter"
                )
            }
            Box {
                FilledTonalIconToggleButton(
                    checked = uiState.filter.hasSorted(),
                    onCheckedChange = { showSortMenu = true },
                    shapes = IconButtonDefaults.toggleableShapes(),
                ) {
                    Icon(
                        Icons.Rounded.SwapVert,
                        contentDescription = "sort"
                    )
                }
                if (showSortMenu) {
                    SortMenu(
                        uiState.filter,
                        onClose = { showSortMenu = false },
                        onSort = { homeViewModel.updateFilter(it) }
                    )
                }
            }

            if (uiState.filter.hasFiltered() || uiState.filter.hasSorted()) {
                IconButton(
                    onClick = { homeViewModel.resetFilter() },
                    shapes = IconButtonDefaults.shapes(),
                ) {
                    Icon(
                        Icons.Rounded.CleaningServices,
                        contentDescription = "clear filter"
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                containerColor = BottomAppBarDefaults.bottomAppBarFabColor,
                elevation = FloatingActionButtonDefaults.bottomAppBarFabElevation()
            ) {
                Icon(Icons.Rounded.Add, "Add torrent")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun BlankAccount(
    modifier: Modifier = Modifier,
    isExpanded: Boolean = false,
    onGoSetting: () -> Unit = {}
) {
    Column(
        modifier = modifier.padding(MdSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No server account available yet",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(MdSpacing.lg))

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MdSpacing.sm)
        ) {
            Button(
                onClick = { onGoSetting() },
                shapes = ButtonDefaults.shapes(),
            ) {
                Text(
                    text = "Add server account",
                    style = MaterialTheme.typography.labelLargeEmphasized,
                )
            }
            Text(
                text = if (isExpanded) {
                    "or select a server on the left"
                } else {
                    "or swipe right for settings"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
fun BlankAccountPrev() {
    Theme {
        BlankAccount()
    }
}

//fun formatUnixTimestamp(unixTimestamp: Long): String {
//    val instant = Instant.fromEpochSeconds(unixTimestamp)
//    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
////    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//    return localDateTime.format(formatter)
//}