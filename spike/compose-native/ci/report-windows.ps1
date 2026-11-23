# Measures the Windows executable: size, dependents, first frame and working set, screenshot.
# Usage: report-windows.ps1 <distribution-dir> <out-dir>
param([string]$Dist, [string]$Out)
$ErrorActionPreference = 'Continue'
New-Item -ItemType Directory -Force $Out | Out-Null
$exe = Get-ChildItem $Dist -Filter *.exe | Select-Object -First 1
$report = Join-Path $Out 'report.txt'
function Log($s) { $s | Tee-Object -FilePath $report -Append }

Log "## windows x64 executable"
Log "build ok: yes"
Log "exe: $($exe.FullName)"
Log "exe bytes (MinGW, not stripped by the plugin): $($exe.Length)"
Log "distribution files:"
Get-ChildItem $Dist -Recurse -File | ForEach-Object { Log ("  {0} {1}" -f $_.FullName.Substring($Dist.Length), $_.Length) }
$total = (Get-ChildItem $Dist -Recurse -File | Measure-Object Length -Sum).Sum
Log "distribution total bytes: $total"

$vswhere = "${env:ProgramFiles(x86)}\Microsoft Visual Studio\Installer\vswhere.exe"
$vs = & $vswhere -latest -products * -property installationPath
$dumpbin = Get-ChildItem "$vs\VC\Tools\MSVC" -Recurse -Filter dumpbin.exe | Where-Object { $_.FullName -match 'Hostx64\x64' } | Select-Object -First 1
if ($dumpbin) {
    Log "dumpbin /dependents:"
    & $dumpbin.FullName /dependents $exe.FullName | ForEach-Object { Log "  $_" }
} else { Log "dumpbin not found" }

for ($i = 1; $i -le 3; $i++) {
    Log "measure run $i"
    $o = Join-Path $Out "measure-$i.txt"
    $p = Start-Process -FilePath $exe.FullName -ArgumentList '--measure' -WorkingDirectory $Dist -RedirectStandardOutput $o -RedirectStandardError "$o.err" -PassThru -NoNewWindow
    if (-not $p.WaitForExit(120000)) { $p.Kill(); Log "timeout" }
    Log "exit=$($p.ExitCode)"
    Get-Content $o, "$o.err" -ErrorAction SilentlyContinue | ForEach-Object { Log $_ }
}

# Screenshot of the whole screen while the window is open.
Add-Type -AssemblyName System.Windows.Forms, System.Drawing
$p = Start-Process -FilePath $exe.FullName -WorkingDirectory $Dist -PassThru -RedirectStandardOutput "$Out\window.txt" -RedirectStandardError "$Out\window.err"
Start-Sleep -Seconds 10
Log "window still running after 10 s: $(-not $p.HasExited)"
try {
    $b = [System.Windows.Forms.SystemInformation]::VirtualScreen
    $bmp = New-Object System.Drawing.Bitmap $b.Width, $b.Height
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.CopyFromScreen($b.Left, $b.Top, 0, 0, $bmp.Size)
    $bmp.Save("$Out\screenshot.png")
    Log "screenshot saved ($($b.Width)x$($b.Height))"
} catch { Log "screenshot failed: $_" }
if (-not $p.HasExited) { $p.Kill() }
Get-Content "$Out\window.txt", "$Out\window.err" -ErrorAction SilentlyContinue | ForEach-Object { Log $_ }
if ($env:GITHUB_STEP_SUMMARY) { "``````" | Add-Content $env:GITHUB_STEP_SUMMARY; Get-Content $report | Add-Content $env:GITHUB_STEP_SUMMARY; "``````" | Add-Content $env:GITHUB_STEP_SUMMARY }
