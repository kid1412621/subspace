package me.nanova.subspace.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
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

    var name by rememberSaveable { mutableStateOf("") }
    var host by rememberSaveable { mutableStateOf("") }
    var user by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

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
        Surface(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize(),
            tonalElevation = 1.dp
        ) {
            Column(
//                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalArrangement = Arrangement.spacedBy(26.dp),
            ) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    placeholder = { Text("Server name") },
                )
                TextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text("Host") },
                    singleLine = true,
                    placeholder = { Text("http://host") },
                )

                TextField(
                    value = user,
                    onValueChange = { user = it },
                    label = { Text("User") },
                    singleLine = true,
                    placeholder = { Text("Your username") }
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    singleLine = true,
                    placeholder = { Text("Your password") },
                    visualTransformation = PasswordVisualTransformation()
                )

                Button(onClick = {
                    viewModel.saveAccount(
                        Account(
                            name = name,
                            url = host,
                            user = user,
                            pass = password,
                            type = AccountType.QT
                        )
                    )

                    if (added) {
                        navController.navigate(Routes.Home.name)
                    }
                }) {
                    Text("Submit")
                }
            }
        }
    }
}
