package me.nanova.subspace.ui.page


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CleaningServices
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import me.nanova.subspace.R
import me.nanova.subspace.ui.Routes
import me.nanova.subspace.ui.component.AccountMenu
import me.nanova.subspace.ui.component.FilterMenu
import me.nanova.subspace.ui.component.SortMenu
import me.nanova.subspace.ui.component.TorrentList
import me.nanova.subspace.ui.vm.HomeUiState
import me.nanova.subspace.ui.vm.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(
    homeViewModel: HomeViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val uiState by homeViewModel.homeUiState.collectAsState()
    val currentAccount by homeViewModel.currentAccount.collectAsState(initial = null)
    val accounts by homeViewModel.accounts.collectAsState(initial = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            val result = snackbarHostState.showSnackbar(it)
            if (result == SnackbarResult.Dismissed) {
                uiState.error = null
            }
        }
    }


    DismissibleNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DismissibleDrawerSheet(drawerState) {
                AccountMenu(
                    currentAccountId = currentAccount?.id,
                    accounts = accounts,
                    onAccountAdding = { navController.navigate(Routes.Settings.name) },
                    onAccountSelected = {
                        homeViewModel.switchAccount(it)
                        scope.launch {
                            drawerState.close()
                        }
                    }
                )
            }
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(LocalContext.current.resources.getString(R.string.app_name))
                    }
                )
            },
            bottomBar = {
                BottomBar(currentAccount != null, uiState, homeViewModel)
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState)
            },
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding)
                    .fillMaxSize(),
            ) {
                if (currentAccount == null) {
                    BlankAccount(onGoSetting = { navController.navigate(Routes.Settings.name) })
                } else {
                    TorrentList(homeViewModel)
                }
            }
        }
    }
}

@Composable
private fun BottomBar(
    show: Boolean = false,
    uiState: HomeUiState,
    homeViewModel: HomeViewModel,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }

    if (show) {
        if (showFilterSheet) {
            FilterMenu(
                uiState.filter,
                onClose = { showFilterSheet = false },
                onFilter = { homeViewModel.updateFilter(it) }
            )
        }

        if (showSortMenu) {
            SortMenu(
                uiState.filter,
                onClose = { showSortMenu = false },
                onSort = { homeViewModel.updateFilter(it) })
        }

        BottomAppBar(
            actions = {
                IconButton(onClick = { /* do something */ }, enabled = false) {
                    Icon(Icons.Rounded.Search, contentDescription = "search")
                }
                IconButton(
                    onClick = { showFilterSheet = true },
                    colors = if (uiState.filter.hasFiltered())
                        IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.primaryContainer)
                    else IconButtonDefaults.iconButtonColors(),
                ) {
                    Icon(
                        Icons.Rounded.FilterList,
                        contentDescription = "filter"
                    )
                }
                IconButton(
                    onClick = { showSortMenu = true },
                    colors = if (uiState.filter.hasSorted())
                        IconButtonDefaults.iconButtonColors(MaterialTheme.colorScheme.primaryContainer)
                    else IconButtonDefaults.iconButtonColors(),
                ) {
                    Icon(
                        Icons.Rounded.SwapVert,
                        contentDescription = "sort"
                    )
                }
                // IconButton no built-in longPress support
                if (uiState.filter.hasFiltered() || uiState.filter.hasSorted()) {
                    IconButton(onClick = { homeViewModel.resetFilter() }) {
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
}

@Composable
private fun BlankAccount(
    onGoSetting: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        Text(
            text = "No server account available yet",
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 20.sp
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = { onGoSetting() }) {
                Text(buildAnnotatedString {
                    withStyle(
                        SpanStyle(fontWeight = FontWeight.ExtraBold)
                    ) {
                        append("Click ")
                    }
                    append("to add server account")
                })
            }
            Spacer(Modifier.heightIn(15.dp))
            Text(
                text = buildAnnotatedString {
                    append("or")
                    withStyle(
                        SpanStyle(fontWeight = FontWeight.Bold)
                    ) {
                        append(" Swipe right ")
                    }
                    append("for settings")
                },
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
@Preview
fun BlankAccountPrev() {
    BlankAccount()
}

//fun formatUnixTimestamp(unixTimestamp: Long): String {
//    val instant = Instant.fromEpochSeconds(unixTimestamp)
//    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
////    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//    return localDateTime.format(formatter)
//}