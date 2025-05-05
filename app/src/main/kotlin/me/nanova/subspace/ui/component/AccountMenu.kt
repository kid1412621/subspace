package me.nanova.subspace.ui.component

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
    onAccountEditing: (Account) -> Unit = {},
    onAccountDeleting: (Account) -> Unit = {},
) {
    val lazyColumnState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LazyColumn(
        state = lazyColumnState,
        modifier = Modifier.fillMaxWidth(),
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
                        Icon(Icons.Rounded.Add, contentDescription = "add new server")
                    }
                }
            }
            HorizontalDivider()
        }

        items(accounts, key = { it.id }) {
            val isCurrent = it.isCurrent(currentAccountId)
            var showOverlay by remember { mutableStateOf(false) }

            NavigationDrawerItem(
                label = {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp) // Fixed height for the animation container
                        ) {
                            NavItemInfoLabels(
                                !showOverlay, isCurrent, it,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )

                            NavItemActionButtons(
                                showOverlay,
                                onDismiss = { showOverlay = false },
                                onEditing = { onAccountEditing(it) },
                                onDeleting = { onAccountDeleting(it) },
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                        Icon(
                            Icons.Outlined.MoreVert,
                            "Actions on account",
                            modifier = Modifier.clickable { showOverlay = !showOverlay }
                        )
                    }
                },
                icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = it.type.toMonoIcon()),
                        contentDescription = it.user,
                        modifier = Modifier.size(43.dp),
                        tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                    )
                },
                selected = isCurrent,
                modifier = Modifier
                    .padding(10.dp, 15.dp)
                    .fillMaxWidth(),
                onClick = { onAccountSelected(it) },
            )
            HorizontalDivider()
        }
    }
}

private fun Account.isCurrent(id: Long?) = id?.equals(this.id) == true

@Composable
private fun NavItemInfoLabels(
    showLabel: Boolean,
    isCurrent: Boolean,
    account: Account,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showLabel,
        enter = fadeIn() + slideInVertically { -it },
        exit = fadeOut() + slideOutVertically { -it },
        modifier = modifier
    ) {
        Column {
            Text(
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                text = account.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                text = "${account.user}@${account.url}",
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}

@Composable
private fun NavItemActionButtons(
    showOverlay: Boolean,
    onDismiss: () -> Unit,
    onEditing: () -> Unit,
    onDeleting: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = showOverlay,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it },
        modifier = modifier
    ) {

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            FilledIconButton(
                onClick = {
                    onEditing()
                    onDismiss()
                }
            ) {
                Icon(Icons.Outlined.Edit, "Edit account")
            }
            FilledIconButton(
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                onClick = {
                    onDeleting()
                    onDismiss()
                }
            ) {
                Icon(Icons.Outlined.Delete, "Delete account")
            }
            FilledIconButton(
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                onClick = { onDismiss() }
            ) {
                Icon(Icons.Outlined.PushPin, "Pin account on the top")
            }
        }
    }

    if (showOverlay) {
        BackHandler {
            onDismiss()
        }
    }
}

@Composable
@Preview
fun AccountMenuPrev() {
    val accounts = mutableListOf(
        Account(
            id = 1,
            type = AccountType.QBITTORENT,
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
            url = "qb.host.com",
            user = "user",
            pass = "secret",
            created = 123412
        ),
    )
    accounts += (3..12).map {
        Account(
            id = it.toLong(),
            type = AccountType.QBITTORENT,
            name = "name",
            url = "host",
            user = "user",
            pass = "secret",
            created = 243
        )
    }

    AccountMenu(currentAccountId = 1, accounts = accounts)
}