# Changelog

All notable changes to mytop-ui. Versions follow [Semantic Versioning](https://semver.org/)
(in 0.x, only a new minor version may break the interface; CI enforces it).

## [0.4.0]

### Added
- `components/`: the Compose Multiplatform components (mytop-project/tracker#867 step 3, #920), a Gradle build of its own
  (it consumes `tokens` and `notices` of the same commit as a composite build, and compose-native's patched fork
  `1.13.0-beta01-mytop.1` for Linux and Windows native). Targets: `jvm` (the code path Android uses), `linuxX64`,
  `linuxArm64`, `mingwX64`. A gallery binary (`components-gallery`) draws every component; CI screenshots it on Linux.
  The Android library target and the Apple targets are not built yet: Compose 1.13 needs AGP 9.1 and compileSdk 37 for
  the first, and compose-native's fork publishes no macOS artifacts for the second.
- **Migrated from droidtop `ui-shared`** (droidtop `origin/main` aa40732a4; the files keep their packages
  `dev.droidtop.shell.gamepad` and `dev.droidtop.ui.shared` and their public APIs, so droidtop switches by dependency
  and deletes its copies): `DesignTokens.kt` (Space, Measure, Corners, Elevation, SideMenu, SettingsLayout, ProgressLook,
  DialogLook, TypeRole, DroidtopTypography, NarrowPane, ChromeColors, MenuTokens), `MotionTokens.kt` (FocusLook),
  `ui/shared/Motion.kt`, `ui/shared/KeyFocus.kt` (holdKeyFocus), `TabRail.kt` (TabRail, RailTab, RailColors),
  `InPlaceHint.kt` (InPlaceHint, hintRing), `SelectionFrame.kt` (selectionFrame, rowFocus: the focus ring of a row or
  a control), `FocusRequest.kt`, `ShellControls.kt` (ShellSwitch, ShellSlider), `Shine.kt`, `ScrollFollow.kt`,
  `ChromeBar.kt` (opticallyCentred, frameEdge), `QuickGlyphs.kt` (QuickGlyph, QuickGlyphIcon).
- Taken out of droidtop files that stay there, as new files with the code unchanged: `PadModality` (from
  `input/PadGate.kt`) and `Modifier.padRow` (from `TouchActions.kt`).
- `ComponentTheme` and `PaletteColors`: where `MenuTokens` reads its colours, from the tokens module's `ThemePalette`
  (droidtop's `MenuTokens` read `GamingTheme.palette`). The one seam: a program sets `ComponentTheme.palette` from the
  palette its theme engine resolved. `MenuTokens.NoticeDanger` is `noticeColors(...).dangerInk`.

### Not moved yet, and what blocks each (droidtop's switch deletes only what moved)
- `ShellChip`, `ViewStrip`: need `onPad` / `PadPress` (Android `KeyEvent`), `focusRing` (`FocusGlide`, which keys on an
  Android `View`), `focusMarquee` (`MenuRow`) and `labelCase` (`GamingTheme`, `ReadingPrefs`).
- `ShoulderStrip`: extends `ShellWindow` (window metrics, `ControllerLayouts`).
- `ShellControls`' `ShellProgressBar`, `ShellLoading` and `shellJobColors`: need `JobProgress` and `JobLook` (step 2, notices
  and jobs); the rest of the file moved. `DesignTokens` is otherwise whole.
- droidtop's switch, after its main builds again: depend on the components module, delete the moved files, and in
  `TouchActions.kt` and `PadGate.kt` delete `padRow` and `PadModality`. Set `ComponentTheme.palette` wherever
  `GamingTheme` publishes its palette.

## [0.3.0]

### Changed (breaking, which a new 0.x minor may be)
- The Kotlin modules (`contract`, `notices`, `tokens`, `format`) are Kotlin Multiplatform, with their code in
  `commonMain`, for the JVM and Kotlin/Native (`linuxX64`, `linuxArm64`, `mingwX64`, `macosArm64`, `macosX64`), so a
  compose-native app and Android use the same code (mytop-project/tracker#958). Built with Kotlin 2.4.20. Package names are unchanged.
- **Source layout (programs that compile a module's sources, as droidtop does for notices):** the sources moved from
  `<module>/src/main/kotlin` to `<module>/src/commonMain/kotlin`. Point the `srcDir` there.
- `ValueFormat.time(epochMs, is24Hour, zone)`: `zone` is now a `kotlinx.datetime.TimeZone`
  (default `TimeZone.currentSystemDefault()`), no longer a `java.util.TimeZone`. A caller that passes no zone changes
  nothing. `format` now depends on `kotlinx-datetime` 0.8.0 (an `api` dependency, as the type is in the signature).
  `kotlinx.datetime.TimeZone.of(id)` takes the id `java.util.TimeZone.getTimeZone(id)` did.
- `Answer.guard` names a throwable that has no message by its class name as before; one with no class name (an
  anonymous class) is named "Error" where the JVM gave an empty string.

### Unchanged on purpose
- Every result of `ValueFormat` and the notice colours reads exactly as before: the digits are written in common code
  (sizes in whole numbers, halves up as `%.1f` did; counts grouped by thousands), and `java.lang.Math.round` is replaced
  by `ColorMath.roundHalfUp` (internal), which rounds halves up as Java does, including the value just below a half
  (0.49999999999999994 is 0). JVM tests compare both against `java.lang.Math`, `String.format` and `Calendar` over
  every tie and a few hundred thousand random values.

### Added
- CI builds and tests every target on its host: JVM and linuxX64 on Linux, mingwX64 on Windows, macosArm64 on macOS;
  linuxArm64 and macosX64 are compiled and linked there (their test binaries are not run).
- The binary-compatibility dump (`<module>/api/<module>.api`) is the JVM target's. It has no klib dump yet.

## [0.2.2]

### Added
- `format/`: `ValueFormat`, droidtop runtime-common's one formatter for sizes, durations, counts and times, moved
  unchanged with its test (mytop-project/tracker#867). It is what the plugin API's facets format their values with, so
  the facet model can move to mytop-plugins next.

## [0.2.1]

### Added
- `tokens/`: `ThemePalette` (every colour role a UI draws, plus a theme's typefaces and texture, and droidtop's own
  palette as `DEFAULT`), `FocusRingInks` and `noticeColors`/`noticeInputs`. They are droidtop runtime-common's
  `GamingThemeColors`, `FocusRingInks` and `NoticeInputs.kt`, moved unchanged but for the palette's name, so the
  shared UI reads a palette without droidtop's theme engine (mytop-project/tracker#867, ui-shared step 2). New tests.

## [0.2.0]

### Added
- `notices/`: the notice colours shared by droidtop and Enginehost (`ColorMath`, `NoticeColors`, `NoticeChecks`),
  Droidtop/notices moved here with its three commits, unchanged but for its build file, which is now a module of
  this build. droidtop pins Droidtop/notices at its last commit (64ad439), so this is the same code.
  mytop-project/tracker#867 step 1.
- The public API is checked in (`contract/kotlin/api/contract.api`, `notices/api/notices.api`) and CI holds the code to
  it: `apiCheck`, `scripts/check-api-compat.sh` for Kotlin and `cargo-semver-checks` for the Rust contract fail a
  break the version does not announce (mytop-project/tracker#867).

## [0.1.0]

### Added
- `contract/`: the backend contract (`ok`, `unavailable`, `error`, `pending`) as Pythia's web UI and backend
  already speak it, written down, with one set of fixtures and an implementation in Kotlin
  (`dev.droidtop.lib.ui.contract.Answer`), Rust (`ui-contract`), Python (`ui_contract`) and JavaScript
  (`contract.mjs`). Each decodes every fixture the same way, and each has a guard that turns a failure
  (an exception, a Rust panic) into an `error` answer. mytop-project/tracker#867 D8.
