package me.nanova.subspace.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import java.util.Locale

@Composable
fun ValidTextField(
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
            touched = true
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
                focusManager.moveFocus(FocusDirection.Down)
            },
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .onFocusChanged { focusState ->
                if (touched && !focusState.isFocused) {
                    errorMsg = validate(required, fieldValue, label, validations)
                    onValidation(errorMsg.isBlank())
                }
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
