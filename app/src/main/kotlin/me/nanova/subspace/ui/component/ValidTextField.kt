package me.nanova.subspace.ui.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalComposeUiApi::class)
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
    onValidation: (Boolean) -> Unit = { }
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // State to hold the error message
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Validation function
    fun validate(input: String): Boolean {
        // Check if the field is required and empty
        if (required && input.isBlank()) {
            errorMsg = "$label is required"
            return false
        }
        // Apply custom validation rules
        validations.forEach { (rule, message) ->
            if (!rule(input)) {
                errorMsg = message
                return false
            }
        }
        // If all checks pass, clear the error
        errorMsg = null
        return true
    }

    // Validate on initial render and when value changes
    LaunchedEffect(value) {
        val isValid = validate(value)
        onValidation(isValid)
    }

    // Render the TextField
    TextField(
        value = value,
        onValueChange = { newValue ->
            onChanged(newValue) // Update parent state
            val isValid = validate(newValue) // Validate new input
            onValidation(isValid) // Notify parent of validity
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        visualTransformation = visualTransformation,
        supportingText = { if (errorMsg != null) Text(errorMsg!!) }, // Display error message
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = if (last) ImeAction.Done else ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                focusManager.moveFocus(FocusDirection.Down) // For software keyboard "Next"
            },
            onDone = {
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        isError = errorMsg != null, // Show error state
        modifier = modifier
            .onPreviewKeyEvent { keyEvent ->
                // Handle Tab key press for focus navigation
                if (keyEvent.type == KeyEventType.KeyDown && keyEvent.key == Key.Tab) {
                    focusManager.moveFocus(if (keyEvent.isShiftPressed) FocusDirection.Up else FocusDirection.Down)
                    return@onPreviewKeyEvent true // Consume the event
                }
                false // Do not consume other key events
            }
    )
}
