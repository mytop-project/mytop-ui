#!/usr/bin/env bash
# Measures the Linux executable: stripped size, shared-library dependencies, first frame and RSS
# under Xvfb (Mesa llvmpipe), and a screenshot. Usage: report-linux.sh <binary> <out-dir>
set -euo pipefail
bin=${1:?binary}
out=${2:?out dir}
mkdir -p "$out"
cp "$bin" "$out/mytop-spike"
chmod +x "$out/mytop-spike"
exe="$out/mytop-spike"
export LD_LIBRARY_PATH=/usr/local/lib${LD_LIBRARY_PATH:+:$LD_LIBRARY_PATH}
export LIBGL_ALWAYS_SOFTWARE=1
export SDL_VIDEODRIVER=x11

{
  echo "## $(uname -m) executable"
  echo "build ok: yes"
  echo "unstripped bytes: $(stat -c %s "$exe")"
  strip -o "$out/mytop-spike.stripped" "$exe"
  echo "stripped bytes: $(stat -c %s "$out/mytop-spike.stripped")"
  echo "shared libraries (ldd):"
  ldd "$exe" | sed 's/^/  /'
  echo "file: $(file -b "$exe")"
} | tee "$out/report.txt"

for i in 1 2 3; do
  echo "measure run $i" | tee -a "$out/report.txt"
  if timeout 120 xvfb-run -a -s "-screen 0 1280x800x24" "$exe" --measure >"$out/measure-$i.txt" 2>"$out/measure-$i.err"; then
    echo "exit=0" >>"$out/measure-$i.txt"
  else
    echo "exit=$?" >>"$out/measure-$i.txt"
  fi
  cat "$out/measure-$i.txt" "$out/measure-$i.err" | tee -a "$out/report.txt"
done

# Screenshot: run the window for a few seconds and capture the whole virtual screen.
Xvfb :99 -screen 0 1280x800x24 &
xvfb=$!
sleep 2
DISPLAY=:99 "$exe" >"$out/window.log" 2>&1 &
app=$!
sleep 10
if kill -0 "$app" 2>/dev/null; then
  echo "window still running after 10 s: yes" | tee -a "$out/report.txt"
else
  echo "window still running after 10 s: NO" | tee -a "$out/report.txt"
fi
xwd -root -display :99 -out "$out/screen.xwd"
convert "$out/screen.xwd" "$out/screenshot.png"
kill "$app" 2>/dev/null || true
kill "$xvfb" 2>/dev/null || true
cat "$out/window.log" | tee -a "$out/report.txt" || true
rm -f "$out/screen.xwd"
{ echo '```'; cat "$out/report.txt"; echo '```'; } >>"${GITHUB_STEP_SUMMARY:-/dev/null}"
