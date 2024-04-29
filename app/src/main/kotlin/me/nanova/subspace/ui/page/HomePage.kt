package me.nanova.subspace.ui.page


import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.DismissibleDrawerSheet
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch
import me.nanova.subspace.R
import me.nanova.subspace.ui.Routes
import me.nanova.subspace.ui.component.AccountMenu
import me.nanova.subspace.ui.component.BlankAccount
import me.nanova.subspace.ui.component.TorrentList
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

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sheetState = rememberModalBottomSheetState()
    var showFilterSheet by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()


    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showFilterSheet = false
            },
            sheetState = sheetState
        ) {
            Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showFilterSheet = false
                    }
                }
            }) {
                Text("Hide bottom sheet")
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
                if (currentAccount != null) {
                    if (showSortMenu) {
                        DropdownMenu(
                            expanded = true,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = "Name") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.SortByAlpha,
                                        contentDescription = "Sort by name"
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.filter.reverse) {
                                        Icon(
                                            Icons.Rounded.ArrowDropDown,
                                            contentDescription = "Descending"
                                        )
                                    } else {
                                        Icon(
                                            Icons.Rounded.ArrowDropUp,
                                            contentDescription = "Ascending"
                                        )
                                    }
                                },
                                onClick = {
                                    homeViewModel.updateSort(
                                        uiState.filter.copy(
                                            sort = "name",
                                            reverse = if (uiState.filter.sort == "name") !uiState.filter.reverse else uiState.filter.reverse
                                        )
                                    )
                                    showSortMenu = false
                                })
                            DropdownMenuItem(
                                text = { Text(text = "Added On") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Rounded.AccessTime,
                                        contentDescription = "Sort by add time"
                                    )
                                },
                                trailingIcon = {
                                    if (uiState.filter.reverse) {
                                        Icon(
                                            Icons.Rounded.ArrowDropDown,
                                            contentDescription = "Descending"
                                        )
                                    } else {
                                        Icon(
                                            Icons.Rounded.ArrowDropUp,
                                            contentDescription = "Ascending"
                                        )
                                    }
                                },
                                onClick = {
                                    homeViewModel.updateSort(
                                        uiState.filter.copy(
                                            sort = "added_on",
                                            reverse = if (uiState.filter.sort == "added_on") !uiState.filter.reverse else uiState.filter.reverse
                                        )
                                    )
                                    showSortMenu = false
                                })
                            DropdownMenuItem(
                                text = { Text(text = "Speed") },
                                onClick = { /*TODO*/ })
                        }
                    }

                    BottomAppBar(
                        actions = {
                            IconButton(onClick = { /* do something */ }, enabled = false) {
                                Icon(Icons.Rounded.Search, contentDescription = "search")
                            }
                            IconButton(onClick = { showFilterSheet = true }) {
                                Icon(
                                    Icons.Rounded.FilterList,
                                    contentDescription = "filter"
                                )
                            }
                            IconButton(onClick = { showSortMenu = true }) {
                                Icon(
                                    Icons.Rounded.SwapVert,
                                    contentDescription = "sort"
                                )
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
            },
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .consumeWindowInsets(innerPadding)
                    .padding(innerPadding)
                    .fillMaxSize(),
                tonalElevation = 1.dp
            ) {
                if (currentAccount == null) {
                    BlankAccount(onGoSetting = { navController.navigate(Routes.Settings.name) })
                } else {
                    TorrentList(
                        uiState,
                        onRefresh = {
                            homeViewModel.refresh()
                        }
                    )
                }
            }
        }
    }
}

//fun formatUnixTimestamp(unixTimestamp: Long): String {
//    val instant = Instant.fromEpochSeconds(unixTimestamp)
//    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
////    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//    return localDateTime.format(formatter)
//}