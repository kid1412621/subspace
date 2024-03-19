package me.nanova.subspace.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.domain.model.Account

@Composable
fun Settings(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavHostController,
) {

    var host by rememberSaveable { mutableStateOf("") }
    var user by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TextField(
            value = host,
            onValueChange = { host = it },
            label = { Text("Host") },
            singleLine = true,
            placeholder = { Text("http://host") }
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
                    url = host,
                    user = user,
                    pass = password,
                    type = AccountType.QT
                )
            )
            navController.navigate(Routes.Home.name)
        }) {
            Text("Submit")
        }
    }
}
