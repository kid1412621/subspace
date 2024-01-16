package me.nanova.subspace.ui


import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import androidx.compose.material3.DismissibleNavigationDrawer
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Layout(
    homeViewModel: HomeViewModel,
    navController: NavHostController,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    var menuExpanded by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val lazyListState = rememberLazyListState()

    val filter by homeViewModel.filter.collectAsState()
    val uiState by homeViewModel.homeUiState.collectAsState()
    val list by uiState.list.collectAsState(emptyList())

    if (refreshState.isRefreshing) {
        LaunchedEffect(true) {
            homeViewModel.getTorrents(filter)
            refreshState.endRefresh()
        }
    }

    LaunchedEffect(Unit) {
        homeViewModel.getTorrents(filter)
    }

    val scope = rememberCoroutineScope()


    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showBottomSheet = false
            },
            sheetState = sheetState
        ) {
            // Sheet content
            Button(onClick = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
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
            ModalDrawerSheet {
                Text("Drawer title", modifier = Modifier.padding(16.dp))
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    NavigationDrawerItem(
                        label = { Text(text = "Drawer Item") },
                        selected = false,
                        onClick = { navController.navigate(Routes.Settings.name) }
                    )
                }
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
                        Text("Top app bar")
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
//                    modifier = Modifier.align()
                    onClick = { }) {
                    Text(text = "Extended FAB")
                }
            },
            bottomBar = {
                if (menuExpanded) {
                    DropdownMenu(
                        expanded = true,
                        onDismissRequest = { menuExpanded = false }
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
                                if (filter.reverse) {
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
                                    filter.copy(
                                        sort = "name",
                                        reverse = if (filter.sort == "name") !filter.reverse else filter.reverse
                                    )
                                )
                                menuExpanded = false
                                scope.launch {
                                    lazyListState.animateScrollToItem(0)
                                }
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
                                if (filter.reverse) {
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
                                    filter.copy(
                                        sort = "added_on",
                                        reverse = if (filter.sort == "added_on") !filter.reverse else filter.reverse
                                    )
                                )
                                menuExpanded = false
                                scope.launch {
                                    lazyListState.animateScrollToItem(0)
                                }
                            })
                        DropdownMenuItem(text = { Text(text = "Speed") }, onClick = { /*TODO*/ })
                    }
                }

                BottomAppBar(
                    actions = {
                        IconButton(onClick = { showBottomSheet = true }) {
                            Icon(Icons.Rounded.Search, contentDescription = "sort")
                        }
                        IconButton(onClick = { /* do something */ }) {
                            Icon(
                                Icons.Rounded.FilterList,
                                contentDescription = "filter"
                            )
                        }
                        IconButton(onClick = { menuExpanded = true }) {
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
                            Icon(Icons.Rounded.Add, "Localized description")
                        }
                    }
                )
            },
        ) { innerPadding ->
            Surface(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
//                    .nestedScroll(state.nestedScrollConnection),
                tonalElevation = 1.dp
            ) {
                Box(
                    Modifier.nestedScroll(refreshState.nestedScrollConnection)
                ) {
                    LazyColumn(
                        state = lazyListState,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {

                        if (!refreshState.isRefreshing && uiState.state == CallState.Success) {
                            items(
                                list,
                                key = { it.name }
                            ) {
                                ListItem(
                                    modifier = Modifier.animateItemPlacement(),
                                    headlineContent = { Text(it.name) },
                                    supportingContent = {
                                        Text(it.addedOn.toString())
                                    },
                                    leadingContent = {
                                        Icon(
                                            Icons.Filled.Favorite,
                                            contentDescription = "Localized description",
                                        )
                                    },
                                    trailingContent = { Text(it.state) }
                                )

                                HorizontalDivider()
                            }
                        }
                    }
                    PullToRefreshContainer(
                        modifier = Modifier.align(Alignment.TopCenter),
                        state = refreshState,
                    )
                }
            }
        }
    }
}


@Composable
@Preview
fun LayoutPrev() {
//    Layout(null, HomeViewModel().apply {
//        uiState = UiState.Success(
//            listOf(
//            )
//        )
//    })
}

//fun formatUnixTimestamp(unixTimestamp: Long): String {
//    val instant = Instant.fromEpochSeconds(unixTimestamp)
//    val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
////    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
//    return localDateTime.format(formatter)
//}