package me.nanova.subspace.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDropUp
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import me.nanova.subspace.domain.model.QTListParams
import me.nanova.subspace.util.toCamelCase

@Composable
fun SortMenu(
    filter: QTListParams,
    onClose: () -> Unit = {},
    onSort: (QTListParams) -> Unit = {},
    onReset: () -> Unit = {},
) {
    fun handleSortSelection(label: String) {
        onSort(
            filter.copy(
                sort = label,
                reverse = if (label == filter.sort) !filter.reverse else filter.reverse
            )
        )
        onClose()
    }

    DropdownMenu(
        expanded = true,
        onDismissRequest = { onClose() }
    ) {
        val sortItems = listOf(
            "name" to SortItem(icon = Icons.Rounded.SortByAlpha),
            "added_on" to SortItem(icon = Icons.Rounded.AccessTime),
            "dlspeed" to SortItem("Download Speed", Icons.Rounded.Speed)
        )

        sortItems.forEach { item ->
            SortMenuItem(
                label = item.first,
                detail = item.second,
                sort = filter.sort,
                reverse = filter.reverse,
                onSelect = {
                    handleSortSelection(item.first)
                })
        }
    }

}

private data class SortItem(val text: String? = null, val icon: ImageVector)

@Composable
private fun SortMenuItem(
    label: String,
    detail: SortItem,
    sort: String?,
    reverse: Boolean,
    onSelect: (String) -> Unit
) {
    val selected = sort == label

    DropdownMenuItem(
        text = { Text(text = detail.text ?: label.toCamelCase()) },
        colors = if (selected)
            MenuDefaults.itemColors(MaterialTheme.colorScheme.primary)
        else MenuDefaults.itemColors(),
        leadingIcon = {
            Icon(
                detail.icon,
                contentDescription = "Sort by $label"
            )
        },
        trailingIcon = {
            if (selected) {
                Icon(
                    if (reverse) Icons.Rounded.ArrowDropDown else Icons.Rounded.ArrowDropUp,
                    contentDescription = if (reverse) "Descending" else "Ascending"
                )
            }
        },
        onClick = {
            onSelect(label)
        })
}
