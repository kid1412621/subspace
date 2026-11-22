package me.nanova.subspace.ui.page

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.CloudSync
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.nanova.subspace.BuildConfig
import me.nanova.subspace.domain.model.ThemeMode
import me.nanova.subspace.ui.Navigator
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.vm.SettingsViewModel

@Composable
fun SettingsPage(
    viewModel: SettingsViewModel = hiltViewModel(),
    navigator: Navigator,
) {
    val settings by viewModel.settings.collectAsState()
    var showThemeDialog by remember { mutableStateOf(false) }
    var showIntervalDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = settings.themeMode,
            onDismiss = { showThemeDialog = false },
            onSelect = {
                viewModel.setThemeMode(it)
                showThemeDialog = false
            }
        )
    }

    if (showIntervalDialog) {
        IntervalSelectionDialog(
            currentIntervalMs = settings.refreshIntervalMs,
            onDismiss = { showIntervalDialog = false },
            onSelect = {
                viewModel.setRefreshInterval(it)
                showIntervalDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.goBack() },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLargeEmphasized
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MdSpacing.sm, vertical = MdSpacing.xs),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm)
            ) {
                // Section: Appearance
                SettingsSectionHeader("Appearance & Display")
                Card(
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = MdSpacing.xs)) {
                        SettingsClickableRow(
                            icon = Icons.Outlined.Brightness4,
                            title = "Theme",
                            subtitle = settings.themeMode.label,
                            onClick = { showThemeDialog = true }
                        )
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Straighten,
                            title = "Metric SI Units",
                            subtitle = if (settings.useSIUnits) "Base 1000 (KB, MB, GB)" else "Base 1024 (KiB, MiB, GiB)",
                            checked = settings.useSIUnits,
                            onCheckedChange = { viewModel.setUseSIUnits(it) }
                        )
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Speed,
                            title = "Transfer Speeds in Bits",
                            subtitle = if (settings.showInBits) "Display in Mbps" else "Display in MB/s",
                            checked = settings.showInBits,
                            onCheckedChange = { viewModel.setShowInBits(it) }
                        )
                    }
                }

                // Section: Sync & Network
                SettingsSectionHeader("Sync & Network")
                Card(
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = MdSpacing.xs)) {
                        val intervalLabel = when (settings.refreshIntervalMs) {
                            0L -> "Manual refresh only"
                            1000L -> "1 second"
                            2000L -> "2 seconds (Default)"
                            5000L -> "5 seconds"
                            10000L -> "10 seconds"
                            30000L -> "30 seconds"
                            else -> "${settings.refreshIntervalMs / 1000} seconds"
                        }
                        SettingsClickableRow(
                            icon = Icons.Outlined.CloudSync,
                            title = "Refresh Interval",
                            subtitle = intervalLabel,
                            onClick = { showIntervalDialog = true }
                        )
                        SettingsSwitchRow(
                            icon = Icons.Outlined.Wifi,
                            title = "Sync on Wi-Fi Only",
                            subtitle = "Pause automatic refresh on mobile / metered data",
                            checked = settings.refreshOnlyOnWifi,
                            onCheckedChange = { viewModel.setRefreshOnlyOnWifi(it) }
                        )
                    }
                }

                // Section: Confirmations
                SettingsSectionHeader("Confirmations")
                Card(
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(vertical = MdSpacing.xs)) {
                        SettingsSwitchRow(
                            icon = Icons.Outlined.DeleteForever,
                            title = "Confirm on Torrent Delete",
                            subtitle = "Prompt before deleting torrents",
                            checked = settings.confirmDelete,
                            onCheckedChange = { viewModel.setConfirmDelete(it) }
                        )
                    }
                }

                // Section: About
                SettingsSectionHeader("About")
                Card(
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(MdSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)
                    ) {
                        Text(
                            text = "Subspace",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Remote controller for BitTorrent clients",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(MdSpacing.md))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLargeEmphasized,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = MdSpacing.xs, top = MdSpacing.xs)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = MdSpacing.md, vertical = MdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = MdSpacing.md, vertical = MdSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.md)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeSelectionDialog(
    currentMode: ThemeMode,
    onDismiss: () -> Unit,
    onSelect: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Text(
                text = "Choose Theme",
                style = MaterialTheme.typography.headlineSmallEmphasized
            )
        },
        text = {
            Column {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(mode) }
                            .padding(vertical = MdSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentMode == mode,
                            onClick = { onSelect(mode) }
                        )
                        Text(
                            text = mode.label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = MdSpacing.sm)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLargeEmphasized)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun IntervalSelectionDialog(
    currentIntervalMs: Long,
    onDismiss: () -> Unit,
    onSelect: (Long) -> Unit
) {
    val options = listOf(
        1000L to "1 second",
        2000L to "2 seconds (Default)",
        5000L to "5 seconds",
        10000L to "10 seconds",
        30000L to "30 seconds",
        0L to "Manual refresh only"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        title = {
            Text(
                text = "Refresh Interval",
                style = MaterialTheme.typography.headlineSmallEmphasized
            )
        },
        text = {
            Column {
                options.forEach { (ms, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(ms) }
                            .padding(vertical = MdSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentIntervalMs == ms,
                            onClick = { onSelect(ms) }
                        )
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = MdSpacing.sm)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                shapes = ButtonDefaults.shapes()
            ) {
                Text("Cancel", style = MaterialTheme.typography.labelLargeEmphasized)
            }
        }
    )
}
