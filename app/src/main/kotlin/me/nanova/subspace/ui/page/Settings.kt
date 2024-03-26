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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.TextField
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
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import me.nanova.subspace.data.AccountType
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.ui.Routes
import me.nanova.subspace.ui.vm.SettingsViewModel
import java.util.Locale

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
    val validations by remember { mutableStateOf( mutableMapOf<String, Boolean>()) }
    var submitting by rememberSaveable { mutableStateOf(false) }
    var passwordHidden by rememberSaveable { mutableStateOf(true) }

    Surface(
        modifier = Modifier
//            .padding(contentPadding)
            .fillMaxWidth(),
//            tonalElevation = 1.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp, 35.dp),
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

            ValidTextField(
                label = "Name",
                value = account.name,
                leadingIcon = { Icon(Icons.Filled.Abc, contentDescription = "name") },
                placeholder = "Server Name",
                keyboardType = KeyboardType.Uri,
                onChanged = { account = account.copy(name = it) },
                onValidation = { validations["name"] = it }
            )
            ValidTextField(
                label = "host",
                value = account.url,
                placeholder = "http(s)://host:port/path",
                leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = "host") },
                onChanged = { account = account.copy(url = it) },
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
                onChanged = { account = account.copy(name = it) },
                onValidation = { validations["user"] = it }
            )
            ValidTextField(
                label = "password",
                value = account.pass,
                onChanged = { account = account.copy(pass = it) },
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
                enabled = !submitting && validations.values.all { it },
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
private fun ValidTextField(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    required: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    last: Boolean = false,
    onChanged: (String) -> Unit,
    validations: Map<(String) -> Boolean, String> = mapOf(),
    onValidation: (Boolean) -> Unit = { },
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    var fieldValue by remember { mutableStateOf(value) }
    var errorMsg by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }

    TextField(
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            onChanged(it)
        },
        label = { Text(label.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }) },
        isError = touched && required && errorMsg.isNotBlank(),
        singleLine = true,
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        supportingText = { if (errorMsg.isNotBlank()) Text(errorMsg) },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (last) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                if (!last) {
                    focusManager.moveFocus(FocusDirection.Down)
                    errorMsg = validate(required, fieldValue, label, validations)
                    onValidation(errorMsg.isNotBlank())
                }
            },
            onDone = {
                if (last) {
                    errorMsg = validate(required, fieldValue, label, validations)
                    onValidation(errorMsg.isNotBlank())
                    keyboardController?.hide()
                }
            }
        ),
        modifier = modifier
            .onFocusChanged { focusState ->
                touched = focusState.isFocused != true
            }
    )
}

private fun validate(
    required: Boolean,
    value: String,
    label: String,
    validations: Map<(String) -> Boolean, String>,
): String {
    var errorMsg = ""
    if (required && value.isBlank()) errorMsg = "$label is required."
    if (validations.isNotEmpty()) {
        errorMsg += validations
            .filter { (predicate, _) -> !predicate(value) }
            .map { (_, errorMessage) -> errorMessage }
            .joinToString()
    }

    return errorMsg
}


@Composable
@Preview
fun NewAccountFormPrev() {
    AccountForm()
}
