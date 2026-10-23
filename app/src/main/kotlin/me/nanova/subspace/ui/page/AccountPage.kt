package me.nanova.subspace.ui.page

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import me.nanova.subspace.domain.model.Account
import me.nanova.subspace.domain.model.AccountType
import me.nanova.subspace.ui.Navigator
import me.nanova.subspace.ui.component.ValidTextField
import me.nanova.subspace.ui.mapper.toIcon
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.theme.Theme
import me.nanova.subspace.ui.vm.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountPage(
    id: Long? = null,
    viewModel: AccountViewModel = hiltViewModel(),
    navigator: Navigator,
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
            navigator.goBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                navigationIcon = {
                    IconButton(
                        onClick = { navigator.goBack() },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Text(
                        text = if (id == null) "Add Server" else "Edit Server",
                        style = MaterialTheme.typography.titleLargeEmphasized
                    )
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        },
    ) { contentPadding ->
        AccountForm(
            modifier = Modifier
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding),
            loading,
            account,
            onUpdateAccount = { viewModel.updateAccount(it) },
            onSubmitAccount = { viewModel.saveAccount(it, id == null) })
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        modifier = modifier.fillMaxSize(),
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MdSpacing.md, vertical = MdSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Server Type",
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Start)
                )
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(MdSpacing.xs),
                    verticalArrangement = Arrangement.spacedBy(MdSpacing.xs),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp)
                ) {
                    items(AccountType.entries.toTypedArray()) { type ->
                        val selected = type == account.type
                        Surface(
                            selected = selected,
                            onClick = { onUpdateAccount(account.copy(type = type)) },
                            shape = if (selected) MaterialTheme.shapes.largeIncreased else MaterialTheme.shapes.medium,
                            color = if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                            border = if (selected) BorderStroke(
                                MdSpacing.micro,
                                MaterialTheme.colorScheme.primary
                            ) else null,
                        ) {
                            Column(
                                modifier = Modifier.padding(MdSpacing.xs),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(MdSpacing.xxs)
                            ) {
                                Image(
                                    modifier = Modifier.size(40.dp),
                                    alignment = Alignment.Center,
                                    painter = painterResource(id = type.toIcon()),
                                    contentDescription = type.name,
                                    colorFilter = if (!selected) ColorFilter
                                        .colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                                    else null
                                )
                                Text(
                                    text = type.name.lowercase()
                                        .replaceFirstChar { it.uppercase() },
                                    style = if (selected) MaterialTheme.typography.labelSmallEmphasized else MaterialTheme.typography.labelSmall,
                                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MdSpacing.xxs))

                ValidTextField(
                    label = "Name",
                    value = account.name,
                    leadingIcon = { Icon(Icons.Filled.Abc, contentDescription = null) },
                    placeholder = "Server Name",
                    onChanged = { onUpdateAccount(account.copy(name = it)) },
                    onValidation = { validations["name"] = it }
                )
                ValidTextField(
                    label = "Host",
                    value = account.url,
                    placeholder = "http(s)://host:port/path",
                    keyboardType = KeyboardType.Uri,
                    leadingIcon = { Icon(Icons.Filled.Dns, contentDescription = null) },
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
                    label = "User",
                    value = account.user,
                    placeholder = "Server account username",
                    leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                    onChanged = { onUpdateAccount(account.copy(user = it)) },
                    onValidation = { validations["user"] = it }
                )
                ValidTextField(
                    label = "Password",
                    value = account.pass,
                    onChanged = { onUpdateAccount(account.copy(pass = it)) },
                    placeholder = "Server account password",
                    leadingIcon = { Icon(Icons.Filled.Password, contentDescription = null) },
                last = true,
                keyboardType = KeyboardType.Password,
                trailingIcon = {
                    IconButton(
                        onClick = { passwordHidden = !passwordHidden },
                        shapes = IconButtonDefaults.shapes(),
                    ) {
                        Icon(
                            imageVector = if (passwordHidden) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = if (passwordHidden) "Show password" else "Hide password"
                        )
                    }
                },
                visualTransformation = if (passwordHidden) PasswordVisualTransformation() else VisualTransformation.None,
                onValidation = { validations["pass"] = it }
            )

                Spacer(modifier = Modifier.height(MdSpacing.xs))

                Button(
                enabled = !loading && validations.isNotEmpty() && validations.values.all { it },
                    shapes = ButtonDefaults.shapes(),
                    modifier = Modifier.fillMaxWidth(),
                onClick = {
                    focusManager.clearFocus()
                    onSubmitAccount(account)
                }
            ) {
                if (loading) {
                    LoadingIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(
                        text = "Submit",
                        style = MaterialTheme.typography.labelLargeEmphasized
                    )
                }
                }
            }
        }
    }
}


@Composable
@Preview(showBackground = true)
fun NewAccountFormPrev() {
    Theme {
        AccountForm()
    }
}
