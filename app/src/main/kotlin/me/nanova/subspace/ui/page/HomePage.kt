package me.nanova.subspace.ui.page


import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.Speed
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
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.input.pointer.pointerInput
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BottomBar(
    show: Boolean = false,
    uiState: HomeUiState,
    homeViewModel: HomeViewModel,
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    if (show) {
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showFilterSheet = false
                },
                sheetState = sheetState
            ) {
                Button(onClick = {
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showFilterSheet = false
                            }
                        }
                }) {
                    Text("Hide bottom sheet")
                }
            }
        }

        if (showSortMenu) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { showSortMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Name") },
                    colors = if (uiState.filter.sort == "name")
                        MenuDefaults.itemColors(MaterialTheme.colorScheme.primary)
                    else MenuDefaults.itemColors(),
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.SortByAlpha,
                            contentDescription = "Sort by name"
                        )
                    },
                    trailingIcon = {
                        if (uiState.filter.sort == "name") {
                            Icon(
                                if (uiState.filter.reverse) Icons.Rounded.ArrowDropDown
                                else Icons.Rounded.ArrowDropUp,
                                contentDescription = if (uiState.filter.reverse) "Descending" else "Ascending"
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
                    colors = if (uiState.filter.sort == "added_on")
                        MenuDefaults.itemColors(MaterialTheme.colorScheme.primary)
                    else MenuDefaults.itemColors(),
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.AccessTime,
                            contentDescription = "Sort by add time"
                        )
                    },
                    trailingIcon = {
                        if (uiState.filter.sort == "added_on") {
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
                    text = { Text(text = "Download Speed") },
                    colors = if (uiState.filter.sort == "dlspeed")
                        MenuDefaults.itemColors(MaterialTheme.colorScheme.primary)
                    else MenuDefaults.itemColors(),
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Speed,
                            contentDescription = "Sort by download speed"
                        )
                    },
                    trailingIcon = {
                        if (uiState.filter.sort == "dlspeed") {
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
                        }
                    },
                    onClick = {
                        homeViewModel.updateSort(
                            uiState.filter.copy(
                                sort = "dlspeed",
                                reverse = if (uiState.filter.sort == "dlspeed") !uiState.filter.reverse else uiState.filter.reverse
                            )
                        )
                        showSortMenu = false
                    })
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
                IconButton(onClick = { showSortMenu = true },
                    modifier = Modifier.pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                uiState.filter.sort = null
                                uiState.filter.reverse = null
                            }
                        )
                    }) {
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