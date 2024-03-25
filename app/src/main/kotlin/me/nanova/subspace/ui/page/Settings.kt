package me.nanova.subspace.ui.page

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Password
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.ui.Routes
import me.nanova.subspace.ui.vm.SettingsViewModel

@Composable
fun Settings(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarMessage by viewModel.snackbarMessage.collectAsState()
    val added by viewModel.added.collectAsState()


    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            val result = snackbarHostState.showSnackbar(it)
            if (result == SnackbarResult.Dismissed) {
                viewModel.snackbarMessage.value = null
            }
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
    ) { contentPadding ->
        AccountForm(
            onSubmit = {
                viewModel.saveAccount(it)

                if (added) {
                    navController.navigate(Routes.Home.name)
                }
            }
        )
    }
}

@Composable
private fun AccountForm(
//    modifier: Modifier = Modifier.fillMaxWidth(),
//    contentPadding: PaddingValues,
    onSubmit: (Account) -> Unit = {},
) {
    var account by remember { mutableStateOf(Account(type = AccountType.QT)) }
    var submitting by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
//            .padding(contentPadding)
            .fillMaxWidth(),
//            tonalElevation = 1.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 10.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 55.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(50.dp, 0.dp)
            ) {
                items(AccountType.entries.toTypedArray()) {
                    Image(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { account = account.copy(type = it) },
                        alignment = Alignment.Center,
                        painter = painterResource(id = it.toIcon()),
                        contentDescription = it.name,
                        colorFilter = if (it != account.type) ColorFilter
                            .colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        else null
                    )
                }
            }

            TextField(
                value = account.name,
                onValueChange = { account = account.copy(name = it) },
                label = { Text("Name") },
                singleLine = true,
                placeholder = { Text("Server name") },
                leadingIcon = { Icon(Icons.Filled.Abc, contentDescription = "name") }
            )
            TextField(
                value = account.url,
                onValueChange = { account = account.copy(url = it) },
                label = { Text("Host") },
                singleLine = true,
                placeholder = { Text("http(s)://host:port/path") },
                leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = "host") }
            )
            TextField(
                value = account.user,
                onValueChange = { account = account.copy(user = it) },
                label = { Text("User") },
                singleLine = true,
                placeholder = { Text("Your username") },
                leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = "user") }
            )
            TextField(
                value = account.pass,
                onValueChange = { account = account.copy(pass = it) },
                label = { Text("Password") },
                singleLine = true,
                placeholder = { Text("Your password") },
                visualTransformation = PasswordVisualTransformation(),
                leadingIcon = { Icon(Icons.Filled.Password, contentDescription = "password") }
            )

            Button(
                enabled = !submitting,
                onClick = {
                    submitting = true
                    onSubmit(account)
                    submitting = false
                }) {
                if (submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp))
                } else {
                    Text("Submit")
                }
            }
        }
    }
}

@Composable
@Preview
fun NewAccountFormPrev() {
    AccountForm()
}
