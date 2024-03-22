package me.nanova.subspace.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.nanova.subspace.R
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.domain.model.Account

@Composable
fun AccountMenu(
    currentAccountId: Long?,
    accounts: List<Account> = emptyList(),
    onAccountSelected: (Account) -> Unit = {},
    onAccountAdding: () -> Unit = {},
) {
    ModalDrawerSheet {
        Row(horizontalArrangement = Arrangement.Absolute.SpaceBetween) {
            Text("Server", modifier = Modifier.padding(16.dp))

            IconButton(onClick = { onAccountAdding }) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "filter"
                )
            }
        }

        HorizontalDivider()

        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            accounts.forEach {
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                text = it.url,
                                style = MaterialTheme.typography.labelSmall,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_qt),
                            contentDescription = it.user,
                            modifier = Modifier.size(30.dp)
                        )
                    },
//                    modifier = Modifier.height(100.dp),
                    selected = currentAccountId?.equals(it.id) ?: false,
                    onClick = { onAccountSelected(it) }
                )

                HorizontalDivider()
            }
        }
    }
}

@Composable
@Preview
fun AccountMenuPrev() {
    val accounts = listOf(
        Account(
            id = 1,
            type = AccountType.QT,
            name = "server name",
            url = "host",
            user = "user",
            pass = "secret",
            created = 123412
        ),
        Account(
            id = 2,
            type = AccountType.QT,
            name = "server name 2",
            url = "host",
            user = "user",
            pass = "secret",
            created = 123412
        )
    );
    AccountMenu(currentAccountId = 1, accounts = accounts)
}
