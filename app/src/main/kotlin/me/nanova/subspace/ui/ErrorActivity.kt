package me.nanova.subspace.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import me.nanova.subspace.BuildConfig
import me.nanova.subspace.exception.GlobalExceptionHandler
import me.nanova.subspace.ui.theme.MdSpacing
import me.nanova.subspace.ui.theme.Theme
import kotlin.time.Duration.Companion.milliseconds

class ErrorActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.isNavigationBarContrastEnforced = false

        val errorDetails = intent.getStringExtra(GlobalExceptionHandler.EXTRA_ERROR_DETAILS)
            ?: "No stack trace available."
        val errorSummary = intent.getStringExtra(GlobalExceptionHandler.EXTRA_ERROR_SUMMARY)
            ?: errorDetails.lineSequence().firstOrNull()?.takeIf { it.isNotBlank() }
            ?: "Unexpected Fatal Exception"
        val errorThread = intent.getStringExtra(GlobalExceptionHandler.EXTRA_ERROR_THREAD)
            ?: "main"

        setContent {
            Theme {
                ErrorScreen(
                    errorSummary = errorSummary,
                    errorThread = errorThread,
                    errorDetails = errorDetails,
                    onRestartClick = {
                        val restartIntent = Intent(this, MainActivity::class.java).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        }
                        startActivity(restartIntent)
                        finish()
                    },
                    onCloseClick = {
                        finishAffinity()
                    }
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(
    errorSummary: String,
    errorThread: String = "main",
    errorDetails: String,
    onRestartClick: () -> Unit,
    onCloseClick: () -> Unit = {},
) {
    val context = LocalContext.current
    var showStackTrace by rememberSaveable { mutableStateOf(false) }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(copied) {
        if (copied) {
            delay(2000L.milliseconds)
            copied = false
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                ),
                title = {
                    Text(
                        text = "Crash Report",
                        style = MaterialTheme.typography.titleLargeEmphasized
                    )
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .consumeWindowInsets(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = MdSpacing.md, vertical = MdSpacing.md),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(MdSpacing.sm)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.BugReport,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = "Something went wrong",
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Subspace encountered an unexpected fatal error and had to stop.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(MdSpacing.xxs))

                // Summary Card
                Card(
                    shape = MaterialTheme.shapes.largeIncreased,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(MdSpacing.md),
                        verticalArrangement = Arrangement.spacedBy(MdSpacing.xs)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Exception Summary",
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "v${BuildConfig.VERSION_NAME} • $errorThread",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        SelectionContainer {
                            Text(
                                text = errorSummary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(MdSpacing.xxs))

                        FilledTonalButton(
                            onClick = {
                                val clipboard =
                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                        ?: return@FilledTonalButton
                                val reportText = buildString {
                                    appendLine("Subspace v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
                                    appendLine("Thread: $errorThread")
                                    appendLine("Summary: $errorSummary")
                                    appendLine()
                                    append(errorDetails)
                                }
                                @Suppress("UsePropertyAccessSyntax")
                                clipboard.setPrimaryClip(
                                    ClipData.newPlainText("Subspace Crash Report", reportText)
                                )
                                copied = true
                            },
                            shapes = ButtonDefaults.shapes(),
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(
                                imageVector = if (copied) Icons.Outlined.Check else Icons.Outlined.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                            Text(
                                text = if (copied) "Copied" else "Copy Error Log",
                                style = MaterialTheme.typography.labelLargeEmphasized
                            )
                        }
                    }
                }

                // Expandable Stack Trace Card
                Card(
                    shape = MaterialTheme.shapes.large,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showStackTrace = !showStackTrace }
                                .padding(horizontal = MdSpacing.md, vertical = MdSpacing.sm),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Technical Stack Trace",
                                style = MaterialTheme.typography.titleSmallEmphasized,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = if (showStackTrace) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                                contentDescription = if (showStackTrace) "Collapse stack trace" else "Expand stack trace",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        AnimatedVisibility(visible = showStackTrace) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = MdSpacing.sm,
                                        end = MdSpacing.sm,
                                        bottom = MdSpacing.sm
                                    )
                                    .heightIn(max = 320.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerLowest
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = errorDetails,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontFamily = FontFamily.Monospace
                                        ),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier
                                            .verticalScroll(rememberScrollState())
                                            .horizontalScroll(rememberScrollState())
                                            .padding(MdSpacing.sm)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(MdSpacing.xs))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(MdSpacing.sm)
                ) {
                    OutlinedButton(
                        onClick = onCloseClick,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(
                            text = "Close",
                            style = MaterialTheme.typography.labelLargeEmphasized
                        )
                    }

                    Button(
                        onClick = onRestartClick,
                        shapes = ButtonDefaults.shapes(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.RestartAlt,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.width(ButtonDefaults.IconSpacing))
                        Text(
                            text = "Restart App",
                            style = MaterialTheme.typography.labelLargeEmphasized
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ErrorScreenPreview() {
    Theme {
        ErrorScreen(
            errorSummary = "IllegalStateException: Sample fatal crash in UI layer",
            errorThread = "main",
            errorDetails = """java.lang.IllegalStateException: Sample fatal crash in UI layer
                |	at me.nanova.subspace.ui.MainActivity.onCreate(MainActivity.kt:45)
                |	at android.app.Activity.performCreate(Activity.java:8595)
                |	at android.app.Instrumentation.callActivityOnCreate(Instrumentation.java:1456)
                |	at android.app.ActivityThread.performLaunchActivity(ActivityThread.java:3764)""".trimMargin(),
            onRestartClick = {},
            onCloseClick = {}
        )
    }
}
