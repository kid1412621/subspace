package me.nanova.subspace.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.CategoryInfo
import me.nanova.subspace.domain.model.DomainTorrentState
import me.nanova.subspace.domain.model.GenericTorrentFilter

// Define filter types based on GenericTorrentFilter properties
private enum class FilterType(val icon: ImageVector, val title: String, val showCondition: (GenericTorrentFilter) -> Boolean) {
    Status(Icons.Filled.QuestionMark, "Status", { it.status?.isNotEmpty() == true }),
    Category(Icons.Filled.Category, "Category", { it.category != null }),
    Tag(Icons.AutoMirrored.Filled.Label, "Tag", { it.tags?.isNotEmpty() == true });
    // Query is not explicitly a type here, but could be if UI is added for it.
}

// Temporary data holder for filter changes within the bottom sheet
private data class GenericFiltersHolder(
    var status: MutableSet<DomainTorrentState> = mutableSetOf(),
    var category: String? = null,
    var tags: MutableList<String> = mutableListOf(),
    var query: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterMenu(
    currentFilter: GenericTorrentFilter,
    allCategories: Map<String, CategoryInfo>, // Changed from QBCategories
    allTags: List<String>,
    onClose: () -> Unit = {},
    onApplyFilter: (GenericTorrentFilter) -> Unit = {}, // Changed to GenericTorrentFilter
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    // Initialize temporary filter holder with current filter values
    val tempFilters = remember {
        GenericFiltersHolder(
            status = currentFilter.status?.toMutableSet() ?: mutableSetOf(),
            category = currentFilter.category,
            tags = currentFilter.tags?.toMutableList() ?: mutableListOf(),
            query = currentFilter.query
        )
    }

    // Determine which filter sections are initially active based on currentFilter
    val activeFilterTypes = remember {
        mutableStateListOf(
            *FilterType.entries
                .filter { it.showCondition(currentFilter) }
        onDismissRequest = {
            onClose()
        },
        sheetState = sheetState,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .animateContentSize()
        ) {
            // Segmented button to toggle filter sections (Status, Category, Tag)
            MultiChoiceSegmentedButtonRow {
                FilterType.entries.forEachIndexed { index, filterType ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = FilterType.entries.size
                        ),
                        colors = if (filterType in activeFilterTypes)
                            SegmentedButtonDefaults.colors(MaterialTheme.colorScheme.primaryContainer)
                        else
                            SegmentedButtonDefaults.colors(),
                        icon = {
                            SegmentedButtonDefaults.Icon(active = filterType in activeFilterTypes) {
                                Icon(
                                    imageVector = filterType.icon,
                                    contentDescription = filterType.title,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        },
                        onCheckedChange = {
                            if (!activeFilterTypes.remove(filterType)) {
                                activeFilterTypes.add(filterType)
                                // When a filter type is activated, initialize its value in tempFilters if not already set
                                when (filterType) {
                                    FilterType.Status -> if (tempFilters.status.isEmpty()) {
                                        // tempFilters.status.add(DomainTorrentState.ALL) // Default or first available
                                    }
                                    FilterType.Category -> if (tempFilters.category == null) {
                                        // tempFilters.category = allCategories.keys.firstOrNull() // Default or first available
                                    }
                                    FilterType.Tag -> if (tempFilters.tags.isEmpty()) {
                                        // tempFilters.tags.add(allTags.firstOrNull()) // Default or first available
                                    }
                                }
                            } else {
                                // When deactivating, clear the filter from tempFilters
                                when (filterType) {
                                    FilterType.Status -> tempFilters.status.clear()
                                    FilterType.Category -> tempFilters.category = null
                                    FilterType.Tag -> tempFilters.tags.clear()
                                }
                            }
                        },
                        checked = filterType in activeFilterTypes
                    ) {
                        Text(filterType.title)
                    }
                }
            }

            // Display filter options based on activeFilterTypes
            activeFilterTypes.sortedBy { it.ordinal }.forEach { type ->
                when (type) {
                    FilterType.Status -> StatusFilterMenu(tempFilters.status) { newStatusSet ->
                        tempFilters.status = newStatusSet
                    }
                    FilterType.Category -> CategoryFilterMenu(tempFilters.category, allCategories) { newCategory ->
                        tempFilters.category = newCategory
                    }
                    FilterType.Tag -> TagFilterMenu(tempFilters.tags, allTags) { newTags ->
                        tempFilters.tags = newTags
                    }
                }
            }

            Button(
                onClick = {
                    val newFilter = GenericTorrentFilter(
                        status = if (activeFilterTypes.contains(FilterType.Status)) tempFilters.status.ifEmpty { null } else null,
                        category = if (activeFilterTypes.contains(FilterType.Category)) tempFilters.category else null,
                        tags = if (activeFilterTypes.contains(FilterType.Tag)) tempFilters.tags.ifEmpty { null } else null,
                        query = tempFilters.query // Query is not directly managed by FilterType UI in this version
                    )
                    onApplyFilter(newFilter)
                    scope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onClose()
                            }
                        }
                },
                modifier = Modifier.padding(top = 20.dp)
            ) {
                Text("Confirm")
            }
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StatusFilterMenu(
    currentStatusSet: MutableSet<DomainTorrentState>,
    onUpdate: (MutableSet<DomainTorrentState>) -> Unit
) {
    val allPossibleStates = DomainTorrentState.entries.filterNot { it == DomainTorrentState.UNKNOWN } // Exclude UNKNOWN or other irrelevant states

    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    Text(
        "Status",
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        color = MaterialTheme.colorScheme.outline
    )
    FlowRow(
        Modifier
            .fillMaxWidth(1f)
            .wrapContentHeight(align = Alignment.Top),
        horizontalArrangement = Arrangement.Start,
    ) {
        allPossibleStates.forEach { state ->
            val isSelected = currentStatusSet.contains(state)
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newSet = currentStatusSet.toMutableSet()
                    if (isSelected) newSet.remove(state) else newSet.add(state)
                    onUpdate(newSet)
                },
                label = { Text(state.name.lowercase().replaceFirstChar { it.uppercase() }) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                leadingIcon = {
                    if (isSelected)
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = state.name,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterMenu(
    currentCategory: String?,
    allCategories: Map<String, CategoryInfo>,
    onUpdate: (String?) -> Unit
) {
    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    if (allCategories.isEmpty()) {
        Text(
            "No available category",
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            color = MaterialTheme.colorScheme.outline
        )
        return
    }

    Text(
        "Category",
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        color = MaterialTheme.colorScheme.outline
    )
    FlowRow(
        Modifier
            .fillMaxWidth(1f)
            .wrapContentHeight(align = Alignment.Top),
        horizontalArrangement = Arrangement.Start,
    ) {
        // Add a chip for "All" or "None" category
        FilterChip(
            selected = currentCategory == null,
            onClick = { onUpdate(null) },
            label = { Text("All") },
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            leadingIcon = {
                if (currentCategory == null)
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "All Categories",
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
            }
        )
        allCategories.keys.forEach { categoryName ->
            val isSelected = categoryName == currentCategory
            FilterChip(
                selected = isSelected,
                onClick = {
                    onUpdate(if (isSelected) null else categoryName)
                },
                label = { Text(categoryName) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                leadingIcon = {
                    if (isSelected)
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = categoryName,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagFilterMenu(
    currentTags: MutableList<String>, // Now expects a list for multi-select
    allTags: List<String>,
    onUpdate: (MutableList<String>) -> Unit
) {
    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    if (allTags.isEmpty()) {
        Text("No available tags", fontSize = MaterialTheme.typography.labelSmall.fontSize, color = MaterialTheme.colorScheme.outline)
        return
    }

    Text("Tags", fontSize = MaterialTheme.typography.labelSmall.fontSize, color = MaterialTheme.colorScheme.outline)
    FlowRow(
        Modifier.fillMaxWidth(1f).wrapContentHeight(align = Alignment.Top),
        horizontalArrangement = Arrangement.Start,
    ) {
        allTags.forEach { tag ->
            val isSelected = currentTags.contains(tag)
            FilterChip(
                selected = isSelected,
                onClick = {
                    val newTags = currentTags.toMutableList()
                    if (isSelected) newTags.remove(tag) else newTags.add(tag)
                    onUpdate(newTags)
                },
                label = { Text(tag) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                leadingIcon = {
                    if (isSelected)
                        Icon(Icons.Filled.Done, contentDescription = tag, modifier = Modifier.size(FilterChipDefaults.IconSize))
                }
            )
        }
    }
}


// Previews need to be updated to reflect new function signatures and data types

@Preview(showBackground = true)
@Composable
fun StatusFilterMenuPreview() {
    StatusFilterMenu(
        currentStatusSet = remember { mutableStateListOf(DomainTorrentState.DOWNLOADING, DomainTorrentState.PAUSED).toMutableSet() },
        onUpdate = {}
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryFilterMenuPreview() {
    CategoryFilterMenu(
        currentCategory = "Movies",
        allCategories = mapOf(
            "Movies" to CategoryInfo("Movies", "/path/movies"),
            "TV Shows" to CategoryInfo("TV Shows", "/path/tv")
        ),
        onUpdate = {}
    )
}

@Preview(showBackground = true)
@Composable
fun TagFilterMenuPreview() {
    TagFilterMenu(
        currentTags = remember { mutableStateListOf("HD", "Action").toMutableList() },
        allTags = listOf("HD", "Action", "Comedy", "Drama"),
        onUpdate = {}
    )
}
