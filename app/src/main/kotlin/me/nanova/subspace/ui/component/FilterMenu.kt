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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetState
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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.FilterState
import me.nanova.subspace.domain.model.QBCategories
import me.nanova.subspace.domain.model.QBCategory
import me.nanova.subspace.domain.model.QBListParams
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.theme.Theme

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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FilterMenu(
    filter: QBListParams,
    categories: QBCategories,
    tags: List<String>,
    onClose: () -> Unit = {},
    onFilter: (QBListParams) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberBottomSheetState(initialValue = SheetValue.Hidden)
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
            verticalArrangement = Arrangement.spacedBy(MdSpacing.xs),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MdSpacing.md, vertical = MdSpacing.sm)
                .animateContentSize(
                    animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec<IntSize>()
                )
        ) {
            MultiChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                FilterType.entries.forEachIndexed { index, filterType ->
                    val isChecked = filterType in checkedList
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = FilterType.entries.size
                        ),
                        colors = SegmentedButtonDefaults.colors(),
                        icon = {
                            SegmentedButtonDefaults.Icon(active = isChecked) {
                                Icon(
                                    imageVector = filterType.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                                )
                            }
                        },
                        onCheckedChange = {
                            if (!checkedList.remove(filterType)) {
                                checkedList.add(filterType)
                            }
                        },
                        checked = isChecked
                    ) {
                        Text(
                            text = filterType.name,
                            style = if (isChecked) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
                        )
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
                shapes = ButtonDefaults.shapes(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = MdSpacing.sm)
            ) {
                Text(
                    text = "Confirm",
                    style = MaterialTheme.typography.labelLargeEmphasized
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun StatusFilterMenu(
    initial: String,
    onUpdate: (String) -> Unit = {}
) {
    val radioOptions = FilterState.entries.map { it.toString() }
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(initial) }

    HorizontalDivider(
        modifier = Modifier.padding(vertical = MdSpacing.xxs),
        color = MaterialTheme.colorScheme.outlineVariant
    )
    Text(
        "Status",
        style = MaterialTheme.typography.labelMediumEmphasized,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Column(modifier = Modifier.selectableGroup()) {
        radioOptions.chunked(2).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MdSpacing.sm)
            ) {
                rowOptions.forEach { option ->
                    val selected = option == selectedOption
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .selectable(
                                selected = selected,
                                onClick = {
                                    onOptionSelected(option)
                                    onUpdate(option)
                                },
                                role = Role.RadioButton
                            )
                            .padding(horizontal = MdSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selected,
                            onClick = null // for accessibility
                        )
                        Text(
                            text = option,
                            style = if (selected) MaterialTheme.typography.bodyLargeEmphasized else MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = MdSpacing.xs)
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

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun CategoryFilterMenu(
    initial: String?,
    categories: QBCategories,
    onUpdate: (String?) -> Unit = {}
) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = MdSpacing.xxs),
        color = MaterialTheme.colorScheme.outlineVariant
    )
    if (categories.isEmpty()) {
        Text(
            "No available category",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    var selected by remember { mutableStateOf(initial) }
    Text(
        "Category",
        style = MaterialTheme.typography.labelMediumEmphasized,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    FlowRow(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
    ) {
        categories.map { it.key }.fastForEach {
            val isSelected = it == selected
            FilterChip(
                selected = isSelected,
                onClick = {
                    selected = if (isSelected) null else it
                    onUpdate(selected)
                },
                shapes = FilterChipDefaults.shapes(),
                label = {
                    Text(
                        text = it,
                        style = if (isSelected) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
                    )
                },
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                leadingIcon = {
                    if (isSelected)
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TagFilterMenu(
    initial: String?,
    tags: List<String> = emptyList(),
    onUpdate: (String?) -> Unit = {}
) {
    HorizontalDivider(
        modifier = Modifier.padding(vertical = MdSpacing.xxs),
        color = MaterialTheme.colorScheme.outlineVariant
    )
    if (tags.isEmpty()) {
        Text(
            "No available tag",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    var selected by remember { mutableStateOf(initial) }
    Text(
        "Tag",
        style = MaterialTheme.typography.labelMediumEmphasized,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    FlowRow(
        Modifier
            .fillMaxWidth()
            .wrapContentHeight(align = Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
    ) {
        tags.fastForEach {
            val isSelected = it == selected
            FilterChip(
                selected = isSelected,
                onClick = {
                    selected = if (isSelected) null else it
                    onUpdate(selected)
                },
                shapes = FilterChipDefaults.shapes(),
                label = {
                    Text(
                        text = it,
                        style = if (isSelected) MaterialTheme.typography.labelLargeEmphasized else MaterialTheme.typography.labelLarge
                    )
                },
                modifier = Modifier.align(alignment = Alignment.CenterVertically),
                leadingIcon = {
                    if (isSelected)
                        Icon(
                            imageVector = Icons.Filled.Done,
                            contentDescription = null,
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
    Theme {
        Column {
            StatusFilterMenu(
                initial = "downloading",
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryFilterMenuPreview() {
    Theme {
        Column {
            CategoryFilterMenu(
                initial = "cat1",
                categories = listOf(QBCategory("cat1"), QBCategory("cat2")).associateBy { it.name },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TagFilterMenuPreview() {
    Theme {
        Column {
            TagFilterMenu(
                initial = "tagA",
                tags = listOf("tagA", "tagC")
            )
        }
    }
}
