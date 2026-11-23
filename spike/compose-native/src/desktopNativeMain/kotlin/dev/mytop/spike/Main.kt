package dev.mytop.spike

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

/** Platform probes; the actuals are in linuxMain and mingwX64Main. */
expect fun msSinceProcessStart(): Long

expect fun residentKb(): Long

/** Flushes stdout and ends the process with status 0 without the window teardown (see report). */
expect fun quitNow()

fun main(args: Array<String>) {
    val measure = "--measure" in args
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Mytop Compose Native Spike",
            state = rememberWindowState(size = DpSize(480.dp, 760.dp)),
        ) {
            SpikeTheme {
                SpikeContent()
            }
            if (measure) {
                LaunchedEffect(Unit) {
                    // The first callback runs before the first frame is composed and drawn, the
                    // second after that frame was drawn and swapped, which is the first present.
                    withFrameNanosTwice()
                    println("first-frame-ms=${msSinceProcessStart()}")
                    println("rss-kb=${residentKb()}")
                    if ("--clean-exit" in args) exitApplication() else quitNow()
                }
            }
        }
    }
}

private suspend fun withFrameNanosTwice() {
    androidx.compose.runtime.withFrameNanos { }
    androidx.compose.runtime.withFrameNanos { }
}

@Composable
private fun SpikeTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = darkColorScheme()) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}

@Composable
private fun SpikeContent() {
    var count by remember { mutableStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Compose Native spike", style = MaterialTheme.typography.headlineMedium)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(onClick = { count++ }) { Text("Clicked $count times") }
        }
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("An image-free card", style = MaterialTheme.typography.titleMedium)
                Text("Kotlin/Native, Skia, SDL3. No JVM at runtime.")
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items((1..50).toList()) { n ->
                Text(
                    "Row $n",
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
