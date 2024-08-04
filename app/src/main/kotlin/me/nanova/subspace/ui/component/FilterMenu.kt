package me.nanova.subspace.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.QTListParams

private enum class Filters {
    Status, Category, Tag;
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterMenu(
    filter: QTListParams,
    onClose: () -> Unit = {},
    onFilter: (QTListParams) -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = {
            onClose()
        },
        sheetState = sheetState
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
        ) {
            AllFilterMenu()
            Button(
                onClick = {
//                    onFilter(filter.copy(filter = ))
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
    defaultFilters: List<Filters> = emptyList()
) {
    val options = Filters.entries.map { it.name }.toList()
    val checkedList = remember { mutableStateListOf(*defaultFilters.toTypedArray()) }
    val icons =
        listOf(
            Icons.Filled.QuestionMark,
            Icons.Filled.Category,
            Icons.AutoMirrored.Filled.Label
        )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        MultiChoiceSegmentedButtonRow {
            options.forEachIndexed { index, label ->
                val filter = Filters.valueOf(label)
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size
                    ),
                    colors = if (filter in checkedList)
                        SegmentedButtonDefaults.colors(MaterialTheme.colorScheme.primaryContainer)
                    else
                        SegmentedButtonDefaults.colors(),
                    icon = {
                        SegmentedButtonDefaults.Icon(active = filter in checkedList) {
                            Icon(
                                imageVector = icons[index],
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
                    Text(label)
                }
            }
        }

        checkedList.asReversed().forEach {
            when (it) {
                Filters.Status -> StatusFilterMenu()
                Filters.Category -> CategoryFilterMenu()
                Filters.Tag -> TagFilterMenu()
            }
        }
    }
}

@Composable
private fun StatusFilterMenu() {
    val radioOptions = listOf("Active", "Downloading", "Seeding", "Paused", "Completed")
    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }

    Column(Modifier.selectableGroup()) {
        radioOptions.forEach { text ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(56.dp)
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

@Composable
private fun CategoryFilterMenu() {
    var selected by remember { mutableStateOf(false) }
    FilterChip(
        selected = selected,
        onClick = { selected = !selected },
        label = { Text("Filter chip") },
        leadingIcon =
        if (selected) {
            {
                Icon(
                    imageVector = Icons.Filled.Done,
                    contentDescription = "Localized Description",
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else {
            null
        }
    )
}

@Composable
private fun TagFilterMenu() {

}

@Preview
@Composable
fun AllFilterMenuPreview() {
    AllFilterMenu(Filters.entries)
}