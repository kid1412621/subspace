package me.nanova.subspace.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import me.nanova.subspace.domain.model.QTCategories
import me.nanova.subspace.domain.model.QTCategory
import me.nanova.subspace.domain.model.QTListParams

private enum class FilterType(val icon: ImageVector, val showCondition: (QTListParams) -> Boolean) {
    Status(Icons.Filled.QuestionMark, { it.filter != "all" }),
    Category(Icons.Filled.Category, { it.category != null }),
    Tag(Icons.AutoMirrored.Filled.Label, { it.tag != null });
}

private data class QTFilters(
    var filter: String = "all",
    var category: String? = null,
    var tag: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterMenu(
    filter: QTListParams,
    categories: QTCategories,
    tags: List<String>,
    onClose: () -> Unit = {},
    onFilter: (QTListParams) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val tmp = QTFilters(filter = filter.filter, tag = filter.tag, category = filter.category)

    ModalBottomSheet(
        onDismissRequest = {
            onClose()
        },
        sheetState = sheetState
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
        ) {
            AllFilterMenu(filter = filter, categories = categories, tags = tags, tmp = tmp)
            Button(onClick = {
                onFilter(filter.copy(tag = tmp.tag, filter = tmp.filter, category = tmp.category))
                scope.launch { sheetState.hide() }
                    .invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            onClose()
                        }
                    }
            }) {
                Text("Confirm")
            }
        }
    }
}

@Composable
private fun AllFilterMenu(
    filter: QTListParams,
    tmp: QTFilters,
    categories: QTCategories = emptyMap(),
    tags: List<String> = emptyList(),
) {
    val checkedList = remember {
        mutableStateListOf(*FilterType.entries
            .filter { it.showCondition(filter) }
            .toTypedArray()
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        MultiChoiceSegmentedButtonRow {
            FilterType.entries.forEachIndexed { index, filter ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = FilterType.entries.size
                    ),
                    colors = if (filter in checkedList)
                        SegmentedButtonDefaults.colors(MaterialTheme.colorScheme.primaryContainer)
                    else
                        SegmentedButtonDefaults.colors(),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = filter in checkedList) {
                            Icon(
                                imageVector = filter.icon,
                                contentDescription = null,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        }
                    },
                    onCheckedChange = {
                        if (!checkedList.remove(filter)) {
                            checkedList.add(filter)
                        }
                    },
                    checked = filter in checkedList
                ) {
                    Text(filter.name)
                }
            }
        }

        checkedList.asReversed().forEach { type ->
            when (type) {
                FilterType.Status -> StatusFilterMenu()
                FilterType.Category -> CategoryFilterMenu(filter, tmp, categories)
                FilterType.Tag -> TagFilterMenu(filter, tags) { tmp.tag = it }
            }
        }
    }
}

@Composable
private fun StatusFilterMenu() {
    val radioOptions = listOf("Active", "Downloading", "Seeding", "Paused", "Completed")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    Text(
        "Status",
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        color = MaterialTheme.colorScheme.outline
    )
    Column(modifier = Modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .selectable(
                        selected = (text == selectedOption),
                        onClick = { onOptionSelected(text) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (text == selectedOption),
                    onClick = null // null recommended for accessibility with screenreaders
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterMenu(
    filter: QTListParams,
    tmp: QTFilters,
    categories: QTCategories
) {
    var selected by remember { mutableStateOf(filter.category) }

    HorizontalDivider(Modifier.padding(vertical = 5.dp))
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
        categories.map { it.key }.fastForEach {
            FilterChip(
                selected = it == selected,
                onClick = {
                    selected = if (it == selected) null else it
                    tmp.category = selected
                },
                label = { Text(it) },
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .align(alignment = Alignment.CenterVertically),
                leadingIcon = {
                    if (it == selected)
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = it,
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
    filter: QTListParams,
    tags: List<String> = emptyList(),
    onUpdate: (String?) -> Unit
) {
    var selected by remember { mutableStateOf(filter.tag) }

    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    Text(
        "Tag",
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        color = MaterialTheme.colorScheme.outline
    )
    FlowRow(
        Modifier
            .fillMaxWidth(1f)
            .wrapContentHeight(align = Alignment.Top),
        horizontalArrangement = Arrangement.Start,
    ) {
        tags.fastForEach {
            FilterChip(
                selected = it == selected,
                onClick = {
                    selected = if (it == selected) null else it
                    onUpdate(selected)
                },
                label = { Text(it) },
                modifier = Modifier
                    .padding(horizontal = 5.dp)
                    .align(alignment = Alignment.CenterVertically),
                leadingIcon = {
                    if (it == selected)
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = it,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AllFilterMenuPreview() {
    AllFilterMenu(
        QTListParams(filter = "downloading", category = "cat1", tag = "tagA"),
        QTFilters(category = "cat1", tag = "tagA"),
        categories = listOf(QTCategory("cat1"), QTCategory("cat2")).associateBy { it.name },
        tags = listOf("tag1", "tagA")
    )
}