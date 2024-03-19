package me.nanova.subspace.ui.component


import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun BlankAccount(
    onGoSetting: () -> Unit = {}
) {
    Box(
    ) {
        Text("No account available...")
        Button(onClick = { onGoSetting() }) {
            Text("Go to add an account")
        }

    }
}