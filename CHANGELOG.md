# Changelog

All notable changes to lib-ui. Versions follow [Semantic Versioning](https://semver.org/)
(in 0.x, only a new minor version may break the interface; CI enforces it).

## [0.3.0]

### Changed (breaking, which a new 0.x minor may be)
- The Kotlin modules (`contract`, `notices`, `tokens`, `format`) are Kotlin Multiplatform, with their code in
  `commonMain`, for the JVM and Kotlin/Native (`linuxX64`, `linuxArm64`, `mingwX64`, `macosArm64`, `macosX64`), so a
  compose-native app and Android use the same code (Droidtop/tracker#958). Built with Kotlin 2.4.20. Package names are unchanged.
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
  unchanged with its test (Droidtop/tracker#867). It is what the plugin API's facets format their values with, so
  the facet model can move to lib-plugins next.

## [0.2.1]

### Added
- `tokens/`: `ThemePalette` (every colour role a UI draws, plus a theme's typefaces and texture, and droidtop's own
  palette as `DEFAULT`), `FocusRingInks` and `noticeColors`/`noticeInputs`. They are droidtop runtime-common's
  `GamingThemeColors`, `FocusRingInks` and `NoticeInputs.kt`, moved unchanged but for the palette's name, so the
  shared UI reads a palette without droidtop's theme engine (Droidtop/tracker#867, ui-shared step 2). New tests.

## [0.2.0]

### Added
- `notices/`: the notice colours shared by droidtop and Enginehost (`ColorMath`, `NoticeColors`, `NoticeChecks`),
  Droidtop/notices moved here with its three commits, unchanged but for its build file, which is now a module of
  this build. droidtop pins Droidtop/notices at its last commit (64ad439), so this is the same code.
  Droidtop/tracker#867 step 1.
- The public API is checked in (`contract/kotlin/api/contract.api`, `notices/api/notices.api`) and CI holds the code to
  it: `apiCheck`, `scripts/check-api-compat.sh` for Kotlin and `cargo-semver-checks` for the Rust contract fail a
  break the version does not announce (Droidtop/tracker#867).

## [0.1.0]

### Added
- `contract/`: the backend contract (`ok`, `unavailable`, `error`, `pending`) as Pythia's web UI and backend
  already speak it, written down, with one set of fixtures and an implementation in Kotlin
  (`dev.droidtop.lib.ui.contract.Answer`), Rust (`ui-contract`), Python (`ui_contract`) and JavaScript
  (`contract.mjs`). Each decodes every fixture the same way, and each has a guard that turns a failure
  (an exception, a Rust panic) into an `error` answer. Droidtop/tracker#867 D8.
