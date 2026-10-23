package me.nanova.subspace.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.nanova.subspace.exception.GlobalExceptionHandler
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.theme.Theme

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val errorDetails = intent.getStringExtra(GlobalExceptionHandler.EXTRA_ERROR_DETAILS)
            ?: "No error details provided."

        setContent {
            Theme {
                ErrorScreen(
                    errorDetails = errorDetails,
                    onRestartClick = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ErrorScreen(errorDetails: String, onRestartClick: () -> Unit) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Application Error",
                        style = MaterialTheme.typography.titleLargeEmphasized
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues)
                .padding(MdSpacing.sm)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Oops! Something went wrong.",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = MdSpacing.sm)
            )
            Text(
                text = "We're sorry for the inconvenience. The application encountered an unexpected error.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = MdSpacing.xs)
            )

            Spacer(modifier = Modifier.height(MdSpacing.sm))

            Text(
                text = "Error Details",
                style = MaterialTheme.typography.titleMediumEmphasized,
                modifier = Modifier.padding(bottom = MdSpacing.xs)
            )
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Limit height and make it scrollable
                    .padding(vertical = MdSpacing.xs),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
            ) {
                Text(
                    text = errorDetails,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .verticalScroll(rememberScrollState()) // Inner scroll for details if too long
                        .padding(MdSpacing.sm) // Padding inside the details box
                )
            }

            Spacer(modifier = Modifier.height(MdSpacing.md))

            Button(
                onClick = onRestartClick,
                shapes = ButtonDefaults.shapes()
            ) {
                Text("Restart App")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    Theme {
        ErrorScreen(
            errorDetails = """java.lang.RuntimeException: This is a sample crash report.
                |at com.example.MainActivity.onCreate(MainActivity.java:25)
                |at com.example.MainActivity.onCreate(MainActivity.java:25)
                |at com.example.MainActivity.onCreate(MainActivity.java:25)
                |at com.example.MainActivity.onCreate(MainActivity.java:25)
                |...""".trimMargin(),
            onRestartClick = {}
        )
    }
}
