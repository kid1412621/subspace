package me.nanova.subspace.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.nanova.subspace.exception.GlobalExceptionHandler
import me.nanova.subspace.ui.theme.Theme

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ErrorScreen(errorDetails: String, onRestartClick: () -> Unit) {
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Application Error") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Oops! Something went wrong.",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "We're sorry for the inconvenience. The application encountered an unexpected error.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Error Details:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = errorDetails,
                style = TextStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp) // Limit height and make it scrollable
                    .padding(vertical = 8.dp)
                    .verticalScroll(rememberScrollState()) // Inner scroll for details if too long
                    .padding(8.dp) // Padding inside the details box
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))

            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = onRestartClick) {
                Text("Restart App")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    MaterialTheme {
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
