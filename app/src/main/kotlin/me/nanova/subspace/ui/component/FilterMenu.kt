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
import me.nanova.subspace.domain.model.FilterState
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.QBCategory
import me.nanova.subspace.domain.model.QBListParams

private enum class FilterType(val icon: ImageVector, val showCondition: (QBListParams) -> Boolean) {
    Status(Icons.Filled.QuestionMark, { it.filter != "all" }),
    Category(Icons.Filled.Category, { it.category != null }),
    Tag(Icons.AutoMirrored.Filled.Label, { it.tag != null });
}

private data class QBFilters(
    var filter: String = "all",
    var category: String? = null,
    var tag: String? = null,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterMenu(
    filter: QBListParams,
    categories: QBCategories,
    tags: List<String>,
    onClose: () -> Unit = {},
    onFilter: (QBListParams) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val tmp = QBFilters(filter = filter.filter, tag = filter.tag, category = filter.category)
    val checkedList = remember {
        mutableStateListOf(
            *FilterType.entries
                .filter { it.showCondition(filter) }
                .toTypedArray()
        )
    }

    ModalBottomSheet(
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
                    FilterType.Status -> StatusFilterMenu(filter.filter) { tmp.filter = it }
                    FilterType.Category -> CategoryFilterMenu(filter.category, categories) {
                        tmp.category = it
                    }

                    FilterType.Tag -> TagFilterMenu(filter.tag, tags) { tmp.tag = it }
                }
            }

            Button(
                onClick = {
                    onFilter(
                        filter.copy(
                            filter = tmp.filter,
                            tag = if (checkedList.contains(FilterType.Tag)) tmp.tag else null,
                            category = if (checkedList.contains(FilterType.Category)) tmp.category else null
                        )
                    )
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

@Composable
private fun StatusFilterMenu(
    initial: String,
    onUpdate: (String) -> Unit = {}
) {
    val radioOptions = FilterState.entries.map { it.toString() }
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(initial) }

    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    Text(
        "Status",
        fontSize = MaterialTheme.typography.labelSmall.fontSize,
        color = MaterialTheme.colorScheme.outline
    )
    Column(modifier = Modifier.selectableGroup()) {
        radioOptions.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowOptions.forEach { option ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .selectable(
                                selected = (option == selectedOption),
                                onClick = {
                                    onOptionSelected(option)
                                    onUpdate(option)
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = (option == selectedOption),
                            onClick = null // for accessibility
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                // Fill the empty space if items are odd-numbered
                if (rowOptions.size < 2) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CategoryFilterMenu(
    initial: String?,
    categories: QBCategories,
    onUpdate: (String?) -> Unit = {}
) {
    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    if (categories.isEmpty()) {
        Text(
            "No available category",
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            color = MaterialTheme.colorScheme.outline
        )
        return
    }

    var selected by remember { mutableStateOf(initial) }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagFilterMenu(
    initial: String?,
    tags: List<String> = emptyList(),
    onUpdate: (String?) -> Unit = {}
) {
    HorizontalDivider(Modifier.padding(vertical = 5.dp))
    if (tags.isEmpty()) {
        Text(
            "No available tag",
            fontSize = MaterialTheme.typography.labelSmall.fontSize,
            color = MaterialTheme.colorScheme.outline
        )
        return
    }

    var selected by remember { mutableStateOf(initial) }
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
fun StatusFilterMenuPreview() {
    StatusFilterMenu(
        initial = "downloading",
    )
}

@Preview(showBackground = true)
@Composable
fun CategoryFilterMenuPreview() {
    CategoryFilterMenu(
        initial = "cat1",
        categories = listOf(QBCategory("cat1"), QBCategory("cat2")).associateBy { it.name },
    )
}

@Preview(showBackground = true)
@Composable
fun TagFilterMenuPreview() {
    TagFilterMenu(
        initial = "tagA",
        tags = listOf("tagA", "tagC")
    )
}
