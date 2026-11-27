package dev.droidtop.components.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.droidtop.shell.gamepad.Corners
import dev.droidtop.shell.gamepad.DroidtopTypography
import dev.droidtop.shell.gamepad.InPlaceHint
import dev.droidtop.shell.gamepad.MenuTokens
import dev.droidtop.shell.gamepad.QuickGlyph
import dev.droidtop.shell.gamepad.QuickGlyphIcon
import dev.droidtop.shell.gamepad.RailColors
import dev.droidtop.shell.gamepad.RailTab
import dev.droidtop.shell.gamepad.ShellChip
import dev.droidtop.shell.gamepad.ShellLoading
import dev.droidtop.shell.gamepad.ShellProgressBar
import dev.droidtop.shell.gamepad.ViewStrip
import dev.droidtop.shell.gamepad.FocusGlideHost
import dev.droidtop.shell.gamepad.ShellSlider
import dev.droidtop.shell.gamepad.ShellSwitch
import dev.droidtop.shell.gamepad.Space
import dev.droidtop.shell.gamepad.TabRail
import dev.droidtop.shell.gamepad.input.PadModality
import dev.droidtop.shell.gamepad.rowFocus
import dev.droidtop.shell.gamepad.selectionFrame
import dev.droidtop.shell.gamepad.shine
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.delay
import platform.posix.fflush

/**
 * The gallery: every component of the library drawn once, with the tokens that style it. CI runs it under Xvfb with
 * `--shot`, takes a screenshot and uploads it, so a change to a component is seen, not only compiled.
 */
@OptIn(ExperimentalForeignApi::class)
fun main(args: Array<String>) {
    val shot = "--shot" in args
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "mytop-ui components",
            state = rememberWindowState(size = DpSize(960.dp, 2400.dp)),
        ) {
            GalleryTheme { FocusGlideHost(Modifier.fillMaxSize()) { Gallery() } }
        }
        if (shot) {
            LaunchedEffect(Unit) {
                delay(3_000)
                println("gallery: ready")
                fflush(null)
                delay(6_000)
                println("gallery: ok")
                fflush(null)
                exitApplication()
            }
        }
    }
}

@Composable
private fun GalleryTheme(content: @Composable () -> Unit) {
    val scheme = darkColorScheme(
        primary = MenuTokens.Accent,
        onPrimary = MenuTokens.OnAccent,
        background = MenuTokens.Ground,
        onBackground = MenuTokens.OnSurface,
        surface = MenuTokens.Card,
        onSurface = MenuTokens.OnSurface,
        onSurfaceVariant = MenuTokens.OnSurfaceMuted,
    )
    MaterialTheme(colorScheme = scheme, typography = DroidtopTypography) {
        Surface(modifier = Modifier.fillMaxSize(), color = MenuTokens.Ground) { content() }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.Sm)) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MenuTokens.SectionLabel)
        content()
    }
}

@Composable
private fun Gallery() {
    // The selection is drawn while a pad drives (PadModality); the gallery shows it.
    LaunchedEffect(Unit) { PadModality.padDriving() }
    var on by remember { mutableStateOf(false) }
    var tab by remember { mutableStateOf<Any?>("home") }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1_200)
            on = !on
        }
    }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(Space.Lg),
        verticalArrangement = Arrangement.spacedBy(Space.Xl),
    ) {
        Text("mytop-ui components", style = MaterialTheme.typography.headlineMedium, color = MenuTokens.OnSurface)

        Section("Colours (MenuTokens, from the tokens module's palette)") {
            val swatches = listOf(
                "Ground" to MenuTokens.Ground, "Card" to MenuTokens.Card, "CardFocused" to MenuTokens.CardFocused,
                "CardInset" to MenuTokens.CardInset, "Surface" to MenuTokens.Surface,
                "SurfaceSelected" to MenuTokens.SurfaceSelected, "Accent" to MenuTokens.Accent,
                "Affirmative" to MenuTokens.Affirmative, "NoticeDanger" to MenuTokens.NoticeDanger,
                "Favourite" to MenuTokens.Favourite, "Launch" to MenuTokens.Launch, "Selected" to MenuTokens.Selected,
            )
            for (row in swatches.chunked(6)) {
                Row(horizontalArrangement = Arrangement.spacedBy(Space.Sm)) {
                    for ((name, color) in row) Swatch(name, color)
                }
            }
        }

        Section("Space and corners") {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.Md), verticalAlignment = Alignment.Bottom) {
                for (step in listOf(Space.Hair, Space.Xs, Space.Sm, Space.Md, Space.Lg, Space.Xl, Space.Xxxl)) {
                    Box(Modifier.width(step).height(32.dp).background(MenuTokens.Accent))
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(Space.Md)) {
                for (shape in listOf(Corners.Crisp, Corners.Tile, Corners.Panel, Corners.Plate, Corners.Pill)) {
                    Box(Modifier.size(width = 96.dp, height = 48.dp).clip(shape).background(MenuTokens.CardFocused))
                }
            }
        }

        Section("Type (DroidtopTypography)") {
            Text("Headline", style = MaterialTheme.typography.headlineSmall, color = MenuTokens.OnSurface)
            Text("Title medium", style = MaterialTheme.typography.titleMedium, color = MenuTokens.OnSurface)
            Text("Body large, the row title", style = MaterialTheme.typography.bodyLarge, color = MenuTokens.OnSurface)
            Text("Body medium, the supporting line", style = MaterialTheme.typography.bodyMedium, color = MenuTokens.OnSurfaceMuted)
            Text("LABEL LARGE", style = MaterialTheme.typography.labelLarge, color = MenuTokens.Value)
        }

        Section("Controls and motion (the switch and the sheen animate)") {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.Lg), verticalAlignment = Alignment.CenterVertically) {
                ShellSwitch(on)
                ShellSwitch(!on)
                ShellSlider(0.6f, selected = true, modifier = Modifier.width(240.dp))
                ShellSlider(0.3f, selected = false, modifier = Modifier.width(240.dp))
            }
            Box(
                Modifier.size(width = 200.dp, height = 40.dp).clip(Corners.Pill).background(MenuTokens.Accent).shine(on, play = true),
                contentAlignment = Alignment.Center,
            ) { Text("Shine", color = MenuTokens.OnAccent) }
        }

        Section("Selection frame and row focus (the pad's ring)") {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.Md)) {
                for (selected in listOf(false, true)) {
                    Box(
                        Modifier.size(width = 200.dp, height = 56.dp).selectionFrame(selected, Corners.Crisp),
                        contentAlignment = Alignment.Center,
                    ) { Text(if (selected) "selected" else "at rest", color = MenuTokens.OnSurface) }
                }
            }
            Box(Modifier.fillMaxWidth().height(48.dp).rowFocus(shown = true), contentAlignment = Alignment.CenterStart) {
                Text("A row with the cursor on it", color = MenuTokens.OnSurface, modifier = Modifier.padding(start = Space.Lg))
            }
        }

        Section("Tab rail, labelled") {
            val tabs = listOf(
                RailTab("home", "Home", QuickGlyph.HOME),
                RailTab("apps", "Apps", QuickGlyph.APPS, badge = 3),
                RailTab("people", "People", QuickGlyph.PEOPLE, dot = true),
                RailTab("more", "More", QuickGlyph.MORE),
            )
            TabRail(
                tabs = tabs,
                selected = tab,
                vertical = false,
                labelled = true,
                colors = RailColors(
                    selected = MenuTokens.SurfaceSelected,
                    ink = MenuTokens.OnSurface,
                    muted = MenuTokens.OnSurfaceMuted,
                    accent = MenuTokens.Accent,
                    onAccent = MenuTokens.OnAccent,
                ),
                onSelect = { tab = it.key },
                modifier = Modifier.fillMaxWidth().height(72.dp),
            )
        }

        Section("Chips, the view strip and progress") {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.Md), verticalAlignment = Alignment.CenterVertically) {
                ShellChip("Filter", selected = false, onClick = {})
                ShellChip("On", on = true, selected = false, onClick = {})
                ShellChip("Save", primary = true, selected = true, onClick = {})
                ShellChip("Play", large = true, selected = false, onClick = {})
                ShellChip("Disabled", primary = true, enabled = false, selected = false, onClick = {})
            }
            ViewStrip(
                labels = listOf("All", "Installed", "Favourites", "Recent"),
                active = 1,
                focused = 2,
                pill = null,
                onSelect = {},
                onClearPill = {},
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Space.Lg), verticalAlignment = Alignment.CenterVertically) {
                ShellProgressBar(0.4f, Modifier.width(240.dp))
                ShellProgressBar(1f, Modifier.width(240.dp))
                ShellLoading()
            }
        }

        Section("Quick glyphs") {
            Row(horizontalArrangement = Arrangement.spacedBy(Space.Md)) {
                for (glyph in listOf(QuickGlyph.NETWORK, QuickGlyph.VOLUME, QuickGlyph.BRIGHTNESS, QuickGlyph.MOON, QuickGlyph.BATTERY, QuickGlyph.GAMEPAD, QuickGlyph.SETTINGS, QuickGlyph.SEARCH)) {
                    QuickGlyphIcon(glyph, MenuTokens.OnSurface, Modifier.size(32.dp))
                }
            }
        }

        Section("In-place hint") {
            InPlaceHint("Press A to launch, B to go back", onDismiss = {}, modifier = Modifier.width(420.dp))
        }
    }
}

@Composable
private fun Swatch(name: String, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(Space.Xs)) {
        Box(Modifier.size(width = 120.dp, height = 48.dp).clip(Corners.Crisp).background(color))
        Text(name, style = MaterialTheme.typography.bodyMedium, color = MenuTokens.OnSurfaceMuted)
    }
}
