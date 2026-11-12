#!/usr/bin/env bash
set -euo pipefail
probe_root=$(cd -- "$(dirname -- "$0")" && pwd)
probe_sdk=${ANDROID_HOME:-/opt/android-sdk}
probe_tools="$probe_sdk/build-tools/36.0.0"
probe_out=${1:-/tmp/vibe-nfc-field-probe-build}
mkdir -p "$probe_out/classes" "$probe_out/checks" "$probe_out/dex"
"$probe_tools/aapt2" link -o "$probe_out/base.apk" -I "$probe_sdk/platforms/android-36/android.jar" --manifest "$probe_root/AndroidManifest.xml"
javac --release 17 -classpath "$probe_sdk/platforms/android-36/android.jar" -d "$probe_out/classes" "$probe_root"/src/hk/vibefinance/nfcprobe/*.java
javac --release 17 -d "$probe_out/checks" "$probe_root/src/hk/vibefinance/nfcprobe/EventLog.java" "$probe_root/EventLogCheck.java"
java -ea -cp "$probe_out/checks" hk.vibefinance.nfcprobe.EventLogCheck
jar --create --file "$probe_out/classes.jar" -C "$probe_out/classes" .
"$probe_tools/d8" --min-api 36 --lib "$probe_sdk/platforms/android-36/android.jar" --output "$probe_out/dex" "$probe_out/classes.jar"
cp "$probe_out/base.apk" "$probe_out/unsigned.apk"
(cd "$probe_out/dex" && zip -q "$probe_out/unsigned.apk" classes.dex)
"$probe_tools/zipalign" -f 4 "$probe_out/unsigned.apk" "$probe_out/aligned.apk"
"$probe_tools/apksigner" sign --ks "$HOME/.android/debug.keystore" --ks-pass pass:android --out "$probe_out/nfc-field-probe-debug.apk" "$probe_out/aligned.apk"
"$probe_tools/apksigner" verify "$probe_out/nfc-field-probe-debug.apk"
printf '%s\n' "$probe_out/nfc-field-probe-debug.apk"
