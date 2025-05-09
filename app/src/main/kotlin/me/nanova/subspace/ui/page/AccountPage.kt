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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.ui.Routes
import me.nanova.subspace.ui.component.ValidTextField
import me.nanova.subspace.ui.vm.AccountViewModel

@Composable
fun AccountPage(
    id: Long? = null,
    viewModel: AccountViewModel = hiltViewModel(),
    navController: NavHostController,
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val snackBarMessage by viewModel.snackBarMessage.collectAsState()
    val loading by viewModel.loading.collectAsState()
    val account by viewModel.account.collectAsState()
    val added by viewModel.submitted.collectAsState()

    LaunchedEffect(id) {
        id?.let { viewModel.initData(it) }
    }

    LaunchedEffect(snackBarMessage) {
        snackBarMessage?.let {
            val result = snackBarHostState.showSnackbar(it)
            if (result == SnackbarResult.Dismissed) {
                viewModel.snackBarMessage.value = null
            }
        }
    }

    LaunchedEffect(added) {
        if (added) {
            navController.navigate(Routes.Home.name)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
    ) { contentPadding ->
        AccountForm(
            modifier = Modifier.padding(contentPadding),
            loading,
            account,
            onUpdateAccount = { viewModel.updateAccount(it) },
            onSubmitAccount = { viewModel.saveAccount(it, id == null) })
    }
}

@Composable
private fun AccountForm(
    modifier: Modifier = Modifier,
    loading: Boolean = false,
    account: Account = Account(type = AccountType.QBITTORENT),
    onUpdateAccount: (Account) -> Unit = {},
    onSubmitAccount: (Account) -> Unit = {},
) {

    val focusManager = LocalFocusManager.current

    val validations = remember { mutableStateMapOf<String, Boolean>() }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    Surface(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 30.dp),
            verticalArrangement = Arrangement.spacedBy(26.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 55.dp),
                horizontalArrangement = Arrangement.spacedBy(15.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier
                    .fillMaxWidth(fraction = 0.8f)
                    .padding(bottom = 10.dp)
            ) {
                items(AccountType.entries.toTypedArray()) {
                    Image(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable { onUpdateAccount(account.copy(type = it)) },
                        alignment = Alignment.Center,
                        painter = painterResource(id = it.toIcon()),
                        contentDescription = it.name,
                        colorFilter = if (it != account.type) ColorFilter
                            .colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                        else null
                    )
                }
            }

            ValidTextField(
                label = "Name",
                value = account.name,
                leadingIcon = { Icon(Icons.Filled.Abc, contentDescription = "name") },
                placeholder = "Server Name",
                onChanged = { onUpdateAccount(account.copy(name = it)) },
                onValidation = { validations["name"] = it }
            )
            ValidTextField(
                label = "host",
                value = account.url,
                placeholder = "http(s)://host:port/path",
                keyboardType = KeyboardType.Uri,
                leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = "host") },
                onChanged = { onUpdateAccount(account.copy(url = it)) },
                validations = mapOf(
                    { it: String ->
                        """\bhttps?://\S+""".toRegex(RegexOption.IGNORE_CASE).matches(it)
                    }
                            to "URL should start with http:// or https://"
                ),
                onValidation = { validations["host"] = it }
            )
            ValidTextField(
                label = "user",
                value = account.user,
                placeholder = "Server account username",
                leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = "user") },
                onChanged = { onUpdateAccount(account.copy(user = it)) },
                onValidation = { validations["user"] = it }
            )
            ValidTextField(
                label = "password",
                value = account.pass,
                onChanged = { onUpdateAccount(account.copy(pass = it)) },
                placeholder = "Server account password",
                leadingIcon = { Icon(Icons.Filled.Password, contentDescription = "password") },
                last = true,
                keyboardType = KeyboardType.Password,
                trailingIcon = {
                    IconButton(onClick = { passwordHidden = !passwordHidden }) {
                        Icon(
                            imageVector = if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordHidden) "Show password" else "Hide password"
                        )
                    }
                },
                visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                onValidation = { validations["pass"] = it }
            )

            Button(
                enabled = !loading && validations.isNotEmpty() && validations.values.all { it },
                onClick = {
                    focusManager.clearFocus()
                    onSubmitAccount(account)
                }
            ) {
                if (loading) {
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
