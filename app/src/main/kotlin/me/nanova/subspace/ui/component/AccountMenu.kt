package me.nanova.subspace.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AccountType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AccountMenu(
    currentAccountId: Long?,
    accounts: List<Account> = emptyList(),
    onAccountSelected: (Account) -> Unit = {},
    onAccountAdding: () -> Unit = {},
) {

    val lazyColumnState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        state = lazyColumnState,
        modifier = Modifier.fillMaxWidth(),
//            verticalArrangement = Arrangement.spacedBy(15.dp),
    ) {
        stickyHeader {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        lazyColumnState.animateScrollToItem(0)
                    }
                }
            ) {
                Row(
                    modifier = Modifier.padding(7.dp, 0.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Servers",
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.titleLarge,
                    )

                    FilledIconButton(
                        onClick = { onAccountAdding() },
                        modifier = Modifier.padding(10.dp),
                    ) {
                        Icon(
                            Icons.Rounded.Add,
                            contentDescription = "add new server"
                        )
                    }
                }
            }

            HorizontalDivider()
        }

        items(accounts, key = { it.id }) {
            NavigationDrawerItem(
                label = {
                    Column {
                        Text(
                            color = if (it.isCurrent(currentAccountId)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            text = it.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            color = if (it.isCurrent(currentAccountId)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                            text = "${it.user}@${it.url}",
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(0.dp, 2.dp)
                        )
                    }
                },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = it.type.toMonoIcon()),
                        contentDescription = it.user,
                        modifier = Modifier.size(33.dp),
                        tint = if (it.isCurrent(currentAccountId)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                },
                modifier = Modifier.padding(10.dp, 15.dp),
                selected = it.isCurrent(currentAccountId),
                onClick = { onAccountSelected(it) }
            )

            HorizontalDivider()
        }
    }
}

private fun Account.isCurrent(id: Long?) = id?.equals(this.id) ?: false

@Composable
@Preview
fun AccountMenuPrev() {
    val accounts = mutableListOf(
        Account(
            id = 1,
            type = AccountType.QT,
            name = "server name toooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo long",
            url = "host",
            user = "user",
            pass = "secret",
            created = 123412
        ),
        Account(
            id = 2,
            type = AccountType.TRANSMISSION,
            name = "server name 2",
            url = "qt.host.com",
            user = "user",
            pass = "secret",
            created = 123412
        ),
    )
    accounts += (3..12).toList().map {
        Account(
            id = it.toLong(),
            type = AccountType.QT,
            name = "name",
            url = "host",
            user = "user",
            pass = "secret",
            created = 243
        )
    }

    AccountMenu(currentAccountId = 1, accounts = accounts)
}
